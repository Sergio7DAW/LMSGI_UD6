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
 *	Abstract interface for binary IO.	
 *  See also static methods bytesForXXX in ByteOutputBase.
 */
public interface ByteOutput
{
    void putByte(int b)
        throws IOException;
    
    void putBytes(byte[] buf, int length)
        throws IOException;
    
    void padding(int size, int value)
        throws IOException;

    //void putBytes(byte[] buf, int start, int length) throws IOException;
    
    /**
     * Writes a signed int as 4 bytes (MSB first)
     */
    void putInt( int code )
        throws IOException;
    /**
     * Writes an unsigned int in variable size.
     */
    void putVint( int code )
        throws IOException;

    /**
     * Writes a signed long as 8 bytes (MSB first)
     */
    void putLong(long l)
        throws IOException;

    void putVlong( long code )
        throws IOException;
    
    void putDouble(double value)
        throws IOException;
    
    void putString(String s)
        throws IOException;
    
    void putChars(char[] chars, int start, int length)
        throws IOException;

    /** 
     * Always performs a flush.
     */ 
    void close()
        throws IOException;

    /** 
     * Forces writing of the buffer.
     */ 
    void flush()
        throws IOException;
    
    /** marks the beginning of a non-breakable segment: if used, a flush
     * should happen only on such a mark.
     */ 
    void flushMark()
        throws IOException;
}
