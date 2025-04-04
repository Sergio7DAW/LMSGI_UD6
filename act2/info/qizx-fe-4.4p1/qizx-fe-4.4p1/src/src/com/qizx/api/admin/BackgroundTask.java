/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.api.admin;

import com.qizx.util.ProgressHandler;

public class BackgroundTask implements ProgressHandler
{
    protected String taskName;
    protected String libraryName;
    protected long startTime;
    protected long endTime; // expected if not yet finished
    // fraction done so far:
    private volatile double done;

    public BackgroundTask(String taskName, String libraryName)
    {
        this.taskName = taskName;
        this.libraryName = libraryName;
        startTime = System.currentTimeMillis();
    }

    public String getTaskName()
    {
        return taskName;
    }

    public String getLibraryName()
    {
        return libraryName;
    }

    public long getStartTime()
    {
        return startTime;
    }

    public void setStartTime(long start)
    {
        startTime = start;
    }

    public long getEndTime()
    {
        return endTime;
    }

    public void setEndTime(long endTime)
    {
        this.endTime = endTime;
    }

    public double getDone()
    {
        return done;
    }

    public void setDone(double done)
    {
        this.done = done;
    }

    public void progressDone(double fractionDone)
    {
        this.done = fractionDone;
        if (fractionDone >= 0.1 && done != 1) {
            long now = System.currentTimeMillis();
            long dur = (long) ((now - startTime) / fractionDone);
            endTime = startTime + dur;
            
            long rounding = dur / 50;
            if(rounding < 5000)
                rounding = 5000;
            else if(rounding < 20000)
                rounding = 20000;
            else
                rounding = 60000;

            dur -= dur % rounding;
            endTime = startTime + dur;
        }
    }

    @Override
    public void completed()
    {
        this.done = 1;
        this.endTime = System.currentTimeMillis();
    }

    @Override
    public String toString()
    {
        return "BackgroundTask[taskName=" + taskName 
                + ", libraryName=" + libraryName
                + ", startTime=" + startTime
                + ", endTime=" + endTime
                + ", done=" + done + "]";
    }
}
