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
 * Simple String parsing utility.
 * 
 */
public class StringParser
{
    protected String s;
    protected int ptr;
    protected int end;
    
    protected int latestInt; // parsed by parseInt;
    
    protected void init(String input)
    {
        s = input;
        end = s.length();
        ptr = 0;
    }

    protected boolean parseDec(int minDigitCount)
    {
        char c = currentChar();
        int cnt = 0;
        latestInt = 0;
        for(; c >= '0' && c <= '9'; ++cnt) {
            latestInt = 10 * latestInt + c - '0';
            c = nextChar();
        }
        return (cnt >= minDigitCount);
    }

    protected boolean pick(char c)
    {
        if(currentChar() != c) 
            return false;
        ++ ptr;
        return true;
    }

    protected char nextChar()
    {
        return (ptr >= end - 1)? 0 : s.charAt(++ ptr);
    }
    
    protected char currentChar()
    {
        return (ptr >= end)? 0 : s.charAt(ptr);
    }
}
