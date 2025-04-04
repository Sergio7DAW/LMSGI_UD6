/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.util;

import com.qizx.util.basic.Unicode;
import com.qizx.util.basic.Util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A String Pattern based on regular expressions. 
 * Trick: define this as CharSequence to use the Matcher several times
 */
public class RegexMatchPattern extends StringPattern
    implements CharSequence // Caution: for reuse of matcher
{
    protected Matcher matcher;
    protected char[] toMatch;
    protected boolean diacriticsSensitive = true;

    public RegexMatchPattern(Pattern pattern)
    {
        super(null, 0);
        matcher = pattern.matcher(this);
        // fixed prefix:
        String pat = pattern.pattern();
        int reject = 0, size = pat.length();
        for (; reject < size; reject++) {
            char c = pat.charAt(reject);
            if (".\\[{*+?".indexOf(c) >= 0) // OPTIM
                break;
        }
        fixedPrefix = Util.subArray(pat.toCharArray(), 0, reject);
        fixedPrefixStart = reject;
        caseSensitive = (pattern.flags() & Pattern.CASE_INSENSITIVE) == 0;
    }

    public boolean matches(char[] string)
    {
        toMatch = string;
        matcher.reset(); // mandatory
        return matcher.matches();
    }

    public int match(char[] string)
    {
        // compare fixed prefix: used to determine if beyond 
        if(fixedPrefix.length > string.length)
            return NOMATCH;
        int cmp = Util.prefixCompare(fixedPrefix, string, fixedPrefix.length,
                                     caseSensitive, diacriticsSensitive);
        if(cmp != 0)
            return (cmp < 0) ? BEYOND : NOMATCH;
        // normal matching:
        return matches(string) ? MATCH : NOMATCH; // OPTIM
    }

    public char charAt(int index)
    {
        return diacriticsSensitive? toMatch[index]
                                  : Unicode.collapseDiacritic(toMatch[index]);
    }

    public int length()
    {
        return toMatch == null ? 0 : toMatch.length;
    }

    public CharSequence subSequence(int start, int end)
    {
        return null; // not used;
    }

    public String toString()
    {
        return "Regexp " + matcher.pattern().pattern();
    }

    public boolean isDiacriticsSensitive()
    {
        return diacriticsSensitive;
    }

    public void setDiacriticsSensitive(boolean diacriticsSensitive)
    {
        this.diacriticsSensitive = diacriticsSensitive;
    }
}
