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
 * Product information.
 */
public interface Product
{

    /** Name of the product, namely: Qizx. */
    String PRODUCT_NAME = "Qizx";


    /** Name of the vendor of the product. */
    String VENDOR = "XMLmind";

    /** Main web address of the vendor of the product. */
    String VENDOR_URL = "http://www.qizx.com/";

    /** Major version number. */
    int MAJOR_VERSION = 4;

    /** Minor version number. */
    int MINOR_VERSION = 4;

    /** Maintenance version number. */
    int PATCH_VERSION = 1;

    /** Full version number. */
    String FULL_VERSION = MAJOR_VERSION + "." + MINOR_VERSION
                        + (PATCH_VERSION == 0 ? "" : ("p" + PATCH_VERSION));
    

    String VARIANT = "";


    /**
     * Supported major XQuery version number.
     */
    int XQUERY_MAJOR_VERSION = 1;
    /**
     * Supported minor XQuery version number.
     */
    int XQUERY_MINOR_VERSION = 1;
    /**
     * Supported XQuery version.
     */
    String XQUERY_VERSION = XQUERY_MAJOR_VERSION + "." + XQUERY_MINOR_VERSION;
}
