/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.util;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Selects the N greatest items in a list of Items. Items are added iteratively.
 * <p>
 * Implementation is faster than a QuickSort followed by a selection of the best.
 * It is tends to be linear on large lists.
 */
public class QuickBest
{
    static final int EXTRA = 4;
    
    protected int bestSize;
    protected int count;
    protected Item[] items;
    protected Item worst;
    protected boolean dirty;
    
    static private Comparator reverse = new Comparator() {
        public int compare(Object o1, Object o2)
        {
            return ((Item) o2).compareTo(o1);
        }
    };
    
    /**
     * Resets the selection, specifying the desired number of best items.
     * @param bestSize maximum number of best items.
     */
    public void reset(int bestSize)
    {
        this.bestSize = bestSize;
        if(items == null || bestSize * (1 + EXTRA) > items.length) {
            items = new Item[(1 + EXTRA) * bestSize];
        }
        count = 0;
        worst = null;
        dirty = true;
    }

    /**
     * Adds an item to the best list. An item is deemed better if greater using
     * the compareTo method.
     * @param probe a temporary, reused representation of the item. When the
     * item is deemed a potential best item, it is copied using its method
     *  {@link Item#replicate()}.
     */
    public void add(Item probe)
    {
        if(worst != null && probe.compareTo(worst) < 0) {
            
            return; // get lost you loser
        }
        if(count >= items.length) {
            select(); // makes room
            // retest
            if(probe.compareTo(worst) < 0)
                return;
        }
        items[count++] = probe.replicate();
        
        dirty = true;
    }
    
    /**
     * Returns the rank-th best item selected, or null if rank is larger than
     * the 
     */
    public Item getBest(int rank)
    {
        if(dirty)
            select();
        return rank < bestSize? items[rank] : null;
    }
    
    /**
     * When buffer is full, sort items and select the N best.
     * Also compute worst, which helps to quickly discard bad items.
     */
    private void select()
    {
        Arrays.sort(items, 0, count, reverse);  // Arrays.sort sorts in ascending order
        if(count > bestSize)
            count = bestSize;
        worst = items[bestSize - 1];
        
        dirty = false;
    }

    /**
     * An item to be selected. Must be Comparable.
     */
    public interface Item extends Comparable
    {
        /**
         * Creates a copy of this item. Used by method add() to remember an item
         * which is a potential winner.
         */
        Item replicate();
    }
}
