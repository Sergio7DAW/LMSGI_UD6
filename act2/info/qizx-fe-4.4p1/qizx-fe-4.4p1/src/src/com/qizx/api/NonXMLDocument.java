/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.api;

import java.io.InputStream;

/**
 * Represents a non-XML Document stored in a XML Library.
 * <p>
 * Its contents is a plain sequence of bytes, and can be read through a sequential
 * Stream.
 * <p>
 * When importing in the database (using {@link Library#importNonXMLDocument})
 * compression can be specified. Compression is recommended for text-like data and
 * not recommended for already compressed data such as images.
 * <p>
 * As a {@link LibraryMember}, a NonXMLDocument can have searchable properties
 * (aka metadata).
 * <p>
 * It is recommended to set property "<code>mime-type</code>" to the value of 
 * the data's mime-type, so that the contents can be later properly rendered.
 * @see LibraryMember
 */
public interface NonXMLDocument
    extends LibraryMember
{
    /**
     * Returns the uncompressed size in bytes of the contained data.
     */
    long  size()
        throws DataModelException;
    
    /**
     * Returns true if the contained data is compressed.
     */
    boolean isCompressed()
        throws DataModelException;
    
    /**
     * Opens for sequential read.
     * @return a InputStream allowing to read the binary contents.
     * @throws DataModelException if the Blob is deleted; <a
     *         href='Library.html#std_exc'>common causes</a>
     */
    InputStream open()
        throws DataModelException;
}
