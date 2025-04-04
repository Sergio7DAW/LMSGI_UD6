/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.util.basic;

import com.qizx.util.io.ByteInput;
import com.qizx.util.io.ByteOutput;

import java.io.IOException;

public class IntersectionIntSet extends IntSet
{
    IntSet set1;
    IntSet set2;
    //public static long timer1, timer2;
    
    public IntersectionIntSet(IntSet set1, IntSet set2)
    {
        this.set1 = set1;
        this.set2 = set2;
    }

    public static IntSet make(IntSet set1, IntSet set2)
    {
        if (set1 == null)
            return set2;
        if (set2 == null)
            return set1;
        return new IntersectionIntSet(set1, set2);
    }

    @Override
    public boolean test(int item)
    {
        return set1.test(item) && set2.test(item);
    }

    @Override
    public int getNext(int item)
    {
        for(;;) {
            int next = set1.getNext(item);
            if (next < 0)
                return -1;
            if (set2.test(next))
                return next;
            item = next + 1;
        }
    }

    @Override
    public int getNextNotIn(int item)
    {
        throw new RuntimeException("not implemented");
    }

    @Override
    public int getUpperBound()
    {
        return Math.max(set1.getUpperBound(), set2.getUpperBound());
    }

    @Override
    public long rank(int item)
    {
        throw new RuntimeException("not implemented");
    }

    @Override
    public IntSet copy()
    {
        return new IntersectionIntSet(set1.copy(), set2.copy());
    }

    @Override
    public IntSet shallowCopy()
    {
        return new IntersectionIntSet(set1, set2);
    }

    @Override
    public void clear()
    {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void add(int item)
    {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void add(IntSet docs)
    {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void remove(int docId)
    {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void remove(IntSet docs)
    {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void load(ByteInput input)
        throws IOException
    {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void save(ByteOutput output)
        throws IOException
    {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void dump(String message)
        throws IOException
    {
        throw new RuntimeException("not implemented");
    }

}
