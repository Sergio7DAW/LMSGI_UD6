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
 * Representation of a GDay value.
 */
public class GDay extends DateTimeBase
{
    public GDay(int day, int tzSign, int tzHour, int tzMinute)
        throws DateTimeException
    {
        super(EPOCH, 1, day, 0, 0, 0, tzSign, tzHour, tzMinute);
    }

    public GDay(DateTimeBase dt) throws DateTimeException
    {
        this(dt.day, dt.tzSign, dt.tzHour, dt.tzMinute);
    }

    public DateTimeBase parse(String that)
        throws DateTimeException
    {
        return parseGDay(that);
    }

    public static GDay parseGDay(String s)
        throws DateTimeException
    {
        int day;
        int[] tz = new int[3];

        if (!s.startsWith("---"))
            throw new DateTimeException("invalid gDay syntax", s);
        int pos = 3;

        int tzPos = parseTimeZone(s, pos, tz);
        if (tzPos != pos + 2)
            throw new DateTimeException("invalid gDay syntax", s);
        day = parseInt(s, pos, tzPos);

        GDay parsed;
        try {
            parsed = new GDay(day, tz[0], tz[1], tz[2]);
        }
        catch (IllegalArgumentException e) {
            throw new DateTimeException("invalid gDay value", s);
        }
        return parsed;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("---");
        appendPadded(day, 2, buffer);
        appendTimeZone(tzSign, tzHour, tzMinute, buffer);
        return buffer.toString();
    }
}
