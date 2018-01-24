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

package org.eclipse.jetty.http2.server;

import org.eclipse.betty.http.BadMessageException;
import org.eclipse.betty.http.HttpFields;
import org.eclipse.betty.http.MetaData.Request;
import org.eclipse.betty.io.Connection;
import org.eclipse.betty.io.EndPoint;
import org.eclipse.betty.server.ConnectionFactory;
import org.eclipse.betty.server.Connector;
import org.eclipse.betty.server.HttpConfiguration;
import org.eclipse.betty.server.HttpConnectionFactory;
import org.eclipse.betty.util.annotation.Name;
import org.eclipse.betty.util.log.Log;
import org.eclipse.betty.util.log.Logger;


/* ------------------------------------------------------------ */
/** HTTP2 Clear Text Connection factory.
 * <p>This extension of HTTP2ServerConnection Factory sets the
 * protocol name to "h2c" as used by the clear text upgrade mechanism
 * for HTTP2 and marks all TLS ciphers as unacceptable.
 * </p>
 * <p>If used in combination with a {@link HttpConnectionFactory} as the
 * default protocol, this factory can support the non-standard direct
 * update mechanism, where a HTTP1 request of the form "PRI * HTTP/2.0"
 * is used to trigger a switch to a HTTP2 connection.    This approach
 * allows a single port to accept either HTTP/1 or HTTP/2 direct
 * connections.
 */
public class HTTP2CServerConnectionFactory extends HTTP2ServerConnectionFactory implements ConnectionFactory.Upgrading
{
    private static final Logger LOG = Log.getLogger(HTTP2CServerConnectionFactory.class);

    public HTTP2CServerConnectionFactory(@Name("config") HttpConfiguration httpConfiguration)
    {
        this(httpConfiguration,"h2c");
    }
    
    public HTTP2CServerConnectionFactory(@Name("config") HttpConfiguration httpConfiguration, @Name("protocols") String... protocols)
    {
        super(httpConfiguration,protocols);
        for (String p:protocols)
            if (!HTTP2ServerConnection.isSupportedProtocol(p))
                throw new IllegalArgumentException("Unsupported HTTP2 Protocol variant: "+p);
    }

    @Override
    public boolean isAcceptable(String protocol, String tlsProtocol, String tlsCipher)
    {
        // Never use TLS with h2c
        return false;
    }

    @Override
    public Connection upgradeConnection(Connector connector, EndPoint endPoint, Request request, HttpFields response101) throws BadMessageException
    {
        if (LOG.isDebugEnabled())
            LOG.debug("{} upgraded {}{}", this, request.toString(), request.getFields());

        if (request.getContentLength() > 0)
            return null;

        HTTP2ServerConnection connection = (HTTP2ServerConnection)newConnection(connector, endPoint);
        if (connection.upgrade(request))
            return connection;
        return null;
    }
}
