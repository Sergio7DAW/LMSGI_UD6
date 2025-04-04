/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.api;

import java.util.EventListener;

/**
 * Listens to update operations on a XML Library.
 * <p>
 * A LibraryMemberObserver can be regarded as a pre-commit trigger whose actions
 * are part of the current transaction.
 * <p>
 * See {@link PostCommitTrigger} for actions executed <em>after</em> a commit.
 * <p>
 * A LibraryMemberObserver can be used for:
 * <ul>
 * <li>Implementing application-specific logging.
 * <li>Specifically, it can be used to automatically add Properties on created
 * objects. For example:
 * <ul>
 * <li>To manage custom indexes through metadata properties computed from the
 * contents of the document.
 * <li>in relation with an {@link AccessControl} implementation. For example to
 * define the user name and the access-rights. This also allows implementing
 * sophisticated access control based on the contents of a Document or Collection.
 * </ul>
 * </ul>
 * <p>
 * Notes:
 * <ul>
 * <li>Events are generated as soon as the corresponding action is performed is a
 * session (Library), and in the same thread. Therefore any modification action
 * performed by a LibraryObserver will also be part of the transaction in which
 * the initial action happened (and therefore cancelled if the transaction is
 * rolled back).
 * <li>A LibraryMemberObserver can only modify metadata properties; the delete,
 * copy and rename operations od Documents or Collections are ineffective.
 * <li>No AccessControl is active for operations called from this observer. Only
 * trusted code must be used in a LibraryObserver.
 * <li>a LibraryObserver is attached to a LibraryManager, not to a specific
 * session (Library). Due to isolation between sessions, a LibraryObserver must be
 * prepared to see different instances of the same Document or Collection (with
 * different contents or properties) in different transactions.
 * <li>In order to prevent recursive looping, no further event is generated when
 * Library Members are modified by a method of LibraryMemberObserver.
 * </ul>
 * @see PostCommitTrigger
 */
public interface LibraryMemberObserver
    extends EventListener
{
    /**
     * Called just after a library member is created or overwritten (for a
     * Document). This can be used for example to add automatically computed
     * properties to the object (document or collection).
     * 
     * @param member a library member just created
     * @exception DataModelException if thrown, the exception is reported to
     * the application, but this does not cancel the creation of the member
     */
    void memberCreated(LibraryMember member);

    /**
     * Called just after the library member is renamed.
     * 
     * @param member the library member renamed. Renaming a collection
     * generates one event for the collection itself, not for contained
     * members.
     * @param oldPath previous path of the member
     */
    void memberRenamed(LibraryMember member, String oldPath);

    /**
     * Called just before the library member is deleted.
     * 
     * @param member library member to be deleted
     */
    void memberDeleted(LibraryMember member);

    /**
     * Called just after a property of a member is modified or removed. This
     * can be used for example to update automatically computed properties.
     * <p>Note: this method is <i>not</i> called for modifications performed
     * by a LibraryMemberObserver, to avoid recursive looping.
     * 
     * @param member the library member whose property changes
     * @param propertyName name of the modified or removed property
     */
    void propertyModified(LibraryMember member, String propertyName);
}
