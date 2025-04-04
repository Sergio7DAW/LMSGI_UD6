/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.util;


/**
 * Matching of a string with a pattern.
 * <p>
 * This superclass implements the identity comparison.
 */
public class ExactPattern extends StringPattern
{
    public ExactPattern(char[] pattern, int length)
    {
        setPattern(pattern, length);
    }
    
    public ExactPattern(String pattern)
    {
        this(pattern.toCharArray(), pattern.length());
    }


    public boolean singleMatch()
    {
        return true;
    }

    public String toString()
    {
        return "ExactPattern(" + new String(pattern) + ")";
    }

    public int match(char[] string)
    {
        int plen = pattern.length;
        int slen = string.length;
        for (int i = 0, len = Math.min(plen, slen); i < len; i++) {
            int diff = string[i] - pattern[i];
            if(diff > 0)
                return BEYOND;
            if(diff != 0)
                return NOMATCH;
        }
        return (plen < slen) ? BEYOND : (plen > slen) ? NOMATCH : MATCH;
    }
    
    public boolean matches(char[] string)
    {
        if (string.length != pattern.length)
            return false;
        for (int i = string.length; --i >= 0;)
            if (string[i] != pattern[i])
                return false;
        return true;
    }
}
