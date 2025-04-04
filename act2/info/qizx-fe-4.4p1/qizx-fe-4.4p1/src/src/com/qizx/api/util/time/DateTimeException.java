/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */

package com.qizx.api.util.time;

/**
 *	
 */
public class DateTimeException extends Exception
{
    public String parsedInput;

    public DateTimeException(String msg)
    {
        super(msg);
    }

    public DateTimeException(String msg, String parsedInput)
    {
        super(msg);
        this.parsedInput = parsedInput;
    }
}
