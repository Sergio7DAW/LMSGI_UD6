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
 *	Representation of Date values.
 *	
 *	@author Xavier Franc. Most of this code borrowed from XMLmind XML Editor (courtesy
 *	of H. Shafie), and adapted.
 */
public class Date extends DateTimeBase
{
    public Date(int year, int month, int day, int tzSign, int tzHour,
                int tzMinute) throws DateTimeException
    {
        super(year, month, day, 0, 0, 0, tzSign, tzHour, tzMinute);
    }

    public Date(DateTimeBase dt) throws DateTimeException
    {
        super(dt.year, dt.month, dt.day, 0, 0, 0, dt.tzSign, dt.tzHour,
              dt.tzMinute);
    }
    
    /**
     * Create a Date from an UTC time stamp (assumed to be a multiple of 1 day).
     * @param timeMillis time considered UTC
     */
    public Date(long timeMillis)
    {
        setSecondsFromEpoch(timeMillis / 1000.0, LOCAL);    // UTC
    }

    public DateTimeBase parse(String that)
        throws DateTimeException
    {
        return parseDate(that);
    }

    public DateTimeBase adjustToTimezone(int tzInMinutes, boolean set)
    {
        DateTimeBase result = super.adjustToTimezone(tzInMinutes, set);
        normalize(result);
        return result;
    }

    public static void normalize(DateTimeBase dt)
    {
        dt.hour = 0;
        dt.minute = 0;
        dt.second = 0;
    }

    public static Date parseDate(String s)
        throws DateTimeException
    {
        int year;
        int month;
        int day;
        int[] tz = new int[3];

        int charCount = s.length();
        if (charCount == 0)
            throw new DateTimeException("date syntax", s);

        int pos = 0;
        boolean negativeYear = false;
        if (s.charAt(pos) == '-') {
            negativeYear = true;
            ++pos;
        }
        int nextPos = s.indexOf('-', pos);
        if (nextPos < pos + 4)
            throw new DateTimeException("invalid syntax", s);
        year = parseInt(s, pos, nextPos);
        if (year < 10000 && nextPos - pos > 4)
            throw new DateTimeException("invalid year: leading zeroes", s);

        if (negativeYear)
            year = -year;
        pos = nextPos + 1;

        if (pos + 2 >= charCount || s.charAt(pos + 2) != '-')
            throw new DateTimeException("invalid syntax", s);
        month = parseInt(s, pos, pos + 2);
        pos += 3;

        nextPos = parseTimeZone(s, pos, tz);
        if (nextPos != pos + 2)
            throw new DateTimeException("invalid syntax", s);

        day = parseInt(s, pos, nextPos);

        Date result = new Date(year, month, day, tz[0], tz[1], tz[2]);
        return result;
    }

    public DateTimeBase copy()
    {
        try {
            return new Date(year, month, day, tzSign, tzHour, tzMinute );
        }
        catch (DateTimeException cannotHappen) {
            return null;
        }
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        if (year < 0) {
            buffer.append('-');
            appendPadded(-year, 4, buffer);
        }
        else
            appendPadded(year, 4, buffer);
        buffer.append('-');
        appendPadded(month, 2, buffer);
        buffer.append('-');
        appendPadded(day, 2, buffer);
        appendTimeZone(tzSign, tzHour, tzMinute, buffer);

        return buffer.toString();
    }
}
