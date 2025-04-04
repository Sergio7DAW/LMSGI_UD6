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
 * Abstract access control to the members of a Library. Checks whether a
 * {@link User} may read or modify contents or properties of Documents and
 * Collections.
 * <p>
 * An object implementing this interface can be set when opening a Library
 * session.
 * <p>
 * Attention: AccessControl is not in charge of <i>authenticating</i> the User.
 * This is the responsibility of the application.
 * <ul>
 * <li>Authenticating means creating a {@link User} based on verified credentials.
 * <li>Access Control means checking if an authenticated User may or may not
 * access a particular object in the Library.
 * </ul>
 * <p>
 * <b>Implementation constraints:</b>
 * <ul>
 * <li>It is possible to create an instance of AccessControl for each Library
 * session, or for each managed XML Library (shared by sessions), or even a single
 * instance for a Library Manager. This is entirely dependent on the way the
 * AccessControl works and on its implementation. An implementation may therefore
 * be required to be thread-safe.
 * <li>A concrete AccessControl should attempt to implement each check method in
 * the fastest possible way, because each method in the API can involve one or
 * several access checks. Caching is recommended.
 * </ul>
 */
public interface AccessControl
{
    
    /**
     * Checks if a User has the permission to get the contents of a Library
     * member.
     * <ul>
     * <li>For a Document: permission to read and query the XML contents <li>For
     * a Collection: permission to list contained members and to perform
     * queries.
     * </ul>
     * @param user
     *        an implementation of a User, suitable for this AccessControl
     * @param member
     *        Library object to check for permission
     * @return true if the permission is granted.
     */
    boolean mayReadContent(User user, LibraryMember member);

    /**
     * Checks if a User has the permission to modify the contents of a Library
     * Object. <ul>
     * <li>For a Document: permission to replace the XML contents
     * <li>For a Collection: permission to add or suppress contained members.
     * </ul>
     * @param user an implementation of a User, suitable for this AccessControl
     * @param member Library object to check for permission
     * @return true if the permission is granted.
     */
    boolean mayChangeContent(User user, LibraryMember member);

    /**
     * Checks if a User has the permission to get the value of a property of a
     * Library member.
     * @param user an implementation of a User, suitable for this AccessControl
     * @param member Library Object to check for permission
     * @param propertyName name of the property to get. Attention: it may be
     *        null, meaning 'any property' (for example when controlling for
     *        the method getPropertyNames() of LibraryMember).
     * @return true if the permission is granted.
     */
    boolean mayReadProperty(User user, LibraryMember member,
                            String propertyName);

    /**
     * Checks if a User has the permission to modify the value of a property of
     * a Library member.
     * 
     * @param user
     *        an implementation of a User, suitable for this AccessControl
     * @param member
     *        Library Object to check for permission
     * @param propertyName
     *        name of the property to set. Never null
     * @return true if the permission is granted.
     */
    boolean mayChangeProperty(User user, LibraryMember member,
                              String propertyName);
}
