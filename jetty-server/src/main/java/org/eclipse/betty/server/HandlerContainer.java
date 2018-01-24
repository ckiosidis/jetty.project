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

import org.eclipse.betty.server.handler.HandlerCollection;
import org.eclipse.betty.server.handler.HandlerList;
import org.eclipse.betty.server.handler.HandlerWrapper;
import org.eclipse.betty.util.annotation.ManagedAttribute;
import org.eclipse.betty.util.annotation.ManagedObject;
import org.eclipse.betty.util.component.LifeCycle;

/**
 * A Handler that contains other Handlers.
 * <p>
 * The contained handlers may be one (see @{link {@link HandlerWrapper})
 * or many (see {@link HandlerList} or {@link HandlerCollection}.
 *
 */
@ManagedObject("Handler of Multiple Handlers")
public interface HandlerContainer extends LifeCycle
{
    /* ------------------------------------------------------------ */
    /**
     * @return array of handlers directly contained by this handler.
     */
    @ManagedAttribute("handlers in this container")
    public Handler[] getHandlers();
    
    /* ------------------------------------------------------------ */
    /**
     * @return array of all handlers contained by this handler and it's children
     */
    @ManagedAttribute("all contained handlers")
    public Handler[] getChildHandlers();
    
    /* ------------------------------------------------------------ */
    /**
     * @param byclass the child handler class to get
     * @return array of all handlers contained by this handler and it's children of the passed type.
     */
    public Handler[] getChildHandlersByClass(Class<?> byclass);
    
    /* ------------------------------------------------------------ */
    /**
     * @param byclass the child handler class to get
     * @return first handler of all handlers contained by this handler and it's children of the passed type.
     * @param <T> the type of handler
     */
    public <T extends Handler> T getChildHandlerByClass(Class<T> byclass);
}
