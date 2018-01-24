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


package org.eclipse.jetty.maven.plugin;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import org.eclipse.betty.io.ByteBufferPool;
import org.eclipse.betty.io.EndPoint;
import org.eclipse.betty.server.ConnectionFactory;
import org.eclipse.betty.server.Connector;
import org.eclipse.betty.server.Server;
import org.eclipse.betty.server.ServerConnector;
import org.eclipse.betty.util.annotation.ManagedAttribute;
import org.eclipse.betty.util.component.Graceful;
import org.eclipse.betty.util.component.ContainerLifeCycle;
import org.eclipse.betty.util.thread.Scheduler;




/**
 * MavenServerConnector
 *
 * As the ServerConnector class does not have a no-arg constructor, and moreover requires
 * the server instance passed in to all its constructors, it cannot
 * be referenced in the pom.xml. This class wraps a ServerConnector, delaying setting the
 * server instance. Only a few of the setters from the ServerConnector class are supported.
 */
public class MavenServerConnector extends ContainerLifeCycle implements Connector
{
    public static String PORT_SYSPROPERTY = "jetty.http.port";
    
    public static final int DEFAULT_PORT = 8080;
    public static final String DEFAULT_PORT_STR = String.valueOf(DEFAULT_PORT);
    public static final int DEFAULT_MAX_IDLE_TIME = 30000;
    
    private Server server;
    private ServerConnector delegate;
    private String host;
    private String name;
    private int port;
    private long idleTimeout;
    private int lingerTime;
    
    
    public MavenServerConnector()
    {
    }
    
    public void setServer(Server server)
    {
        this.server = server;
    }

    public void setHost(String host)
    {
        this.host = host;
    }
   
    public String getHost()
    {
        return this.host;
    }

    public void setPort(int port)
    {
        this.port = port;
    }
    
    public int getPort ()
    {
        return this.port;
    }

    public void setName (String name)
    {
        this.name = name;
    }
    
    public void setIdleTimeout(long idleTimeout)
    {
        this.idleTimeout = idleTimeout;
    }
    
    public void setSoLingerTime(int lingerTime)
    {
        this.lingerTime = lingerTime;
    }
    
    @Override
    protected void doStart() throws Exception
    {

        if (this.server == null)
            throw new IllegalStateException("Server not set for MavenServerConnector");

        this.delegate = new ServerConnector(this.server);
        this.delegate.setName(this.name);
        this.delegate.setPort(this.port);
        this.delegate.setHost(this.host);
        this.delegate.setIdleTimeout(idleTimeout);
        this.delegate.setSoLingerTime(lingerTime);
        this.delegate.start();

        super.doStart();
    }

    @Override
    protected void doStop() throws Exception
    {
        this.delegate.stop();
        super.doStop();
        this.delegate = null;
    }

    /** 
     * @see Graceful#shutdown()
     */
    @Override
    public Future<Void> shutdown()
    {
        checkDelegate();
        return this.delegate.shutdown();
    }

    /** 
     * @see Connector#getServer()
     */
    @Override
    public Server getServer()
    {
        return this.server;
    }

    /** 
     * @see Connector#getExecutor()
     */
    @Override
    public Executor getExecutor()
    {
        checkDelegate();
        return this.delegate.getExecutor();
    }

    /** 
     * @see Connector#getScheduler()
     */
    @Override
    public Scheduler getScheduler()
    {
        checkDelegate();
        return this.delegate.getScheduler();
    }

    /** 
     * @see Connector#getByteBufferPool()
     */
    @Override
    public ByteBufferPool getByteBufferPool()
    {
        checkDelegate();
        return this.delegate.getByteBufferPool();
    }

    /** 
     * @see Connector#getConnectionFactory(java.lang.String)
     */
    @Override
    public ConnectionFactory getConnectionFactory(String nextProtocol)
    {
        checkDelegate();
        return this.delegate.getConnectionFactory(nextProtocol);
    }

    /** 
     * @see Connector#getConnectionFactory(java.lang.Class)
     */
    @Override
    public <T> T getConnectionFactory(Class<T> factoryType)
    {
        checkDelegate();
        return this.delegate.getConnectionFactory(factoryType);
    }

    /** 
     * @see Connector#getDefaultConnectionFactory()
     */
    @Override
    public ConnectionFactory getDefaultConnectionFactory()
    {
        checkDelegate();
        return this.delegate.getDefaultConnectionFactory();
    }

    /** 
     * @see Connector#getConnectionFactories()
     */
    @Override
    public Collection<ConnectionFactory> getConnectionFactories()
    {
        checkDelegate();
        return this.delegate.getConnectionFactories();
    }

    /** 
     * @see Connector#getProtocols()
     */
    @Override
    public List<String> getProtocols()
    {
        checkDelegate();
        return this.delegate.getProtocols();
    }

    /** 
     * @see Connector#getIdleTimeout()
     */
    @Override
    @ManagedAttribute("maximum time a connection can be idle before being closed (in ms)")
    public long getIdleTimeout()
    {
        checkDelegate();
        return this.delegate.getIdleTimeout();
    }

    /** 
     * @see Connector#getTransport()
     */
    @Override
    public Object getTransport()
    {
        checkDelegate();
        return this.delegate.getTransport();
    }

    /** 
     * @see Connector#getConnectedEndPoints()
     */
    @Override
    public Collection<EndPoint> getConnectedEndPoints()
    {
        checkDelegate();
        return this.delegate.getConnectedEndPoints();
    }

    /** 
     * @see Connector#getName()
     */
    @Override
    public String getName()
    {
        return this.name;
    }

    public int getLocalPort()
    {
        return this.delegate.getLocalPort();
    }
    
    private void checkDelegate() throws IllegalStateException
    {
        if (this.delegate == null)
            throw new IllegalStateException ("MavenServerConnector delegate not ready");
    }
}
