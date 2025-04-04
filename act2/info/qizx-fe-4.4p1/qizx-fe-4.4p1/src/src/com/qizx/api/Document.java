/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.api;

import com.qizx.api.util.XMLSerializer;

/**
 * Represents a well-formed XML Document stored in a XML Library.
 * <p>
 * The tree structure of a Document can be traversed and queried using the
 * XPath/XQuery language.
 * <p>
 * As a {@link LibraryMember}, it can bear properties (aka metadata) than can be
 * queried.
 * @see LibraryMember
 */
public interface Document
    extends LibraryMember
{
    /**
     * Returns the root Node of the document, of type document-node().
     * @return a node of type document-node(), root of the document.
     * @throws DataModelException if the document is deleted; 
     * <a href='Library.html#std_exc'>common causes</a>
     */
    Node getDocumentNode()
        throws DataModelException;

    /**
     * Streaming export in pull mode.
     * @return a XMLPullStream iterator allowing to extract the document
     *         contents in "pull" style.
     * @throws DataModelException if the document is deleted; <a
     *         href='Library.html#std_exc'>common causes</a>
     */
    XMLPullStream export()
        throws DataModelException;

    /**
     * Streaming export in push mode.
     * @param output an object implementing the XMLPushStream interface
     * typically {@link XMLSerializer}, PushStreamToSAX, PushStreamToDOM.
     * @throws DataModelException if the document is deleted; <a
     *         href='Library.html#std_exc'>common causes</a>
     */
    void export(XMLPushStream output)
        throws DataModelException;
}
