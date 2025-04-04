/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of the Qizx application components
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.apps.util;

import com.qizx.util.logging.LogFormatter;

import java.util.logging.Handler;
import java.util.logging.LogRecord;


public class ConsoleLogger extends Handler
{
    public ConsoleLogger()
    {
        setFormatter(new LogFormatter());
    }

    @Override
    public void publish(LogRecord record)
    {
        if (!isLoggable(record))
            return;

        System.err.print(getFormatter().format(record));
        System.err.flush();
    }

    @Override
    public void flush()
    {
    }

    @Override
    public void close()
        throws SecurityException
    {
    }
}
