/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.api;

import com.qizx.api.util.SAXToPushStream;

import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

import javax.xml.stream.XMLEventWriter;

/**
 * Import method for structured or semi-structured data.
 * <p>
 * Implementations of this interface read a document or data fragment represented
 * in such formats as JSON, HTML, CSV, mail, RTF... and convert it into XML
 * representation based on adhoc Schema or DTD.
 * <p>An implementation must have a public constructor without arguments.
 */
public interface ContentImporter
{
    /**
     * Returns a list of names that can be used to designate the data format
     * imported by this object.
     * For example a CSV importer would return "csv", an HTML5 importer would
     * return {"html5", "html"} etc.
     */
    String[] getNames();
    
    /**
     * Configure with implementation-specific properties.  
     * @param options a set of properties, 
     * @throws DataModelException 
     */
    void configure(Properties options) throws DataModelException;
    
    /**
     * Takes the data input from a String.
     * @param data a String
     */
    void setInput(String data)
        throws DataModelException;
    
    /**
     * Takes the data input from a byte stream.
     * @param input a byte stream
     */
    void setInput(InputStream input)
        throws DataModelException;
    
    /**
     * Takes the data input from a character stream.
     * @param input a character stream
     */
    void setInput(Reader input)
        throws DataModelException;
     
    
    /**
     * Actual import work.
     * @param handler a push-style handler similar to {@link XMLEventWriter}. In
     *        order to use SAX2 rather than this interface, it only needs to be
     *        wrapped in a {@link SAXToPushStream} adapter:
     *        <code>new SAXToPushStream(handler)</code> returns an object which is
     *        both a {@link DefaultHandler} and a {@link LexicalHandler}
     * @param documentPath path of the resulting document in the XML Library. For
     *        information purpose.
     * @throws DataModelException
     */
    void parse(XMLPushStream handler, String documentPath)
        throws DataModelException;
    

    /**
     * Content Importers available by default.
     */
    public String[] DEFAULT_IMPORTERS = {
            "com.qizx.xmodule.importer.json.JsonImporter",
            "com.qizx.xmodule.importer.html.HTMLImporter",
    };
}
