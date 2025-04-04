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
 * Extended binary input used for ZIPDocuments.
 */
public interface ZIPInput
    extends ByteInput
{
    byte PADDING = (byte) 0xff;

    long magicSeek(byte[] magic) throws IOException;

    void close() throws IOException;

    long tell();

    void seek(long nodeId) throws IOException;

    void relativeSeek(int shift) throws IOException;

    void skipString() throws IOException;

    long skipPadding() throws IOException;

    char[] getChars(int reserve) throws IOException;

    int getChars(char[] buffer, int offset) throws IOException;

    void getString(StringBuffer sb) throws IOException;

    long getSignedVlong() throws IOException;
}
