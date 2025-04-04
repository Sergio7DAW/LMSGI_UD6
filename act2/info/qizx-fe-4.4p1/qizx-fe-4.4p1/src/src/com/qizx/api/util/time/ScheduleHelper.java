/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.api.util.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Helper for recurring schedule such as "12:00" or "Friday 00:30".
 */
public class ScheduleHelper
{
    SimpleDateFormat timeFmt;
    SimpleDateFormat weekTimeFmt;

    Calendar calendar = GregorianCalendar.getInstance();

    public ScheduleHelper()
    {
        timeFmt = new SimpleDateFormat("HH:mm");
        timeFmt.setLenient(true);
        weekTimeFmt = new SimpleDateFormat("E HH:mm");
        weekTimeFmt.setLenient(true);
    }

    /**
     * Checks whether the date matches the schedule specification
     * @param date date to check
     * @param interval in hours between actions
     * @param moment a recurring time in the form <code>hh:mm</code> or
     *        <code>dayOfWeek hh:mm</code>. Note that the value is used modulo the
     *        interval. For example: "Sunday 00:30" with interval 24 (hours) would
     *        be similar to "00:30", or "02:30" with interval=2 (hours) would be
     *        equivalent to "00:30".
     * @return true if the date (rounded to the minute) matches the schedule
     *         specification
     * @throws ParseException if timeOfDay has an invalid value
     */
    public boolean checkTime(Date date, int interval, String moment)
        throws ParseException
    {
        int minint = interval * 60;
        int td1 = timeDelta(date);
        int td2 = convertAt(moment);
        return td1 % minint == td2 % minint;
    }

    /**
     * Returns the next occurrence of the recurring schedule, starting from a
     * date. If the date matches the schedule, it is returned (truncated to the
     * minute)
     * @param date date to check
     * @param interval in hours between actions
     * @param moment a recurring time in the form <code>hh:mm</code> or
     *        <code>dayOfWeek hh:mm</code>. Note that the value is used modulo the
     *        interval. For example: "Sunday 00:30" with interval=24 (hours) would
     *        be equivalent to "00:30", or "02:30" with interval=2 (hours) would be
     *        equivalent to "00:30".
     * @return next occurrence (rounded to the minute) matching the schedule
     *         specification
     * @throws ParseException if timeOfDay has an invalid value
     */
    public Date nextOccurrence(Date date, int interval, String moment)
        throws ParseException
    {
        int minint = interval * 60;
        int td1 = timeDelta(date) % minint;
        int td2 = convertAt(moment) % minint;

        long dt = date.getTime();
        dt -= dt % 60000;

        int delta = td2 - td1;
        return new Date(dt + 60000L * (delta >= 0 ? delta : delta + minint));
    }

    public int convertAt(String time)
        throws ParseException
    {
        Date date;
        try {
            date = timeFmt.parse(time);
        }
        catch (Exception e) {
            date = weekTimeFmt.parse(time);
        }
        
        return timeDelta(date);
    }

    /**
     * Given a date, compute the number of minutes from the reference date to this
     * date. The reference date is the preceding Sunday at 00:00
     */
    int timeDelta(Date date)
    {
        calendar.setTime(date);
        int d = calendar.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY; // >= 0
        int h = calendar.get(Calendar.HOUR_OF_DAY);
        int m = calendar.get(Calendar.MINUTE);
        return (d * 24 + h) * 60 + m;
    }

    public static void main(String[] args)
    {
        try {
            ScheduleHelper ch = new ScheduleHelper();
            Date date = new Date();
            int interval = 24 * 2;
            String time = " 18:37";

            System.err.println("match " + ch.checkTime(date, interval, time));

            System.err.println("next occ "
                               + ch.nextOccurrence(date, interval, time));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
