/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.api.util;

/**
 * Stores options used when importing documents into an XML Library.
 * 
 */
public class ImportOptions
{
    private boolean stripWhiteSpace;

    public boolean getStripWhiteSpace()
    {
        return stripWhiteSpace;
    }

    public void setStripWhiteSpace(boolean stripWhiteSpace)
    {
        this.stripWhiteSpace = stripWhiteSpace;
    }

    public ImportOptions copy()
    {
        ImportOptions nop = new ImportOptions();
        nop.stripWhiteSpace = stripWhiteSpace;
        return nop;
    }
}
