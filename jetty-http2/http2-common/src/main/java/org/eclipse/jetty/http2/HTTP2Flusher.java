//
//  ========================================================================
//  Copyright (c) 1995-2018 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.http2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Queue;

import org.eclipse.jetty.http2.frames.Frame;
import org.eclipse.jetty.http2.frames.WindowUpdateFrame;
import org.eclipse.betty.io.ByteBufferPool;
import org.eclipse.betty.io.EofException;
import org.eclipse.betty.io.WriteFlusher;
import org.eclipse.betty.util.Callback;
import org.eclipse.betty.util.IteratingCallback;
import org.eclipse.betty.util.component.ContainerLifeCycle;
import org.eclipse.betty.util.component.Dumpable;
import org.eclipse.betty.util.log.Log;
import org.eclipse.betty.util.log.Logger;

public class HTTP2Flusher extends IteratingCallback implements Dumpable
{
    private static final Logger LOG = Log.getLogger(HTTP2Flusher.class);

    private final Queue<WindowEntry> windows = new ArrayDeque<>();
    private final Deque<Entry> frames = new ArrayDeque<>();
    private final Queue<Entry> entries = new ArrayDeque<>();
    private final List<Entry> actives = new ArrayList<>();
    private final HTTP2Session session;
    private final ByteBufferPool.Lease lease;
    private Entry stalled;
    private Throwable terminated;

    public HTTP2Flusher(HTTP2Session session)
    {
        this.session = session;
        this.lease = new ByteBufferPool.Lease(session.getGenerator().getByteBufferPool());
    }

    public void window(IStream stream, WindowUpdateFrame frame)
    {
        Throwable closed;
        synchronized (this)
        {
            closed = terminated;
            if (closed == null)
                windows.offer(new WindowEntry(stream, frame));
        }
        // Flush stalled data.
        if (closed == null)
            iterate();
    }

    public boolean prepend(Entry entry)
    {
        Throwable closed;
        synchronized (this)
        {
            closed = terminated;
            if (closed == null)
            {
                frames.offerFirst(entry);
                if (LOG.isDebugEnabled())
                    LOG.debug("Prepended {}, frames={}", entry, frames.size());
            }
        }
        if (closed == null)
            return true;
        closed(entry, closed);
        return false;
    }

    public boolean append(Entry entry)
    {
        Throwable closed;
        synchronized (this)
        {
            closed = terminated;
            if (closed == null)
            {
                frames.offer(entry);
                if (LOG.isDebugEnabled())
                    LOG.debug("Appended {}, frames={}", entry, frames.size());
            }
        }
        if (closed == null)
            return true;
        closed(entry, closed);
        return false;
    }

    private int getWindowQueueSize()
    {
        synchronized (this)
        {
            return windows.size();
        }
    }

    public int getFrameQueueSize()
    {
        synchronized (this)
        {
            return frames.size();
        }
    }

    @Override
    protected Action process() throws Throwable
    {
        if (LOG.isDebugEnabled())
            LOG.debug("Flushing {}", session);

        synchronized (this)
        {
            if (terminated != null)
                throw terminated;

            while (!windows.isEmpty())
            {
                WindowEntry entry = windows.poll();
                entry.perform();
            }

            for (Entry entry : frames)
            {
                entries.offer(entry);
                actives.add(entry);
            }
            frames.clear();
        }


        if (entries.isEmpty())
        {
            if (LOG.isDebugEnabled())
                LOG.debug("Flushed {}", session);
            return Action.IDLE;
        }

        while (!entries.isEmpty())
        {
            Entry entry = entries.poll();
            if (LOG.isDebugEnabled())
                LOG.debug("Processing {}", entry);

            // If the stream has been reset or removed, don't send the frame.
            if (entry.isStale())
            {
                if (LOG.isDebugEnabled())
                    LOG.debug("Stale {}", entry);
                continue;
            }

            try
            {
                if (entry.generate(lease))
                {
                    if (entry.getDataBytesRemaining() > 0)
                        entries.offer(entry);
                }
                else
                {
                    if (stalled == null)
                        stalled = entry;
                }
            }
            catch (Throwable failure)
            {
                // Failure to generate the entry is catastrophic.
                if (LOG.isDebugEnabled())
                    LOG.debug("Failure generating frame " + entry.frame, failure);
                failed(failure);
                return Action.SUCCEEDED;
            }
        }

        List<ByteBuffer> byteBuffers = lease.getByteBuffers();
        if (byteBuffers.isEmpty())
        {
            complete();
            return Action.IDLE;
        }

        if (LOG.isDebugEnabled())
            LOG.debug("Writing {} buffers ({} bytes) for {} frames {}", byteBuffers.size(), lease.getTotalLength(), actives.size(), actives);
        session.getEndPoint().write(this, byteBuffers.toArray(new ByteBuffer[byteBuffers.size()]));
        return Action.SCHEDULED;
    }

    void onFlushed(long bytes) throws IOException
    {
        // For the given flushed bytes, we want to only
        // forward those that belong to data frame content.
        for (Entry entry : actives)
        {
            int frameBytesLeft = entry.getFrameBytesRemaining();
            if (frameBytesLeft > 0)
            {
                int update = (int)Math.min(bytes, frameBytesLeft);
                entry.onFrameBytesFlushed(update);
                bytes -= update;
                IStream stream = entry.stream;
                if (stream != null && !entry.isControl())
                {
                    Object channel = stream.getAttribute(IStream.CHANNEL_ATTRIBUTE);
                    if (channel instanceof WriteFlusher.Listener)
                        ((WriteFlusher.Listener)channel).onFlushed(update - Frame.HEADER_LENGTH);
                }
                if (bytes == 0)
                    break;
            }
        }
    }

    @Override
    public void succeeded()
    {
        if (LOG.isDebugEnabled())
            LOG.debug("Written {} frames for {}", actives.size(), actives);

        complete();

        super.succeeded();
    }

    private void complete()
    {
        lease.recycle();

        actives.forEach(Entry::complete);

        if (stalled != null)
        {
            // We have written part of the frame, but there is more to write.
            // The API will not allow to send two data frames for the same
            // stream so we append the unfinished frame at the end to allow
            // better interleaving with other streams.
            int index = actives.indexOf(stalled);
            for (int i = index; i < actives.size(); ++i)
            {
                Entry entry = actives.get(i);
                if (entry.getDataBytesRemaining() > 0)
                    append(entry);
            }
            for (int i = 0; i < index; ++i)
            {
                Entry entry = actives.get(i);
                if (entry.getDataBytesRemaining() > 0)
                    append(entry);
            }
            stalled = null;
        }

        actives.clear();
    }

    @Override
    protected void onCompleteSuccess()
    {
        throw new IllegalStateException();
    }

    @Override
    protected void onCompleteFailure(Throwable x)
    {
        lease.recycle();

        Throwable closed;
        synchronized (this)
        {
            closed = terminated;
            terminated = x;
            if (LOG.isDebugEnabled())
                LOG.debug("{}, active/queued={}/{}", closed != null ? "Closing" : "Failing", actives.size(), frames.size());
            actives.addAll(frames);
            frames.clear();
        }

        actives.forEach(entry -> entry.failed(x));
        actives.clear();

        // If the failure came from within the
        // flusher, we need to close the connection.
        if (closed == null)
            session.abort(x);
    }

    void terminate(Throwable cause)
    {
        Throwable closed;
        synchronized (this)
        {
            closed = terminated;
            terminated = cause;
            if (LOG.isDebugEnabled())
                LOG.debug("{}", closed != null ? "Terminated" : "Terminating");
        }
        if (closed == null)
            iterate();
    }

    private void closed(Entry entry, Throwable failure)
    {
        entry.failed(failure);
    }

    @Override
    public String dump()
    {
        return ContainerLifeCycle.dump(this);
    }

    @Override
    public void dump(Appendable out, String indent) throws IOException
    {
        out.append(toString()).append(System.lineSeparator());
    }

    @Override
    public String toString()
    {
        return String.format("%s[window_queue=%d,frame_queue=%d,actives=%d]",
                super.toString(),
                getWindowQueueSize(),
                getFrameQueueSize(),
                actives.size());
    }

    public static abstract class Entry extends Callback.Nested
    {
        protected final Frame frame;
        protected final IStream stream;

        protected Entry(Frame frame, IStream stream, Callback callback)
        {
            super(callback);
            this.frame = frame;
            this.stream = stream;
        }

        public abstract int getFrameBytesRemaining();

        public abstract void onFrameBytesFlushed(int bytesFlushed);

        public int getDataBytesRemaining()
        {
            return 0;
        }

        protected abstract boolean generate(ByteBufferPool.Lease lease);

        private void complete()
        {
            if (isStale())
                failed(new EofException("reset"));
            else
                succeeded();
        }

        @Override
        public void failed(Throwable x)
        {
            if (stream != null)
            {
                stream.close();
                stream.getSession().removeStream(stream);
            }
            super.failed(x);
        }

        private boolean isStale()
        {
            return !isProtocol() && stream != null && stream.isReset();
        }

        private boolean isProtocol()
        {
            switch (frame.getType())
            {
                case DATA:
                case HEADERS:
                case PUSH_PROMISE:
                case CONTINUATION:
                    return false;
                case PRIORITY:
                case RST_STREAM:
                case SETTINGS:
                case PING:
                case GO_AWAY:
                case WINDOW_UPDATE:
                case PREFACE:
                case DISCONNECT:
                    return true;
                default:
                    throw new IllegalStateException();
            }
        }

        private boolean isControl()
        {
            switch (frame.getType())
            {
                case DATA:
                    return false;
                default:
                    return true;
            }
        }

        @Override
        public String toString()
        {
            return frame.toString();
        }
    }

    private class WindowEntry
    {
        private final IStream stream;
        private final WindowUpdateFrame frame;

        public WindowEntry(IStream stream, WindowUpdateFrame frame)
        {
            this.stream = stream;
            this.frame = frame;
        }

        public void perform()
        {
            FlowControlStrategy flowControl = session.getFlowControlStrategy();
            flowControl.onWindowUpdate(session, stream, frame);
        }
    }
}
