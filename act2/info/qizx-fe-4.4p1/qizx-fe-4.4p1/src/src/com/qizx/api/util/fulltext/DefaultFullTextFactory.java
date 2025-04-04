/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.api.util.fulltext;

import com.qizx.api.fulltext.FullTextFactory;
import com.qizx.api.fulltext.Scorer;
import com.qizx.api.fulltext.Stemmer;
import com.qizx.api.fulltext.TextTokenizer;
import com.qizx.api.fulltext.Thesaurus;

/**
 * Fulltext service provider plugged by default.
 * <p>Provides a generic TextTokenizer and a standard Scorer. Might be extended
 * in future versions to provide stemmer and thesaurus.
 */
public class DefaultFullTextFactory
    implements FullTextFactory
{
    public TextTokenizer getTokenizer(String languageCode)
    {
        return new DefaultTextTokenizer();
    }

    public Stemmer getStemmer(String languageCode)
    {
        return null; // not available by default
    }

    public Thesaurus getThesaurus(String uri, String languageCode,
                                  String relationship,
                                  int levelMin, int levelMax)
    {
        return null; // not available by default
    }
    

    public Scorer createScorer()
    {
        return new DefaultScorer();
    }
}
