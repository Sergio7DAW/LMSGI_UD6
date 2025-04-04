/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.api;

import com.qizx.api.Configuration.Property;
import com.qizx.api.admin.BackgroundTask;
import com.qizx.api.fulltext.FullTextFactory;
import com.qizx.api.util.DefaultModuleResolver;
import com.qizx.api.util.backup.BackupSession;
import com.qizx.api.util.backup.BackupTarget;
import com.qizx.api.util.logging.Log;
import com.qizx.api.util.logging.Statistic;
import com.qizx.xdm.DocumentPool;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * Manages a group of XML Libraries.
 * <p>
 * A LibraryManager manages a bundle of XML Libraries and federates resources like
 * memory caches. It can be seen as the core of a server.
 * <p>
 * User sessions ({@link Library}) are obtained from a LibraryManager, by calling
 * the {@link #openLibrary(String)} methods.
 * <p>
 * A LibraryManager instance can be obtained through the bootstrap class
 * {@link Configuration}:
 * <ul>
 * <li>by opening an existing Library Group: ({@link Configuration#openLibraryGroup} ).
 * <li>by creating a new Library Group ( {@link Configuration#createLibraryGroup}).
 * <li>by creating an empty manager: ({@link Configuration#createManager}) where
 * Libraries can be dynamically added (advanced use).
 * </ul>
 * <p>
 * A Library Group is merely a directory that contains a sub-directory for each
 * XML Library belonging to the group. Note that there is no delete method for a
 * group: to delete it, simply delete its storage directory.
 */
public interface LibraryManager
{
    /**
     * Returns the names of XML Libraries managed by this object.
     * 
     * @return a sorted array of Library names
     * @exception DataModelException if there is a system access problem.
     */
    String[] listLibraries()
	throws DataModelException;

    /**
     * Creates a new XML Library and manages it in this LibraryManager.
     * @param libraryName symbolic name of a Library.
     * @param libraryDirectory the location of the new Library. It represents a
     *        directory. If the directory exists, it must be empty. If value is
     *        null, the Library will be created as a sub-directory of the 'group
     *        directory' of this LibraryManager, which must be defined (see
     *        {@link #setGroupDirectory}. The sub-directory will have the name of
     *        the Library (parameter libraryName).
     * @exception DataModelException if specified library already exists, if
     *            specified library name already used.
     * @since 4.0 This method has been changed in 4.0, it has different parameters
     *        and no more opens a session.
     */
    void createLibrary(String libraryName, File libraryDirectory)
        throws LibraryException;

    /**
     * Physically destroys a Library.
     * @param libraryName symbolic name of the Library to destroy
     * @return <code>true</code> if specified library has been deleted;
     *         <code>false</code> otherwise (because such library does not exist)
     * @exception DataModelException if there is a system access problem, if no
     *            managed Library has the specified name.
     */
    boolean deleteLibrary(String libraryName)
	throws LibraryException;

    /**
     * Starts managing an existing XML Library in this LibraryManager. Sessions on
     * the Library can then be opened by {@link #openLibrary}.
     * @param libraryName symbolic name of a Library. Must not clash with an
     *        already managed Library.
     * @param libraryDirectory the location of the Library. If value is null and
     *        if the Group directory is defined (see {@link #setGroupDirectory}),
     *        the LibraryManager will look for a Library located at
     *        <em>group-directory/library-name</em>.
     * @exception DataModelException if specified library name already used, if
     *            Library cannot be located.
     * @since 4.0
     */
    void manageLibrary(String libraryName, File libraryDirectory)
        throws LibraryException;

    /**
     * Closes and detaches a XML Library from this LibraryManager. The Library is
     * then no more accessible by {@link #openLibrary}. All sessions if any are
     * automatically closed after a delay.
     * @param libraryName symbolic name of a managed Library.
     * @param graceTimeMillis a time in milliseconds for which the method will
     *        wait if there are Library sessions with unfinished transactions.
     *        After that time a rollback will be forced on all uncommitted
     *        transactions.
     * @return <code>true</code> if all sessions have been closed without forced
     *         rollback.
     * @exception DataModelException if there is a system access problem.
     * @since 4.0
     */
    boolean unmanageLibrary(String libraryName, int graceTimeMillis)
        throws LibraryException;

    /**
     * Shuts down the manager by unmanaging all the Libraries and releasing
     * resources.
     * @param graceTimeMillis a time in milliseconds for which the method will wait
     *        if there are Library sessions with unfinished transactions. After
     *        that time a rollback will be forced on all uncommitted transactions.
     * @return <code>true</code> if all sessions have been closed without forced
     *         rollback.
     * @exception DataModelException if there is a system access problem.
     */
    boolean closeAllLibraries(int graceTimeMillis)
	    throws DataModelException;

    /**
     * Opens a new session on a Library.
     * <p>
     * Notice that no authentication is performed at this level. The user name is
     * used by the AccessControl to check the permissions of the user.
     * @param libraryName symbolic name of a Library
     * @param accessControl an implementation of {@link AccessControlException}.
     *        this instance can be specific to this session, or specific to the
     *        concerned Library: this is under the control of the implementation.
     * @param user represents an authentified User. If null, then the
     *        AccessControl must be null too (corresponds to the simplified
     *        version without access control).
     * @return a session on the specified Library or <code>null</code> if such a
     *         Library does not exist.
     * @exception DataModelException if there is a system access problem. This
     *            happens normally only when the XML Library is already locked by
     *            another application.
     * @since 4.0 modified to add AccessControl parameter
     */
    Library openLibrary(String libraryName,
                        AccessControl accessControl, User user)
	throws DataModelException;

    /**
     * Opens a new session on a Library.
     * <p>Simplified version used when there is no Access Control.
     * 
     * @param libraryName symbolic name of a Library
     * @return a session on the specified Library or <code>null</code> if such
     * a Library does not exist
     * @exception DataModelException if there is a system access problem. This
     * happens normally only when the XML Library is already used by another
     * application.
     * @since 4.0
     */
    Library openLibrary(String libraryName)
	    throws DataModelException;
    
    /**
     * Returns a list of active (non closed) sessions.
     * @since 4.4
     */
    List<Library> listSessions();

    /**
     * Finds a session (Library) by its public identifier.
     * @param id a public identifier returned by {@link XQuerySession#getIdentifier()}.
     * @return a session, or null if not found.
     * @since 4.4
     */
    XQuerySession findSession(int id);
    
    /**
     * Opens a backup session on a Library: a {@link BackupSession} is the source
     * of a backup operation and allows comparison of contents with a
     * {@link BackupTarget} in order to perform incremental backup or Replication.
     * @param libraryName symbolic name of a Library
     * @return a low-level session that is used as source.
     * @throws DataModelException 
     */
    BackupSession openForBackup(String libraryName)
        throws DataModelException;
    
    /**
     * Opens or creates a Library used as backup output.
     * <p>Used for implementing backup. Advanced use, not for usual applications.
     * @param location physical location of the XML Library
     * @param incremental true if the backup is incremental. If not incremental,
     * or if the storage versions differ, then a previously existing backup 
     * Library is erased and recreated.
     * @param version storage version number
     * @return an interface used as low-level output of 
     */
    BackupTarget createBackup(File location, String libraryName, int version,
                              boolean incremental)
         throws DataModelException, IOException;
    
    /**
     * Returns the top-level directory of the Library Group. This directory in
     * general contains one sub-directory for each contained Library. It is
     * non null if this LibraryManager was open with {@link
     * Configuration#openLibraryGroup}.
     * 
     * @return a File which is the directory containing the Library Group
     * managed by this object.
     * @since 4.0
     */
    File getGroupDirectory();

    /**
     * Sets the top-level directory of the Library Group. This directory in
     * general contains one sub-directory for each contained Library.
     * 
     * @param directory a File which is the root location of the Library Group
     * managed by this object.
     * @since 4.0
     */
    void setGroupDirectory(File directory);
    
    /**
     * Configures a LibraryManager with a set of properties.
     * 
     * @param properties a set of properties. Properties are
     * defined by {@link Property}.
     * <p>Unknown properties are silently ignored.
     * @return true if any configuration property has actually been changed.
     * @exception QizxException
     * <p>When one or several invalid property values are specified. An
     * invalid value does not prevent valid properties to be taken into
     * account.
     * @since 4.2
     */
    public boolean configure(Properties properties)
        throws Exception;
    
    /**
     * Configures a XQuerySessionManager with a {@link Property}.
     * @param property a Property. 
     * @param value see each Property
     * @return true if Property is recognized and its value actually changes.
     * @throws Exception
     */
    public boolean configure(Property property, Object value)
        throws Exception;
    
    /**
     * Gets the current configuration properties of the LibraryManager.
     * <p>The configuration is loaded from the property file group.conf
     * located in the main directory of the XML Library Group (this file is
     * normally created by {@link #saveConfiguration()}). It is loaded
     * only if the LibraryManager has a root directory and if this file exists.
     * <p>The returned values  reflect the changes made with the
     * {@link #configure configure()} methods.
     * @return a mapping of configuration Property descriptors to values.
     */
    public Map<Property,Object> getConfiguration();
    
    /**
     * Saves the current configuration properties of the LibraryManager
     * onto a file attached to the Library Group (named group.conf).
     * <p>The LibraryManager must have a defined root directory.
     */
    public void saveConfiguration()
        throws DataModelException;
    
   /**
     * Returns a list of statistics collected from the managed XML Libraries
     * and from shared resources (caches, disk I/O etc).
     * @param targetMap a map that can aggregate or discard some queries
     */
    void collectStatistics(Statistic.Map targetMap);

    /**
     * Defines the maximum memory size the generated LibraryManager can use.
     * 
     * @param size maximum size in bytes of memory allocated to the
     * LibraryManager.
     * @see #getMemoryLimit
     */
    public void setMemoryLimit(long size);

    /**
     * Defines the maximum memory size the generated LibraryManager can use.
     * 
     * @return maximum size in bytes of memory allocated to the
     * LibraryManager.
     * @see #setMemoryLimit
     */
    public long getMemoryLimit();

    /**
     * Gets the document cache.
     */
    DocumentPool getTransientDocumentCache();
    
    /**
     * Sets the maximum memory size for the document cache. The document cache
     * stores transient documents which are parsed in memory but not stored in a
     * XML Library.
     * @param size maximum memory size in bytes. Decreasing this size will flush
     *        the cache accordingly.
     * @return the former maximum memory size in bytes
     */
    long setTransientDocumentCacheSize(long size);

    /**
     * Gets the current maximum memory size for the document cache.
     * 
     * @return a size in bytes
     */
    long getTransientDocumentCacheSize();

    /**
     * Sets the resolver used for resolving a module into actual locations. By
     * default, a LibraryManager uses a {@link DefaultModuleResolver}.
     * 
     * @param resolver replacement for the current resolver
     */
    void setModuleResolver(ModuleResolver resolver);

    /**
     * Returns the current {@link ModuleResolver}.
     * 
     * @return the current Module resolver
     */
    ModuleResolver getModuleResolver();
    
    
    /**
     * Defines a default FullTextFactory for all the XML Libraries opened or
     * created from this Library Manager.
     * <p>A FullTextFactory provides access to- or allows redefining full-text
     * resources such as text tokenizer, stemming, thesaurus and scoring
     * method.
     * 
     * @param factory an implementation of FullTextFactory. A new instance is
     * cloned for each Library. A factory can also be defined for a specific
     * Library.
     */
    void setFullTextFactory(FullTextFactory factory);
    
    /**
     * Returns the FullTextFactory specified by {@link
     * #setFullTextFactory(FullTextFactory)}.
     * 
     * @return the FullTextFactory specified by {@link
     * #setFullTextFactory(FullTextFactory)} or null if none has been defined.
     */
    FullTextFactory getFullTextFactory();
    
    /**
     * Returns the logger associated with the LibraryManager. This object can
     * be used by applications to change its configuration.
     * <p>This logger logs starts and shutdowns, and incidents on managed
     * Libraries. It is created with level INFO.
     * <p>
     * 
     * @since 4.0
     */
    Logger getLogger();

    /**
     * Returns the logger associated with a Library. This object can be used
     * by applications to change its configuration.
     * <p>This logger logs internal events of each XML Library.
     * <p>It is created with a daily rolling file output in
     * library/logs/lib.log at level WARNING.
     * 
     * @since 4.0
     */
    Logger getLibraryLogger(String libraryName);

    /**
     * Adds a Logger handler which is used both by the Library Manager and all
     * the managed Libraries.
     * 
     * @param handler
     */
    void addLogHandler(Handler handler);

    /**
     * Adds an observer for access and update operations.
     * <p>A {@link LibraryMemberObserver} is a pre-commit trigger whose actions are
     * part of a transaction.
     * @param observer an object implementing the LibraryMemberObserver interface.
     */
    void addLibraryObserver(LibraryMemberObserver observer);

    /**
     * Removes an observer of access and update operations.
     * 
     * @param observer an object implementing the {@link LibraryMemberObserver} interface.
     */
    void removeLibraryObserver(LibraryMemberObserver observer);

    /**
     * Returns a list of active LibraryMemberObserver's.
     * 
     * @return a non-null (but possibly empty) array of observers
     */
    LibraryMemberObserver[] getLibraryObservers();

    /**
     * Adds a post-commit Trigger.
     * <p>
     * @param libraryName the name of a managed Library, or <code>null</code>. A
     *        null value acts as a wildcard: the trigger is added to all currently
     *        managed Libraries.
     * @param trigger an object implementing the {@link PostCommitTrigger}
     *        interface.
     */
    void addPostCommitTrigger(String libraryName, PostCommitTrigger trigger);

    /**
     * Removes a post-commit Trigger.
     * <p>
     * @param libraryName the name of a managed Library, or <code>null</code>. A
     *        null value acts as a wildcard: the trigger is removed from all currently
     *        managed Libraries.
     * @param trigger an object implementing the {@link PostCommitTrigger}
     *        interface.
     */
    void removePostCommitTrigger(String libraryName, PostCommitTrigger trigger);

    /**
     * Returns a list of post-commit Triggers.
     * 
     * @param libraryName the name of a managed Library. A
     *        null value is not allowed here.
     * @return a non-null (but possibly empty) array of Triggers.
     */
    PostCommitTrigger[] getPostCommitTriggers(String libraryName);

    /**
     * Performs a validity check of all managed Libraries.
     * 
     * @param log interface reporting errors and messages
     * @param deep perform deep inspection if true
     * @param allowFixing if true and errors are detected, perform fixes if
     * possible. Requires the Libraries to be writable.
     */
    void sanityCheck(Log log, boolean deep, boolean allowFixing);

    /**
     * Performs a validity check of all managed Libraries.
     * 
     * @param log interface reporting errors and messages
     * @param modeMask a bit combination of {@link #CHECK_STORAGE} (for a full
     * check of the contents storage), 
     * {@link #CHECK_CONTENTS}
     * {@link #CHECK_INDEXES}. . 
     * @param allowFixing if true and errors are detected, perform fixes if
     * possible. Requires the Libraries to be writable.
     */
    void sanityCheck(Log log, int modeMask, boolean allowFixing);

    /**
     * Mask for {@link #sanityCheck}: fully check the document storage
     */
    int CHECK_STORAGE = 1;
    /**
     * Mask for {@link #sanityCheck}: fully check the document contents
     */
    int CHECK_CONTENTS = 2;
    /**
     * Mask for {@link #sanityCheck}: check the indexes for integrity & consistency
     */
    int CHECK_INDEXES = 4;

    /**
     * Returns a list of past- or current maintenance tasks.
     * @param timeline timeline=0 for "current tasks".
     *  if timeline > 0 list all tasks that started within
     * this number of hours in the past. For example timeline=24 returns all tasks
     * that started in the past 24 hours.
     */
    List<BackgroundTask> listBackgroundTasks(int timeline);
    
}
