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
 *	
 */
public class ByteInputStream 
    extends ByteInputBase
{
    InputStream in;
    
    public ByteInputStream( File file ) throws FileNotFoundException {
        this(new FileInputStream(file));
    }
    
    public ByteInputStream( InputStream in ) {
        super(4096);
        this.in = in;
    }
    
    public ByteInputStream() {
        this( (InputStream) null );
    }
    
    protected int readBuffer() throws IOException {
        return in.read(data, 0, maxBufferSize);
    }
    
    public void close() throws IOException {
        in.close();
    }
}
