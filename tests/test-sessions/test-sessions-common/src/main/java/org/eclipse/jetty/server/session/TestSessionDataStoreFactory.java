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


package org.eclipse.jetty.server.session;

import org.eclipse.betty.server.session.AbstractSessionDataStoreFactory;
import org.eclipse.betty.server.session.SessionDataStore;
import org.eclipse.betty.server.session.SessionDataStoreFactory;
import org.eclipse.betty.server.session.SessionHandler;

/**
 * TestSessionDataStoreFactory
 *
 *
 */
public class TestSessionDataStoreFactory extends AbstractSessionDataStoreFactory
{

    /** 
     * @see SessionDataStoreFactory#getSessionDataStore(SessionHandler)
     */
    @Override
    public SessionDataStore getSessionDataStore(SessionHandler handler) throws Exception
    {
       TestSessionDataStore store = new TestSessionDataStore();
       store.setSavePeriodSec(getSavePeriodSec());
       return store;
    }

}
