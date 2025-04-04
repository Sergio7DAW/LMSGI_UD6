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
 * Collections can contain XML Documents and sub-Collections.
 * <p>
 * They are similar to folders in a disk file-system, and thus can be used to
 * organize documents in hierarchies. It is possible to restrict XQuery queries
 * to the documents contained within a particular Collection or set of
 * Collections.
 * <p>
 * As a {@link LibraryMember}, a Collection can hold Metadata Properties.
 * <p>
 * A Collection can be used as the root of a XQuery <em>Path Expression</em>,
 * either explicitly through the XQuery function fn:collection or the extension
 * function xlib:collection, or implicitly in an {@link Expression} through the
 * method {@link Expression#bindImplicitCollection(LibraryMember)}.
 * @see LibraryMember
 */
public interface Collection
    extends LibraryMember
{
    /**
     * Returns the direct child of this collection (Document or Collection)
     * that bears this name.
     * @param name a simple name without slash.
     * @return the handle of the child member, or null if does not exist
     * @exception DataModelException <a href='Library.html#std_exc'>common causes</a>
     */
    LibraryMember getChild(String name)
        throws DataModelException;

    /**
     * Returns an iterator over members (Documents and Collections) directly
     * contained in this Collection.
     * @return a forward only iterator
     * @throws DataModelException <a href='Library.html#std_exc'>common causes</a>
     */
    LibraryMemberIterator getChildren()
	throws DataModelException;


    /**
     * Returns an iterator over members directly contained in this Collection
     * and accepted by the specified filter.
     * @param filter a LibraryMemberFilter implementation that accepts or
     *        rejects a member
     * @return a forward only iterator
     * @exception DataModelException <a href='Library.html#std_exc'>common causes</a>
     */
    LibraryMemberIterator getChildren(LibraryMemberFilter filter)
        throws DataModelException;

    /**
     * Returns an iterator over descendant Documents and Collections whose
     * <em>properties</em> match a boolean XQuery expression.
     * <p>Example:
     * <pre>
     *    Expression exp = 
     *      library.compileExpression("type='document'  
     *                                 and import-date gt xs:date('2006-12-31')
     *                                 and dm.totalSize &gt; 10000");
     *    LibraryMemberIterator result = collection.queryProperties(exp);
     * </pre>
     * <em>This metadata query would return all documents (property
     * <b>type</b>) whose <b>import-date</b> property is greater than the
     * value given and whose document size (property <b>dm.totalSize</b>) is
     * greater than 10000 (bytes).</em>
     * <p><b>Metadata Property Query</b>: the particular form of expression
     * suitable for queryProperties is called a metadata query. It can be any
     * boolean expression that could be used as a predicate in a Path
     * expression (between square brackets).
     * <ul>
     * <li>The properties (in this example: 'type', 'import-date',
     * 'dm.totalSize') appear as simple names
     * <li>The value of properties can be either a typed atomic value (number,
     * date, string) or an XML tree (element and children nodes). In fact
     * everything happens as if each property was an XML element with a typed
     * content which is the property value.
     * <li>In the latter case (property with a node value), a more complex
     * path expression like a full-text query can be used for the property,
     * e.g <code>ft:contains('completed', taskinfo/status)</code>.
     * </ul>
     * 
     * @param query a metadata query about the <em>properties</em> of
     * contained Documents or Collections.
     * @return Library members, descendants of this collection, having
     * properties matching the specified query.
     * @throws EvaluationException on run-time error in the query expression
     * @throws DataModelException <a href='Library.html#std_exc'>common causes</a>
     */
    LibraryMemberIterator queryProperties(Expression query)
	    throws EvaluationException, DataModelException;

    /**
     * Creates a Collection direct child of this collection. If the child
     * collection already exists, it is simply returned.
     * @param name a simple name without slash.
     * @return a descriptor of the created Collection
     * @throws DataModelException <a href='Library.html#std_exc'>common causes</a>
     */
    Collection createChildCollection(String name)
        throws DataModelException;

}
