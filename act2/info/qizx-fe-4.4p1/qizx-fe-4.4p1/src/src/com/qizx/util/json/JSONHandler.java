/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.util.json;

/**
 * Handler of JSON parsing events. Similar to SAX2 handler.
 */
public interface JSONHandler
{
    /**
     * Called on start of a map (Object).
     * <p>
     * This event is followed by a mapKey event for each key and a value event or
     * a balanced map/array sequence for each value.
     */
    void mapStart()
        throws JSONException;

    /**
     * Called at start of a pair of a map (Object).
     * @param value value of the key in the pair
     * @param length length of the key in the pair
     */
    void pairStart(char[] value, int length)
        throws JSONException;

    /**
     * Called at end of a map pair.
     * @throws JSONException
     */
    void pairEnd()
        throws JSONException;

    /**
     * Called on end of a map (Object).
     */
    void mapEnd()
        throws JSONException;

    /**
     * Called on start of an array. This event is followed by a value event or a
     * balanced map/array sequence for each array element.
     */
    void arrayStart()
        throws JSONException;

    /**
     * Called on end of a map (Object).
     */
    void arrayEnd()
        throws JSONException;

    void stringValue(char[] value, int length)
        throws JSONException;

    void doubleValue(double value)
        throws JSONException;

    void booleanValue(boolean value)
        throws JSONException;

    void nullValue()
        throws JSONException;
}
