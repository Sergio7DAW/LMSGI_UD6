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
 * Filter for functions searching Documents or Collections inside a Library.
 */
public interface LibraryMemberFilter
{
    /**
     * The implemented method should return true to accept the member.
     * @param member a library member to accept or to reject by the filter
     * @return true if the member item is accepted.
     */
    boolean accept(LibraryMember member);
}
