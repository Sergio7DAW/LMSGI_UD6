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
 * A specific class of DataModelException raised by Library operations.
 */
public class LibraryException extends DataModelException
{
    /** Error code used by default. */
    public static final QName MISC =
        IQName.get(NamespaceContext.ERR, "XLIB0001");
    /** Error code used for a non-existent Document or Collection.  */
    public static final QName MEMBER_NOT_FOUND =
        IQName.get(NamespaceContext.ERR, "XLIB0002");
    /** Code for errors in import operations. */
    public static final QName IN_IMPORT =
        IQName.get(NamespaceContext.ERR, "XLIB0003");
    /** Code for errors in update operations. */
    public static final QName IN_UPDATE =
        IQName.get(NamespaceContext.ERR, "XLIB0004");
    /** Code for errors in query operations. */
    public static final QName QUERY =
        IQName.get(NamespaceContext.ERR, "XLIB0005");
    /** Code used when trying to open a locked Library. */
    public static final QName LOCKED =
        IQName.get(NamespaceContext.ERR, "XLIB0006");
    public static final QName NON_XML =
        IQName.get(NamespaceContext.ERR, "XLIB0007");

    
    /**
     * Constructs a DataModelException from a simple message and an exception.
     * The error code is MISC.
     * @param message reason for the exception
     * @param cause wrapped cause
     */
    public LibraryException(String message, Throwable cause)
    {
        super(message, cause);
        setErrorCode(MISC);
    }

    /**
     * Constructs a DataModelException from a simple message. 
     * The error code is MISC.
     * @param message reason for the exception
     */
    public LibraryException(String message)
    {
        super(message);
        setErrorCode(MISC);
    }

    /**
     * Constructs a LibraryException with a message and an XQuery error code.
     * @param errorCode the QName of the error (in principle the XQuery error
     * namespace <code>http://www.w3.org/2005/xqt-errors</code>)
     * @param message reason for the exception
     */
    public LibraryException(QName errorCode, String message)
    {
        super(errorCode, message);
    }    
}
