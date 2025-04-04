/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.util.basic;

import com.qizx.util.ProgressHandler;

/**
 * Convenience implementation of ProgressHandler.
 * <p>
 * Can represent a part of a larger parent task (specifying its share in the
 * parent task).
 * <p>If the task is performed by another thread, {@link #awaitCompletion()}
 * can be used to block until the task is completed (notified through method
 * {@link #completed()}).
 */
public class TaskInProgress implements ProgressHandler
{
    protected String name;
    protected TaskInProgress parent;
    protected ProgressHandler handler;
    protected double share;   // in parent
    protected boolean isLast; // in parent
    protected boolean trace; 
    // new: abort task
    protected volatile boolean aborted; 
    // fraction done so far:
    protected volatile double done;
    // cumulated by completed children:
    protected volatile double childDone;
    
    
    public TaskInProgress(String name, TaskInProgress parent)
    {
        this(name, parent, 1);
    }
    
    public TaskInProgress(String name, TaskInProgress parent, double share)
    {
        this.name = name;
        this.parent = parent;
        this.share = share;
    }
    
    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return "TaskInProgress [name=" + name + ", share=" + share
               + ", isLast=" + isLast + ", childDone=" + childDone + "]";
    }

    public void setProgressHandler(ProgressHandler handler)
    {
        this.handler = handler; 
    }

    public void setTrace(boolean trace)
    {
        this.trace = trace;
    }

    public TaskInProgress getParent()
    {
        return parent;
    }

    public void setParent(TaskInProgress parent)
    {
        this.parent = parent;
    }

    public double getShare()
    {
        return share;
    }

    public void setShare(double share)
    {
        this.share = share;
    }

    public boolean isLast()
    {
        return isLast;
    }

    public void setLast(boolean isLastTask)
    {
        this.isLast = isLastTask;
    }

    public void reset()
    {
        done = 0;
    }

    public synchronized void progressDone(double fractionDone)
    {
        done = childDone + fractionDone;
        if (trace) 
            System.err.println(this +" progressDone "+done);
        if(handler != null)
            handler.progressDone(done);
        if (parent != null) {
            parent.progressDone(done * share);
        }
    }

    public double getProgressDone()
    {
        return done;
    }
    
    public double getAllDone()
    {
        return childDone;
    }

    /**
     * Declares a task completed. Updates the 'done' part of the parent,
     * iteratively propagated to ancestors.
     * CALL ONLY on leaf tasks!
     */
    public void completed()
    {
        if (trace) 
            System.err.println(this +" COMPLETED");
        synchronized(this) {
            done = 1;
            notifyAll();
        }
        if(handler != null)
            handler.completed();
        
        if(parent != null)
            parent.childCompleted(share);
        
        if(isLast) {
            if(parent != null)
                parent.completed();
        }
    }
    
    protected void childCompleted(double childShare)
    {
        childDone += childShare;
        progressDone(0);    // additional
    }

    public boolean isCompleted()
    {
        return done >= 1;
    }

    public boolean isAborted()
    {
        return aborted;
    }

    public void setAborted(boolean aborted)
    {
        this.aborted = aborted;
    }
}
