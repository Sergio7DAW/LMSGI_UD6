/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.api;

import com.qizx.api.util.DOMToPushStream;
import com.qizx.api.util.PushStreamResult;
import com.qizx.api.util.SAXToPushStream;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

/**
 * Work session on a XML Library.
 * <p>
 * A Library is both:
 * <ul>
 * <li>The equivalent of a database for a DBMS or the equivalent of a volume
 * in a file system. It contains a hierarchy of {@link Collection}s and XML
 * {@link Document}s. 
 * <li>A <em>connection</em> to this database, supporting XQuery queries and
 * updates with isolation and transactions. About isolation see
 * {@link #refresh()}. About transactions, see also {@link #commit},
 * {@link #rollback}, {@link #lock lock()}.
 * <p>
 * A Library is also a factory for compiling XML Query {@link Expression}s,
 * creating {@link Item}s and obtaining XQuery {@link ItemType}s and
 * {@link QName}s. The XML Query context associated with a Library can be used
 * as a predefined context for compiled Expressions.
 * <p>
 * As a connection, a Library belongs to a given {@link User} and therefore is
 * limited in the operations it can perform by the privileges of this user (see
 * {@link AccessControl}). Access control is enabled at the level of the
 * {@link LibraryManager}.
 * </ul>
 * <p>
 * This class is not thread-safe. As a connection, it is supposed to be used by
 * only one thread at a time.
 * <p id="std_exc">
 * <b>Common Exception causes</b>: the Library is closed (LibraryException);
 * Access is denied ({@link AccessControlException}); a library member does
 * not exist (LibraryException with code
 * {@link LibraryException#MEMBER_NOT_FOUND}).
 */
public interface Library
    extends XQuerySession
{
    /**
     * Returns the name of the library. This is a simple name relative to the
     * Library Group, not a directory path.
     * @return the name of the library
     */
    String getName();

    /**
     * Returns the user associated with this Library session.
     * @return the User specified when opening the session.
     * If Access Control is not enabled, this value can be null.
     */
    User getUser();

    /**
     * Returns the Access Control associated with this Library.
     * @return an instance of the AccessControl associated with the XML Library.
     * If Access Control is not enabled, this value can be null.
     * @since 3.2
     */
    AccessControl getAccessControl();

    /**
     * Access to the default XQuery Context. This context is inherited
     * by expressions compiled from this session.
     * @return the XQuery context used as a basis for compiled expressions.
     */
    XQueryContext getContext();

    /**
     * Returns the root collection of the Library. The root collection (whose
     * path is "/") is created initially and cannot be deleted.
     * <p>
     * This is a convenience method, equivalent to
     * <code>getCollection("/")</code>.
     * @return the handle of the root Collection
     * @exception DataModelException <a href="#std_exc">common causes</a>
     */
    Collection getRootCollection()
        throws DataModelException;

    /**
     * Gets a Collection by its path inside the Library.
     * @param absolutePath path of the collection inside the Library.
     * @return the collection, or null if no such collection exists.
     * @exception DataModelException <a href="#std_exc">common causes</a>
     */
    Collection getCollection(String absolutePath)
        throws DataModelException;

    /**
     * Returns the specified member (Document or Collection) or
     * <code>null</code> if such member does not exist.
     * @param absolutePath path of the member inside the Library.
     * @return the library member, or null if no such member exists.
     * @exception DataModelException <a href="#std_exc">common causes</a>
     */
    LibraryMember getMember(String absolutePath)
        throws DataModelException;

    /**
     * Gets a Document by its path inside the Library.
     * @param absolutePath path of the document inside the Library.
     * @return the document, or null if no such document exists.
     * @exception DataModelException  <a href="#std_exc">common causes</a>
     */
    Document getDocument(String absolutePath)
        throws DataModelException;

    /**
     * Gets a non-XML Document by its path inside the Library.
     * @param absolutePath path of the document inside the Library.
     * @return the document, or null if no such document exists.
     * @exception DataModelException  <a href="#std_exc">common causes</a>
     */
    NonXMLDocument getNonXMLDocument(String absolutePath)
        throws DataModelException;

    /**
     * Creates an executable Expression by compiling a XQuery script.
     * @param xquery a string containing a XQuery script.
     * @return an executable Expression that can be used for several
     *         executions. Before an execution, the expression can be prepared
     *         by binding values with variables
     * @exception CompilationException thrown after compilation if parsing or 
     * static analysis errors are detected. A CompilationException bears a list
     * of {@link Message}s.
     */
    Expression compileExpression(String xquery)
        throws CompilationException;

    /**
     * Creates a new Collection. Also creates the parent collections if needed.
     * @param absolutePath the path of the collection, starting from the root
     *        collection of the Library.
     * @return a descriptor of the collection.
     * @exception DataModelException <a href="#std_exc">common causes</a>
     */
    Collection createCollection(String absolutePath)
        throws DataModelException;

    /**
     * Imports a Document into the Library by parsing a SAX InputSource.
     * <p>This method simply calls {@link #importDocument(String, InputSource, XMLReader)},
     *  passing a default parser (namespace aware).
     * <ul>
     * <li>If a document with the same path already exists, it is replaced by
     * the new contents; however existing user properties are kept (system
     * properties are updated).
     * <li>If the enclosing Collection does not exist it is created
     * automatically.
     * </ul>
     * @param documentPath path in the Library assigned to the document.
     * @param source a SAX InputSource
     * @return a handle to the newly created document. This object can be used
     *         to set user-defined properties
     * @throws DataModelException XML parsing error (wraps a SAX exception);
     *  <a href="#std_exc">common causes</a>
     */
    Document importDocument(String documentPath, InputSource source)
        throws DataModelException;

    /**
     * Imports a Document into the Library by parsing a SAX InputSource, using
     * a custom XML parser.
     * <ul>
     * <li>If a document with the same path already exists, it is replaced by
     * the new contents; however existing user properties are kept (system
     * properties are updated).
     * <li>If the enclosing Collection does not exist it is created
     * automatically.
     * </ul>
     * @param documentPath path in the Library assigned to the document.
     * @param source a SAX InputSource
     * @param xmlReader a SAX XMLReader (parser) that can be setup before use.
     * @return a handle to the newly created document. This object can be used
     *         to set user-defined properties
     * @throws DataModelException XML parsing error (wraps a SAX exception);
     *  <a href="#std_exc">common causes</a>
     */
    Document importDocument(String documentPath, InputSource source,
                            XMLReader xmlReader)
        throws DataModelException;

    /**
     * Imports a Document into the Library by using an implementation of 
     * ImportMethod. The interface ImportMethod defines conversion filters from
     * any structured or semi-structured
     * <ul>
     * <li>If a document with the same path already exists, it is replaced by the
     * new contents; however existing user properties are kept (system properties
     * are updated).
     * <li>If the enclosing Collection does not exist it is created automatically.
     * </ul>
     * @param documentPath path in the Library assigned to the document.
     * @param importer
     * @return a handle to the newly created document. This object can be used to
     *         set user-defined properties
     * @throws DataModelException XML parsing error (wraps a SAX exception); <a
     *         href="#std_exc">common causes</a>
     */
    Document importDocument(String documentPath, ContentImporter importer)
        throws DataModelException;

    /**
     * Imports a Document into the Library by parsing XML text from an URL.
     * <ul>
     * <li>If a document with the same path already exists, it is replaced by
     * the new contents; however existing user properties are kept (system
     * properties are updated).
     * <li>If the enclosing Collection does not exist it is created
     * automatically.
     * </ul>
     * @param documentPath path in the Library assigned to the document.
     * @param url location of the document to import, must be a valid URL
     *        supported by the run-time environment. In particular, special
     *        characters should be correctly escaped, even if the URL actually
     *        points to a file.
     * @return a handle to the newly created document. This object can be used
     *         to set user-defined properties
     * @throws DataModelException XML parsing error (wraps a SAX exception);
     *  <a href="#std_exc">common causes</a>
     */
    Document importDocument(String documentPath, URL url)
        throws DataModelException;

    /**
     * Imports a Document into the Library by parsing XML text from a File.
     * <ul>
     * <li>If a document with the same path already exists, it is replaced by
     * the new contents; however existing user properties are kept (system
     * properties are updated).
     * <li>If the enclosing Collection does not exist it is created
     * automatically.
     * </ul>
     * @param documentPath path in the Library assigned to the document.
     * @param file location of the document to import, must contain a
     *        well-formed XML document.
     * @return a handle to the newly created document. This object can be used
     *         to set user-defined properties
     * @throws DataModelException XML parsing error (wraps a SAX exception);
     *  <a href="#std_exc">common causes</a>
     */
    Document importDocument(String documentPath, File file)
        throws DataModelException;

    /**
     * Imports a Document into the Library by parsing a string representing a
     * XML fragment.
     * <ul>
     * <li>If a document with the same path already exists, it is replaced by
     * the new contents; however existing user properties are kept (system
     * properties are updated).
     * <li>If the enclosing Collection does not exist it is created
     * automatically.
     * </ul>
     * @param documentPath path in the Library assigned to the document.
     * @param data XML fragment as a String. It must represent a well-formed
     *        XML document.
     * @return a handle to the newly created document. This object can be used
     *         to set user-defined properties
     * @throws DataModelException XML parsing error (wraps a SAX exception);
     *  <a href="#std_exc">common causes</a>
     */
    Document importDocument(String documentPath, String data)
        throws DataModelException;

    /**
     * Document import primitive: returns a push-style interface allowing
     * storing a document from a source other than a parser.
     * <p>
     * This source can be for example a SAX2 event generator (through adapter
     * {@link SAXToPushStream}), a DOM document (through adapter
     * {@link DOMToPushStream}), a XSLT engine (through a {@link
     * PushStreamResult}).
     * @param documentPath path in the Library assigned to the document.
     * @return a push stream used to create the document content
     * @exception DataModelException <a href="#std_exc">common causes</a>
     * @see #endImportDocument 
     * @see #cancelImportDocument
     */
    XMLPushStream beginImportDocument(String documentPath)
        throws DataModelException;

    /**
     * Document import primitive: finishes a document import started with
     * beginImportDocument() and returns a Document descriptor. The document
     * descriptor can be used to get or set properties.
     * @return a handle to the newly created document. This object can be used
     *         to set user-defined properties
     * @exception DataModelException <a href="#std_exc">common causes</a>
     * @see #beginImportDocument beginImportDocument
     * @see #cancelImportDocument()
     */
    Document endImportDocument()
        throws DataModelException;

    /**
     * Cancels a document import initiated by beginImportDocument.
     * <p>
     * Can be used if for some reason a document has to be cancelled.
     * This method is called automatically when a beginImportDocument is not
     * followed by endImportDocument.
     * @throws DataModelException  <a href="#std_exc">common causes</a>
     */
    void cancelImportDocument() throws DataModelException;

    /**
     * Imports a non-XML document into the Library by reading from an InputStream.
     * <ul>
     * <li>It is recommended to use LibraryMember properties to store the mime-type
     * and the character encoding (if applicable), so that the contents can be
     * rendered without alteration.
     * <li>If a non-XML Document with the same path already exists, it is replaced
     * by the new contents; however existing user properties are kept (system
     * properties are updated). 
     * <li>Replacing an XML Document by a non-XML Document or
     * conversely is not allowed.
     * <li>If the enclosing Collection does not exist it is created automatically.
     * </ul>
     * @param path path in the Library assigned to the Blob.
     * @param compress if true, compress contents. This is recommended for
     *        text-like data and not recommended for already compressed data such
     *        as images.
     * @param source a binary InputStream that is read to get the data.
     * @return a handle to the newly created lobdocument. This object can be used
     *         to set user-defined properties, in particular the mime-type.
     * @throws DataModelException <a href="#std_exc">common causes</a>
     */
    NonXMLDocument importNonXMLDocument(String path, boolean compress,
                                        InputStream source)
        throws DataModelException;

    /**
     * Renames the specified member of the Library (Document or Collection).
     * <p>
     * @param srcPath path of the source member
     * @param dstPath destination path. This path should not point to an existing
     *        member, but its parent collection should exist
     * @return a handle to the renamed member
     * @throws DataModelException if the source member does not exist; if the
     *         destination member does exist; if its parent is not an existing
     *         collection; <a href="#std_exc">common causes</a>
     */
    LibraryMember renameMember(String srcPath, String dstPath)
        throws DataModelException;

    /**
     * Copies the specified member of the Library (Document or Collection).
     * <p>
     * If the specified member is a Collection, all its contained members -
     * sub-collections and documents - are recursively copied.
     * @param srcPath path of the source member
     * @param dstPath destination path for the copy. This path should not point
     *        to an existing member, but its parent collection should exist
     * @return a handle to the copied member
     * @throws DataModelException if the source member does not exist; if the
     *         destination member does exist; if its parent is not an existing
     *         collection; <a href="#std_exc">common causes</a>
     */
    LibraryMember copyMember(String srcPath, String dstPath)
        throws DataModelException;

    /**
     * Deletes the specified member of the Library.
     * <p>
     * If the specified member is a Collection, all its contained members -
     * sub-collections and documents - are recursively deleted.
     * @param path of a library member
     * @return <code>true</code> if specified member has been deleted;
     *         <code>false</code> otherwise (because such member does not
     *         exist)
     * @throws DataModelException <a href="#std_exc">common causes</a>
     */
    boolean deleteMember(String path)
        throws DataModelException;

    /**
     * Refreshes the view of the Library. 
     * <p>The view of the Library is stable as long as this method is not called
     * (or lock, commit, rollback), i.e. changes performed by other sessions
     * are not visible. 
     * <p>
     * Conversely, after calling this method, deletions performed by other
     * transactions become visible, so the programmer <em>should re-acquire or
     * check every library member used again (for example by using getDocument
     * or getCollection)</em>, otherwise exceptions could be thrown when using 
     * a deleted object.
     * <p>
     * This method may not be called if a transaction has begun. In that case
     * only commit() or rollback() may be used.
     * @exception DataModelException <a href="#std_exc">common causes</a>
     */
    void refresh()
        throws LibraryException;

    /**
     * Locks one or several Collections or Documents to initiate a safe
     * concurrent update.
     * <ul>
     * <li>This operation is optional: it is necessary only when a Library can
     * updated concurrently by several threads.
     * <li>The lock set on members is <em>advisory</em> that is, if another
     * thread updates one of these members without locking it, the result is
     * unpredictable.
     * <li>Members are unlocked automatically by commit() or rollback().
     * <li>An implicit refresh is performed by this method, to ensure that the
     * update is performed on the latest state of the Library. As a
     * consequence, Document nodes and Properties read before the call to this
     * method could become stale. So it is recommended to read or re-read this
     * data afterwards.
     * <li>No other call to lock() can be made until the end of the
     * transaction (commit or rollback). This prevents dead-locks and ensures
     * consistency of the Library state. If several documents or collections
     * need to be locked, they have to be passed in the first argument array.
     * <li>Members are locked atomically. If one of them is already locked,
     * and if the timeOut argument is strictly positive, the method will wait
     * until either all members are available, or the time-out is elapsed.
     * <li>If only one Collection or Document needs to be locked, the more
     * convenient methods {@link #lockCollection(String, int)} or
     *  {@link #lockDocument(String, int)} can be used instead.
     * </ul>
     * @param memberPaths a list of Documents or Collections. When a member is
     *        locked, another transaction cannot lock an enclosing (or
     *        ancestor) collection.
     *        <p>
     *        When a Collection is specified, then all enclosed documents and
     *        collections are also locked implicitly.
     * @param timeOutMillis if strictly positive, specifies a time in
     *        milliseconds to wait for when one of the Library members is
     *        already locked. If this time is elapsed and one member is still
     *        locked, the function returns false and no lock is set.
     *        <p>
     *        If equal to 0, the method will not wait. This can be used as a
     *        "trylock" function.
     *        <p>
     *        If negative, wait indefinitely (this is not recommended).
     * @return true if all members have been locked within the time imparted.
     * @throws DataModelException <a href="#std_exc">common causes</a>
     */
    boolean lock(String[] memberPaths, int timeOutMillis)
        throws DataModelException;

    /**
     * Locks a Collection to initiate a safe concurrent update. All the
     * contents of the collection (documents and sub-collections) are locked at
     * the same time.
     * <ul>
     * <li>This operation is optional: it is necessary only in applications
     * where a Collection can be updated concurrently by several threads.
     * <li>The lock set on the collection is <em>advisory</em> that is, if
     * another thread updates its contents without lock, then the result is
     * unpredictable.
     * <li>The collection is unlocked automatically by commit() or rollback().
     * <li>An implicit refresh is performed by this method, to ensure that the
     * update is performed on the latest state of the Library. In particular
     * some library objects could have been deleted. This is why the method
     * returns a new handle to the Collection: if a non-null value is returned,
     * it is guaranteed that it still exists and has been successfully locked.
     * <li>No other call to lock() can be made until the end of the
     * transaction (commit or rollback). If several objects need to be locked,
     * either lock a common ancestor Collection (in extreme cases, the root
     * collection), or use {@link #lock(String[], int) lock}.
     * </ul>
     * @param path path of the Collection.
     * @param timeOutMillis if strictly positive, specifies a time in
     *        milliseconds to wait for when the collection or one of its
     *        descendants or ancestors is already locked. If this time is
     *        elapsed and one member is still locked, the function returns
     *        false and no lock is set.
     *        <p>
     *        If equal to 0, the method will not wait. This can be used as a
     *        "trylock" function.
     *        <p>
     *        If negative, wait indefinitely (this is not recommended).
     * @return The collection if the lock is successful, i.e the collection
     *         exists and could be locked within the time imparted. Otherwise
     *         null is returned.
     * @exception DataModelException <a href="#std_exc">common causes</a>
     * @see #lock #lockDocument
     */
    Collection lockCollection(String path, int timeOutMillis)
        throws DataModelException;

    /**
     * Locks a Document to initiate a safe concurrent update.
     * <ul>
     * <li>This operation is optional: it is necessary only in applications
     * where a Document can be updated concurrently by several threads.
     * <li>The lock set on the document is <em>advisory</em> that is, if
     * another thread updates its contents without lock, then the result is
     * unpredictable.
     * <li>The document is unlocked automatically by commit() or rollback().
     * <li>An implicit refresh is performed by this method, to ensure that the
     * update is performed on the latest state of the Library. In particular
     * some library objects could have been deleted. This is why the method
     * returns a new handle to the document: if a non-null value is returned,
     * it is guaranteed that it still exists and has been successfully locked.
     * <li>No other call to lock() can be made until the end of the
     * transaction (commit or rollback). If several objects need to be locked,
     * either lock a common ancestor Collection (in extreme cases, the root
     * collection), or use {@link #lock(String[], int)}.
     * </ul>
     * @param path path of the document.
     * @param timeOutMillis if strictly positive, specifies a time in
     *        milliseconds to wait for when the collection or one of its
     *        descendants or ancestors is already locked. If this time is
     *        elapsed and one member is still locked, the function returns
     *        false and no lock is set.
     *        <p>
     *        If equal to 0, the method will not wait. This can be used as a
     *        "trylock" function.
     *        <p>
     *        If negative, wait indefinitely (this is not recommended).
     * @return The document if the lock is successful, i.e the document exists 
     * and could be locked within the time imparted. Otherwise null is returned.
     * @exception DataModelException <a href="#std_exc">common causes</a>
     * @see #lock #lockDocument
     */
    Document lockDocument(String path, int timeOutMillis)
        throws DataModelException;

    /**
     * Commits the current transaction. If this method completes without error,
     * updates performed in the transaction are guaranteed persistent, and
     * become visible to other sessions (provided they do a refresh, lock,
     * commit, or rollback).
     * <p>Unlocks all Library members previously locked.
     * <p>
     * After completion, the state of the Library also reflects changes 
     * possibly performed by other transactions (i.e is "refreshed").
     * <p>The progress of the
     * @exception LibraryException <a href="#std_exc">common causes</a>
     */
    void commit()
        throws LibraryException;

    /**
     * Returns true if the Library is in read-only mode. This mode is set
     * when the Library is actually opened by the related LibraryManager.
     * <p>Attempts to modify the contents of a read-only Library raise
     * AccessControlException.
     * @since 3.1
     */
    boolean isReadOnly();
    
    /**
     * Returns the auto-commit flag: if set to true, a commit is performed
     * after the execution of each updating Expression (expression using
     * XQuery Update instructions).
     * @return true if a commit is performed after the execution of each
     *         updating Expression.
     */
    boolean isAutoCommitting();
    
    /**
     * Sets the auto-commit flag: if set to true, a commit is performed after
     * the execution of each updating Expression (expression using XQuery
     * Update instructions).
     * @param autoCommitting a boolean indicating whether a commit is performed
     *        after the execution of each updating Expression.
     */
    void setAutoCommitting(boolean autoCommitting);

    /**
     * Cancels the current transaction.
     * <p>Unlocks all Library members previously locked.
     * <p>
     * After completion, the state of the Library is also "refreshed", i.e.
     * reflects possible changes possibly performed by other transactions.
     * @exception DataModelException <a href="#std_exc">common causes</a>
     */
    void rollback()
        throws LibraryException;

    /**
     * Returns true if a transaction is in progress and library members have
     * been created, modified or deleted.
     * @return true if any modification is in progress for this session
     */
    boolean isModified();

    /**
     * Terminates a session. No operation can be performed anymore once a
     * session is closed.
     * <p>
     * This method may not be called if a transaction has begun. In that case
     * only commit() or rollback() may be used.
     * @throws DataModelException <a href="#std_exc">common causes</a>
     */
    void close()
        throws LibraryException;

    /**
     * Returns true if the session is closed. No operation can be performed
     * anymore once a session is closed.
     * @return true if the session is closed
     */
    boolean isClosed();

    /**
     * Defines the indexing rules used in this Library.
     * <p>
     * Normally followed by a reindexing operation: see {@link #reIndex()}.
     * <p>
     * If Access Control is enforced, this operation requires the permission
     * to modify the properties of the root collection.
     * <p>
     * Notice that the new configuration is immediately visible by all sessions
     * opened on the Library, through the method getIndexing(): there is no
     * isolation.
     * @param specification a parsed Indexing specification
     * @throws DataModelException <a href="#std_exc">common causes</a>
     */
    void setIndexing(Indexing specification)
        throws DataModelException;

    /**
     * Gets the current indexing rules.
     * @return the indexing specification used for document contents by this
     *         Library.
     * @throws DataModelException <a href="#std_exc">common causes</a>
     */
    Indexing getIndexing()
        throws DataModelException;

    /**
     * Completely rebuilds the indexes.
     * <p>
     * This is a synchronous (blocking) and potentially lengthy operation. The
     * progress of the operation can be observed using a
     * LibraryProgressObserver. <br>
     * <b>Note</b>: this method was initially asynchronous (v2.0).
     * <p>
     * If Access Control is enforced, this operation requires the permission to
     * modify the properties of the root collection.
     * @see #setProgressObserver
     * @throws DataModelException <a href="#std_exc">common causes</a>
     */
    void reIndex()
        throws DataModelException;

    /**
     * Optimizes the underlying Library storage, in particular the indexes.
     * Nothing is done is the Library is already optimized.
     * <p>
     * This is a synchronous (blocking) and potentially lengthy operation. The
     * progress of the operation can be observed using a
     * LibraryProgressObserver. <br>
     * <b>Note</b>: this operation was initially asynchronous (v2.0).
     * <p>
     * If Access Control is enforced, this operation requires the permission to
     * modify the properties of the root collection.
     * @see #setProgressObserver
     * @throws DataModelException <a href="#std_exc">common causes</a>
     */
    void optimize()
        throws DataModelException;

    /**
     * Performs an optimization of the underlying Library storage in limited time.
     * Nothing is done if the Library is already optimized.
     * <p>
     * This is a synchronous (blocking) operation. The progress of the operation
     * can be observed using a LibraryProgressObserver. <br>
     * If Access Control is enforced, this operation requires the permission to
     * modify the properties of the root collection.
     * @param timeHint a time in seconds granted for the operation. This is only a
     *        hint, actual time can be shorter or slightly longer and can depend
     *        on the hardware.
     * @param blocking if set to true, then the operation is blocking, otherwise
     *        it is performed asynchronously
     * @see #setProgressObserver
     * @throws DataModelException <a href="#std_exc">common causes</a>
     */
    void quickOptimize(int timeHint, boolean blocking)
        throws DataModelException;

    /**
     * Makes a snapshot copy of the Library to a directory.
     * <p>If a {@link LibraryProgressObserver} is defined, its method
     * {@link LibraryProgressObserver#backupProgress(double)} is called as
     * the backup operation progresses.
     * @param backupDir directory where the Library is copied. If this
     *        directory already exists, its contents are first erased.
     * @throws DataModelException on IO error on the backup destination; <a
     *            href="#std_exc">common causes</a>
     */
    void backup(File backupDir)
        throws DataModelException;

    /**
     * Makes a snapshot copy of the Library to a directory, transferring only
     * the modified portions of the Library.
     * <p>This operation may revert to a full backup if the target Library 
     * is incompatible with the source Library (e.g different versions).
     * <p>If a {@link LibraryProgressObserver} is defined, its method
     * {@link LibraryProgressObserver#backupProgress(double)} is called as
     * the backup operation progresses.
     * @param backupDir directory where the Library is copied. 
     * @throws DataModelException on IO error on the backup destination; <a
     *            href="#std_exc">common causes</a>
     */
    void incrementalBackup(File backupDir)
        throws DataModelException;

    /**
     * Sets a listener used to monitor the progress of different operations on
     * the Library.
     * @param listener an implementation of LibraryProgressObserver used to 
     * monitor import, commit or backup.
     * @see #getProgressObserver
     */
    void setProgressObserver(LibraryProgressObserver listener);

    /**
     * Returns the observer used for monitoring import, commit or backup.
     * @return the observer set by setProgressObserver (null by default).
     * @see #setProgressObserver
     */
    LibraryProgressObserver getProgressObserver();
}
