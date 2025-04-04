/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.api.util.text;

import com.qizx.api.CompilationException;
import com.qizx.xquery.impl.Lexer;


/**
 * Iterator on tokens of a XQuery script. 
 * Used for implementing syntax coloring in Qizx Studio.
 * <p>Returns the lexical group and the position and size of each token.
 */
public class LexicalTokenizer extends Lexer
{
//    private int curToken;
    private int prevTokenEnd = 0;

    // lexical categories:
    /** Lexical category of element constructors. */
    public final static int LC_TAG = Lexer.LC_TAG;
    /** Lexical category of whitespace. */
    public final static int LC_SPACE = Lexer.LC_SPACE;
    /** Lexical category of numeric literals. */
    public final static int LC_NUMBER = Lexer.LC_NUMBER;
    /** Lexical category of string literals. */
    public final static int LC_STRING = Lexer.LC_STRING;
    /** Lexical category of others tokens. */
    public final static int LC_MISC = Lexer.LC_MISC;
    /** Lexical category of identifiers. */
    public final static int LC_NAME = Lexer.LC_NAME;
    /** Lexical category of reserved keywords. */
    public final static int LC_KEYWORD = Lexer.LC_KEYWORD;
    /** Lexical category of XQuery comments. */
    public final static int LC_COMMENT = Lexer.LC_COMMENT;
    /** Lexical category of XQuery pragmas. */
    public final static int LC_PRAGMA = Lexer.LC_PRAGMA;
    /** Lexical category of XQuery function calls. */
    public final static int LC_FUNCTION = Lexer.LC_FUNCTION;

    /**
     * Constructs a tokenizer of a XQuery script.
     * @param script source XQuery script to tokenize
     */
    public LexicalTokenizer(String script)
    {
        showComments = true;
        startLexer(script);
        //debug = true;
    }

    /**
     * Moves to next token and returns its category.
     * @return token code (LC_xxx)
     * @throws CompilationException if parsing error
     */
    public int nextToken()
        throws CompilationException
    {
        prevTokenEnd = tokenEnd;
        int curToken = getToken();
          
        if (curToken == T_END)
            return 0;
        return getTokenCategory(curToken);
    }

    /**
     * Returns the text of the current token: null if the end is reached.
     */
    public String getTokenValue()
    {
        return source.substring(tokenStart, tokenEnd);
    }

    /**
     * Returns the start position of the current token.
     */
    public int getTokenStart()
    {
        return tokenStart;
    }

    /**
     * Returns the length of the current token.
     */
    public int getTokenLength()
    {
        return tokenEnd - tokenStart;
    }
    

    /**
     * Size of leading space before the current token.
     */
    public int getSpaceLength()
    {
        return tokenStart - prevTokenEnd;
    }

    /**
     * Gets the space that is before the current token. Returns null if no
     * space.
     */
    public String getSpace()
    {
        if (tokenStart <= prevTokenEnd)
            return null;
        return source.substring(prevTokenEnd, tokenStart);
    }

    void parsedPragma(int location, String prefix, String localName,
                      String body)
    {
    }

    void parsedExtension(int location, String prefix, String localName,
                         String body)
    {
    }
}
