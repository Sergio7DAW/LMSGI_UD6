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
 * Matches the strings that start with a given prefix.
 */
public class PrefixPattern extends StringPattern
{
    public PrefixPattern(char[] pattern, int length)
    {
        super(pattern, length);
    }

    public PrefixPattern(String pattern)
    {
        this(pattern.toCharArray(), pattern.length());
    }

    public int match(char[] string)
    {
        int plen = pattern.length;
        for (int i = 0, len = Math.min(plen, string.length); i < len; i++) {
            int diff = string[i] - pattern[i];
            if(diff > 0)
                return BEYOND;
            if(diff != 0)
                return NOMATCH;
        }
        return (string.length < plen)? NOMATCH : MATCH;
    }

    public boolean matches(char[] string)
    {
        // boost by checking length:
        if(string.length < pattern.length)
            return false;        
        return match(string) == MATCH;
    }
}
