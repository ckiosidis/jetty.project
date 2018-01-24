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

package org.eclipse.jetty.memcached.session;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import org.eclipse.betty.server.session.SessionContext;
import org.eclipse.betty.server.session.SessionData;
import org.eclipse.betty.server.session.SessionDataMap;
import org.eclipse.betty.util.annotation.ManagedAttribute;
import org.eclipse.betty.util.annotation.ManagedObject;
import org.eclipse.betty.util.component.AbstractLifeCycle;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;



/**
 * MemcachedSessionDataMap
 *
 * Uses memcached as a cache for SessionData.
 */
@ManagedObject
public class MemcachedSessionDataMap extends AbstractLifeCycle implements SessionDataMap
{
    public static final String DEFAULT_HOST = "localhost";
    public static final String DEFAULT_PORT = "11211";
    protected MemcachedClient _client;
    protected int _expirySec = 0;
    protected boolean _heartbeats = true;
    protected XMemcachedClientBuilder _builder;

    
    

    /**
     * @param host address of memcache server
     * @param port address of memcache server
     */
    public MemcachedSessionDataMap(String host, String port)
    {
        if (host == null || port == null)
            throw new IllegalArgumentException("Host: "+host+" port: "+port);
        _builder = new XMemcachedClientBuilder(host+":"+port);
    }
    
    
    public MemcachedSessionDataMap (List<InetSocketAddress> addresses)
    {
        _builder = new XMemcachedClientBuilder(addresses);
    }
    
    
    public MemcachedSessionDataMap (List<InetSocketAddress> addresses, int[] weights)
    {
        _builder = new XMemcachedClientBuilder(addresses, weights);
    }
    
    /**
     * @return the builder
     */
    public XMemcachedClientBuilder getBuilder()
    {
        return _builder;
    }

 
    
    
    /**
     * @param sec the expiry to use in seconds
     */
    public void setExpirySec (int sec)
    {
        _expirySec = sec;
    }
    
    /**
     * Expiry time for memached entries.
     * @return memcached expiry time in sec
     */
    @ManagedAttribute(value="memcached expiry time in sec", readonly=true)
    public int getExpirySec ()
    {
        return _expirySec;
    }

    @ManagedAttribute(value="enable memcached heartbeats", readonly=true)
    public boolean isHeartbeats()
    {
        return _heartbeats;
    }


    public void setHeartbeats(boolean heartbeats)
    {
        _heartbeats = heartbeats;
    }


    /** 
     * @see SessionDataMap#initialize(SessionContext)
     */
    @Override
    public void initialize(SessionContext context)
    {
        try
        {
            _client = _builder.build();
            _client.setEnableHeartBeat(isHeartbeats());
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
    }

    /** 
     * @see SessionDataMap#load(java.lang.String)
     */
    @Override
    public SessionData load(String id) throws Exception
    {
        SessionData data = _client.get(id);
        return data;
    }

    
    /** 
     * @see SessionDataMap#store(java.lang.String, SessionData)
     */
    @Override
    public void store(String id, SessionData data) throws Exception
    {
        _client.set(id, _expirySec, data);
    }        

    
    /** 
     * @see SessionDataMap#delete(java.lang.String)
     */
    @Override
    public boolean delete(String id) throws Exception
    {
        _client.delete(id);
        return true; //delete returns false if the value didn't exist
    }



    @Override
    protected void doStop() throws Exception
    {
        super.doStop();
        if (_client != null)
        {
            _client.shutdown();
            _client = null;
        }
    }
    
    
}
