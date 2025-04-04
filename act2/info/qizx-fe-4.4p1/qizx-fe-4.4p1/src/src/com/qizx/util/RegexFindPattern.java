/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.util;

import java.util.regex.Pattern;

/**
 *	A String Pattern based on regular expressions.
 * Trick: define this as CharSequence so to reuse the Matcher
 */
public class RegexFindPattern extends RegexMatchPattern
{    
    public RegexFindPattern(Pattern pattern) {
        super(pattern);
        // cannot use prefix here:
        fixedPrefix = new char[0];
    }
    
    public boolean matches(char[] string) {
        toMatch = string;
        matcher.reset();        // mandatory
        return matcher.find();
    }
}
