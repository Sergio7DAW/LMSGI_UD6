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
 * Monitors the progress of diverse operations on a Library.
 * <p><b>Caution: </b>implementations of these methods should never throw
 * RuntimeExceptions.
 */
public interface LibraryProgressObserver
{
    /**
     * Monitors the progress of an importDocument operation.
     * @param size estimated size in characters of XML contents stored so far.
     */
    void importProgress(double size);

    /**
     * Monitors the progress of a commit operation.
     * @param fraction estimated completed fraction, from 0 to 1.
     */
    void commitProgress(double fraction);

    /**
     * Monitors the progress of a backup operation.
     * @param fraction estimated completed fraction, from 0 to 1.
     */
    void backupProgress(double fraction);

    /**
     * Monitors the progress of an "optimize" operation ({@link Library#optimize()}).
     * @param fraction estimated completed fraction. As long as the
     *        optimization task is not actually started, the value is negative
     *        (-1). The task can be delayed by operations on other Library
     *        sessions (e.g commits). As the optimization task runs, the
     *        fraction value goes from 0 to 1.
     */
    void optimizationProgress(double fraction);

    /**
     * Monitors the progress of a "re-index" operation ({@link Library#reIndex()}).
     * @param fraction estimated completed fraction. As long as the
     *        reindexing task is not actually started, the value is negative
     *        (-1). The task can be delayed by operations on other Library
     *        sessions (e.g commits). As the reindexing task runs, the
     *        fraction value goes from 0 to 1.
     */
    void reindexingProgress(double fraction);
}
