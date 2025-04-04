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
import com.qizx.util.io.CoreByteInput;
import com.qizx.util.io.ZIPOutput;

import java.io.IOException;

/**
 * A set of positive integers.
 */
public abstract class IntSet
{
    private static final int FORMAT_DRL = 0xdf;

    public int size()
    {
        int s = 0;
        for (int i = -1; (i = getNext(i + 1)) >= 0;) {
            ++ s;
        }
        return s;
    }
    
    public abstract boolean test(int ch);

    public abstract int/*LId*/ getNext(int item);
    
    public abstract int/*LId*/ getNextNotIn(int/*LId*/ id);

    /**
     * Return a value greater than last (highest) element of set.
     * No need for precision.
     */
    public abstract int/*LId*/ getUpperBound();

    public abstract long rank(int item);

    public abstract void add(int item);
    
    public void add(int/*LId*/ first, int/*LId*/ last)
    {
        if(first < 0)
            throw new IllegalArgumentException("negative element " + first);
        for (int/*LId*/ i = first; i <= last; i++)
            add(i);
    }

    public abstract void remove(int docId);
    
    public void remove(int/*LId*/ first, int/*LId*/ last)
    {
        if(first < 0)
            throw new IllegalArgumentException("negative element " + first);
        for (int/*LId*/ i = first; i <= last; i++)
            remove(i);
    }

    public abstract void clear();

    public abstract void remove(IntSet docs);

    public abstract void add(IntSet docs);

    /**
     * Returns a new set which is the union of 'this' and 'that'.
     * The implementation will likely the same as 'this'.
     */
    public static IntSet unionOf(IntSet set1, IntSet set2)
    {
        if(set1 == null)
            return (set2 == null)? null : set2.copy();
        if(set2 == null)
            return set1.copy();
        return set1.unionWith(set2);
    }
    
    /**
     * Returns a new set which is the union of 'this' and 'that'.
     * The implementation will likely the same as 'this'.
     */
    public IntSet unionWith(IntSet that)
    {
        // default implementation: can be optimized
        IntSet res = copy();
        for (int i = -1; (i = that.getNext(i + 1)) >= 0;) {
            res.add(i);
        }
        return res;
    }

    public static IntSet differenceOf(IntSet set1, IntSet set2)
    {
        if(set1 == null)
            return (set2 == null) ? null : set2.copy();
        IntSet res = set1.copy();
        if (set2 != null) {
            for (int i = -1; (i = set2.getNext(i + 1)) >= 0;) {
                res.remove(i);
            }
        }
        return res;
    }

    public static IntSet intersectionOf(IntSet set1, IntSet set2)
    {
        if(set1 == null || set2 == null)
            return null;
        return set1.intersectionWith(set2);
    }

    /**
     * Returns a new set which is the intersection of 'this' and set.
     * The implementation will likely the same as 'this'.
     */
    public IntSet intersectionWith(IntSet set)
    {
        // default implementation: can be optimized
        IntSet res = copy();
        for (int i = -1; (i = this.getNext(i + 1)) >= 0;) {
            if(!set.test(i))
                res.remove(i);
        }
        return res;
    }

    public boolean intersectsRange(int min, int max)
    {
        return (this.getNext(min - 1) <= max);
    }

    public static IntSet decode(byte[] bytes) throws IOException
    {
        return decode(new CoreByteInput(bytes, bytes.length));
    }

    public static IntSet decode(ByteInput in) throws IOException
    {
        // All sets should have a magic header by default // TODO
        int header = in.getByte();
        if(header == ArrayIntSet.HEADER) {
            ArrayIntSet set = new ArrayIntSet();
            set.load(in);
            return set;
        }
        return null;
    }

    /**
     * Returns a deep copy of the set.
     */
    public abstract IntSet copy();

    /**
     * Returns a shallow copy of the set. 
     * In PagedIntSet, copy on write is used.
     * In ArrayIntSet no difference between shallowCopy and deep copy.
     */
    public abstract IntSet shallowCopy();
    

    public abstract void load(ByteInput input)
        throws IOException;
    
    public abstract void save(ByteOutput output)
        throws IOException;

    /**
     * Like save but with a leading and trailing mark (known of
     * {@link #decode(byte[])}).
     */
    public void serialize(ByteOutput output)
        throws IOException
    {
        throw new IOException("not implemented");
    }

    /**
     * Print to System.err with a leading message
     */
    public abstract void dump(String message)
        throws IOException;

    public String show(int maxSize)
    {
        int s = size();
        StringBuffer buf = new StringBuffer(s + " [");
        int/*LId*/ id = -1;
        for (; buf.length() < maxSize;) {
            id = getNext(id + 1);
            if (id < 0)
                break;
            if(buf.length() > 1)
                buf.append(' ');
            buf.append(Long.toString(id));
        }
        buf.append(id < 0 ? "]" : "...]");
        return buf.toString();
    }

    public void saveDiffRunLength(ZIPOutput out)
        throws IOException
    {
        out.putVint(FORMAT_DRL);    // format mark
        int elem = -1;
        int prev = 0;
        for (;;) {
            int first = getNext(elem);
            if (first < 0)
                break;
            int last = getNextNotIn(first + 1);
            // differential coding of 'first:
            out.putVint(first - prev);
            prev = first;
            out.putVint(last - first);
            elem = last;
        }
        // marks the end:
        out.putVint(0);
    }

    public void loadDiffRunLength(ByteInput input)
        throws IOException
    {
        int code = input.getVint();
        if (code != FORMAT_DRL)
            throw new IOException("set not coded in differential run-length format");

        int prev = 0;
        for (;;) {
            code = input.getVint();
            if (code == 0)
                break;
            int first = code + prev;
            prev = first;
            int last = input.getVint() + first;
            add(first, last - 1);
        }
    }
}
