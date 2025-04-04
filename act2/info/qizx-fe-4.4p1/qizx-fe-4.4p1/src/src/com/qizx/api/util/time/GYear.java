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
 * Representation of a GYear value.
 */
public class GYear extends DateTimeBase
{
    public GYear(int year, int tzSign, int tzHour, int tzMinute)
        throws DateTimeException
    {
        super(year, 1, 1, 0, 0, 0, tzSign, tzHour, tzMinute);
    }

    public GYear(DateTimeBase dt) throws DateTimeException
    {
        this(dt.year, dt.tzSign, dt.tzHour, dt.tzMinute);
    }

    public DateTimeBase parse(String that)
        throws DateTimeException
    {
        return parseGYear(that);
    }

    public static GYear parseGYear(String s)
        throws DateTimeException
    {
        int year;
        int[] tz = new int[3];

        int charCount = s.length();
        if (charCount == 0)
            throw new DateTimeException("invalid gYear syntax", s);

        int pos = 0;
        boolean negativeYear = false;
        if (s.charAt(pos) == '-') {
            negativeYear = true;
            ++pos;
        }
        int tzPos = parseTimeZone(s, pos, tz);
        if (tzPos < pos + 4)
            throw new DateTimeException("invalid gYear syntax", s);
        year = parseInt(s, pos, tzPos);
        if (year < 10000 && tzPos - pos > 4)
            throw new DateTimeException("invalid year: leading zeroes", s);
        if (negativeYear)
            year = -year;

        return new GYear(year, tz[0], tz[1], tz[2]);
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        if (year < 0) {
            buffer.append('-');
            appendPadded(-year, 4, buffer);
        }
        else {
            appendPadded(year, 4, buffer);
        }
        appendTimeZone(tzSign, tzHour, tzMinute, buffer);

        return buffer.toString();
    }
}
