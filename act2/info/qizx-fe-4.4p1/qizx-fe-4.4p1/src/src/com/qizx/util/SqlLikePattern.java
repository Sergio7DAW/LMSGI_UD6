/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.util;

import com.qizx.util.basic.Util;

/**
 * SQL-LIKE pattern matching. Similar to Glob pattern but slightly different
 * syntax.
 */
public class SqlLikePattern extends LikePattern
{
    public SqlLikePattern(String pattern)
    {
        this(pattern.toCharArray(), pattern.length());
    }

    public SqlLikePattern(char[] pattern, int length)
    {
        super(null, 0);
        StringBuffer buf = new StringBuffer(length);
        int fix = -1;
        for (int p = 0; p < length; ++p)
            switch (pattern[p]) {
            case '%':
                if(fix < 0) {
                    fix = p;
                    fixedPrefixStart = buf.length();
                }
                buf.append('*');
                break;
            case '_':
                if(fix < 0) {
                    fix = p;
                    fixedPrefixStart = buf.length();
                }
                buf.append('?');
                break;
            case '*':
                buf.append("\\*");
                break;
            case '?':
                buf.append("\\?");
                break;
            default:
                buf.append(pattern[p]);
                break;
            }
        
        this.pattern = new char[buf.length()];
        buf.getChars(0, this.pattern.length, this.pattern, 0);
        
        fixedPrefix = Util.subArray(pattern, 0, fix < 0? pattern.length : fix);
    }
}
