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

package org.eclipse.jetty.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.betty.http.HttpCompliance;
import org.eclipse.betty.http.HttpVersion;
import org.eclipse.betty.io.ChannelEndPoint;
import org.eclipse.betty.io.Connection;
import org.eclipse.betty.io.EndPoint;
import org.eclipse.betty.io.ManagedSelector;
import org.eclipse.betty.io.SocketChannelEndPoint;
import org.eclipse.betty.server.Connector;
import org.eclipse.betty.server.HttpChannelOverHttp;
import org.eclipse.betty.server.HttpConfiguration;
import org.eclipse.betty.server.HttpConnection;
import org.eclipse.betty.server.HttpConnectionFactory;
import org.eclipse.betty.server.Request;
import org.eclipse.betty.server.ServerConnector;
import org.eclipse.betty.server.handler.AbstractHandler;
import org.eclipse.jetty.toolchain.test.AdvancedRunner;
import org.eclipse.betty.util.thread.Scheduler;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Extended Server Tester.
 */
@RunWith(AdvancedRunner.class)
public class ExtendedServerTest extends HttpServerTestBase
{
    @Before
    public void init() throws Exception
    {
        startServer(new ServerConnector(_server,new HttpConnectionFactory()
        {
            @Override
            public Connection newConnection(Connector connector, EndPoint endPoint)
            {
                return configure(new ExtendedHttpConnection(getHttpConfiguration(), connector, endPoint), connector, endPoint);
            }
        })
        {
            @Override
            protected ChannelEndPoint newEndPoint(SocketChannel channel, ManagedSelector selectSet, SelectionKey key) throws IOException
            {
                return new ExtendedEndPoint(channel,selectSet,key, getScheduler());
            }

        });
    }

    private static class ExtendedEndPoint extends SocketChannelEndPoint
    {
        private volatile long _lastSelected;

        public ExtendedEndPoint(SelectableChannel channel, ManagedSelector selector, SelectionKey key, Scheduler scheduler)
        {
            super(channel,selector,key,scheduler);
        }

        public ExtendedEndPoint(SocketChannel channel, ManagedSelector selector, SelectionKey key, Scheduler scheduler)
        {
            super(channel,selector,key,scheduler);
        }

        @Override
        public Runnable onSelected()
        {
            _lastSelected=System.currentTimeMillis();
            return super.onSelected();
        }

        long getLastSelected()
        {
            return _lastSelected;
        }
    }

    private static class ExtendedHttpConnection extends HttpConnection
    {
        public ExtendedHttpConnection(HttpConfiguration config, Connector connector, EndPoint endPoint)
        {
            super(config,connector,endPoint,HttpCompliance.RFC7230_LEGACY,false);
        }

        @Override
        protected HttpChannelOverHttp newHttpChannel()
        {
            return new HttpChannelOverHttp(this, getConnector(), getHttpConfiguration(), getEndPoint(), this)
            {
                @Override
                public boolean startRequest(String method, String uri, HttpVersion version)
                {
                    getRequest().setAttribute("DispatchedAt",((ExtendedEndPoint)getEndPoint()).getLastSelected());
                    return super.startRequest(method,uri,version);
                }
            };
        }
    }

    @Test
    public void testExtended() throws Exception
    {
        configureServer(new DispatchedAtHandler());

        try (Socket client = newSocket(_serverURI.getHost(), _serverURI.getPort()))
        {
            OutputStream os = client.getOutputStream();

            long start=System.currentTimeMillis();
            os.write("GET / HTTP/1.0\r\n".getBytes(StandardCharsets.ISO_8859_1));
            os.flush();
            Thread.sleep(200);
            long end=System.currentTimeMillis();
            os.write("\r\n".getBytes(StandardCharsets.ISO_8859_1));
            
            // Read the response.
            String response = readResponse(client);

            Assert.assertThat(response, Matchers.containsString("HTTP/1.1 200 OK"));
            Assert.assertThat(response, Matchers.containsString("DispatchedAt="));
            
            String s=response.substring(response.indexOf("DispatchedAt=")+13);
            s=s.substring(0,s.indexOf('\n'));
            long dispatched=Long.valueOf(s);
            
            Assert.assertThat(dispatched, Matchers.greaterThanOrEqualTo(start));
            Assert.assertThat(dispatched, Matchers.lessThan(end));
        }
    }
    

    protected static class DispatchedAtHandler extends AbstractHandler
    {
        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
        {
            baseRequest.setHandled(true);
            response.setStatus(200);
            response.getOutputStream().print("DispatchedAt="+request.getAttribute("DispatchedAt")+"\r\n");
        }
    }
}
