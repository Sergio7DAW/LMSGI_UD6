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
import java.util.zip.CRC32;

/**
 * A set of integer values. Implemented as an array of bits.
 */
public class ArrayIntSet extends IntSet
    implements java.io.Serializable
{
    public static final int HEADER = 0xa1;
    // use bytes as cell:
    private static final int SHIFT = 5;
    private static final int USIZE = 1 << SHIFT;  // unit size in bits
    private static final int MASK = USIZE - 1;    // on lower bits of element
    private static final int ALLSETU = -1;
    // number of set bits in a hex digit
    private static final int[] HEXCOUNT = {
        0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4
    };

    // loBound is the smallest value represented by bits
    private int/*LId*/   loBound; // multiple of USIZE (aligned)
    private int/*LId*/   hiBound; // multiple of USIZE (aligned)
    private int[] bits;

    //TODO manage several bit groups for very sparse sets

    /**
     * Builds an empty set.
     */
    public ArrayIntSet()
    {
    }

    /**
     * Builds a set containing a single element.
     */
    public ArrayIntSet(int/*LId*/ firstElement)
    {
        add(firstElement);
    }

    public ArrayIntSet(int/*LId*/ firstElement, int/*LId*/ lastELement)
    {
        add(firstElement, lastELement);
    }

    public int size()
    {
        if (bits == null || hiBound == loBound)
            return 0;
        int sz = 0;
        for (int c = bits.length; --c >= 0;) {
            sz += countBits(bits[c]);
        }
        return sz;
    }

    @Override
    public long rank(int element)
    {
        if (bits == null || element < loBound || element >= hiBound)
            return -1;
        int elem = element - loBound;
        int last = elem / USIZE;
        int rank = 0;
        for (int c = 0; c < last; c++) {
            rank += countBits(bits[c]);
        }
        int mask = (1 << (elem % USIZE)) - 1;
        return rank + Util.countBitsInMask(bits[last] & mask);
    }

    private int countBits(int b)
    {
        if (b == 0)
            return 0;
        return HEXCOUNT[b & 0xf]         + HEXCOUNT[(b >> 4) & 0xf]
	         + HEXCOUNT[(b >> 8) & 0xf]  + HEXCOUNT[(b >> 12) & 0xf]
	         + HEXCOUNT[(b >> 16) & 0xf] + HEXCOUNT[(b >> 20) & 0xf]
	         + HEXCOUNT[(b >> 24) & 0xf] + HEXCOUNT[(b >> 28) & 0xf];
    }

    public boolean test(int/*LId*/ element)
    {
        if (element < loBound || element >= hiBound)
            return false;
        int/*LId*/ bit = element - loBound;
        return (bits[(int) (bit >> SHIFT)] & (1 << (bit & MASK))) != 0;
    }

    /**
     * Iteration mechanism: returns the first id in the set that is >= to
     * argument.
     */
    public int/*LId*/ getNext(int/*LId*/ id)
    {
        int/*LId*/ bit = id - loBound;
        if (bit < 0)
            bit = 0;
        int cell = (int) (bit >> SHIFT);
        bit &= MASK;
        int unitCount = (bits == null)? 0 : bits.length;
        for (; cell < unitCount; bit = 0, cell++) {
            int unit = bits[cell] >>> bit;
        if (unit == 0)
            continue;
        for (; bit < USIZE; )
            if ((unit & 1) != 0)
                return loBound + cell * USIZE + bit;
            else if((unit & 0xff) == 0) {
                bit += 8; 
                unit >>>= 8;
            }
            else {
                ++ bit;
                unit >>>= 1;
            }
        }
        return -1;
    }

    /**
     * Iteration mechanism: returns the first id >= to argument that is NOT in the set.
     */
    public int/*LId*/ getNextNotIn(int/*LId*/ id)
    {
        int/*LId*/ bit = id - loBound;
        if (bit < 0)
            bit = 0;
        int cell = (int) (bit >> SHIFT);
        bit &= MASK;
        int unitCount = (bits == null)? 0 : bits.length;
        for (; cell < unitCount; bit = 0, cell++) {
            int unit = bits[cell] >>> bit;
            if (unit == ALLSETU)
                continue;
            for (; bit < USIZE; )
                if ((unit & 1) == 0)
                    return loBound + cell * USIZE + bit;
                else if((unit & 0xff) == 0xff) {
                    bit += 8; 
                    unit >>>= 8;
                }
                else {
                    ++ bit;
                    unit >>>= 1;
                }
        }
        return hiBound;
    }

    @Override
    public int getUpperBound()
    {
        return hiBound; // TODO better
    }

    public int getMemoryUsed()
    {
        return (bits == null? 0 : bits.length) * 4 + 40;
    }
    
    public String details()
    {
        return ("size=" + size() + "\t1st=" + getNext(0) +
                "\tlo=" + loBound + "\thi=" + hiBound + "\tu=" + getMemoryUsed()
                + " bits=" + (bits == null? "null" : bits.length));
//        int minSize = (hiBound - loBound + USIZE - 1) / USIZE;
//        if (bits.length / (double) minSize > 1.2
//            || size() * 10 < minSize * 4)
//            System.err.println("  compacity!");
    }

    public void add(int/*LId*/ element)
    {
        if(element < 0)
            throw new IllegalArgumentException("negative element " + element);
        int/*LId*/ unit = element & ~MASK; 
        extend(unit, unit + USIZE);
        // lowBound may have changed:
        int/*LId*/ bit = element - loBound;
        bits[(int) (bit >> SHIFT)] |= (1 << (bit & MASK));        
    }

    public void add(int/*LId*/ first, int/*LId*/ last)
    {
        if(first < 0)
            throw new IllegalArgumentException("negative element " + first);
        extend(first & ~MASK, (last + MASK) & ~MASK); // optim
        for (int/*LId*/ i = first; i <= last; i++)
            add(i);
    }

    @Override
    public void add(IntSet set)
    {
        if (set instanceof ArrayIntSet) {
            add((ArrayIntSet) set);
        }
        else for(int i = -1; (i = set.getNext(i + 1)) >= 0; ) {
            add(i);
        }
    }

    public void add(ArrayIntSet set)
    {
        extend(set.loBound, set.hiBound);
        int pos = (int) ((set.loBound - loBound) / USIZE);
        int[] sbits = set.bits;
        if(sbits != null)
            for (int i = sbits.length; --i >= 0;)
                bits[i + pos] |= sbits[i];
    }

    public void expectedbounds(int lowBound, int highBound)
    {
        extend(lowBound & ~MASK, (hiBound + MASK) & ~MASK);
    }

    public void remove(int/*LId*/ element)
    {
        if (element < loBound || element >= hiBound)
            return;
        int bit = (int) (element - loBound);
        bits[bit >> SHIFT] &= ~(1 << (bit & MASK));
    }

    public void remove(int/*LId*/ first, int/*LId*/ last)
    {
        for (int/*LId*/ i = first; i <= last; i++)
            remove(i);
    }

    @Override
    public void remove(IntSet set)
    {
        if (set instanceof ArrayIntSet) {
            remove((ArrayIntSet) set);
        }
        else for(int i = -1; (i = set.getNext(i + 1)) >= 0; ) {
            remove(i);
        }
    }

    public void remove(ArrayIntSet set)
    {
        int/*LId*/ lo = Math.max(loBound, set.loBound);
        int/*LId*/ hi = Math.min(hiBound, set.hiBound);
        if (hi <= lo)
            return;

        int up = (int) ((hi - loBound) / USIZE);
        int diff = (int) ((loBound - set.loBound) / USIZE);
        for(int i = (int) ((lo - loBound) / USIZE); i < up; i++ )
            bits[i] &= ~ set.bits[i + diff];
    }

    public void invert(int/*LId*/ element)
    {
        if (element < loBound || element >= hiBound)
            return;
        int/*LId*/ bit = element - loBound;
        bits[(int) (bit >> SHIFT)] ^= ~(1 << (bit & MASK));
    }

    public void clear()
    {
        bits = null;
        hiBound = loBound = 0;
    }

    public void optimize()
    {
        if (bits == null)
            return;
        int len = bits.length;
        int rstart = 0, rend = len;
        for(; rend > 0; --rend)
            if(bits[rend - 1] != 0)
                break;
        for(; rstart < rend; rstart++)
            if(bits[rstart] != 0)
                break;
        int reduc = rstart + (len - rend);
           
        if (reduc < 8 || reduc < 0.15 * len)   // size reduction < 15%
            return;     // not worth the trip
        int newLen = len - reduc;
        int[] newbits = new int[newLen];
        System.arraycopy(bits, rstart, newbits, 0, newLen);
        loBound += rstart * USIZE;
        hiBound = loBound + newLen * USIZE;
        // assertion:
        if (hiBound - loBound > bits.length * USIZE)
            Check.bug("badly sized bit array: "+bits.length+" "+loBound+"-"+hiBound);
        bits = newbits;
    }

    public IntSet copy()
    {
        ArrayIntSet clone = new ArrayIntSet();
        clone.loBound = loBound;
        clone.hiBound = hiBound;
        if (bits != null)
            clone.bits = (int[]) bits.clone();
        return clone;
    }

    @Override
    public IntSet shallowCopy()
    {
        return copy();
    }

    @Override
    public IntSet unionWith(IntSet set)
    {
        if (!(set instanceof ArrayIntSet))
            return super.unionWith(set);

        ArrayIntSet that = (ArrayIntSet) set;
        if (that.hiBound == that.loBound)
            return this.copy();
        if (this.hiBound == this.loBound)
            return that.copy();

        int/*LId*/lo = Math.min(this.loBound, that.loBound);
        int/*LId*/hi = Math.max(this.hiBound, that.hiBound);

        ArrayIntSet res = new ArrayIntSet();
        res.init(lo, hi);

        int pos = (int) ((this.loBound - res.loBound) / USIZE);
        for (int i = this.bits.length; --i >= 0;)
            res.bits[i + pos] = this.bits[i];

        pos = (int) ((that.loBound - res.loBound) / USIZE);
        for (int i = that.bits.length; --i >= 0;)
            res.bits[i + pos] |= that.bits[i];
        return res;
    }

    /**
     * Creates a new set which is the difference of set1 and set2.
     */
    public static ArrayIntSet differenceOf(ArrayIntSet set1, ArrayIntSet set2)
    {
        ArrayIntSet res = (ArrayIntSet) set1.copy();
        if (set2 == null || set2.hiBound == set2.loBound)
            return res;

        int/*LId*/ lo = Math.max(set1.loBound, set2.loBound);
        int/*LId*/ hi = Math.min(set1.hiBound, set2.hiBound);
        if (hi <= lo)
            return res;

        int up = (int) ((hi - res.loBound) / USIZE);
        int diff = (int) ((set1.loBound - set2.loBound) / USIZE);
        for(int i = (int) ((lo - res.loBound) / USIZE); i < up; i++ )
            res.bits[i] &= ~ set2.bits[i + diff];
        return res;
    }

    @Override
    public IntSet intersectionWith(IntSet set)
    {
        if(! (set instanceof ArrayIntSet) )
            return super.intersectionWith(set);

        ArrayIntSet that = (ArrayIntSet) set;
        ArrayIntSet res = new ArrayIntSet();
        if (set == null || that.hiBound == that.loBound ||
                this.hiBound == this.loBound)
            return res; // empty

        int/*LId*/ lo = Math.max(this.loBound, that.loBound);
        int/*LId*/ hi = Math.min(this.hiBound, that.hiBound);
        if (hi <= lo)
            return res; // empty

        res.init(lo, hi);

        int diff = (int) ((lo - this.loBound) / USIZE);
        for(int i = res.bits.length; --i >= 0; )
            res.bits[i] = this.bits[i + diff];

        diff = (int) ((lo - that.loBound) / USIZE);
        for(int i = res.bits.length; --i >= 0; )
            res.bits[i] &= that.bits[i + diff];

        return res;
    }

    /**
     * Tests whether range [lowBound, highBound] has a non-empty intersection
     * with this set.
     */
    public boolean intersectsRange(int lowBound, int highBound)
    {
        int/*LId*/ first = getNext(lowBound);
        return first >= lowBound && first <= highBound; 
    }

    public boolean equals(Object obj)
    {
        if(!(obj instanceof IntSet))
            return false;
        IntSet oset = (IntSet) obj;
        for(int/*LId*/ e1 = -1, e2 = -1; ; ) {
            e1 = getNext(e1 + 1);
            e2 = oset.getNext(e2 + 1);
            if(e1 < 0)
                return e2 < 0;
            if(e1 != e2) {
                
                return false;
            }
        }
    }

    public void save(ByteOutput output)
        throws IOException
    {
        output.putVint/*LId*/(loBound);
        output.putVint/*LId*/(hiBound - loBound);
        if (bits != null)
            for (int i = 0, asize = bits.length; i < asize; i++)
                output.putInt(bits[i]);
    }

    @Override
    public void serialize(ByteOutput output)
        throws IOException
    {
        output.putByte(HEADER);
        save(output);
    }

    public void load(ByteInput input)
        throws IOException
    {
        loBound = input.getVint();
        int span = input.getVint();
        hiBound = loBound + span;
        bits = new int[span / USIZE];
        for (int i = 0, asize = bits.length; i < asize; i++)
            bits[i] = input.getInt();
    }

    public static void skip(ByteInput input)
        throws IOException
    {
        input.getVint();
        int ispan = input.getVint() / USIZE;
        for (int i = 0; i < ispan; i++)
            input.getInt();
    }

    public String toString()
    {
        return show(200);
    }

    private void init(int/*LId*/ low, int/*LId*/ hi)
    {
        this.loBound = low;
        this.hiBound = hi;
        int/*LId*/ span = hi - low;
        if(span > 0)
            bits = new int[(int) (span / USIZE)];
    }

    // Extends the storage to given bounds, keeping the elements
    // low and hi must be multiples of USIZE
    private void extend(int/*LId*/ low, int/*LId*/ hi)
    {
        if(low >= this.loBound && hi <= hiBound)
            return; // OK

        if(hiBound == loBound) { // empty
            init(low, hi);
            return;
        }

        int oldUnits[] = bits;
        int/*LId*/ oldLow = loBound;

        int/*LId*/ newLow = Math.min(loBound, low);
        int/*LId*/ newHi = Math.max(hiBound, hi);

        // optim strategy: add 1/8 to the necessary size, on the proper side
        int addedUnits = (newHi - newLow) / USIZE / 8;
        if (newHi > hiBound) {
            newHi += addedUnits * USIZE;
        }
        else if (newLow < loBound) {
            newLow -= addedUnits * USIZE;
            if(newLow < 0) {
                newLow = 0;
            }
        }
//        int/*LId*/ more = ((newHi - newLow) / 16 / USIZE) * USIZE;
//        newLow -= more;
//        if(newLow < 0) {
//            newLow = 0;
//        }
//        newHi += more;
        

        init(newLow, newHi);

        // copy old units to the right place:
        System.arraycopy(oldUnits, 0, bits, (int) ((oldLow - loBound) / USIZE),
                         oldUnits.length);
    }

    @Override
    public void dump(String message)
    throws IOException
    {
        System.err.println(message + " ArrayIntSet size=" + size());
    }

    // simulate CoreIntSet
    public long simulate()
    {
        int unit = -1, nbUnit = 0;
        for(int i = -1; (i = getNext(i + 1)) > 0; ) {
            int u = (i >> 11);
            if (u != unit) {
                ++ nbUnit;
                unit = u;
            }
        }            
        return nbUnit;
        //return 24 + 256 * nbUnit + 4 * unit / 2048;
    }

    public void digest(CRC32 crc)
    {
         // TODO
    }
}
