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
 * Compiled XML Query expression.
 * <p>
 * An Expression is created with the compileExpression method of
 * {@link Library} or {@link XQuerySession}.
 * <p>
 * Once compiled, an Expression can be evaluated several times. Preparing an
 * Expression for execution involves optional operations like:
 * <ul>
 * <li>Modifying the XML Query static context: {@link #getContext()}
 * <li>Binding a global XQuery variable with a value (simple item or
 * sequence), using one of the bindVariable methods.
 * <li>Binding the <em>implicit collection</em>. The implicit collection is used
 * when a XQuery/XPath "path expression" has no explicit root, and no
 * <em>context item</em> is defined.
 * <p>
 * For example the path expression <code>//foo/bar</code> has no explicit
 * root. Supposing that the collection /a/b has been bound as Implicit
 * Collection in this expression, its evaluation is equivalent to the
 * evaluation of the path expression <code> collection("/a/b")//foo/bar </code>.
 * <p>The implicit collection is also the <em>Default Collection</em> defined by
 * the XQuery specifications: in the example above, using
 * <code>collection()//foo/bar</code> is equivalent to use
 * <code>//foo/bar</code>.
 * <p>
 * This allows reusing the same query on different collections or documents of
 * a Library.
 * <li>Setting a {@link TraceObserver}.
 * </ul>
 * <p>
 * Execution can be aborted by {@link #cancelEvaluation()} or by a time-out
 * (see setTimeOut).
 * <p>
 * This class is not thread safe (an Expression is built from an XQuery session
 * which is itself not thread safe).
 */
public interface Expression
    extends ItemFactory
{
    /**
     * Access to the XQuery Context.
     * @return the XQuery Context of this expression. This context is initially
     * defined by the context of the XQuery session (or Library) and by the
     * expression itself. It can be modified before each execution.
     */
    XQueryContext getContext();
    
    /**
     * Returns a unique identifier of the expression.
     * This identifier is unique within the LibraryManager or the SessionManager.
     */
    String getIdentifier();
    
    /**
     * Returns the Session of the compiled expression.
     * @since 4.3
     */
    XQuerySession getSession();


    /**
     * Returns the XML Library of the compiled expression.
     * Returns null if the expression was not compiled from an XML Library session.
     * @since 4.3
     */
    Library getLibrary();

    /**
     * Returns the source code of the compiled expression.
     * @since 4.0
     */
    String getSource();

    /**
     * Returns the static (or formal) type of the compiled expression.
     * @since 3.1
     */
    SequenceType getStaticType();
    
    /**
     * Returns true if the Expression updates a database or more generally the
     * context. The expression might use XQuery Update operators, or extension functions.
     */
    boolean isUpdating();

    /**
     * Binds a variable to a sequence made of a single item.
     * @param varName name of a variable declared in the prolog of the XQuery
     *        expression.
     * @param value an item create by ItemFactory or obtained from an
     *        ItemSequence (null not accepted)
      * @throws CompilationException if the variable name is not declared as 
      * global in the expression.
     */
    void bindVariable(QName varName, Item value)
        throws CompilationException;

     /**
      * Binds a variable to a sequence.
      * 
      * @param varName name of a variable declared in the prolog of the XQuery
      *        expression.
      * @param value sequence bound to the variable, it is first cloned (null not accepted).
      * @throws CompilationException if the variable name is not declared as 
      * global in the expression.
      */
    void bindVariable(QName varName, ItemSequence value)
        throws CompilationException;

    /**
     * Binds a variable to a sequence made of a single boolean value.
     * 
     * @param varName name of a variable declared in the prolog of the XQuery
     *        expression.
     * @param value boolean value
     * @throws CompilationException if the variable name is not declared as 
     * global in the expression.
     */
    void bindVariable(QName varName, boolean value)
        throws CompilationException;

    /**
     * Binds a variable to a value obtained by converting a long integer to an
     * Item.
     * 
     * @param varName name of a variable declared in the prolog of the XQuery
     *        expression.
     * @param value a long integer value
     * @param type optional type. If non null, the value is converted to this type.
     *        If null, the default type xs:integer is used.
     * @throws EvaluationException if the type is invalid
     * @throws CompilationException if the variable name is not declared as 
     * global in the expression.
     */
    void bindVariable(QName varName, long value, ItemType type)
        throws CompilationException, EvaluationException;

    /**
     * Binds a variable to a sequence made of a single item of type xs:double.
     * 
     * @param varName name of a variable declared in the prolog of the XQuery
     *        expression.
     * @param value double value
     * @throws CompilationException if the variable name is not declared as 
     * global in the expression.
     */
    void bindVariable(QName varName, double value)
        throws CompilationException;

    /**
     * Binds a variable to a sequence made of a single item of type xs:float.
     * 
     * @param varName name of a variable declared in the prolog of the XQuery
     *        expression.
     * @param value float value
     * @throws CompilationException if the variable name is not declared as 
     * global in the expression.
     */
    void bindVariable(QName varName, float value)
        throws CompilationException;

    /**
     * Binds a variable to a value obtained by converting a Java object to an Item
     * or a ItemSequence, according to the general Java to XQuery type mapping
     * (see the documentation for details).
     * <p>
     * The object can be an array: it will be converted to a Sequence.
     * <p>
     * If value null is passed, then the variable is unbound.
     * @param varName name of a variable declared in the prolog of the XQuery
     *        expression.
     * @param value object to convert to XQuery value. If value null is passed,
     *        then the variable is unbound.
     * @param type optional type. If non null, the object (or its items) will be
     *        converted to this type. If null, the object will be converted to the
     *        most general applicable type: for example a Java String to xs:string
     *        type, an array of double to a sequence of xs:double and so on.
     * @throws EvaluationException if the conversion cannot be performed
     * @throws CompilationException if the variable name is not declared as global
     *         in the expression.
     */
    void bindVariable(QName varName, Object value, ItemType type)
        throws CompilationException, EvaluationException;


    /**
     * Defines the <em>Implicit Collection</em> as a set of Nodes defined
     * by a sequence (see above for a definition of Implicit Collection).
     * 
     * @param nodes a sequence of Nodes
     * @throws EvaluationException if one of the items in the sequence is not
     * a node, or if the enumeration of the sequence causes a runtime error.
     */
    void bindImplicitCollection(ItemSequence nodes)
        throws EvaluationException;


    /**
     * Defines the <em>Implicit Collection</em>, as a Collection or Document.
     * <p>
     * This method allows reusing the same query on different collections or
     * documents of a Library.
     * @param root a Document or a Collection, used as implicit root for
     * path expressions.
     * @throws DataModelException <a href='Library.html#std_exc'>common causes</a>
     */
    void bindImplicitCollection(LibraryMember root)
        throws DataModelException;

    /**
     * Defines the <em>Implicit Collection</em> as a set of Documents defined
     * by a query.
     * <p>
     * This is a powerful mechanism allowing queries to be restricted to a set
     * of documents defined by their properties, or the properties of enclosing
     * collections.
     * <p>
     * The iterator passed as argument can be obtained by calling
     * queryProperties or getChildren on a Collection (typically the root
     * collection). If a Collection is enumerated by the iterator, all its
     * enclosed documents at any level are added to the Implicit Collection.
     * <p>
     * Attention: the iterator is consumed by this method. It cannot be reused.
     * @param roots a sequence of Documents or Collections
     * @throws EvaluationException if the enumeration of library members causes
     * a runtime error
     * @throws DataModelException <a href='Library.html#std_exc'>common causes</a>
     */
    void bindImplicitCollection(LibraryMemberIterator roots)
        throws EvaluationException, DataModelException;

    /**
     * Sets the initial value of the current item, accessible by the expression
     * '.'.
     * @param item the current item. If the value is null, the current item
     *        becomes undefined.
     */
    void setCurrentItem(Item item);

    /**
     * Gets the initial value of the current item, accessible by the expression
     * '.'.
     * @return the current item set by setCurrentItem.
     */
    Item getCurrentItem();

    /**
     * Evaluation of the expression, returns a sequence of Items.
     * @return a sequence of Items which is the result of the evaluation
     * @throws EvaluationException thrown by an XQuery dynamic error
     */
    ItemSequence evaluate()
        throws EvaluationException;

    /**
     * Evaluation of the expression, returns a sequence of Items.
     * @return a sequence of Items which is the result of the evaluation
     * @throws EvaluationException thrown by an XQuery dynamic error
     */
    ItemSequence profile()
        throws EvaluationException;

    /**
     * Cancels the evaluation started by {@link #evaluate()}. 
     * Most likely called from a different thread than the evaluation
     *  thread (where evaluate is called).
     */
    void cancelEvaluation();
    
    /**
     * Returns the start time of the running evaluation.
     * <p>If the Expression is currently executing (methods {@link #evaluate()} or
     * {@link #profile()}), or iterating on the returned ItemSequence,
     * the returned value is a time-stamp in milliseconds, otherwise if the
     * Expression is not being executed, the returned value is 0.
     */
    long getStartTime();
    
    /**
     * Releases all resources used by this expression.
     * <p>The expression can no more be used afterwards. 
     */
    void close();
    
    /**
     * Returns true if expression was closed by method {@link #close()}. 
     */
    boolean isClosed();
    
    /**
     * Returns the maximum evaluation time, as defined by
     * {@link #setTimeOut(int)}. By default, this value is 0, meaning no time
     * limit.
     * @return an int representing the current time out value in milliseconds
     */
    int getTimeOut();
    
    /**
     * Defines a maximum time for evaluation. If this time is defined and
     * exceeded, the evaluation aborts with a <b>"TIME0000"</b> error code.
     * 
     * @param maxTime maximum execution time in milliseconds. A value <= 0 can
     *        be used to specify an unlimited time.
     */
    void setTimeOut(int maxTime);

    /**
     * Returns the maximum space allowed for evaluation of the expression.
     * {@link #setSpaceLimit(int)}. By default, this value is 0, meaning no limit.
     * @return an int representing the current space limit in bytes
     */
    int getSpaceLimit();

    /**
     * Defines a maximum for memory space used by evaluation. The purpose is to
     * prevent some Expressions to hog the resources of the XQuery engine.
     * <p>
     * Notice this is approximative: the XQuery engine does its best effort to
     * track the total memory and resources used by an evaluation, but this is 
     * a fairly complex task since many resources are cached or shared.
     * @param limit limit in bytes. A value <= 0 means no limit.
     */
    void setSpaceLimit(int limit);
    
    /**
     * Sets a listener to receive evaluation traces.
     * @param listener an implementation of TraceObserver whose method trace
     * is called each time the fn:trace is called in the expression.
     */
    void setTraceObserver(TraceObserver listener);

    /**
     * Gets the listener used to receive evaluation traces.
     * @return the trace observer set by setTraceObserver, or null by default
     */
    TraceObserver getTraceObserver();

    /**
     * Returns a list of document or collection paths used as roots of Path-Expressions.
     */
    String[] getRootPaths();

    /**
     * Returns true if the expression was executed by {@link #profile()}.
     */
    boolean isProfiled();

    /**
     * Returns a list of profiling annotations if the expression was executed by
     * {@link #profile()}, else null.
     */
    List<Profiling> getProfilingAnnotations();
}
