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

package org.eclipse.betty.server;

import org.eclipse.betty.server.handler.RequestLogHandler;

/** 
 * A <code>RequestLog</code> can be attached to a {@link RequestLogHandler} to enable
 * logging of requests/responses.
 * @see RequestLogHandler#setRequestLog(RequestLog)
 * @see Server#setRequestLog(RequestLog)
 */
public interface RequestLog
{
    /* ------------------------------------------------------------ */
    /**
     * @param request The request to log.
     * @param response The response to log.  Note that for some requests
     * the response instance may not have been fully populated (Eg 400 bad request
     * responses are sent without a servlet response object).  Thus for basic
     * log information it is best to consult {@link Response#getCommittedMetaData()}
     * and {@link Response#getHttpChannel()} directly.
     */
    public void log(Request request, Response response);
}
