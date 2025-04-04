/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.util.json;

import com.qizx.api.DataModelException;
import com.qizx.api.QName;
import com.qizx.api.XMLPushStream;
import com.qizx.xdm.IQName;

/**
 * Builds an XML tree from JSON data.
 */
public class JSONBuilder
    implements JSONHandler
{
    public static final String JSON_NS = "com.qizx.json";
    public static final QName E_MAP = IQName.get(JSON_NS, "map");
    public static final QName E_PAIR = IQName.get(JSON_NS, "pair");
    public static final QName AT_NAME = IQName.get("name");
    public static final QName E_ARRAY = IQName.get(JSON_NS, "array");
    public static final QName E_STRING = IQName.get(JSON_NS, "string");
    public static final QName E_NUMBER = IQName.get(JSON_NS, "number");
    public static final QName E_BOOL = IQName.get(JSON_NS, "boolean");
    public static final QName E_NULL = IQName.get(JSON_NS, "null");

    private XMLPushStream out;

    public JSONBuilder(XMLPushStream handler)
    {
        this.out = handler;
    }

    public void mapStart()
        throws JSONException
    {
        try {
            out.putElementStart(E_MAP);
        }
        catch (DataModelException e) {
            throw wrapped(e);
        }
    }

    public void pairStart(char[] value, int length)
        throws JSONException
    {
        try {
            out.putElementStart(E_PAIR);
            out.putAttribute(AT_NAME, new String(value, 0, length), null);
        }
        catch (DataModelException e) {
            throw wrapped(e);
        }
    }

    public void pairEnd()
        throws JSONException
    {
        try {
            out.putElementEnd(E_PAIR);
        }
        catch (DataModelException e) {
            throw wrapped(e);
        }
    }

    public void mapEnd()
        throws JSONException
    {
        try {
            out.putElementEnd(E_MAP);
        }
        catch (DataModelException e) {
            throw wrapped(e);
        }
    }

    public void arrayStart()
        throws JSONException
    {
        try {
            out.putElementStart(E_ARRAY);
        }
        catch (DataModelException e) {
            throw wrapped(e);
        }
    }

    public void arrayEnd()
        throws JSONException
    {
        try {
            out.putElementEnd(E_ARRAY);
        }
        catch (DataModelException e) {
            throw wrapped(e);
        }
    }

    public void stringValue(char[] value, int length)
        throws JSONException
    {
        try {
            out.putElementStart(E_STRING);
            out.putChars(value, 0, length);
            out.putElementEnd(E_STRING);
        }
        catch (DataModelException e) {
            throw wrapped(e);
        }
    }

    public void booleanValue(boolean value)
        throws JSONException
    {
        try {
            out.putElementStart(E_BOOL);
            out.putAtom(value);
            out.putElementEnd(E_BOOL);
        }
        catch (DataModelException e) {
            throw wrapped(e);
        }
    }

    public void doubleValue(double value)
        throws JSONException
    {
        try {
            out.putElementStart(E_NUMBER);
            out.putAtom(value);
            out.putElementEnd(E_NUMBER);
        }
        catch (DataModelException e) {
            throw wrapped(e);
        }
    }

    public void nullValue()
        throws JSONException
    {
        try {
            out.putElementStart(E_NULL);
            out.putElementEnd(E_NULL);
        }
        catch (DataModelException e) {
            throw wrapped(e);
        }
    }

    private JSONException wrapped(DataModelException e)
    {
        return new JSONException(e.getMessage(), e);
    }
}
