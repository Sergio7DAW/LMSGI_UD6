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
import com.qizx.api.Indexing.NumberSieve;
import com.qizx.util.basic.Util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

/**
 * Converts a number using a Java NumberFormat.
 */
public class FormatNumberSieve extends SieveBase
    implements NumberSieve
{
    /**
     * Name of the parameter used for specifying the format of recognized number
     * values. 
     * The value of the parameter must be accepted by 
     * {@link java.text.DecimalFormat}.
     */
    public static final String FORMAT = "format";
    /**
     * Name of the parameter used for specifying the locale of parsed
     * dates. The value of this parameter can be of different forms (the dash 
     * separator can also be replaced by an underscore):<ul>
     * <li><em>language</em> (ISO 639, accepted by {@link Locale})
     * <li><em>language-country</em>, for example en-US
     * <li><em>language-country-variant</em>, for example en-US-POSIX. The 
     * variant value is generally "WIN", "MAC" or "POSIX".
     * </ul>
     */
    public static final String LOCALE = "locale";

    private NumberFormat format;
    private transient ParsePosition position = new ParsePosition(0);

    /**
     * Constructs a sieve that recognizes a number in US format.
     */
    public FormatNumberSieve()
    {
        format = new DecimalFormat("############.##",
                                   new DecimalFormatSymbols(Locale.US));
        format.setMinimumFractionDigits(0);
        format.setParseIntegerOnly(false);
    }

    /**
     * Constructs a sieve that recognizes a particular Decimal format.
     * @param decimalFormat a format accepted by {@link DecimalFormat}
     */
    public FormatNumberSieve(String decimalFormat)
    {
        format = new DecimalFormat(decimalFormat);
        format.setMinimumFractionDigits(0);
        format.setParseIntegerOnly(false);
    }

    public void setParameters(String[] parameters)
        throws DataModelException
    {
        this.parameters = parameters;
        Locale locale = null;
        String formatSrc = null;
        for (int i = 0; i < parameters.length; i += 2) {
            String paramName = parameters[i];
            String value = parameters[i + 1];
            if (FORMAT.equalsIgnoreCase(paramName)) {
                formatSrc = parameters[i + 1];
            }
            else if (LOCALE.equals(paramName)) {
                locale = Util.getLocale(value);
            }
            else
                throw new DataModelException("invalid sieve parameter " + paramName);
        }
        if(formatSrc != null)
            setFormat(formatSrc, locale);
        else if(format == null)
            throw new DataModelException("format parameter required");
    }

    /**
     * Sets the format used. Convenience method equivalent to
     * setting parameter "format".
     * @param numberFormat a format accepted by {@link DecimalFormat}
     * @param locale if not null, the format is adapted to this locale
     */
    public void setFormat(String numberFormat, Locale locale)
    {
        format = (locale == null) ? new DecimalFormat(numberFormat)
                                  : new DecimalFormat(numberFormat,
                                              new DecimalFormatSymbols(locale));
        format.setParseIntegerOnly(false);
        addParameter("format", numberFormat); // to retrieve it by getParameters()
    }

    /**
     * Returns the currently used Date Format.
     * @return the currently used Date Format.
     */
    public String getFormat()
    {
        return (format instanceof DecimalFormat)?
                ((DecimalFormat) format).toPattern() : "";
    }
    
    public synchronized double convert(String value)
    {
        position.setIndex(0);
        Number n = format.parse(value, position);
        // System.err.println("value "+value+" -> "+n+" pos "+position);
        int pos = position.getIndex();
        if (n == null)
            return Double.NaN;
        if (pos < value.length()) {
            if (!Character.isWhitespace(value.charAt(pos)))
                return Double.NaN;
        }
        return n.doubleValue();
    }
}
