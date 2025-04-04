/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.api.util.logging;

import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Implementation of {@link Log} on top of an OutputStream.
 */
public class StreamLog
    implements Log
{
    private static final int MAX_STACK = 10;
    
    private OutputStream output;
    private PrintStream printer;
    private Level level;
    private int ilevel;
    private int errCnt;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");

    public StreamLog(OutputStream output)
    {
        this.output = output;
        this.printer = new PrintStream(output, true);
    }
    
    public String getName()
    {
        return "streamlog";
    }

    public int getWorkLevel()
    {
        return ilevel;
    }

    public Level getLevel()
    {
        return level;
    }

    public void setLevel(Level level)
    {
        this.level = level;
        this.ilevel = level.level;
    }

    public int getErrorCount()
    {
        return errCnt;
    }

    public void setErrorCount(int count)
    {
        errCnt = count;
    }

    public int getWarningCount()
    {
        return 0;
    }

    public void setWarningCount(int count)
    {
    }

    public boolean allowsDebug()
    {
        return ilevel >= LOG_DEBUG;
    }

    public boolean allowsError()
    {
        return ilevel >= LOG_ERROR;
    }

    public boolean allowsInfo()
    {
        return ilevel >= LOG_INFO;
    }

    public boolean allowsWarning()
    {
        return ilevel >= LOG_WARNING;
    }

    public void error(String message)
    {
        error(message, null);
    }

    public void error(String message, Throwable error)
    {
        ++ errCnt;
        output("ERROR", message, error);
    }

    public void warning(String message)
    {
        warning(message, null);
    }

    public void warning(String message, Throwable error)
    {
        output("WARN ", message, error);
    }

    public void info(String message)
    {
        output("INFO ", message, null);
    }

    public void info(String message, Throwable error)
    {
        output("INFO ", message, error);
    }

    public void debug(String message)
    {
        output("DEBUG", message, null);
    }

    public void debug(String message, Throwable error)
    {
        output("DEBUG", message, error);
    }

    protected void output(String lev, String message, Throwable error)
    {
        printer.print(dateFormat.format(new Date()));
        printer.print("  ");
        printer.print(lev);
        printer.print(" ");
        printer.print(message);
        printer.println();
        if(error != null)
            printException(error);
        printer.flush();
    }
    
    public void printException(Throwable error)
    {
        while (error != null) {
            StackTraceElement[] stack = error.getStackTrace();
            for(int s = 0; s < stack.length && s < MAX_STACK; s++) {
                printer.append("        ");
                printer.append(stack[s].toString());
                printer.append("\n");
            }
            error = error.getCause();
            if(error != null)
                printer.append(" caused by:\n");                
        }
    }
}
