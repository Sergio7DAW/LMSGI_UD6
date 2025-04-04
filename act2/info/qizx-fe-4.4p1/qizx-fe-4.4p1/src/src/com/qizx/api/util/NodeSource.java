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

import org.xml.sax.InputSource;

import java.io.StringReader;

import javax.xml.transform.sax.SAXSource;

/**
 * An extension of SAXSource that allows using a Qizx Document or Node as a
 * source for an XSLT transformation.
 */
public class NodeSource extends SAXSource
{
    private Node rootNode;

    /**
     * Creates a NodeSource from a Node.
     * @param node the XML tree to transform by a XSLT processor.
     */
    public NodeSource(Node node)
    {
        super(new NodeXMLReader(node),
              // dummy input: most XSLT engines dont like it null
              new InputSource(new StringReader("<dummy/>")));
        rootNode = node;
    }

    /**
     * Returns the Node used as input by the XSLT transformation.
     * @return the root node set in the constructor
     */
    public Node getRootNode()
    {
        return rootNode;
    }
}
