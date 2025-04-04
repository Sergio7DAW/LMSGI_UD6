/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of the Qizx application components
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.apps.studio.gui;

import com.qizx.util.logging.LogFormatter;

import java.awt.Color;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.text.Style;


public class TextPortLogger extends Handler
{
    protected TextPort textPort;
    protected Style errorStyle;
    protected Style warningStyle;

    public TextPortLogger(TextPort textPort)
    {
        this.textPort = textPort;
        LogFormatter formatter = new LogFormatter();
        formatter.setStackMax(0);
        setFormatter(formatter);
        errorStyle = textPort.addStyle("error", Color.red, false);
        warningStyle = textPort.addStyle("warning", new Color(240, 100, 0), false);
    }

    protected void newEvent(Level level)
    {
         // redefinable
    }

    @Override
    public void publish(LogRecord record)
    {
        if(!isLoggable(record))
            return;
        Style style = null;
        if(record.getLevel() == Level.SEVERE)
            style = errorStyle;
        else if(record.getLevel() == Level.WARNING)
            style = warningStyle;
        textPort.appendText(getFormatter().format(record), style);
        newEvent(record.getLevel());
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
