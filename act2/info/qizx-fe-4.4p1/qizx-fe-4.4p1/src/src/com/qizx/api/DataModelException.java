/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.api;

/**
 * Exception raised by operations on XML data.
 */
public class DataModelException extends QizxException
{
    /**
     * Default constructor, should not be used.
     */
    protected DataModelException()
    {
    }

    /**
     * Constructs a DataModelException from a simple message. The error code
     * is undefined.
     * @param message reason for the exception
     */
    public DataModelException(String message)
    {
        super(message);
    }

    /**
     * Constructs a DataModelException from a simple message and an exception.
     * The error code is undefined.
     * @param message reason for the exception
     * @param cause wrapped cause
     */
    public DataModelException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructs a DataModelException with a message and an XQuery error code.
     * @param errorCode the QName of the error (in principle the XQuery error
     * namespace <code>http://www.w3.org/2005/xqt-errors</code>)
     * @param message reason for the exception
     */
    public DataModelException(QName errorCode, String message)
    {
        super(errorCode, message);
    }

    /**
     * Constructs a DataModelException with a message and an XQuery error code
     * in string form (uses the err: namespace).
     * @param code XQuery code such as XPTY0004
     * @param message reason for the exception
     */
    public DataModelException(String code, String message)
    {
        super(message);
        setErrorCode(code);
    }
}
