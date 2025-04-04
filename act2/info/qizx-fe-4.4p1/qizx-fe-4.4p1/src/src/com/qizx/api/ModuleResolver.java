/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.api;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.transform.Source;

/**
 * Resolves a module URI specified by a XQuery 'import module' declaration.
 */
public interface ModuleResolver
{
    /**
     * Resolves a module namespace to one or several compilation units.
     * 
     * @param moduleNamespaceURI target namespace of the module to import.
     * @param locationHints optional URI's used as resolution hints.
     * @return one or several physical locations of module units.
     * @throws MalformedURLException if provided module URI's or hints are invalid
     */
    URL[] resolve(String moduleNamespaceURI, String[] locationHints)
        throws MalformedURLException;

    /**
     * Resolves an URL for an XSLT stylesheet.
     * @param url URL of the stylesheet
     * @return null if not resolved, otherwise a resolved JAXP Source
     */
    Source resolveTemplates(String url);
}
