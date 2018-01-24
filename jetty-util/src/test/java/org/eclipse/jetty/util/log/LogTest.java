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

package org.eclipse.jetty.util.log;


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.betty.util.log.JavaUtilLog;
import org.eclipse.betty.util.log.Log;
import org.eclipse.betty.util.log.Logger;
import org.eclipse.betty.util.log.Slf4jLog;
import org.eclipse.betty.util.log.StdErrLog;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class LogTest
{
    private static Logger originalLogger;
    private static Map<String,Logger> originalLoggers;

    @BeforeClass
    public static void rememberOriginalLogger()
    {
        originalLogger = Log.getLog();
        originalLoggers = new HashMap<String, Logger>(Log.getLoggers());
        Log.getMutableLoggers().clear();
    }

    @AfterClass
    public static void restoreOriginalLogger()
    {
        Log.setLog(originalLogger);
        Log.getMutableLoggers().clear();
        Log.getMutableLoggers().putAll(originalLoggers);
    }
    
    @Test
    public void testDefaultLogging()
    {
        Logger log = Log.getLogger(LogTest.class);
        log.info("Test default logging");
    }
    
    @Test
    public void testNamedLogNamed_StdErrLog()
    {
        Log.setLog(new StdErrLog());

        assertNamedLogging(Red.class);
        assertNamedLogging(Blue.class);
        assertNamedLogging(Green.class);
    }

    @Test
    public void testNamedLogNamed_JUL()
    {
        Log.setLog(new JavaUtilLog());

        assertNamedLogging(Red.class);
        assertNamedLogging(Blue.class);
        assertNamedLogging(Green.class);
    }

    @Test
    public void testNamedLogNamed_Slf4J() throws Exception
    {
        Log.setLog(new Slf4jLog());

        assertNamedLogging(Red.class);
        assertNamedLogging(Blue.class);
        assertNamedLogging(Green.class);
    }

    private void assertNamedLogging(Class<?> clazz)
    {
        Logger lc = Log.getLogger(clazz);
        Assert.assertEquals("Named logging (impl=" + Log.getLog().getClass().getName() + ")",lc.getName(),clazz.getName());
    }
    
    @Test
    public void testCondensePackage()
    {
        String cases[][] = new String[][]{
                {null, ""},
                {"org.eclipse.Foo.\u0000", "oe.Foo"},
                {".foo", "foo"},
                {".bar.Foo", "b.Foo"},
                {"org...bar..Foo", "ob.Foo"}
        };
    
        StdErrLog log = new StdErrLog();
        
        for (int i = 0; i < cases.length; i++)
        {
            // System.err.printf("newLogger(%s)%n", cases[i][0]);
            StdErrLog logger = (StdErrLog) log.newLogger(cases[i][0]);
            assertThat("log[" + cases[i][0] + "] condenses to name", logger._abbrevname, is(cases[i][1]));
        }
    }
}
