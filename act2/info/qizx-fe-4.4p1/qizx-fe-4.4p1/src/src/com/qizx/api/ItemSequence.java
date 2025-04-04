/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.api;

import com.qizx.api.admin.Profiling;

import java.util.List;


/**
 * Sequence of Items returned by the evaluation of a XQuery Expression.
 * <p>
 * It can be used as input if bound to a variable of an expression.
 * <p>
 * Attention: the expansion of a result sequence through moveToNextItem or
 * moveTo can cause runtime errors (EvaluationException) due to lazy - or late -
 * evaluation mechanisms used in Qizx.
 */
public interface ItemSequence
    extends Item
{
    /**
     * Moves to the next item. An iteration loop typically looks like this:
     * <pre>
     *  ItemSequence seq = ...;
     *  while(seq.moveToNextItem()) {
     *      Item item = seq.getCurrentItem();
     *      // process item...
     *  }
     * </pre>
     * <p><b>Attention:</b> if access control is enabled, a result item belonging
     * to a blocked document is silently discarded.
     * @return true if another item has been found. The item is available
     *         through {@link #getCurrentItem()}).
     * @throws EvaluationException
     */
    boolean moveToNextItem()
        throws EvaluationException;

    /**
     * Returns the current item of the sequence. If the sequence is in an
     * invalid state, the result is indetermined.
     * @return a non-null Item.
     */
    Item getCurrentItem();

    /**
     * Returns the total number of items, without moving the current position.
     * <p>
     * Uses lazy evaluation if possible.
     * <p>
     * This method does not take into account items forbidden by Access Control
     * (change from v2.1).
     * @return the number of items as a long integer
     * @throws EvaluationException if the expansion of the sequence caused a
     *         runtime error
     */
    long countItems()
        throws EvaluationException;

    /**
     * Returns an estimation of the number of <i>Documents</i> returned by this
     * sequence. This method can be used when an exact count of all results would
     * be too long to compute. It is designed to work on collections of millions
     * of documents.
     * <p>The value returned by this method is the same as calling
     * {@link #estimatedDocumentCount(int)} with an argument of 100. See the
     * latter method for more details.
     * <p>
     * This method is cached, i.e it does not recompute the value if called several
     * times.
     * @throws EvaluationException  if the expansion of the sequence caused a
     * runtime error
     */
    long estimatedDocumentCount()
        throws EvaluationException;

    /**
     * Returns an estimation of the number of <i>Documents</i> returned by this
     * sequence. This method can be used when an exact count of all results would
     * be too long to compute. It is designed to work on collections of millions
     * of documents.
     * <p>
     * The estimated count provided by this method is valid under the following
     * conditions:
     * <ul>
     * <li>There is zero or one result node of the query per document
     * <li>The domain (root collection) on which the query is applied does not
     * contain documents that cannot be matched by the query (such documents would
     * only perturbate the estimation).
     * </ul>
     * This method uses the current position and the 'minimalPosition' argument
     * to estimate the count. A higher value of minimalPosition will provide a
     * more accurate estimation, at the cost of a higher computation time.
     * @param minimalPosition the minimal position to reach before doint the estimation
     * @throws EvaluationException  if the expansion of the sequence caused a
     * runtime error
     */
    long estimatedDocumentCount(int minimalPosition)
        throws EvaluationException;
    
    /**
     * Moves the current position to the specified value.
     * <p>
     * Attention: if the sequence is returned by an XQuery expression
     * evaluation, moving backwards involves reevaluating the expression and
     * therefore can be inefficient and potentially have side-effects if
     * extension functions are used in the expression.
     * 
     * @param position number of items before desired position: moveTo(0) is
     *        equivalent to rewind.
     * @throws EvaluationException if the expansion of the sequence caused a
     * runtime error
     */
    void moveTo(long position)
        throws EvaluationException;

    /**
     * Returns the current position. Position 0 corresponds to "before first",
     * position N means just after the N-th item (which is available through
     * {@link #getCurrentItem()}).
     * @return the current item position, 0 initially
     */
    long getPosition();

    /**
     * Returns the full-text score, if applicable.
     * <p>
     * If this sequence is produced by evaluating an XQuery expression that
     * contains a <code>ftcontains</code> operator, then the value returned
     * is the score of the current <em>document</em> evaluated against the
     * top-level <i>full-text selection</i>. Otherwise the value returned is 0.
     * <p>
     * This is the value returned by the score clause of
     * <code>for</code> and <code>let</code>.
     * @return a value between 0 and 1 which represents the score of the
     *         current <em>document</em> evaluated against the
     *         <code>ftcontains</code> expression.
     * @throws EvaluationException 
     */
    double getFulltextScore()
        throws EvaluationException;
    
    /**
     * Skips items in forward direction.
     * <p>
     * Performs lazy evaluation if possible.
     * @param count number of items to skip. Must be >= 0 otherwise ignored.
     * @return the actual number of skipped items: smaller than
     *         <code>count</code> if the end of the sequence is reached.
     * @throws EvaluationException if the expansion of the sequence caused a
     *         runtime error
     */
    int skip(int count)
        throws EvaluationException;
    
    /**
     * Closes the sequence after use, and releases resources immediately,
     *  instead of waiting for this to happen automatically.
     * <p>It is recommended to call this method when a sequence is no
     * more needed. Failing to do so could result into errors due to an excess
     * of used up resources.
     * <p>The sequence can no more be used after calling this method. Calling
     * this method twice has no effect.
     */
    void close();
    
    /**
     * Returns the Expression the evaluation of which this sequence is the result.
     * <p>When the sequence is not the result of {@link Expression#evaluate()},
     * this method returns null.
     * @return an Expression or null
     */
    Expression getExpression();

    /**
     * Returns a list of Profiling annotations when the query was executed through
     * the profile() method.
     * In embedded mode, this means simply calling getProfilingAnnotations()
     * on the related Expression.
     */
    List<Profiling> getProfilingAnnotations();
}
