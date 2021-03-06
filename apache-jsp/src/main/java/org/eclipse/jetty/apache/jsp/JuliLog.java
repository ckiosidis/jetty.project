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

package org.eclipse.jetty.apache.jsp;

import org.eclipse.betty.util.log.Log;
import org.eclipse.betty.util.log.Logger;
import org.eclipse.betty.util.log.StdErrLog;

public class JuliLog implements org.apache.juli.logging.Log
{
    public static org.apache.juli.logging.Log getInstance(String name)
    {
        return new JuliLog(name);
    }
    
    private final Logger _logger;
    private final StdErrLog _stdErrLog;

    public JuliLog()
    {    
        _logger= Log.getRootLogger();
        _stdErrLog=(_logger instanceof StdErrLog) ? (StdErrLog)_logger:null;
    }
    
    public JuliLog(String name)
    {
        _logger= Log.getLogger(name);
        _stdErrLog=(_logger instanceof StdErrLog) ? (StdErrLog)_logger:null;
    }
    
    @Override
    public boolean isDebugEnabled()
    {
        return _logger.isDebugEnabled();
    }

    @Override
    public boolean isErrorEnabled()
    {
        return _stdErrLog==null?true:_stdErrLog.getLevel()<= StdErrLog.LEVEL_WARN;
    }

    @Override
    public boolean isFatalEnabled()
    {
        return _stdErrLog==null?true:_stdErrLog.getLevel()<= StdErrLog.LEVEL_WARN;
    }

    @Override
    public boolean isInfoEnabled()
    {
        return _stdErrLog==null?true:_stdErrLog.getLevel()<= StdErrLog.LEVEL_INFO;
    }

    @Override
    public boolean isTraceEnabled()
    {
        return _stdErrLog==null?true:_stdErrLog.getLevel()<= StdErrLog.LEVEL_DEBUG;
    }

    @Override
    public boolean isWarnEnabled()
    {
        return _stdErrLog==null?true:_stdErrLog.getLevel()<= StdErrLog.LEVEL_WARN;
    }

    @Override
    public void trace(Object message)
    {
        if (message instanceof String)
            _logger.debug((String)message);
        else
            _logger.debug("{}",message);
    }

    @Override
    public void trace(Object message, Throwable t)
    {
        if (message instanceof String)
            _logger.debug((String)message,t);
        else
            _logger.debug("{}",message,t);
    }

    @Override
    public void debug(Object message)
    {
        if (message instanceof String)
            _logger.debug((String)message);
        else
            _logger.debug("{}",message);
    }

    @Override
    public void debug(Object message, Throwable t)
    {
        if (message instanceof String)
            _logger.debug((String)message,t);
        else
            _logger.debug("{}",message,t);
    }

    @Override
    public void info(Object message)
    {
        if (message instanceof String)
            _logger.info((String)message);
        else
            _logger.info("{}",message);
    }

    @Override
    public void info(Object message, Throwable t)
    {
        if (message instanceof String)
            _logger.info((String)message,t);
        else
            _logger.info("{}",message,t);
    }

    @Override
    public void warn(Object message)
    {
        if (message instanceof String)
            _logger.warn((String)message);
        else
            _logger.warn("{}",message);
    }

    @Override
    public void warn(Object message, Throwable t)
    {
        if (message instanceof String)
            _logger.warn((String)message,t);
        else
            _logger.warn("{}",message,t);
    }

    @Override
    public void error(Object message)
    {
        if (message instanceof String)
            _logger.warn((String)message);
        else
            _logger.warn("{}",message);
    }

    @Override
    public void error(Object message, Throwable t)
    {
        if (message instanceof String)
            _logger.warn((String)message,t);
        else
            _logger.warn("{}",message,t);
    }

    @Override
    public void fatal(Object message)
    {
        if (message instanceof String)
            _logger.warn((String)message);
        else
            _logger.warn("{}",message);
    }

    @Override
    public void fatal(Object message, Throwable t)
    {
        if (message instanceof String)
            _logger.warn((String)message,t);
        else
            _logger.warn("{}",message,t);
    }
}


