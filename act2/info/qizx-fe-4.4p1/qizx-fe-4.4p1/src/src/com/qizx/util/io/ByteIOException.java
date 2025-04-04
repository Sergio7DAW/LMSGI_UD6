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

public class ByteIOException extends IOException
{
    public ByteIOException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ByteIOException(String message)
    {
        super(message);
    }

}
