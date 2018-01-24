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

package org.eclipse.betty.server.nio;

import java.util.concurrent.Executor;

import org.eclipse.betty.io.ByteBufferPool;
import org.eclipse.betty.server.ConnectionFactory;
import org.eclipse.betty.server.NetworkTrafficServerConnector;
import org.eclipse.betty.server.Server;
import org.eclipse.betty.util.ssl.SslContextFactory;
import org.eclipse.betty.util.thread.Scheduler;

/**
 * @deprecated use {@link NetworkTrafficServerConnector} instead.
 */
@Deprecated
public class NetworkTrafficSelectChannelConnector extends NetworkTrafficServerConnector
{
    public NetworkTrafficSelectChannelConnector(Server server)
    {
        super(server);
    }

    public NetworkTrafficSelectChannelConnector(Server server, ConnectionFactory connectionFactory, SslContextFactory sslContextFactory)
    {
        super(server, connectionFactory, sslContextFactory);
    }

    public NetworkTrafficSelectChannelConnector(Server server, ConnectionFactory connectionFactory)
    {
        super(server, connectionFactory);
    }

    public NetworkTrafficSelectChannelConnector(Server server, Executor executor, Scheduler scheduler, ByteBufferPool pool, int acceptors, int selectors, ConnectionFactory... factories)
    {
        super(server, executor, scheduler, pool, acceptors, selectors, factories);
    }

    public NetworkTrafficSelectChannelConnector(Server server, SslContextFactory sslContextFactory)
    {
        super(server, sslContextFactory);
    }
}
