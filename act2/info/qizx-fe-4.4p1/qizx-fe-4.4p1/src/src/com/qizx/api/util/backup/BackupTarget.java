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

/**
 * Low-level output interface for Backup.
 * <p>
 * Supports full- and incremental backup.
 */
public interface BackupTarget
{
    String getLibraryName();

    /**
     * Returns the version number of the backup database. This allows to check
     * compatibility with the source database: if the versions are different, it
     * is necessary to perform a full backup.
     */
    int getVersion();
    
    /**
     * Returns the commit id of the backup session. The commit-id is incremented
     * on each commit performed by the source database. This is used by the
     * replication system for checking the state of the replica versus the master
     * database.
     */
    long getCommitId()
        throws LibraryException;

    /**
     * Returns a list of signatures/digests for "pages" of MicroFile/Blob
     * identifiers. This is used to quickly identify pages where changes occurred.
     */
    long[] getPageDigests(int pageSize);

    /**
     * Gets a set of identifiers (within a "page") of actually existing
     * MicroFiles.
     */
    IntSet getFileIds(int/*Fid*/firstFid, int pageSize);
    
    
    /**
     * Get identifiers & digests of existing index segments for XML document
     * contents. Segments are in order of increasing id. Even index: id, odd
     * index: digest
     */
    long[] getSegments(int dataset);

    /**
     * Creates or overwrites a microfile
     * @param fileId
     */
    void beginMicroFile(int/*Fid*/fileId, int compressionLevel, int blockSize)
        throws LibraryException;

    void endMicroFile()
        throws LibraryException;

    /**
     * Deletes an obsolete microfile
     * @param fileId
     * @throws LibraryException
     */
    void deleteMicroFile(int/*Fid*/fileId)
        throws LibraryException;

    /**
     * Creates a segment
     * @param segId
     */
    long beginSegment(int dataset, int segId)
        throws LibraryException;

    /**
     * Closes a segment
     * @param segId
     * @throws LibraryException 
     */
    void endSegment(int dataset, int segId)
        throws LibraryException;

    /**
     * Deletes an obsolete segment
     * @param segId
     */
    void deleteSegment(int dataset, int segId);

    /**
     * Adds a data block to the current Microfile or Segment.
     */
    int putBlock(byte[] buffer, int size);

    void commit()
        throws LibraryException;

    /**
     * Closes the target XML Library and releases resources.
     * @throws LibraryException
     */
    void close(long commitId)
        throws LibraryException;
}
