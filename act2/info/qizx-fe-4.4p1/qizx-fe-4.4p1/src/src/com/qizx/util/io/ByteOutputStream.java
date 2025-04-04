/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */

package com.qizx.util.io;

import java.io.*;

/**
 *	OutputByteStream writing to a standard OutputStream.
 */
public class ByteOutputStream
    extends ByteOutputBase
{
    OutputStream out;
    
    public ByteOutputStream( File file ) throws FileNotFoundException {
        this(new FileOutputStream(file));
    }
    
    public ByteOutputStream( OutputStream out ) {
        super(4096);
        this.out = out;
    }
    
    protected void writeBuffer(byte[] buffer, int size) throws IOException {
        out.write(data, 0, size);
    }
    
    public void flush() throws IOException {
        flushBuffer();
        out.flush();
    }
    
    public void close() throws IOException {
        flush();
        out.close();
    }
    
    /**
     *	Closes and synchronizes the file on disk.
     *	CAUTION: assumes the underlying OutputStream is a FileOutputStream.
     */
    public void syncClose() throws IOException {
        flush();
        ((FileOutputStream) out).getFD().sync();
        close();
    }
}
