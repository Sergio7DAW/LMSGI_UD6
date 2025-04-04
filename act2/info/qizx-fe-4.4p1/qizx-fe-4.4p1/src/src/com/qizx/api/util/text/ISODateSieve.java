/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.api.util.text;

import com.qizx.api.DataModelException;
import com.qizx.api.Indexing.DateSieve;
import com.qizx.util.DateTimeParser;

/**
 * Default DateSieve implementation: recognizes ISO date and dateTime.
 * <p>
 * Recognized patterns: YYYY-MM-DD[TZ] and YYYY-MM-DDThh:mm:ss.fff[TZ], where
 * TZ is an optional time-zone specification.
 */
public class ISODateSieve extends DateTimeParser
    implements DateSieve
{
    private String[] parameters;

    /**
     * Returns a number of milliseconds since 1970-01-01 00:00:00 UTC.
     * @param value a possible date to convert.
     */
    public synchronized double convert(String value)
    {
        init(value);
        int len = value.length();

        // Quick pre-tests:
        if (len < 10)
            return fail();
        char c = value.charAt(0);
        if (c < '0' || c > '9' || value.indexOf('-') < 0)
            return fail();

        reset();
        if (!parseDate())
            return fail();

        // optional time:
        if (pick('T')) {
            if (!parseTime())
                return fail();
        }

        // optional timezone
        parseTimezone();
        return getMillisecondsFromEpoch();
    }

    private double fail()
    {
        return Double.NaN;
    }

    // @see com.qizx.api.Indexing.Sieve#getParameters()
    public String[] getParameters()
    {
        return parameters;
    }

    /** 
     * @see com.qizx.api.Indexing.Sieve#setParameters(java.lang.String[]) */
    public void setParameters(String[] parameters)
        throws DataModelException
    {
        this.parameters = parameters;
        if (parameters.length > 0)
            throw new DataModelException("invalid sieve parameter '"
                                         + parameters[0] + "'");
    }

    public String toString()
    {
        return SieveBase.toString(getClass(), parameters);
    }
}
