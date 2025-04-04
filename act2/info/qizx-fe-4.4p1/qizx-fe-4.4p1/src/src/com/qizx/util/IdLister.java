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
 * Displays a sequence of int to the form "n1 to n2, n3 to n4 ..."
 */
public class IdLister
{
    StringBuilder buffer;
    int first, last;
    
    public IdLister() {
        reset();
    }
    
    public void reset()
    {
        first = last = -1;
        buffer = new StringBuilder();
    }

    public void put(int id)
    {
        if (first < 0)
            first = last = id;
        else if (id == last + 1)
            ++last;
        else {
            flushRange();
            first = last = id;
        }
    }

    private void flushRange()
    {
        if (buffer.length() > 0)
            buffer.append(", ");
        if(last > first)
            buffer.append(idForm(first) + " to " + idForm(last));
        else
            buffer.append(idForm(first));
        first = last = -1;

    }

    public boolean hasContents()
    {
        if (last > 0)
            flushRange();
        return buffer.length() > 0;
    }
    
    public StringBuilder buffer()
    {
        if (last > 0)
            flushRange();
        return buffer;
    }
    
    protected String idForm(int id) {
        return Integer.toString(id);
    }

    public void elide(int maxSize)
    {
        if(buffer.length() > maxSize) {
            int pos = maxSize / 2 - 8;
            buffer.delete(pos, buffer.length() - pos);
            buffer.insert(pos, "................");
        }
    }
}
