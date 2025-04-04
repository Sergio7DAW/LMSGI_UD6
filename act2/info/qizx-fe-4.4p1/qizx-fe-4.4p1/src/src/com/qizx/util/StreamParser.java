/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.util;

import com.qizx.api.CompilationException;
import com.qizx.api.DataModelException;
import com.qizx.api.QName;
import com.qizx.api.XMLPullStream;
import com.qizx.util.basic.XMLUtil;

public class StreamParser
{
    private XMLPullStream input;

    private int curAttrIndex, curAttrCount;
    protected QName curAttrName;
    protected String curAttrValue;

    public StreamParser(XMLPullStream input)
    {
        this.input = input;
        curAttrCount = -1;
    }

    protected void wantElemStart(String ns, String name)
        throws CompilationException
    {
        if (!seeElemStart(ns, name))
            syntaxError("expecting xsl:" + name);
    }

    protected void wantElemEnd(String ns, String name)
        throws CompilationException
    {
        if (!pickElemEnd(ns, name))
            syntaxError("expecting xsl:" + name);
    }

    protected boolean pickElemStart(String ns, String name)
        throws CompilationException
    {
        if (!seeElemStart(ns, name))
            return false;
        moveNext();
        return true;
    }

    protected boolean seeElemStart(String ns, String name)
        throws CompilationException
    {
        skip();
        if (input.getCurrentEvent() != XMLPullStream.ELEMENT_START)
            return false;
        QName qname = input.getName();
        return qname.getNamespaceURI() == ns
               && name.equals(qname.getLocalPart());
    }

    protected boolean pickElemEnd(String ns, String name)
        throws CompilationException
    {
        skip();
        if (input.getCurrentEvent() != XMLPullStream.ELEMENT_END)
            return false;
        QName qname = input.getName();
        if (qname.getNamespaceURI() != ns
            || !name.equals(qname.getLocalPart()))
            return false;
        moveNext();
        return true;
    }

    protected boolean nextAttribute()
    {
        if (curAttrCount < 0) {
            curAttrCount = input.getAttributeCount();
            curAttrIndex = 0;
        }
        if (curAttrIndex >= curAttrCount)
            return false;
        curAttrName = input.getAttributeName(curAttrIndex);
        curAttrValue = input.getAttributeValue(curAttrIndex);
        ++ curAttrIndex;
        return true;
    }

    protected boolean isAttr(String ns, String attrName)
    {
        return curAttrName.getNamespaceURI() == ns
               && attrName.equals(curAttrName.getLocalPart());
    }

    protected QName skip()
        throws CompilationException
    {
        loop: for (;;) {
            switch (input.getCurrentEvent()) {
            case XMLPullStream.COMMENT:
            case XMLPullStream.PROCESSING_INSTRUCTION:
                moveNext();
                break;
            case XMLPullStream.TEXT:
                if (!XMLUtil.isXMLSpace(input.getText()))
                    break loop;
                moveNext();
                break;
            default:
                break loop;
            }
        }
        return input.getName();
    }

    protected void skipEvent(int event) throws CompilationException
    {
         if(input.getCurrentEvent() == event)
             moveNext();
    }

    protected int moveNext()
        throws CompilationException
    {
        curAttrCount = -1;
        try {
            return input.moveToNextEvent();
        }
        catch (DataModelException e) {
            throw new CompilationException(e.getMessage());
        }
    }

    protected void syntaxError(String message)
        throws CompilationException
    {
        throw new CompilationException(message);
    }

}
