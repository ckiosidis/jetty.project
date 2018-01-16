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

package org.eclipse.jetty.websocket.core.server;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.server.Request;

public class HandshakerFactory
{
    public static Handshaker getHandshaker(HttpServletRequest request)
    {
        // TODO: flesh out better

        Request baseRequest = Request.getBaseRequest(request);
        if (baseRequest != null)
        {
            Handshaker handshaker = baseRequest.getHttpChannel().getConnector().getBean(Handshaker.class);
            if (handshaker != null)
            {
                return handshaker;
            }
        }

        // TODO: use request knowledge about handshake type. eg: HTTP/1.1 or HTTP/2
        return new RFC6455Handshaker();
    }
}