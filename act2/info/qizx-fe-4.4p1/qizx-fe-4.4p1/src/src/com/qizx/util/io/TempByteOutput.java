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
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *  Temporary buffer, using a temp file if size goes over buffer size.
 */
public class TempByteOutput extends ByteOutputBase
{
    protected File tempFile;
    protected FileOutputStream out;
    protected File tempDir;
    protected int savedSize;
    
    public TempByteOutput(int bufferSize, File tempDir)
    {
        super(bufferSize);
        this.tempDir = tempDir;
    }
    
    public TempByteOutput(int bufferSize)
    {
        this(bufferSize, null);
    }
    
    public long getLength()
    {
        return savedSize + bufPtr;
    }

    protected void writeBuffer(byte[] buffer, int size) throws IOException
    {
        if(tempFile == null && size >= bufferSize) { // 
            tempFile = File.createTempFile("bio", null, tempDir);
            out = new FileOutputStream(tempFile);
        }
        if(out != null)
            out.write(buffer, 0, size);
        savedSize += size;
    }
    
    public void close()  throws IOException
    {
        // flush only if file used
        if(bufPtr > 0) {
            flushBuffer();
            bufPtr = 0;
        }
        if (out != null) {
            out.close();
            out = null;
        }
    }
    
    public void cleanup()  throws IOException
    {
        if (tempFile != null)
            tempFile.delete();
        tempFile = null;
        data = null;
    }

    public File getFile()
    {
        return tempFile;
    }
}
