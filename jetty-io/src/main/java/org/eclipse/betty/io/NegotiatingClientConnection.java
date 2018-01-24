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

package org.eclipse.betty.io;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLEngine;

import org.eclipse.betty.util.BufferUtil;
import org.eclipse.betty.util.log.Log;
import org.eclipse.betty.util.log.Logger;

public abstract class NegotiatingClientConnection extends AbstractConnection
{
    private static final Logger LOG = Log.getLogger(NegotiatingClientConnection.class);

    private final SSLEngine engine;
    private final ClientConnectionFactory connectionFactory;
    private final Map<String, Object> context;
    private volatile boolean completed;

    protected NegotiatingClientConnection(EndPoint endp, Executor executor, SSLEngine sslEngine, ClientConnectionFactory connectionFactory, Map<String, Object> context)
    {
        super(endp, executor);
        this.engine = sslEngine;
        this.connectionFactory = connectionFactory;
        this.context = context;
    }

    public SSLEngine getSSLEngine()
    {
        return engine;
    }

    protected void completed()
    {
        completed = true;
    }

    @Override
    public void onOpen()
    {
        super.onOpen();
        try
        {
            getEndPoint().flush(BufferUtil.EMPTY_BUFFER);
            if (completed)
                replaceConnection();
            else
                fillInterested();
        }
        catch (IOException x)
        {
            close();
            throw new RuntimeIOException(x);
        }
    }

    @Override
    public void onFillable()
    {
        while (true)
        {
            int filled = fill();
            if (completed || filled < 0)
            {
                replaceConnection();
                break;
            }
            if (filled == 0)
            {
                fillInterested();
                break;
            }
        }
    }

    private int fill()
    {
        try
        {
            return getEndPoint().fill(BufferUtil.EMPTY_BUFFER);
        }
        catch (IOException x)
        {
            LOG.debug(x);
            close();
            return -1;
        }
    }

    private void replaceConnection()
    {
        EndPoint endPoint = getEndPoint();
        try
        {
            endPoint.upgrade(connectionFactory.newConnection(endPoint, context));
        }
        catch (Throwable x)
        {
            LOG.debug(x);
            close();
        }
    }

    @Override
    public void close()
    {
        // Gentler close for SSL.
        getEndPoint().shutdownOutput();
        super.close();
    }
}