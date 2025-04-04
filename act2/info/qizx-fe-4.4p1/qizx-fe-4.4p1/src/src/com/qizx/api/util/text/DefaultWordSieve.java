/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */

package com.qizx.api.util.text;

import com.qizx.api.Indexing.WordSieve;
import com.qizx.api.util.fulltext.DefaultTextTokenizer;


/**
 * A basic text tokenizer suitable for most European languages.
 * <p>All methods can be redefined.
 * <ul>
 * <li>Words start with a letter, a digit, or an underscore. They can contain
 * additionally an hyphen '-', and a dot if it is not in last position.
 * <li>Unless specified by constructor argument, characters are converted to
 * lowercase.
 * <li>Unless specified by constructor argument, ISO-8859-1 characters with
 * accents (diacritics) are converted to accent-less equivalent (e.g 'é' is
 * converted to 'e'). More complex mappings such as German ß to "ss" are not
 * supported.
 * <li>No stemming is performed.
 * </ul>
 * @deprecated use {@link DefaultTextTokenizer}
 */
public class DefaultWordSieve extends DefaultTextTokenizer
    implements WordSieve, java.io.Serializable
{    
    /**
     * Default constructor.
     */
    public DefaultWordSieve()
    {
    }

    public WordSieve copy()
    {
        DefaultWordSieve sieve = new DefaultWordSieve();
        return sieve;
    }

}
