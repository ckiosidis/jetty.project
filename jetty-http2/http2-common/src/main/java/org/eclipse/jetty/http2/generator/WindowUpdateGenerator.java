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

package org.eclipse.jetty.http2.generator;

import java.nio.ByteBuffer;

import org.eclipse.jetty.http2.Flags;
import org.eclipse.jetty.http2.frames.Frame;
import org.eclipse.jetty.http2.frames.FrameType;
import org.eclipse.jetty.http2.frames.WindowUpdateFrame;
import org.eclipse.betty.io.ByteBufferPool;
import org.eclipse.betty.util.BufferUtil;

public class WindowUpdateGenerator extends FrameGenerator
{
    public WindowUpdateGenerator(HeaderGenerator headerGenerator)
    {
        super(headerGenerator);
    }

    @Override
    public int generate(ByteBufferPool.Lease lease, Frame frame)
    {
        WindowUpdateFrame windowUpdateFrame = (WindowUpdateFrame)frame;
        return generateWindowUpdate(lease, windowUpdateFrame.getStreamId(), windowUpdateFrame.getWindowDelta());
    }

    public int generateWindowUpdate(ByteBufferPool.Lease lease, int streamId, int windowUpdate)
    {
        if (windowUpdate < 0)
            throw new IllegalArgumentException("Invalid window update: " + windowUpdate);

        ByteBuffer header = generateHeader(lease, FrameType.WINDOW_UPDATE, WindowUpdateFrame.WINDOW_UPDATE_LENGTH, Flags.NONE, streamId);
        header.putInt(windowUpdate);
        BufferUtil.flipToFlush(header, 0);
        lease.append(header, true);

        return Frame.HEADER_LENGTH + WindowUpdateFrame.WINDOW_UPDATE_LENGTH;
    }
}
