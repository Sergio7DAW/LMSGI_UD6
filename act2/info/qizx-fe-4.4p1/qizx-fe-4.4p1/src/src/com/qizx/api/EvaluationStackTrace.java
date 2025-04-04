/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.api;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * A level in an evaluation Stack Trace. 
 * Points to a location in a source query expression or module.
 * <p>
 * Returned by an EvaluationException or by the future debugger interface.
 */
public class EvaluationStackTrace
{
    private String signature;
    private String moduleURI;
    private String sourceLine;

    private int lineNumber;
    private int columnNumber;
    private int position;
    private int endPos;

    /**
     * For internal use.
     * @param signature
     * @param moduleURI
     * @param lineNumber
     * @param srcLine
     * @param columnNumber
     * @param position
     * @param endOffset 
     */
    public EvaluationStackTrace(String signature, String moduleURI,
                                int lineNumber, String srcLine, int columnNumber,
                                int position, int endPosition)
    {
        this.signature = signature;
        this.moduleURI = moduleURI;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
        this.position = position;
        this.endPos = endPosition <= position? (position + 1) : endPosition;
        this.sourceLine = srcLine;
    }

    /**
     * Returns the signature of the called function, or null in the outermost
     * stack level (main query).
     * @return a printable form of the function signature (name, arguments,
     *         returned type)
     */
    public String getSignature()
    {
        return signature;
    }

    /**
     * Returns the URI of the module location for this stack level.
     * @return the physical URI of the module location. Can be null for the
     *         main query.
     */
    public String getModuleURI()
    {
        return moduleURI;
    }

    /**
     * Gets the character position of the evaluation point in this stack level.
     * @return the character position of the evaluation point in the source
     *         XQuery expression.
     */
    public int getPosition()
    {
        return position;
    }
    
    /**
     * Gets the character end-position of the evaluated expression in this stack level.
     * @return the character end-position of the evaluation point in the source
     *         XQuery expression.
     */
    public int getEndPosition()
    {
        return endPos;
    }

    /**
     * Gets the column number of the evaluation point in this stack level.
     * @return the column number of the evaluation point in the source XQuery
     *         expression.
     */
    public int getColumnNumber()
    {
        return columnNumber;
    }

    /**
     * Gets the line number of the evaluation point in this stack level.
     * @return the line number of the evaluation point in the source XQuery
     *         expression.
     */
    public int getLineNumber()
    {
        return lineNumber;
    }

    /**
     * Gets the text of the line in the source code where the evaluation pointer sits.
     * @return the source line without line terminator
     */
    public String getSourceLine()
    {
        return sourceLine;
    }

    /**
     * Prints the location and the function signature if any.
     * @param output a PrintWriter output
     */
    public void print(PrintWriter output)
    {
        String sig = getSignature();
        if(sig == null)
            sig = "main query";
        output.print(" in " + sig +
                     " at line " + lineNumber + " column " + columnNumber);
        if(moduleURI != null)
            output.print(" in " + moduleURI);
        output.println();
    }
    
    
    public String toString()
    {
        StringWriter out = new StringWriter();
        print(new PrintWriter(out));
        return out.toString();
    }

}
