/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.util.json;

import com.qizx.api.DataModelException;
import com.qizx.api.Node;
import com.qizx.api.QName;
import com.qizx.util.basic.XMLUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

public class JSONSerializer
{
    private Writer writer;
    private PrintWriter output;
    private int indent = -1;
    private boolean atBol;
    private int depth;
    private boolean compact;
    
    public JSONSerializer(Writer out)
    {
        this.writer = out;
        this.output = new PrintWriter(out);
    }

    public void setIndent(int indent)
    {
        this.indent = indent;
    }

    public int getIndent()
    {
        return indent;
    }

    public boolean getCompact()
    {
        return compact;
    }

    public void setCompact(boolean compact)
    {
        this.compact = compact;
    }

    public void serialize(Node data)
        throws DataModelException
    {
        if(data == null)
            return;
        QName name = data.getNodeName();
        if(JSONBuilder.E_MAP.equals(name))
            serializeMap(data);
        else if(JSONBuilder.E_ARRAY.equals(name))
            serializeArray(data);
        else if(JSONBuilder.E_STRING.equals(name)) {
            putStringValue(toString(data));
        }
        else if(JSONBuilder.E_NUMBER.equals(name)) {
            output.print(toString(data));            
        }
        else if(JSONBuilder.E_BOOL.equals(name)) {
            output.print(toString(data));   
        }
        else if(JSONBuilder.E_NULL.equals(name)) {
            output.print("null");
        }
        else throw new DataModelException("invalid JSON node: " +
                                          data.getNodeKind() +" name "+name);
    }
    
    public void serializeMap(Node data)
        throws DataModelException
    {
        Node kid = data.getFirstChild();
        boolean first = true;
        //boolean moreThanOne = kid != null && kid.getNextSibling() != null;
        
        putMapStart();
        for(int pos = 1; kid != null; kid = kid.getNextSibling(), ++pos)
        {
            if(!isJsonNode(kid))
                continue;
            doIndent();
            first = putComma(first);
            if (!JSONBuilder.E_PAIR.equals(kid.getNodeName()))
                throw new DataModelException("expecting 'pair' node in JSON map at position " + pos);
            Node nameAttr = kid.getAttribute(JSONBuilder.AT_NAME);
            if (nameAttr == null)
                throw new DataModelException("expecting name attribute in JSON pair at position " + pos);
            putMapKey(nameAttr.getStringValue());
            serialize(kid.getFirstChild());
        }
        putMapEnd();
    }

    public void serializeArray(Node data)
        throws DataModelException
    {
        Node kid = data.getFirstChild();
        boolean first = true;

        putArrayStart();
        for( ; kid != null; kid = kid.getNextSibling())
        {
            if(!isJsonNode(kid))
                continue;
            doIndent();
            first = putComma(first);
            serialize(kid);
        }
        putArrayEnd();
    }

    private boolean putComma(boolean first)
    {
        if(!first) {
            output.write(',');
            if(indent >= 0)
                println(true);
            else if(!compact)
                output.write(' ');
        }
        return false;
    }

    private boolean isJsonNode(Node node)
        throws DataModelException
    {
        return node.getNodeNature() == Node.ELEMENT;
    }

    private String toString(Node data)
    throws DataModelException
    {
        Node kid = data.getFirstChild();
        if (kid == null || kid.getNodeNature() != Node.TEXT)
            throw new DataModelException("malformed JSON value node");
        if (kid.getNextSibling() != null)
            throw new DataModelException("malformed JSON content");
        return kid.getStringValue();
    }

    public void flush()
    {
        output.flush();
    }
    
    public void putMapStart()
    {
        output.write('{');
        putStart();
    }

    public void putMapEnd()
    {
        putEnd();
        output.write('}');
    }

    public void putMapKey(String key)
    {
        putStringValue(key);
        output.write(':');
        if (!compact)
            output.write(' ');
    }
    
    public void putArrayStart()
    {
        output.write('[');
        putStart();
    }

    private void putStart()
    {
        if (indent >= 0) {
            ++ depth;
            println(false);
        }
        else if (!compact)
            output.write(' ');
    }

    public void putArrayEnd()
    {
        putEnd();
        output.write(']');
    }
    
    public void putStringValue(String string)
    {
        output.write('"');
        output.write(string); // TODO
        output.write('"');
    }

    public void putValue(String string)
    {
        output.write(string); 
    }

    private void putEnd()
    {
        if (indent >= 0) {
            -- depth;
            if(!atBol)
                println(true);
            else
                doIndent();
        }
        else if (!compact)
            output.write(' ');
    }

    private void printChar(char c)
        throws IOException
    {
        if(XMLUtil.isSurrogateChar(c)) {
            // TODO
        }
        else
            output.write(c);
    }
    
    public void println(boolean pad)
    {
        try {
            output.println();
            atBol = true;
            if(pad)
                doIndent();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doIndent()
    {
        if (indent >= 0 && atBol) {
            for (int i = depth * indent; --i >= 0; )
                output.print(' ');
            atBol = false;
        }
    }
}
