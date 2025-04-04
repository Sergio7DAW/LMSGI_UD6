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
 * Models a user of a Library, for use with AccessControl.
 * <p>
 * Associated with a session ({@link Library}) when it is opened.
 * <p>
 * Attention: the <i>authentication</i> of the User is not taken in charge by
 * {@link AccessControl}. An application using this API has to perform the
 * authentication and <i>then</i> to create an implementation of User.
 */
public interface User
{
    /**
     * Returns the name of the User.
     * @return a String representing a unique name for the user
     */
    String getName();
}
