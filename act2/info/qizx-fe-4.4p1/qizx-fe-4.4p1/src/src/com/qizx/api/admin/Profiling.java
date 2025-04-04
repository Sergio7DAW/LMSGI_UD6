/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.api.admin;

import com.qizx.xquery.op.Expression;

/**
 * Profiling annotations on a XQuery expression.
 */
public class Profiling
    implements Comparable<Profiling>
{
    public static final String EXECUTED = "Execution count";
    public static final String UNMATCHED_NAME = "Unknown name";
    public static final String INDEXABLE = "Index used";
    public static final String NON_INDEXABLE = "Index not usable";
    public static final String INDEX_USELESS = "Index not worth using";
    public static final String EXPR_REWRITE = "Rewrite";
    public static final String JOIN = "Join";

    protected String type;
    public int count;
    protected int startPoint;
    protected int endPoint;
    protected String message;
    //protected Object argument;

    public Profiling(String type, int count, int startPoint, int endPoint,
                     String message)
    {
        this.type = type;
        this.count = count;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.message = message;
    }

    protected Expression location;
    private Profiling next;

    public Profiling(String type, Expression location, Profiling next)
    {
        this.type = type;
        this.location = location;
        this.next = next;
        startPoint = location.offset;
        endPoint = location.endOffset;
        
    }

    public String displayLoc()
    {
        return type + ": " + startPoint + "-" + endPoint;
    }

    public int startPoint()
    {
        return (location != null)? location.offset : startPoint;
    }

    public int endPoint()
    {
        return (location != null)? location.endOffset : endPoint;
    }

    public String type()
    {
        return type;
    }

    public String getType()
    {
        return type;
    }

    public int getCount()
    {
        return count;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public Profiling getNext()
    {
        return next;
    }

    public int compareTo(Profiling ann)   // wider first
    {
        int d = this.startPoint - ann.startPoint;
        if (d == 0)
            d = ann.endPoint - this.endPoint;
        return d;
    }

    @Override
    public String toString()
    {
        return "Profiling [type=" + type + ", count=" + count
               + ", startPoint=" + startPoint + ", endPoint=" + endPoint
               + ", message=" + message + "]";
    }
}
