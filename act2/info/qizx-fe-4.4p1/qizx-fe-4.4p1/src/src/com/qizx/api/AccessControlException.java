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
 * Exception thrown when an {@link AccessControl} denies a {@link User} the
 * permission to read or modify information in Library.
 */
public class AccessControlException extends LibraryException
{
    /**
     * Constructs an AccessControlException.
     * @param message reason for denying access.
     */
    public AccessControlException(String message)
    {
        super(message);
    }
}
