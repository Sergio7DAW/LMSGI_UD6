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
 * Iterator returned by functions searching Documents or Collections inside a
 * Library.
 * <p>Typically such an iterator is used like this:
 * <pre>
 *  // obtain an iterator: (could be also through queryProperties())
 * LibraryMemberIterator iter = collection.getChildren();
 *  // iterate on members:
 * while(iter.moveToNextMember()) {
 *     LibraryMember current = iter.getCurrentMember();
 *     // do something with library member...
 * }
 * </pre>
 */
public interface LibraryMemberIterator
{
    /**
     * Attempts to move to the next member. If true is returned, this member
     * is available through {@link #getCurrentMember()}.
     * @return true if a next member is found. 
     */
    boolean moveToNextMember();

    /**
     * Returns the current item. If moveToNextMember() has not been called or
     * has returned false, the result is undefined.
     * @return a Library member if called after a moveToNextMember returning true
     */
    LibraryMember getCurrentMember();

    /**
     * Returns a clone of the iterator in its initial state.
     * @return a new copy of this object, positioned before first item
     */
    LibraryMemberIterator reborn();
}
