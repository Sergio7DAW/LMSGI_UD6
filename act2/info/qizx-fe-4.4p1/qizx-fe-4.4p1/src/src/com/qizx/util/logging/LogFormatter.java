/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.util.logging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * An implementation of {@link Formatter} for Qizx log files.
 */
public class LogFormatter extends Formatter
{
    private static final int MAX_STACK = 10;
    private int stackMax = MAX_STACK;
    
    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    
    @Override
    public String format(LogRecord record)
    {
        StringBuilder buf = new StringBuilder(64);
        buf.append(dateFormat.format(new Date(record.getMillis())));
        buf.append("  ");
        buf.append(convertLevel(record.getLevel()));
        buf.append(" ");
        buf.append(record.getMessage());
        buf.append("\r\n");
        if(record.getThrown() != null)
            printException(buf, record.getThrown());
        
        return buf.toString();
    }

    public int getStackMax()
    {
        return stackMax;
    }

    public void setStackMax(int stackMax)
    {
        this.stackMax = stackMax;
    }

    public void printException(StringBuilder buf, Throwable error)
    {
        while (error != null) {
            StackTraceElement[] stack = error.getStackTrace();
            for(int s = 0; s < stack.length && s < stackMax; s++) {
                buf.append("        ");
                buf.append(stack[s].toString());
                buf.append("\n");
            }
            error = error.getCause();
            if(error != null)
                buf.append(" caused by:\n");                
        }
    }

    private String convertLevel(Level level)
    {
        if(level == Level.SEVERE)
            return "ERROR";
        if(level == Level.WARNING)
            return "WARN ";
        if(level == Level.INFO)
            return "INFO ";
        if(level == Level.FINE)
            return "DEBUG ";
        return level.toString();
    }

}
