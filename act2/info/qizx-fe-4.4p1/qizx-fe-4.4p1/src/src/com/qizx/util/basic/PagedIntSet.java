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

/**
 * A set of integer values, implemented as a list of pages of bounded size.
 * <p>Slighly slower than ArrayIntSet for member test, 
 * but for sparse sets,  it uses less memory and is faster for iteration.
 * <p>NOTE: should be a superclass of PagedDocSet
 * <p>TODO optimize sets with 1 element: singleElement field
 */
public class PagedIntSet extends IntSet
{
    public static final int HEADER = 0x94;
    public static final int PAGE_SHIFT = 15;    // 4 Kb memory/page

    /**
     * Bit pages: for each page, pageSet contains ids of this page.
     */
    private ArrayIntSet[] pageSet;
    private int pageShift = PAGE_SHIFT;
    ///private int singleElement; TODO

    public PagedIntSet()
    {
        init(4);
    }

    public PagedIntSet(int/*SId*/ element)
    {
        init(4);
        add(element);
    }

    public PagedIntSet(int/*SId*/ firstElement, int/*SId*/ lastELement)
    {
        init(4);
        add(firstElement, lastELement);
    }

    /**
     * Redefines page size. Clears all content.
     */
    public void setPageSize(int size)
    {
        pageShift = 1;
        while( (1 << pageShift) < size)
            ++ pageShift;
        init(4);
    }
    
    public int getPageSize()
    {
        return 1 << pageShift;
    }
    
    private void init(int pageCount)
    {
        this.pageSet = new ArrayIntSet[pageCount];
    }
    
    public int size()
    {        
        int size = 0;
        for (int p = 0, asize = pageSet.length; p < asize; p++) {
            if(pageSet[p] != null)
                size += pageSet[p].size();
        }
        return size;
    }

    @Override
    public String toString()
    {
        return show(200);
    }

    public int getPageCount()
    {
        return pageSet.length;
    }

    public int getActualPageCount()
    {
        int pc = 0;
        for (int p = 0; p < pageSet.length; p++) {
            if(pageSet[p] != null)
                 pc ++;
        }
        return pc;
    }

    public IntSet getPageSet(int page)
    {
        return (page < 0 || page >= pageSet.length)? null : pageSet[page];
    }

    @Override
    public long rank(int/*SId*/ element)
    {        
        int rank = 0;
        int itemPage = element >> pageShift;
        if (itemPage >= pageSet.length)
            return -1;
        for (int p = 0; p < itemPage; p++) {
            acquirePage(p);
            if(pageSet[p] != null)
                 rank += pageSet[p].size();
        }

        acquirePage(itemPage);
        ArrayIntSet pset = pageSet[itemPage];
        if(pset == null)
            return rank;
        return rank + pset.rank(element);
    }

    public boolean test(int/*SId*/ element)
    {
        if (element < 0)
            return false;
        
        int page = element >> pageShift;
        if (page >= pageSet.length)
            return false;
        acquirePage(page);
        ArrayIntSet pset = pageSet[page];
        return pset != null && pset.test(element);
    }

    @Override
    public int getNext(int/*SId*/ element)
    {
        int page = (element < 0)? 0 : (element >> pageShift);
        for(; page < pageSet.length; page++) {
            if(pageSet[page] == null)
                continue;
            int next = pageSet[page].getNext(element);
            if(next >= 0)
                return next;
        }
        return -1;
    }

    @Override
    public int getNextNotIn(int/*SId*/ id)
    {
        throw new RuntimeException("unimplemented");
    }

    @Override
    public int getUpperBound()
    {
        return pageSet.length << pageShift; // TODO better
    }

    public IntSet copy() // deep copy
    {
        PagedIntSet copy = new PagedIntSet();

        for (int p = 0; p < pageSet.length; p++) {
            ArrayIntSet set = pageSet[p];
            copy.modifyPage(p);
            copy.pageSet[p] = set == null? null : (ArrayIntSet) set.copy();
        }
        return copy;
    }
    
    public PagedIntSet shallowCopy()
    {
        PagedIntSet copy = new PagedIntSet(pageSet.length);
        for (int p = 0; p < pageSet.length; p++) {
            copy.modifyPage(p);
            copy.pageSet[p] = pageSet[p];
        }
        return copy;
    }

    public boolean intersectsRange(int/*SId*/ lowBound, int/*SId*/ highBound)
    {
        int page1 = lowBound >> pageShift;
        int page2 = highBound >> pageShift;
        for (int p = page1; p <= page2; p++) {
            acquirePage(p);
            if (pageSet[p] != null && pageSet[p].intersectsRange(lowBound, highBound))
                return true;
        }
        return false;
    }

    public void clear()
    {
        for (int p = 0; p < pageSet.length; p++) {
            pageSet[p] = null;
        }
    }

    public void add(int/*SId*/ element)
    {
        if (element < 0)
            return;
        int page = modifyPage(element >> pageShift);
        pageSet[page].add(element);
    }

    public void add(int/*SId*/ first, int/*SId*/ last)
    {
        // TODO OPTIM
        for (int i = first; i <= last; ++i) {
            add(i);
        }
    }

    @Override
    public void add(IntSet set)
    {
        // OPTIM
        for (int i = -1; (i = set.getNext(i + 1)) >= 0;) {
            add(i);
        }
    }
    
    public void remove(int/*SId*/ element)
    {
        if (element < 0)
            return;
        int page = acquirePage(element >> pageShift);
        if (pageSet[page] != null)
            pageSet[page].remove(element);
    }

    public void remove(int/*SId*/ first, int/*SId*/ last)
    {
        // OPTIM
        for (int i = first; i <= last; ++i) {
            remove(i);
        }
    }

    @Override
    public void remove(IntSet set)
    {
        // OPTIM
        for (int i = -1; (i = set.getNext(i + 1)) >= 0;) {
            remove(i);
        }
    }

    // ---- dumb conversions:
    
    public static PagedIntSet fromArraySet(ArrayIntSet set)
    {
        PagedIntSet dst = new PagedIntSet();
        
        for(int n = set.getNext(-1); n >= 0; n = set.getNext(n + 1))
            dst.add(n);
        return dst;
    }
    
    public ArrayIntSet toArraySet()
    {
        ArrayIntSet dst = new ArrayIntSet();
        
        for(int n = getNext(-1); n >= 0; n = getNext(n + 1))
            dst.add(n);
        return dst;
    }
    
    /*
     * Loading is only reading the blobIds where each page is stored.
     * Pages are then loaded on demand.
     */
    @Override
    public void load(ByteInput input)
        throws IOException
    {
        if (HEADER != input.getByte())
            throw new IOException("invalid header for PagedIntSet");
        int pageSize = input.getVint();
        // support variable pageSize:
        if (Util.countBitsInMask(pageSize) != 1)
            throw new IOException("invalid page size " + pageSize);
        setPageSize(pageSize);
        
        for(;;) {
            int header = input.getByte();
            if (header == 0)
                break;
            if (header != ArrayIntSet.HEADER)
                throw new IOException("invalid page header for PagedIntSet");
            ArrayIntSet pset = new ArrayIntSet();
            pset.load(input);
            int first = pset.getNext(0);
            if (first >= 0) {
                int page = first >> pageShift;
                modifyPage(page);
                pageSet[page] = pset;
            }
        }
    }

    @Override
    public void save(ByteOutput output)
        throws IOException
    {
        output.putByte(HEADER);
        // storing page size can be useful if we want to change it!:
        output.putVint(1 << pageShift);
        for (int page = 0; page < pageSet.length; page++) {
            ArrayIntSet pset = pageSet[page];
            if (pset != null && pset.getNext(0) >= 0) {
                pset.optimize();
                pset.serialize(output);
            }
            else
                pageSet[page] = null; // recover memory
        }
        output.putByte(0);
    }

    /*
     * dummy in this implementation, meant for subclassing in PagedDocSet
     * If necessary, put the page in mode 'Sync'
     */
    protected int acquirePage(int page)
    {
        return page;
    }

    protected int modifyPage(int page)
    {
        // make sure it is created or loaded:
        if (page >= pageSet.length) {
            int newSize = Math.max(page + 1, pageSet.length + 4);
            
            ArrayIntSet[] oldsets = pageSet;
            pageSet = new ArrayIntSet[newSize];
            System.arraycopy(oldsets, 0, pageSet, 0, oldsets.length);            
        }
        acquirePage(page);
        if(pageSet[page] == null)
            pageSet[page] = new ArrayIntSet();
        return page;
    }

    @Override
    public void dump(String message)
        throws IOException
    {
        System.err.println(message + ": PagedSet " + this + " size=" + size());
        for(int p = 0; p < pageSet.length; p++) {
            System.err.println("   " + p + " size=" + (pageSet[p] == null? -1 : pageSet[p].size()));
        }
    }
}
