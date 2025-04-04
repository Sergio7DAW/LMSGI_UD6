/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of the Qizx application components
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.apps.studio.gui;

import com.qizx.api.DataModelException;
import com.qizx.api.Node;
import com.qizx.api.QName;
import com.qizx.api.QizxException;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 *  Data Model node: strict display of XML contents.
 */
public class XdmNode extends TreePort.TNode
{
    private static final int MAX_LINE = 200;
    private static final int MAX_CHILDREN = 10000;

    static ImageIcon atomIcon = GUI.getIcon("atom.png");
    static ImageIcon elementIcon = GUI.getIcon("element.png");
    static ImageIcon textIcon = GUI.getIcon("text.png");
    static ImageIcon attrIcon = GUI.getIcon("attr.png");
    static ImageIcon piIcon = GUI.getIcon("pi.png");
    static ImageIcon commentIcon = GUI.getIcon("comment.png");
    static ImageIcon documentIcon = GUI.getIcon("document.png");
    

    protected com.qizx.api.Node node;

    public XdmNode(com.qizx.api.Node node)
    {
        this.node = node;
    }

    public boolean isLeaf()
    {
        return node == null || !node.isNode();
    }

    protected void procreate()
    {
        try {
            com.qizx.api.Node[] attrs = node.getAttributes();
            if(attrs != null)
                for (int a = 0; a < attrs.length; a++) {
                    add(new XdmNode(attrs[a]));
                }
            com.qizx.api.Node kid = node.getFirstChild();
            
            int guard = 0;
            for (; kid != null; kid = kid.getNextSibling()) {
                add(new XdmNode(kid));
                if(++guard > MAX_CHILDREN) {
                    add(new XdmNode(null));
                    break;  // TODO special 'too many' node
                }
            }
        }
        catch (QizxException e) {
            GUI.error(e);
        }
    }

    public String toString()
    {
        if (node == null)
            return "... more ...";
        try {
            String kind = node.getNodeKind();
            QName name = node.getNodeName();
            switch (node.getNodeNature()) {
            case Node.DOCUMENT:
                return kind;
            case Node.ELEMENT:
                StringBuffer buf = new StringBuffer(kind + " " + name);
                return buf.toString();
            case Node.TEXT:
                String sval = node.getStringValue();
                if(sval.length() > MAX_LINE)
                    sval = sval.substring(0, MAX_LINE) + " ...";
                return kind + " |" + sval + "|";
            case Node.COMMENT:
                return kind + " <!--" + node.getStringValue() + "-->";
            case Node.PROCESSING_INSTRUCTION:
                return kind
                       + " <?" + name + " " + node.getStringValue() + "?>";
            case Node.ATTRIBUTE:
            case Node.NAMESPACE:
                return kind + " " + node.getNodeName().toString()
                       + "=\"" + node.getStringValue() + '"';
            default:
                return kind + " " + node.getStringValue();
            }
        }
        catch (Exception e) {
            return e.toString();
        }
    }

    protected String getToolTip()
    {
        if(node == null)
            return "too many nodes";
        try {
            return node.getNodeKind() + " node";
        }
        catch (DataModelException e) {
            return "Error: " + e;
        }
    }

    public Icon getIcon(boolean selected, boolean expanded)
    {
        if(node == null)
            return null;
        try {
            switch (node.getNodeNature())
            {
            case Node.DOCUMENT:
                return documentIcon;
            case Node.ELEMENT:
                return elementIcon;
            case Node.ATTRIBUTE:
                return attrIcon;
            case Node.PROCESSING_INSTRUCTION:
                return piIcon;
            case Node.TEXT:
                return textIcon;
            case Node.COMMENT:
                return commentIcon;
            default:
                return atomIcon;
            }
        }
        catch (DataModelException e) {
            return null;
        }
    }
}
