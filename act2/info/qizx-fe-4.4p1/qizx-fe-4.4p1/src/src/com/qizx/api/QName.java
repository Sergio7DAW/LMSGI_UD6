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
 * Qualified name for XML elements and attributes.
 * <p>
 * The {@link ItemFactory} interface is normally invoked for creating the
 * QName's used in the API. It is recommended to cache QNames when possible for
 * better performance.
 * <p>
 * Mandatory requirements for an implementation of this interface:
 * <ul>
 * <li>The hashCode() and equals() methods must be properly implemented.
 * </ul>
 */
public interface QName
{
    /**
     * Returns the local part of the qualified name.
     * @return a String representing the local part value
     */
    public String getLocalPart();

    /**
     * Returns the namespace URI of the QName. 
     * <p>
     * If the QName has no namespace, this value is the blank string (not null).
     * @return a String representing the namespace URI value
     */
    public String getNamespaceURI();

    /**
     * Returns an optional prefix associated with the QName (may be null).
     * @return a String representing the prefix value
     */
    public String getPrefix();

    /**
     * Returns true if this QName has the empty namespace URI.
     * @return true if this QName has the empty namespace URI.
     */
    public boolean hasNoNamespace();
}
