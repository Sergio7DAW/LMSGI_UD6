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
 * The type of an Item as defined in the XML Query language.
 * <p>
 * A predefined type can be obtained from an {@link ItemFactory} - i.e. a
 * Library or an Expression - by the method getType(name).
 * <p><b>Note</b>: in the current version, custom types are not supported.
 */
public interface ItemType extends SequenceType
{
    /**
     * Returns the fully qualified name of the type.
     * @return a qualified name, for example "xs:integer"
     */
    QName getName();

    /**
     * Short name as a string. For example "decimal". Such a name can also be
     * used for obtaining a predefined type from an ItemFactory.
     * @return a string which is the the short name of a predefined type, for
     *         example "decimal".
     */
    String getShortName();

    /**
     * Returns the type from which this type is derived immediately. 
     * @return the parent type
     */
    ItemType getSuperType();

    /**
     * Tests is this type is derived from another type.
     * @param type another Item type
     * @return true if this type is derived from argument type.
     */
    boolean isSubTypeOf(ItemType type);
    
    /**
     * Returns a code describing the node kind, when this type is a
     * node type (element(), attribute(name) etc.).
     * <p>
     * If this type is not a node type, the value ATOMIC_TYPE is returned.
     * <p>
     * If this type is a node type, the code returned corresponds with the node
     * kinds defined in {@link Node}, unless this is the generic type
     * <code>node()</code> in which case the value NODE_TYPE is returned.
     * @return a Node kind (ELEMENT, COMMENT etc) or NODE_TYPE or ATOMIC_TYPE.
     * @since 3.1
     */
    int getNodeKind();
    
    /**
     * Value returned by getNodeKind() when this type is <code>node()</code>
     * (any node).
     * @since 3.1
     */
    int NODE_TYPE = 0;
    
    /**
     * Value returned by getNodeKind() when this type is not a node type (i.e
     * atomic type or xs:anyType or xs:anySimpleType).
     * @since 3.1
     */
    int ATOMIC_TYPE = -1;
    
    /**
     * Returns the name associated with a Node Type.
     * @return a QName or null
     * @since 3.1
     */
    QName getNodeName();
    
    /**
     * Returns the sequence type corresponding to this item type and the
     * occurrence specified as argument.
     * <p>For example if applied to the Item Type xs:string with argument
     *  OCC_ONE_OR_MORE, the result is the SequenceType xs:string+.
     * @param occurrence OCC_ZERO_OR_ONE, OCC_EXACTLY_ONE etc defined in SequenceType
     * @return a SequenceType
     * @since 3.1
     */
    SequenceType getSequenceType(int occurrence);
}
