/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.util;

import com.qizx.api.Node;
import com.qizx.api.QName;
import com.qizx.api.XMLPushStream;
import com.qizx.xdm.CorePushBuilder;
import com.qizx.xdm.IQName;

public class TableGenerator
{
    private static final int TEXT = 0;
    private static final int JSON = 1;
    private static final int NODE = 2;

    private String[] fields;
    private int row;
    private int field;
    private int format;
    private StringBuilder buf;
    private boolean withHeaders = true;
    private XMLPushStream sout;
    private CorePushBuilder nodeBuilder;
    
    private QName rowsName = IQName.get("rows");
    private QName rowName = IQName.get("row");
    private QName cellName = null;
    private String jsonTopName;

    public TableGenerator(String[] fields)
    {
        this.fields = fields;
    }

    public void setJsonFormat(String topName)
    {
        format = JSON;
        jsonTopName = topName;
    }

    public void setTextFormat(boolean withHeaders)
    {
        format = TEXT;
        this.withHeaders = withHeaders;
    }

    public void setNodeFormat()
    {
        format = NODE;
        sout = nodeBuilder = new CorePushBuilder(".");
    }
    
    public void setTableElementName(String name)
    {
        rowsName = IQName.get(name);
    }

    public void setRowElementName(String name)
    {
        rowName = IQName.get(name);
    }
    
    public void setCellElementName(String name)
    {
        cellName  = IQName.get(name);
    }
    

    public Node getNode()
    {
        return nodeBuilder.harvest();
    }

    public void setXMLFormat()
    {
        // TODO
    }

    public void startTable()
        throws Exception
    {
        buf = new StringBuilder();
        row = 0;
        switch (format) {
        case TEXT:
            if (fields != null && withHeaders) {
                field = 0;
                for (String f : fields) {
                    if (field > 0)
                        put('\t');
                    put(f);
                    field++;
                }
                line();
            }
            break;
        case JSON:
            if (jsonTopName != null)
                put("{ " + jsonTopName + ": [\n");
            else
                put("[\n");
            break;
        case NODE:
            sout.putDocumentStart();
            sout.putElementStart(rowsName);
            break;
        }
    }

    public StringBuilder endTable()
        throws Exception
    {
        switch (format) {
        case JSON:
            put("\n] }\n");
            break;
        case NODE:
            sout.putElementEnd(rowsName);
            break;
        }
        return buf;
    }

    public void startRow()
        throws Exception
    {
        field = 0;
        switch (format) {
        case JSON:
            if (row > 0)
                put(",\n");
            put(" { ");
            break;
        case NODE:
            sout.putElementStart(rowName);
            break;
        }
    }

    public void endRow()
        throws Exception
    {
        row++;
        switch (format) {
        case TEXT:
            line();
            break;
        case JSON:
            put(" }");
            break;
        case NODE:
            sout.putElementEnd(rowName);
            break;
        }
    }

    public void fieldValue(String value)
        throws Exception
    {
        switch (format) {
        case TEXT:
            if (field > 0)
                put('\t');
            put(value);
            break;
        case JSON:
            if (field > 0)
                put(", ");
            put(fields[field]);
            put(": ");
            quoted(value);
            break;
        case NODE:
            QName name = IQName.get(fields[field]);
            sout.putElementStart(name);
            sout.putText(value);
            if(value == null || value.length() == 0)
                sout.putText("\u00a0");
            sout.putElementEnd(name);
            break;
        }
        ++field;
    }

    private void quoted(String s)
    {
//        int p = s.length();
//        for (; --p >= 0;)
//            if (!Character.isJavaIdentifierPart(s.charAt(p)))
//                break;
//
//        if (p < 0)
//            put(s);
//        else {
        
        put('"');
        put(s); // TODO better
        put('"');
    }

    private void put(String f)
    {
        buf.append(f);
    }

    private void put(char c)
    {
        buf.append(c);
    }

    private void line()
    {
        buf.append('\n');
    }

    public static void main(String[] args)
    {
        try {
            TableGenerator g = new TableGenerator(new String[] {
                "Id", "Raw Type", "value"
            });
            //g.setJsonFormat();
            g.startTable();
            for (int r = 0; r < 4; r++) {
                g.startRow();
                g.fieldValue("Id" + r);
                g.fieldValue("Ty " + r);
                g.fieldValue("Va" + r);
                g.endRow();
            }
            System.err.println("=\n" + g.endTable() + "<<");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void usage()
    {
        System.err.println("TableGenerator usage: ");
        System.exit(1);
    }
}
