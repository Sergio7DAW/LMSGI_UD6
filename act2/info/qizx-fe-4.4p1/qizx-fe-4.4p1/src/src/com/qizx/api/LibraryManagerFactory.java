/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.api;

import com.qizx.xlib.XMLLibraryEngineFactory;
import com.qizx.xquery.LibraryManagerFactoryImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Handler;

/**
 * Factory used to initialize a LibraryManager.
 * A LibraryManager instance is obtained:
 * <ul>
 * <li>by creating it empty ({@link #createManager()}).
 * <li>by opening a Library Group ({@link #openLibraryGroup}).
 * <li>by creating a Library Group ({@link #createLibraryGroup}).
 * </ul>
 * @deprecated since 4.2
 */
public abstract class LibraryManagerFactory
{
    private static LibraryManagerFactory instance;

    /**
     * Default constructor is protected on purpose.
     */
    protected LibraryManagerFactory()
    {
    }

    /**
     * Creates an instance of the factory, which can be used to configure a
     * LibraryManager.
     * @return a unique instance of this factory
     */
    public static synchronized LibraryManagerFactory getInstance()
    {
        if (instance == null) {
            instance = new LibraryManagerFactoryImpl();
        }
        return instance;
    }

    /**
     * Tests whether a directory contains an XML Library.
     * @param location a directory supposedly containing an
     * XML Library. It can also be any file or directory within an XML Library.
     * @return the directory containing the Qizx XML Library, or null if the
     * location does not correspond to an XML Library.
     * @exception FileNotFoundException if parent directory cannot be accessed
     * @since 4.0 
     */
    public static synchronized File locateLibrary(File location)
        throws FileNotFoundException
    {
        return XMLLibraryEngineFactory.findLibraryRoot(location);
    }

    /**
     * Creates a LibraryManager without group directory and without XML Libraries.
     * <p>
     * Libraries can then be managed through {@link LibraryManager#manageLibrary}
     * or created through {@link LibraryManager#createLibrary}.
     * <p>
     * @return an active LibraryManager managing the new group.
     * @exception QizxException if <tt>storageDir</tt> exists and is not empty.
     */
    public abstract LibraryManager createManager()
        throws QizxException;

    /**
     * Creates a LibraryManager controlling a group of XML Libraries
     * <p>
     * There is initially no XML Library in the created group. A Library can be
     * added by {@link LibraryManager#createLibrary}.
     * @param groupDirectory a directory which is the location of the Library
     *        Group. This directory is created if it does not exist.
     * @return an active LibraryManager managing the new group.
     * @exception QizxException if <tt>storageDir</tt> exists and is not empty.
     */
    public abstract LibraryManager createLibraryGroup(File groupDirectory)
        throws QizxException;

    /**
     * Starts a LibraryManager controlling an existing group of XML Libraries
     * located as sub-directories of the 'group directory'. These XML Libraries
     * are found and opened by scanning the group directory (the descriptor file
     * group.qlg is deprecated).
     * @param groupDirectory a directory which is the root location of the group.
     * @return an active LibraryManager managing the group.
     * @exception QizxException if <tt>storageDir</tt> has not been initialized by
     *            invoking {@link #createLibraryGroup(File)}.
     */
    public abstract LibraryManager openLibraryGroup(File groupDirectory)
        throws QizxException;
    
    /**
     * Defines a supplementary Log Handler used on new LibraryManagers.
     * @param handler
     */
    public abstract void setLogHandler(Handler handler);

    /**
     * Defines the maximum memory size the generated LibraryManager can use.
     * @param size maximum size in bytes of memory allocated to the
     *        LibraryManager.
     * @see #getMemoryLimit
     * @deprecated from 4.0, use {@link LibraryManager#setMemoryLimit(long)}.
     */
    public abstract void setMemoryLimit(long size);

    /**
     * Defines the maximum memory size the generated LibraryManager can use.
     * @return maximum size in bytes of memory allocated to the LibraryManager.
     * @see #setMemoryLimit
     * @deprecated from 4.0, use {@link LibraryManager#getMemoryLimit()}.
     */
    public abstract long getMemoryLimit();

    
    // More options in the future
}
