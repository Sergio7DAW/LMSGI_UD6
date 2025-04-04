/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.util.basic;

import java.util.Properties;

public class OptionUtil
{
    /**
     * Returns a property value
     */
    public static String stringOption(String value, String defaultValue)
    {
        if(value == null || value.length() == 0)
            return defaultValue;
        return value;
    }
    
    /**
     * Returns a property value
     */
    public static String stringOption(Properties options, 
                                      String name, String defaultValue)
    {
        if(options == null)
            return defaultValue;
        return stringOption(options.getProperty(name), defaultValue);
    }

    public static long intOption(String value, int defaultValue)
        throws IllegalArgumentException
    {
        if(value == null)
            return defaultValue;
        value = value.trim();
        if(value.length() == 0)
            return defaultValue;
        return Long.parseLong(value.trim());
    }
    
    public static long intOption(Properties options, 
                                 String name, int defaultValue)
        throws IllegalArgumentException
    {
        if(options == null)
            return defaultValue;
        return intOption(options.getProperty(name), defaultValue);
    }
    
    public static double doubleOption(String value, double defaultValue)
        throws IllegalArgumentException
    {
        if(value == null)
            return defaultValue;
        value = value.trim();
        if(value.length() == 0)
            return defaultValue;
        return Double.parseDouble(value.trim());
    }

    public static double doubleOption(Properties options, 
                                      String name, double defaultValue)
        throws IllegalArgumentException
    {
        if(options == null)
            return defaultValue;
        return doubleOption(options.getProperty(name), defaultValue);
    }

    public static boolean boolOption(String value, boolean defaultValue)
        throws IllegalArgumentException
    {
        if(value == null)
            return defaultValue;
        Boolean.valueOf(value);
        if (value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")
            || value.equals("1"))
            return true;
        if (value.equalsIgnoreCase("no") || value.equalsIgnoreCase("false")
            || value.equals("0"))
            return false;
        throw new IllegalArgumentException("invalid value of boolean option: " + value);
    }
    
    public static boolean boolOption(Properties options, 
                                     String name, boolean defaultValue)
        throws IllegalArgumentException
    {
        if(options == null)
            return defaultValue;
        String value = options.getProperty(name);
        try {
            return boolOption(value, defaultValue);
        }
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("invalid value of boolean option '" + name
                                               + "': " + value);
        }
    }
}
