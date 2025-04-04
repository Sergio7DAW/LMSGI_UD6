/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.util.rest;

import com.qizx.api.QizxException;

public class RESTException extends QizxException
{
    public RESTException(String message)
    {
        super(message);
    }

    public RESTException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
