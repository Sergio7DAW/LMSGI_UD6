/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of the Qizx application components
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.apps.studio;

import com.qizx.apps.studio.gui.AppFrame;
import com.qizx.apps.studio.gui.BasicAction;
import com.qizx.apps.studio.gui.TreePort;
import com.qizx.apps.studio.gui.XdmNode;
import com.qizx.apps.studio.gui.XmlNode;
import com.qizx.apps.util.Property;
import com.qizx.apps.util.QizxConnector;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Map;

import javax.swing.JPopupMenu;

public class MetadataView extends XQDataView
{
    private ActionHandler handler;

    private QizxConnector connector;
    private Map<String, Property> properties;
    private String library;
    private String path;

    public MetadataView(AppFrame app)
    {
        super(app, "Metadata", true);
        this.app = app;
        changeFont(Font.BOLD, false);

        XQDataView.setTreeColors(getRenderer());
    }


    public interface ActionHandler
    {
        JPopupMenu getPropertiesMenu(MetadataView source, String libraryName,
                                     String path);

        JPopupMenu getPropertyMenu(MetadataView source, String libraryName,
                                   String path, Property property);
    }

    public QizxConnector getConnector()
    {
        return connector;
    }

    public void setHandler(ActionHandler handler)
    {
        this.handler = handler;
    }
    
    public void setProperties(Map<String, Property> props,
                              QizxConnector connector,
                              String libraryName, String path)
    {
        properties = props;
        this.connector = connector;
        this.library = libraryName;
        this.path = path;
        refresh();
    }

    private void refresh()
    {
        changeRoot(new PropertiesNode());
        expandAllVisible(40);
    }

    public class PropertiesNode extends TreePort.TNode
    {
        PropertiesNode() {
        }

        public void procreate()
        {
            try {
                String[] pnames = new String[properties.size()];
                int p = 0;
                for (String name : properties.keySet())
                    pnames[p++] = name;
                Arrays.sort(pnames);

                for (String name : pnames) {
                    add(new PropertyNode(properties.get(name)));
                }
            }
            catch (Exception e) {
                ;
            }
        }

        protected JPopupMenu getPopupMenu()
        {
            return handler == null? null
                : handler.getPropertiesMenu(MetadataView.this, library, path);
        }
        
        public boolean equals(Object obj)
        {
            if(obj == this)
                return true;
            return obj instanceof PropertiesNode;
        }
    }
    
    public class PropertyNode extends TreePort.ColoredNode
    {
        Property property;
        private String stringRep;

        PropertyNode(Property prop)
        {
            property = prop;
            
            StringBuilder buf = new StringBuilder();
            buf.append(prop.name);
            endSection(buf, XmlNode.TAG); 
            if (prop.type != null) {
                buf.append(" [");
                buf.append(prop.type);
                buf.append("]");
                endSection(buf, XmlNode.ATTR); 
            }
            buf.append(" = ");
            endSection(buf, XmlNode.TEXT_STYLE);
            if (prop.value != null) {
                buf.append(prop.value);
                endSection(buf, 0);
            }
            stringRep = buf.toString();
        }

        public String toString() {
            return stringRep;
        }

        public boolean isLeaf()
        {
            return (property.nodeValue == null);
        }

        // get children:
        public void procreate()
        {
            if (property.nodeValue == null)
                return;
            if(useMarkup)
                add(new XmlNode(property.nodeValue, MetadataView.this));
            else
                add(new XdmNode(property.nodeValue));
        }

        protected JPopupMenu getPopupMenu()
        {
            return handler == null? null
                : handler.getPropertyMenu(MetadataView.this, library, path, property);
        }
    }

    public void cmdMarkupView(ActionEvent e, BasicAction a)
    {
        useMarkup = true;
        refresh();
    }

    public void cmdDMView(ActionEvent e, BasicAction a)
    {
        useMarkup = false;
        refresh();
    }
}
