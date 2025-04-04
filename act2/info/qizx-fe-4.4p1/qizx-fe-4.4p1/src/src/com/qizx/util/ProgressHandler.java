/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */

package com.qizx.util;

/**
 *	Measures the progress of a time-consuming task.
 */
public interface ProgressHandler
{
    /**
     * Signals progress of task.
     * Value is a fraction between 0 and 1 (i.e 100%). TODO FIX
     */
    void progressDone(double fractionDone);
    
    /**
     * Notifies completion of the task.
     * Should be used rather than progressDone(1)
     */
    void completed();
}
