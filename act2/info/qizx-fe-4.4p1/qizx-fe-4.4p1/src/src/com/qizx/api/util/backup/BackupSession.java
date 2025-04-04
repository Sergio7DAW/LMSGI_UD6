/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.api.util.backup;

import com.qizx.api.LibraryException;
import com.qizx.util.basic.IntSet;
import com.qizx.xlib.blobs.BlobException;

/**
 * Low-level input interface for Backup (full or incremental).
 */
public interface BackupSession
{
    // datasets:
    int DOCS = 0, METADOCS = 1;

    String getLibraryName();

    /**
     * Returns the version number of the source database.
     * This allows to check compatibility with the backup database: if the
     * versions are different, it is necessary to perform a full backup.
     */
    int getVersion();

    /**
     * Returns the commit id of the session. The commit-id is incremented on each
     * commit performed by the source database. This is used by the replication
     * system for checking the state of the replica versus the master database.
     */
    long getCommitId()
        throws LibraryException;

    /**
     * Returns a list of signatures/digests for "pages" of MicroFile/Blob
     * identifiers. This is used to quickly identify pages where changes occurred.
     * TODO future: add a 'storageId' param
     */
    long[] getPageDigests(int pageSize)
        throws LibraryException;

    /**
     * Gets a set of identifiers (within a "page") of actually existing
     * MicroFiles.
     */
    IntSet getFileIds(int/*Fid*/firstFid, int pageSize)
        throws LibraryException;

    /**
     * Returns the total size of a set of microfiles
     * @param toCopy a set of microfiles
     * @throws BlobException 
     */
    long getMicroFilesSize(IntSet toCopy)
        throws LibraryException;

    /**
     * Get identifiers, sizes and digests of index segments for dataset.
     */
    long[] getSegments(int dataset)
        throws LibraryException;

    // ------------- data retrieval --------------------------------------

    /**
     * Starts reading a microfile as a sequence of blocks
     * @param fileId
     * @return the number of blocks
     */
    long beginMicroFile(int/*Fid*/fileId)
        throws LibraryException;

    /**
     * Starts reading a segment as a sequence of blocks
     * @param segId
     * @return the number of blocks
     */
    long beginSegment(int dataset, int segId)
        throws LibraryException;

    /**
     * Ends reading a segment 
     */
    void endSegment(int dataset)
        throws LibraryException;

    /**
     * Returns the block size of the current microfile.
     */
    int getBlockSize()
        throws LibraryException;

    /**
     * Returns the compression level of the current microfile.
     * @return 0 if not compressed, 1 to 9 if compressed.
     */
    int getCompression()
        throws LibraryException;

    /**
     * Gets the bytes of the current block.
     * @param buffer should be large enough to hold the block size
     * @return actual size of the block. This is the raw size (compressed if
     *         compression is used).
     */
    int getBlockBytes(byte[] buffer)
        throws LibraryException;

    /**
     * Closes the target XML Library and releases resources.
     */
    void close()
        throws LibraryException;
}
