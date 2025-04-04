/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */

package com.qizx.util.io;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 *    Reads bytes from a list of data blocks.
 *    Blocks are first added, then reading can begin. Blocks can be
 *    of variable size, but only the last one may be incompletely filled.
 */
public class TempByteInput extends ByteInputBase 
    implements Serializable
{
    private File tempFile;
    private FileInputStream input;
    
    public TempByteInput(TempByteOutput source) throws IOException
    {
        super(source.bufferSize);
        tempFile = source.tempFile;
        if(tempFile == null) {  // data only in buffer
            bufSize = source.savedSize;
            System.arraycopy(source.data, 0, data, 0, bufSize);
        }
        else {
            input = new FileInputStream(tempFile);
        }
    }

//    public void restartOn(byte[] buffer, int size)
//        throws IOException
//    {
//        this.data = buffer;
//        this.bufSize = size;
//        this.ptr = 0;
//    }
    
    @Override
    public void close()
        throws IOException
    {
        if (input != null)
            input.close();
        input = null;
        // tempFile cleaned by TempByteOutput
    }

    protected int readBuffer() throws IOException
    {
        return (input == null)? -1 : input.read(data, 0, data.length);
    }
}
