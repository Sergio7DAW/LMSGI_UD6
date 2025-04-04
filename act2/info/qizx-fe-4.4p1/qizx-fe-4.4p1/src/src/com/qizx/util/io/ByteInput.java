/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.util.io;

import java.io.IOException;

/**
 *      Abstract interface for binary IO.
 */
public interface ByteInput
{
    int getByte()	throws IOException;
    
    int getBytes(byte[] buf)	throws IOException;
    
    /**
     * Reads an int on 4 bytes, MSB first.
     */
    int getInt()   throws IOException;
    /**
     * Reads an unsigned int in variable length.
     */
    int getVint()  throws IOException;
    /**
     * Reads an unsigned long in 8 bytes, MSB first.
     */
    long  getLong()    throws IOException;
    /**
     * Reads an unsigned long in variable length.
     */
    long  getVlong()    throws IOException;
    /**
     * Reads a IEEE double on 8 bytes.
     */
    double getDouble()	throws IOException;

    char[] getChars() throws IOException;
    
    void getChars (char[] buffer, int pos, int length, boolean wide)
	    throws IOException;

    String getString()	throws IOException;
    
    void close() throws IOException;
    
    void inspect();
}
