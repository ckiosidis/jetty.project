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

package org.eclipse.jetty.alpn.java.client;

import java.net.InetSocketAddress;
import java.security.Security;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.conscrypt.OpenSSLProvider;
import org.eclipse.betty.http.HttpFields;
import org.eclipse.betty.http.HttpURI;
import org.eclipse.betty.http.HttpVersion;
import org.eclipse.betty.http.MetaData;
import org.eclipse.jetty.http2.api.Session;
import org.eclipse.jetty.http2.api.Stream;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.frames.DataFrame;
import org.eclipse.jetty.http2.frames.HeadersFrame;
import org.eclipse.betty.util.Callback;
import org.eclipse.betty.util.FuturePromise;
import org.eclipse.betty.util.Jetty;
import org.eclipse.betty.util.Promise;
import org.eclipse.betty.util.ssl.SslContextFactory;

public class ConscryptHTTP2Client
{
    public static void main(String[] args) throws Exception
    {
        Security.addProvider(new OpenSSLProvider());
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setProvider("Conscrypt");
        HTTP2Client client = new HTTP2Client();
        client.addBean(sslContextFactory);
        client.start();

        String host = "webtide.com";
        int port = 443;

        FuturePromise<Session> sessionPromise = new FuturePromise<>();
        client.connect(sslContextFactory, new InetSocketAddress(host, port), new Session.Listener.Adapter(), sessionPromise);
        Session session = sessionPromise.get(5, TimeUnit.SECONDS);

        HttpFields requestFields = new HttpFields();
        requestFields.put("User-Agent", client.getClass().getName() + "/" + Jetty.VERSION);
        MetaData.Request metaData = new MetaData.Request("GET", new HttpURI("https://" + host + ":" + port + "/"), HttpVersion.HTTP_2, requestFields);
        HeadersFrame headersFrame = new HeadersFrame(metaData, null, true);
        CountDownLatch latch = new CountDownLatch(1);
        session.newStream(headersFrame, new Promise.Adapter<>(), new Stream.Listener.Adapter()
        {
            @Override
            public void onHeaders(Stream stream, HeadersFrame frame)
            {
                System.err.println(frame);
                if (frame.isEndStream())
                    latch.countDown();
            }

            @Override
            public void onData(Stream stream, DataFrame frame, Callback callback)
            {
                System.err.println(frame);
                callback.succeeded();
                if (frame.isEndStream())
                    latch.countDown();
            }
        });

        latch.await(5, TimeUnit.SECONDS);

        client.stop();
    }
}
