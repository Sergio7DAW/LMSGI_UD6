/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.api.util;

import com.qizx.api.LibraryMember;
import com.qizx.api.LibraryMemberFilter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * An implementation of LibraryMemberFilter which matches the name of 
 * Library members using Unix shell-like regular expressions.
 * 
 */
public final class GlobFilter implements LibraryMemberFilter
{
    private Matcher matcher;

    /**
     * Builds a GlobFilter with a regular expression.
     * @param regex a Unix-style regular expression, for example 'doc*' matching
     * all strings that start with "doc".
     * @throws PatternSyntaxException if the regex is invalid
     */
    public GlobFilter(String regex) throws PatternSyntaxException
    {
        StringBuffer buffer = new StringBuffer();

        int length = regex.length();
        for (int i = 0; i < length; ++i) {
            char c = regex.charAt(i);

            switch (c) {
            case '*':
                buffer.append(".*");
                break;

            case '?':
                buffer.append('.');
                break;

            case '[':
            {
                buffer.append('[');

                int first = i + 1;
                if (first < length) {
                    int j;

                    loop: for (j = first; j < length; ++j) {
                        c = regex.charAt(j);

                        switch (c) {
                        case '!':
                            // '[!ab]' means '[^ab]'.
                            buffer.append((j == first) ? '^' : '!');
                            break;
                        case ']':
                            buffer.append(']');
                            if (j > first)
                                // '[]ab]'
                                break loop;
                            break;
                        default:
                            buffer.append(c);
                        }
                    }

                    i = j;
                }
            }
                break;

            default:
                if (!Character.isLetterOrDigit(c))
                    // Escape special chars such as '(' or '|'.
                    buffer.append('\\');
                buffer.append(c);
            }
        }

        Pattern pattern = Pattern.compile(buffer.toString());
        matcher = pattern.matcher("dummy");
    }

    // @see com.qizx.api.LibraryMemberFilter#accept(com.qizx.api.LibraryMember)
    public boolean accept(LibraryMember member)
    {
        matcher.reset(member.getName());
        return matcher.matches();
    }
}
