/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.util;

/**
 * Maps Unicode character codes to values (any object or integer).
 */
public class CharTable
{
    private Object[][] pages;
    private int[][] intPages;
    
    public CharTable()
    {
        pages = new Object[256][];
        intPages = new int[256][];
    }
    
    public Object get(int c)
    {
        Object[] page = pages[c >>> 8];
        return (page == null) ? null : page[c & 0xff];
    }
    
    public int getInt(int c)
    {
        int[] page = intPages[c >>> 8];
        return (page == null) ? 0 : page[c & 0xff];
    }
    
    public void put(int c, Object value)
    {
        int pageId = c >>> 8;
        Object[] page = pages[pageId];
        if(page == null)
            page = pages[pageId] = new Object[256];
        page[c & 0xff] = value;
    }
    
    public void putInt(int c, int value)
    {
        int pageId = c >>> 8;
        int[] page = intPages[pageId];
        if(page == null)
            page = intPages[pageId] = new int[256];
        page[c & 0xff] = value;
    }
}
