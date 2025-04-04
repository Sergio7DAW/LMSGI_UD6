/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.util.logging;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/**
 * Implementation of {@link Handler} that changes of log file each day.
 */
public class DailyRollingFileHandler extends StreamHandler
{
    private SimpleDateFormat datePattern =
        new SimpleDateFormat("-yyyy-MM-dd");
    private GregorianCalendar calendar = new GregorianCalendar();
    protected String pathPattern;
    protected long currentDay;
    
    
    /**
     * Builds a DailyRollingFileHandler writing to 
     * @param pathPattern path of the current output file
     */
    public DailyRollingFileHandler(String pathPattern)
    {
        this.pathPattern = pathPattern;
        setFormatter(new LogFormatter());
        setLevel(Level.FINEST);     // filtered by Logger
    }
    
    @Override
    public void publish(LogRecord record)
    {
        long time = record.getMillis();
        long day = getDay(time);
        if(day != currentDay) {
            try {
                startFile(time);
            }
            catch (Exception e) {
                System.err.println("*** Logger: " + e);
            }
            currentDay = day;
        }
//        System.err.println(">>>>>> " + new Date(time) + ": "+record.getMessage());
//        System.err.println("    >> " + isLoggable(record));
        super.publish(record);
        flush();
    }

    // start proper file for record
    private void startFile(long time)
        throws SecurityException, FileNotFoundException
    {
        File current = new File(pathPattern);
        if(current.exists()) {
            long modifTime = current.lastModified();
            int modifDay = getDay(modifTime);
            if(getDay(time) != modifDay)    // what if exactly 1 year ago?
                rollTo(modifTime);
        }
        setOutputStream(new FileOutputStream(current, true));
    }

    private void rollTo(long oldTime)
    {
        File current = new File(pathPattern);
        File old = new File(fileName(oldTime));
        current.renameTo(old);
        
         // TODO delete logs older than D days
    }

    private String fileName(long time)
    {
        return pathPattern + datePattern.format(new Date(time));
    }

    private int getDay(long time)
    {
        // FIX: was returning GMT day
        calendar.setTimeInMillis(time);
        return calendar.get(GregorianCalendar.DAY_OF_YEAR);
    }

    @Override
    public synchronized void close()
        throws SecurityException
    {
        super.close();
    }
}
