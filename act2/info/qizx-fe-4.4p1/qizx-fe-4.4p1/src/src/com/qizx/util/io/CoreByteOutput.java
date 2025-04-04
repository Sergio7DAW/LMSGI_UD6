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
 *  Output to a list of byte blocks.
 *  Can be configured to work with a single block.
 */
public class CoreByteOutput extends ByteOutputBase
{
    byte[][] blocks;
    int []   blockSizes;
    int blockCount;
    
    public CoreByteOutput() {
        super(8192);
        blocks = new byte[8][];
        blockSizes = new int[blocks.length];
    }

    /**
     * Single block output (error if overflow).
     */
    public CoreByteOutput(byte[] data) {
        super(data);
        blocks = null;
    }
    
    public long getLength() throws IOException
    {
        flush();
        long len = 0;
        for (int i = 0; i < blockCount; i++)
            len += blockSizes[i];
        return len;
    }
    
    public byte[] getBytes() throws IOException
    {
        int len = (int) getLength();
        byte[] b = new byte[len];
        int ptr = 0;
        for (int i = 0; i < blockCount; i++) {
            System.arraycopy(blocks[i], 0, b, ptr, blockSizes[i]);
            ptr += blockSizes[i];
        }
        return b;
    }
    

    protected void writeBuffer(byte[] buffer, int size) {
        if(blocks == null)
            return;
        
        if(blockCount >= blocks.length) {
            byte[][] old = blocks;
            blocks = new byte[ old.length * 2 ][];
            System.arraycopy(old, 0, blocks, 0, old.length);
            int[] oldSizes = blockSizes;
            blockSizes = new int[ blocks.length ];
            System.arraycopy(oldSizes, 0, blockSizes, 0, oldSizes.length);
        }
        blockSizes[blockCount] = size;
        byte[] buf = new byte[size];
        System.arraycopy(data, 0, buf, 0, size); 
        blocks[blockCount ++] = buf;
    }
    
    public void close()  throws IOException {
        if(bufPtr > 0)
            flushBuffer();
        bufPtr = 0;
    }
}
