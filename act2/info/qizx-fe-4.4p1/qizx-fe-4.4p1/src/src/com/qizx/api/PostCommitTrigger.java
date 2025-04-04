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
 * Post-commit action.
 * <p>
 * A PostCommitTrigger is registered on a database through {@link LibraryManager}
 * and executed each time a {@link Library#commit() } operation occurs.
 * <p>
 * Filtering the events
 */
public interface PostCommitTrigger
{
    /**
     * Called after a successful commit.
     * <p>
     * @param event an interface providing access to modified Library objects,
     * including deleted objects.
     */
    void commit(CommitEvent event);

    /**
     * Provides access to changes operated by a commit.
     * <p>Affected Documents and Collections are classified in three exclusive groups:<ul>
     * <li>Updated: objects which have been modified, but neither created nor deleted.
     * <li>Deleted: objects which have been deleted by the related commit.
     * <li>Created: new objects, created by the related commit.
     * </ul>
     * A Collection is said updated if at any level a Document or sub-collection
     * has been created, deleted or updated.
     */
    public interface CommitEvent
    {
        /**
         * Returns an XML Library session reflecting the state of the database
         * after the commit.
         * <p>This session has the same User as the Library session which
         * performed the commit.
         */
        Library getLibrary();

        /**
         * Returns the precise time when the commit was performed.
         * @return a time-stamp in milliseconds as returned by {@link System#currentTimeMillis()}.
         */
        long getCommitTime();
        
        /**
         * Returns the number of documents created in the transaction.
         */
        int createdDocumentCount();

        /**
         * Returns an iterator on documents created in the transaction.
         */
        LibraryMemberIterator createdDocuments()
            throws DataModelException;
        
        /**
         * Returns the Document descriptor corresponding to this path <b>if and
         * only if</b> this document has been created in the related commit.
         * @param path path of a document within the Library
         * @return a Document descriptor, or null if the document has not been 
         * created in the related commit (i.e does not exist)
         */
        Document getCreatedDocument(String path)
            throws DataModelException;

        /**
         * Returns the number of documents whose contents have changed in the
         * transaction. Does not include created or deleted documents.
         */
        int updatedDocumentCount();

        /**
         * Returns an iterator on documents whose contents have changed in the
         * transaction. Does not include created or deleted documents.
         */
        LibraryMemberIterator updatedDocuments()
            throws DataModelException;
        
        /**
         * Returns the Document descriptor corresponding to this path <b>if and
         * only if</b> this document has been updated in the related commit.
         * @param path path of a document within the Library
         * @return a Document descriptor, or null if the document has not been 
         * created in the related commit (i.e does not exist)
         */
        Document getUpdatedDocument(String path)
            throws DataModelException;

        /**
         * Returns the number of documents deleted in the transaction.
         */
        int deletedDocumentCount();

        /**
         * Returns an iterator on documents deleted in the transaction.
         */
        LibraryMemberIterator deletedDocuments()
            throws DataModelException;
        
        /**
         * Returns the Document descriptor corresponding to this path <b>if and
         * only if</b> this document has been deleted in the related commit.
         * <p>Notice that this Document is a "zombie" document that cannot
         * be found by queries. Only the metadata properties are accessible:
         * attempt to get the XML contents will raise an error.
         * @param path path of a document within the Library
         * @return a Document descriptor, or null if the document has not been 
         * deleted in the related commit (i.e does not exist)
         */
        Document getDeletedDocument(String path)
            throws DataModelException;

        
        /**
         * Returns the number of collections whose contents have changed in the
         * transaction.
         * <p>
         * 'Changed collection contents' means that a document or a collection,
         * nested at any level inside this collection, has been created, deleted
         * or updated.
         * <p><b>Caution:</b> the root collection / is not counted because
         * it is always
         */
        int updatedCollectionCount();
        
        /**
         * Returns an iterator on collections whose contents have changed in the
         * transaction.
         * <p>
         * 'Changed collection contents' means that a document or a collection,
         * nested at any level inside this collection, has been created, deleted
         * or updated.
         * @throws DataModelException 
         */
        LibraryMemberIterator updatedCollections()
            throws DataModelException;
        
        /**
         * Returns a non-null Collection descriptor if the collection with this
         * path has been updated (i.e anything nested within the collection has
         * changed).
         * @param path path of a collection within the Library
         * @return a non-null Collection descriptor if the collection contents 
         * have changed, null otherwise (even if the collection exists)
         */
        Collection getUpdatedCollection(String path)
            throws DataModelException;

        /**
         * Returns an iterator on collections and documents whose metadata
         * properties have changed.
         */
/////        LibraryMemberIterator updatedProperties();
        
        /**
         * Returns true if the metadata properties of the Collection or the
         * Document with this path have been updated.
         * @param path path of a member of the Library
         */
/////        boolean hasUpdatedProperties(String path);
    }
}
