/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.util.json;

import com.qizx.util.basic.XMLUtil;

import java.io.IOException;
import java.io.Reader;


/**
 * Push-style JSON parser
 */
public class JSONParser
{
    private static final char MAP_START = '{';
    private static final char MAP_END = '}';
    private static final char ARRAY_START = '[';
    private static final char ARRAY_END = ']';
    private static final char KEY_END = ':';
    private static final int LOOKAHEAD = 20;

    private JSONHandler handler;
    
    private Reader inputReader;
    private char curChar;
    private char[] inputBuffer;
    private int inputSize;  // number of chars currently in buffer
    private int inputPtr;
    private int inputLimit;
    
    private char[] tokenBuffer;
    private int tokenSize;
    
    public JSONParser(String input)  throws JSONException
    {
        inputBuffer = input.toCharArray();
        inputSize = inputLimit = inputBuffer.length;
        init();
    }
    
    public JSONParser(char[] input, int length) throws JSONException
    {
        inputBuffer = input;
        inputSize = inputLimit = length;
        init();
    }
    
    public JSONParser(Reader input) throws JSONException
    {
        inputReader = input;
        inputBuffer = new char[4096];
        //inputSize = inputLimit = 0; // need to read a block
        init();
    }
    
    public JSONHandler getHandler()
    {
        return handler;
    }

    public void setHandler(JSONHandler handler)
    {
        this.handler = handler;
    }

    /**
     * Parses a single JSON value (map, array or simple value).
     * @throws JSONException
     */
    public void parse()
        throws JSONException
    {
        boolean b;
        
        if(see(MAP_START))
        {
            if(handler != null)
                handler.mapStart();
            for(; curChar != 0; )
            {
                if(!parseMapPair())
                    break;;
            }
            if(handler != null)
                handler.mapEnd();
        }
        else if(see(ARRAY_START))
        {
            parseArray();
        }
        else if((b = seeKeyWord("true")) || seeKeyWord("false")) {
            if(handler != null)
                handler.booleanValue(b);
        }
        else if(seeKeyWord("null")) {
            if(handler != null)
                handler.nullValue();
        }
        else if(seeNumber()) {
            if(handler != null)
                handler.doubleValue(convertNumber(tokenBuffer, tokenSize));
        }
        else if(seeString()) {
            if(handler != null)
                handler.stringValue(tokenBuffer, tokenSize);
        }
        else
            syntaxError("invalid data");
    }

    private boolean parseMapPair()
        throws JSONException
    {
        if(!seeString() && !seeWord()) {
            if(see(MAP_END))
                return false;
            syntaxError("expecting map key");
        }
        if(handler != null)
            handler.pairStart(tokenBuffer, tokenSize);
        if(!see(KEY_END))
            syntaxError("expecting ':' after pair key");
        parse();
        if(handler != null)
            handler.pairEnd();
        if(!see(',')) {
            if(!see(MAP_END))
                syntaxError("unclosed map");
            return false;
        }
        // accept extra comma
        return true;
    }

    private void parseArray()
        throws JSONException
    {
        if(handler != null)
            handler.arrayStart();
        for(; !see(ARRAY_END); ) {
            if(curChar == 0)
                syntaxError("unclosed array");
            parse();
            if(!see(',')) {
                if(!see(ARRAY_END))
                    syntaxError("unclosed array");
                break;
            }
            // accept extra comma
        }
        if(handler != null)
            handler.arrayEnd();
    }

    private boolean seeNumber() throws JSONException
    {
        if((curChar < '0' || curChar > '9') && curChar != '-')
            return false;
        tokenSize = 0;
        if(see('-')) {
            save('-');
        }
        pickDigits(true);
        if(see('.')) {
            save('.');
            pickDigits(false);
        }
        if(see('e') || see('E')) {
            save('e');
            if(see('-')) {
                save('-');
            }
            pickDigits(true);
        }
        return true;
    }

    private void pickDigits(boolean requested) throws JSONException
    {
        int isize = tokenSize;
        while(curChar >= '0' && curChar <= '9') {
            save(curChar);
            nextChar();
        }
        if(requested && tokenSize == isize)
            syntaxError("digits requested");
    }

    private boolean seeString()
        throws JSONException
    {
        if(!see('"'))
            return false;
        tokenSize = 0;
        int savePos = inputPtr;
        for(; curChar != 0 && curChar != '"'; ) {
            if(curChar == '\\') {
                nextChar();
                switch(curChar) {
                case 'b':
                    save('\b');
                    break;
                case 'f':
                    save('\f');
                    break;
                case 't':
                    save('\t');
                    break;
                case 'r':
                    save('\r');
                    break;
                case 'n':
                    save('\n');
                    break;
                case 'u': // unicode
                    nextChar();
                    int code = hexChar(curChar);
                    nextChar();
                    code = (code << 4) + hexChar(curChar);
                    nextChar();
                    code = (code << 4) + hexChar(curChar);
                    nextChar();
                    code = (code << 4) + hexChar(curChar);
                    save((char) code);
                    break;
                default:
                    save(curChar);
                    break;
                }
            }
            else {
                save(curChar);
            }
            nextChar();
        }
        if(curChar != '"')
            syntaxError("unterminated string starting at position " + savePos);
        nextChar();
        return true;    
    }

    private int hexChar(char ch) throws JSONException
    {
        if(ch >= '0' && ch <= '9')
            return ch - '0';
        if(ch >= 'a' && ch <= 'f')
            return ch - 'a' + 10;
        if(ch >= 'A' && ch <= 'F')
            return ch - 'A' + 10;
        syntaxError("invalid hexa character");
        return -1;
    }
    
    private boolean seeWord() throws JSONException
    {
        skipSpace();
        tokenSize = 0;
        if (!XMLUtil.isNameChar(curChar))
            return false;
        for(;;) {
            save(curChar);
            nextChar();
            if(!XMLUtil.isNameChar(curChar))
                break;
        }
        return true;
    }
    
    private boolean seeKeyWord(String word) throws JSONException
    {
        // - System.out.println("?pick "+s+" at "+inputPtr+" "+(int)curChar);
        int after = inputPtr + word.length() - 1;
        if (curChar != word.charAt(0) || after > inputSize)
            return false;
        for(int i = word.length(); --i >= 1; )
            if(inputBuffer[inputPtr + i - 1] != word.charAt(i)) {
                return false;
            }
        if(after < inputSize && Character.isJavaIdentifierPart(inputBuffer[after]))
            return false;
        inputPtr += word.length() - 1;
        nextChar();
        
        return true;
    }

    private void init() throws JSONException
    {
        tokenBuffer = new char[128];
        tokenSize = 0;
        nextChar();
    }
    
    private boolean see(char c) throws JSONException
    {
        skipSpace();
        if(curChar != c)
            return false;
        nextChar();
        return true;
    }
    
    private void skipSpace() throws JSONException
    {
        for(; curChar == ' ' || curChar == '\t' || curChar == '\r' || curChar == '\n'; )
            nextChar();
    }
    
    private int nextChar() throws JSONException
    {
        if(inputReader != null)
            shiftBuffer();
        if (inputPtr < inputSize)
            curChar = inputBuffer[inputPtr++];
        else {
            curChar = 0;
            inputPtr = inputSize + 1; // as if there were a EOF char
        }
        return curChar;
    }

    // when reading from a file, ensure that we have enough characters ahead
    private void shiftBuffer() throws JSONException
    {
        if(inputReader == null || inputPtr < inputLimit)
            return;
        int leftOver = inputSize - inputPtr;
        if(leftOver > 0) {
            System.arraycopy(inputBuffer, inputPtr, inputBuffer, 0, leftOver);
        }
        inputSize = inputLimit = leftOver;
        inputPtr = 0;
        // now attempt to fill the buffer:
        try {
            for(;;) {
                int wanted = inputBuffer.length - inputSize;
                int actual = inputReader.read(inputBuffer, inputSize, wanted);
                if(actual < wanted) {
                    if(actual < 0) { // eof
                        inputReader = null; // not in charge of closing it
                        return;
                    }
                    // maybe not eof but not enough data available
                    inputSize += actual;
                    inputLimit = inputSize;
                }
                else { // inputSize is buffer size
                    inputLimit = inputSize - LOOKAHEAD;
                    break;
                }
            }
        }
        catch (IOException e) {
            throw new JSONException(e.getMessage(), e);
        }
    }
    
    private int getPosition()
    {
        return inputPtr; // TODO
    }

    private void save(char c)
    {
        if (tokenSize >= tokenBuffer.length) {
            char[] old = tokenBuffer;
            tokenBuffer = new char[old.length * 2];
            System.arraycopy(old, 0, tokenBuffer, 0, old.length);
        }
        tokenBuffer[tokenSize++] = c;
    }

    private double convertNumber(char[] buffer, int length)
    {
        return Double.parseDouble(new String(buffer, 0, length));
    }

    private void syntaxError(String msg) throws JSONException
    {
        throw new JSONException("at position " +  getPosition() + ": " + msg);
    }
}
