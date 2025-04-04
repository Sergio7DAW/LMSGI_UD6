/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.util.io;

import com.qizx.util.QNameTable;

/**
 * Abstract output for ZIP document writer. Makes it independent of a
 * particular implementation (Blobs or BlockStore).
 */
public interface ZIPOutput
    extends ZIPInput, ByteOutput
{
    int getBlockSize();

    void putByte(int value);

    void putBytes(byte[] bytes, int length);

    void putInt(int value);     // 4 bytes MSB first

    void putVint(int value);

    void putVlong(long value);

    void putSignedVlong(long value);

    void putDouble(double value);

    void putString(String value);
    
    void putChars(char[] text, int start, int textLength);

    void saveTable(QNameTable table);

    void magicTrailer(byte[] magic, long header);

    void setBlockPatcher(BlockPatcher holder);
    
    public interface BlockPatcher
    {
        // returns true if block needs no more update (can be discarded from pool)
        boolean isBlockComplete(long offset, int blockSize);
    }
}
