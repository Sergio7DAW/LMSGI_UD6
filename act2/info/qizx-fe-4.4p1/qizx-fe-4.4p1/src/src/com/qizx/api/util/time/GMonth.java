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
 * Representation of a GMonth item value.
 */
public class GMonth extends DateTimeBase
{
    public GMonth(int month, int tzSign, int tzHour, int tzMinute)
        throws DateTimeException
    {
        super(EPOCH, month, 1, 0, 0, 0, tzSign, tzHour, tzMinute);
        // add controls here
    }

    public GMonth(DateTimeBase dt) throws DateTimeException
    {
        this(dt.month, dt.tzSign, dt.tzHour, dt.tzMinute);
    }

    public DateTimeBase parse(String that)
        throws DateTimeException
    {
        return parseGMonth(that);
    }

    public static GMonth parseGMonth(String s)
        throws DateTimeException
    {
        int month;
        int[] tz = new int[3];

        if (!s.startsWith("--"))
            throw new DateTimeException("invalid gMonth syntax", s);

        int pos = 2;
        int tzPos = parseTimeZone(s, pos, tz);
        if (tzPos != pos + 2)
            throw new DateTimeException("invalid gMonth syntax", s);
        month = parseInt(s, pos, tzPos);

        return new GMonth(month, tz[0], tz[1], tz[2]);
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("--");
        appendPadded(month, 2, buffer);
        appendTimeZone(tzSign, tzHour, tzMinute, buffer);
        return buffer.toString();
    }
}
