/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.api.fulltext;

/**
 * Produces a stem from a word.
 * <p>
 * An implementation is likely to be language-specific.
 */
public interface Stemmer
{
    /**
     * Computes a stem from a word token.
     * @param token word to be stemmed
     * @return a character array representing the stem, or the input token
     *         itself if it cannot be stemmed.
     */
    char[] stem(char[] token);
}
