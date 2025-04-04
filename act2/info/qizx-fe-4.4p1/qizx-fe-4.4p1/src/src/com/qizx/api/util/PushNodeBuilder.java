/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.api.util;

import com.qizx.api.Node;
import com.qizx.api.XMLPushStream;
import com.qizx.xdm.CorePushBuilder;

/**
 * An implementation of XMLPushStream that builds an in-memory Node tree and
 * returns the top Node.
 * <p>
 * Nodes are built by calling methods of {@link XMLPushStream} like
 * {@link XMLPushStream#putElementStart putElementStart},
 * {@link XMLPushStream#putAttribute putAttribute},
 * {@link XMLPushStream#putText putText},
 * {@link XMLPushStream#putElementEnd putElementEnd} etc. in the proper order,
 * or the method {@link XMLPushStream#putNodeCopy putNodeCopy}, or both.
 * Finally the {@link #reap()} method returns the top-level node built.
 * <p>
 * The reset() method should be called before reusing this object for building
 * another tree.
 * @since 2.1
 */
public class PushNodeBuilder extends CorePushBuilder
{
    public PushNodeBuilder()
    {
        super("");
    }

    /**
     * Returns the top-level node built with this object. If
     * {@link #putDocumentStart()} has been used first, this will be a document
     * node, else an element.
     * @return the top-level Node built with this object.
     */
    public Node reap()
    {
        flush();
        return super.harvest();
    }
}
