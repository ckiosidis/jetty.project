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

import org.eclipse.betty.util.component.LifeCycle;
import org.eclipse.betty.util.resource.Resource;

/**
 * ServerListener
 *
 * Listener to create a file that signals that the startup is completed.
 * Used by the JettyRunDistro maven goal to determine that the child
 * process is started, and that jetty is ready.
 */
public class ServerListener implements LifeCycle.Listener
{
    
    private String _tokenFile;
    
    public void setTokenFile(String file)
    {
        _tokenFile = file;      
    }

    
    public String getTokenFile ()
    {
        return _tokenFile;
    }
    /** 
     * @see LifeCycle.Listener#lifeCycleStarting(LifeCycle)
     */
    @Override
    public void lifeCycleStarting(LifeCycle event)
    {
       
    }

    /** 
     * @see LifeCycle.Listener#lifeCycleStarted(LifeCycle)
     */
    @Override
    public void lifeCycleStarted(LifeCycle event)
    {
        if (_tokenFile != null)
        {
            try
            {
                Resource r = Resource.newResource(_tokenFile);
                r.getFile().createNewFile();
            }
            catch (Exception e)
            {
                throw new IllegalStateException(e);
            }
        }

    }

    /** 
     * @see LifeCycle.Listener#lifeCycleFailure(LifeCycle, java.lang.Throwable)
     */
    @Override
    public void lifeCycleFailure(LifeCycle event, Throwable cause)
    {
       
    }

    /** 
     * @see LifeCycle.Listener#lifeCycleStopping(LifeCycle)
     */
    @Override
    public void lifeCycleStopping(LifeCycle event)
    {
       
    }

    /** 
     * @see LifeCycle.Listener#lifeCycleStopped(LifeCycle)
     */
    @Override
    public void lifeCycleStopped(LifeCycle event)
    {
        
    }


}
