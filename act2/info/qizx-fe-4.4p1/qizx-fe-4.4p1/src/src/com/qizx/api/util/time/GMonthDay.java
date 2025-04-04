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
 * Representation of a GMonthDay value.
 */
public class GMonthDay extends DateTimeBase
{
    public GMonthDay(int month, int day, int tzSign, int tzHour, int tzMinute)
        throws DateTimeException
    {
        super(0, month, day, 0, 0, 0, tzSign, tzHour, tzMinute);
    }

    public GMonthDay(DateTimeBase dt) throws DateTimeException
    {
        this(dt.month, dt.day, dt.tzSign, dt.tzHour, dt.tzMinute);
    }

    public DateTimeBase parse(String that)
        throws DateTimeException
    {
        return parseGMonthDay(that);
    }

    public static GMonthDay parseGMonthDay(String s)
        throws DateTimeException
    {
        int month;
        int day;
        int[] tz = new int[3];

        if (!s.startsWith("--"))
            throw new DateTimeException("invalid gMonthDay syntax", s);

        int charCount = s.length();
        int pos = 2;
        if (pos + 2 >= charCount || s.charAt(pos + 2) != '-')
            throw new DateTimeException("invalid gMonthDay syntax", s);
        month = parseInt(s, pos, pos + 2);
        pos += 3;

        int tzPos = parseTimeZone(s, pos, tz);
        if (tzPos != pos + 2)
            throw new DateTimeException("invalid gMonthDay syntax", s);
        day = parseInt(s, pos, tzPos);

        return new GMonthDay(month, day, tz[0], tz[1], tz[2]);
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("--");
        appendPadded(month, 2, buffer);
        buffer.append('-');
        appendPadded(day, 2, buffer);
        appendTimeZone(tzSign, tzHour, tzMinute, buffer);

        return buffer.toString();
    }
}
