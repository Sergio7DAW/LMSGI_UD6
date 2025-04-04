/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.api;

/**
 * Type of an Item sequence.
 */
public interface SequenceType
{
    /**
     * Occurrence indicator corresponding with one optional item.
     */
    int OCC_ZERO_OR_ONE = 0;

    /**
     * Occurrence indicator corresponding with one required item.
     */
    int OCC_EXACTLY_ONE = 1;

    /**
     * Occurrence indicator corresponding with '*', any number of items.
     */
    int OCC_ZERO_OR_MORE = 2;

    /**
     * Occurrence indicator corresponding with '+', at least one item.
     */
    int OCC_ONE_OR_MORE = 3;

    /**
     * Returns the base Item Type of the sequence type.
     * @return the base Item Type of the Sequence type.
     */
    public ItemType getItemType();

    /**
     * Returns the occurrence indicator associated with this type
     * (OCC_ZERO_OR_ONE, OCC_EXACTLY_ONE, OCC_ONE_OR_MORE, OCC_ZERO_OR_MORE).
     * @return an int representing the occurrence indicator value
     */
    public int getOccurrence();
}
