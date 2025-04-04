/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.api;

import com.qizx.util.NamespaceContext;
import com.qizx.xdm.IQName;


/**
 * Super-class of all exceptions of the API.
 */
public class QizxException extends Exception
{
    protected QName errorCode;

    protected QizxException()
    {
        super();
    }

    /**
     * Constructs a QizxException from a simple message.
     * The error code is undefined.
     * @param message reason for the exception
     */
    public QizxException(String message)
    {
        super(message);
    }

    /**
     * Constructs a QizxException from a simple message and an exception.
     * The error code is undefined.
     * @param message reason for the exception
     * @param cause wrapped cause
     */
    public QizxException(String message, Throwable cause)
    {
        super(message, cause);
    }


    /**
     * Constructs a QizxException with a message and an XQuery error code.
     * @param errorCode the QName of the error (in principle the XQuery error
     * namespace <code>http://www.w3.org/2005/xqt-errors</code>)
     * @param message reason for the exception
     */
    public QizxException(QName errorCode, String message)
    {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Gets the error code.
     * 
     * @return the errorCode, generally a XQuery error code.
     */
    public QName getErrorCode()
    {
        return errorCode;
    }

    /**
     * Defines the XQuery error code. The namespace of the code is normally
     * "http://www.w3.org/2005/xqt-errors".
     * @param errorCode XQuery code such as XPTY0004
     */
    public void setErrorCode(QName errorCode)
    {
        this.errorCode = errorCode;
    }
    
    /**
     * Defines the XQuery error code. The implicit namespace of the code is
     * "http://www.w3.org/2005/xqt-errors".
     * @param errorCode XQuery code such as XPTY0004
     */
    public void setErrorCode(String errorCode)
    {
         this.errorCode = IQName.get(NamespaceContext.ERR, errorCode);
    }
    
    public void substituteCode(String oldCode, String newCode)
        throws EvaluationException
    {
        if(errorCode != null && oldCode.equals(errorCode.getLocalPart()))
            setErrorCode(newCode);
    }
}
