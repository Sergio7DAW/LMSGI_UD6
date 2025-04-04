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
 * Common interface for objects belonging to a Library, namely Collections and
 * Documents.
 */
public interface LibraryMember
{
    /**
     * Name of the predefined property 'path', whose value is also returned by
     * the getPath() method.
     */
    public static final String PATH = "path";

    /**
     * Name of the predefined property 'nature', whose value is one of the
     * strings "collection" or "document".
     */
    public static final String NATURE = "nature";

    /**
     * Value of the predefined property 'nature' for a Collection, equal to
     * "collection".
     */
    public static final String COLLECTION = "collection";

    /**
     * Value of the predefined property 'nature' for a Document, equal to
     * "document".
     */
    public static final String DOCUMENT = "document";

    /**
     * Value of the predefined property 'nature' for a non-XML Document,
     *  equal to "non-xml".
     */
    public static final String NONXMLDOC = "non-xml";
  

    /**
     * Returns the Library session owning this member.
     * 
     * @return a Library session
     */
    Library getLibrary();

    /**
     * Returns true if the member actually exists (not deleted).
     * @return true if the member actually exists
     */
    boolean exists();

    /**
     * Returns true if the member is a Collection.
     * @return true if the member is a Collection.
     */
    boolean isCollection();

    /**
     * Returns true if the member is a Document (XML or non-XML).
     * @return true if the member is a Document.
     */
    boolean isDocument();

    /**
     * Returns the complete path of the member inside its Library. This path uses
     * forward slashes as separator and always starts with a slash. The root
     * collection has the path "/".
     * @return a String representing the complete path of the member
     */
    String getPath();

    /**
     * Returns the name of the member inside its enclosing Collection. This
     * name cannot contain a slash, except for the root collection, whose name
     * is "/".
     * 
     * @return a String representing the name
     */
    String getName();

    /**
     * Returns the path of the parent of the enclosing Collection. The root
     * collection '/' returns null.
     * 
     * @return a String representing the path of the parent collection
     */
    String getParentPath();

    /**
     * Returns the Collection that directly contains this member.
     * 
     * @return the collection which is the parent of this member;
     * <code>null</code> is returned for the root collection ("/").
     * @exception DataModelException if the library is closed; if the member
     * is already deleted
     */
    Collection getParent()
	    throws DataModelException;

    /**
     * Returns true if that member (Document or Collection) is contained
     * inside this member. A library member does not contain itself.
     * 
     * @param other another library member to check for containment
     * @return true if the another library member is contained
     */
    boolean contains(LibraryMember other);

    /**
     * Starts an update transaction by locking this member (Document or
     * Collection).
     * <p>This method is almost equivalent to calling {@link
     * Library#lockCollection} or {@link Library#lockDocument} on this Library
     * member.
     * <p>The difference is that an exception is thrown if it is found that
     * this object no longer exists in the refreshed state of the Library (due
     * to a deletion by another transaction). In contrast, lockCollection and
     * lockDocument simply return null.
     * <p><b>Note:</b> this method exists for historical reasons. It is
     * recommended to use lockCollection and lockDocument instead.
     * 
     * @param timeoutMillis a maximum time in milliseconds to wait for when
     * the object is already locked. After that time the method returns false
     * and no lock is set.
     * @return true if the lock is successful.
     * @exception DataModelException if the member has been deleted by another
     * transaction; if the library is closed
     */

    boolean lock(int timeoutMillis)
	    throws DataModelException;

    /**
     * Copies the Document or Collection to another location.
     * <p>A Collection is recursively copied with all its contents.
     * <p>As for other updating operations, should be followed by a commit to
     * take effect permanently.
     * 
     * @param newPath path of the copy. It is an error (exception thrown) if
     * this path points to an existing Library member.
     * @exception DataModelException if the library is closed; if the member
     * is already deleted; if the newPath points to an existing member or
     * points inside a non-existing collection,
     */
    void copyTo(String newPath)
	throws DataModelException;

    /**
     * Renames the Document or Collection.
     * <p>Upon successful completion, the path of the library member is
     * updated according the path specified.
     * <p>As for other updating operations, should be followed by a commit to
     * take effect permanently.
     * 
     * @param newPath new path of the member. It is an error (exception
     * thrown) if this path points to an existing Member.
     * @exception DataModelException if the library is closed; if the member
     * is already deleted; if the newPath points to an existing document or
     * points inside a non-existing collection,
     */
    void renameTo(String newPath)
	throws DataModelException;
    
    /**
     * Deletes the Document or Collection. As for other updating operations,
     * should be followed by a commit to take effect permanently.
     * <p>When applied on a Collection, deletes recursively all enclosed
     * documents and collections. Equivalent to
     * Library.deleteMember(getPath()).
     * 
     * @exception DataModelException if the library is closed; if the member
     * is already deleted
     */
    void delete()
	throws DataModelException;

    // -----------------------------------------------------------------------
    // Properties
    // -----------------------------------------------------------------------

    /**
     * Returns a sorted list of current property names of the object.
     * 
     * @return a non-null String array containing property names
     * @exception DataModelException if the library member is deleted; if the
     * Library is closed.
     */
    String[] getPropertyNames()
	throws DataModelException;

    /**
     * Tests if this member has a property of the specified name.
     * 
     * @param propName name of a property (simple XML name without colon).
     * @return true if the property exists
     * @exception DataModelException if the library member is deleted; if the
     * Library is closed.
     */
    boolean hasProperty(String propName)
	throws DataModelException;

    /**
     * Gets the value of a property by its name.
     * 
     * @param propName name of a property (simple XML name without colon).
     * @return the current value, or null if the property is not defined.
     * <p><b>Caution</b>: though a property can have any serializable value,
     * some conversions are performed in setProperty.
     * @exception DataModelException if the library member is deleted; if the
     * Library is closed.
     * @see #setProperty
     */
    Object getProperty(String propName)
	throws DataModelException;

    /**
     * Changes the value of a property.
     * 
     * @param propName name of a property (simple XML name without colon).
     * @param propValue the new value to set. The value can be of any
     * serializable Java type, but some types are treated specially:
     * <ul>
     * <li>java.lang.Float, and java.math.BigDecimal are treated as
     * java.lang.Double, i.e converted to double, and indexed as a double. So
     * getProperty will return java.lang.Double.
     * <li>java.lang.Integer, Short and Byte, and java.math.BigInteger are
     * treated as Long, i.e converted to long. So getProperty will return
     * java.lang.Long. These values are indexed as double.
     * <li>java.lang.Boolean is treated as the type xs:boolean. It is not
     * indexed.
     * <li>java.lang.String is always indexed.
     * <li>java.util.Date as the type xs:dateType, and indexed the same way.
     * <li>a node of the XQuery Data Model (in Qizx, the interface
     * {@link Node}) is stored as such. <b>Attention</b>: DOM nodes, and
     * other kinds of XML representations, are not recognized and not
     * converted automatically. They have to be first converted using
     * ItemFactory.
     * <li>An {@link Item} created using {@link ItemFactory#createItem}. The 
     * item can be cast to one of types supported by properties: boolean, long,
     * double, date, string.
     * <li>Any other object is serialized and stored in binary form. No
     * indexing is performed. Beware that usual deserialization problems might
     * occur if the class of the object is modified.
     * </ul>
     * @return the former value if any, <code>null</code> otherwise.
     * @exception DataModelException if the library member is deleted; if the
     * Library is closed.
     * @see #getProperty
     */
    Object setProperty(String propName, Object propValue)
	    throws DataModelException;

    /**
     * Deletes a property.
     * 
     * @param propName name of a property (simple XML name without colon).
     * @return the former value if any, <code>null</code> otherwise.
     * @exception DataModelException if the library member is deleted; if the
     * Library is closed.
     */
    Object removeProperty(String propName)
	    throws DataModelException;

    /**
     * Convenience method: returns the integer value of a property.
     * 
     * @param propName name of a property (simple XML name without colon).
     * @return the integer value, or -1 if the property is not defined.
     * @exception DataModelException if the library member is deleted; if the
     * Library is closed.
     */
    long getIntegerProperty(String propName)
	    throws DataModelException;

    /**
     * Convenience method: defines a long integer property. This is equivalent
     * to setProperty(propName, new Long(longValue)).
     * 
     * @param propName name of a property (simple XML name without colon).
     * @param propValue long integer new value
     * @return the former value of the property (or null if created).
     * @exception DataModelException if the library member is deleted; if the
     * Library is closed.
     */
    Object setIntegerProperty(String propName, long propValue)
	    throws DataModelException;
}
