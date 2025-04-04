/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of the Qizx application components
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.apps.studio;

import com.qizx.api.LibraryMember;
import com.qizx.api.LibraryMemberIterator;
import com.qizx.api.NonXMLDocument;
import com.qizx.api.QizxException;
import com.qizx.apps.studio.gui.AppFrame;
import com.qizx.apps.studio.gui.BasicAction;
import com.qizx.apps.studio.gui.TreePort;
import com.qizx.apps.util.QizxConnector;
import com.qizx.util.basic.PathUtil;

import java.awt.event.ActionEvent;

import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.event.TreeSelectionEvent;

/**
 * Naked XML Library browser.
 * All logic is implemented through ActionHandler.
 */
public class LibraryBrowser extends TreePort
{
    public static final int MAX_CHILDREN = 1000;
    protected AppFrame app;
    protected QizxConnector connector;
    protected ActionHandler handler;

    protected Icon serverIcon;
    protected Icon libIcon;
    protected Icon collectionIcon;
    protected Icon documentIcon;
    protected Icon nxdataIcon;


    public LibraryBrowser(AppFrame app)
    {
        super(null, true, null);
        this.app = app;
        
        serverIcon = app.getIcon("server.png");
        libIcon    = app.getIcon("library.png");
        collectionIcon = app.getIcon("collection.png");
        documentIcon = app.getIcon("document.png");
        nxdataIcon = app.getIcon("nxdoc.png");
        changeRoot(new LibManagerNode());
    }

    public QizxConnector getConnector()
    {
        return connector;
    }

    public void setConnector(QizxConnector connector)
    {
        this.connector = connector;
        changeRoot(new LibManagerNode());
    }
    
    public ActionHandler getHandler()
    {
        return handler;
    }

    public void setHandler(ActionHandler handler)
    {
        this.handler = handler;
    }

    /**
     * Redisplays the tree inside the node specified by library:path.
     * Lateral nodes are kept in the same expanded/collapsed state.
     */
    public void refresh(String library, String path)
    {
        // creates problems: so inhibit handlers
        ActionHandler saveHandler = handler;
        try {
            handler = null;
            if(connector != null)
                connector.refresh(library);
            smartRefresh(null); // TODO
        }
        catch (Exception e) {
            e.printStackTrace();    // what TODO?
        }
        finally {
            handler = saveHandler;
        }
    }
    
    public interface ActionHandler
    {   
        void selectedLibrary(LibraryBrowser source, String libraryName);
        
        void selectedCollection(LibraryBrowser source, String libraryName,
                                String path);
        
        void selectedDocument(LibraryBrowser source, String libraryName,
                              String path);

        JPopupMenu getDatabaseMenu(LibraryBrowser source);

        JPopupMenu getLibraryMenu(LibraryBrowser source,
                                  String libraryName);
        
        JPopupMenu getCollectionMenu(LibraryBrowser source,
                                     String libraryName, String path);
        JPopupMenu getDocumentMenu(LibraryBrowser source,
                                   String libraryName, String path);
    }

    public class LibManagerNode extends TreePort.TNode
    {
        LibManagerNode() {
        }
        
        public Icon getIcon(boolean selected, boolean expanded) {
            return serverIcon;
        }
        
        public String toString()
        {
            if(connector == null)
                return "[No XML Libraries]";
            return connector.getDisplay();
        }

        public boolean equals(Object obj)
        {
            if(!(obj instanceof LibManagerNode))
                return false;
            return true;
        }

        public void procreate()
        {
            if(connector == null)
                return;
            try {
                try {
                    String[] libs = connector.listLibraries();
                    if(libs != null) {
                        for(int li = 0; li < libs.length; li++) {
                            add( new LibraryNode(libs[li]) );
                        }
                    }
                }
                catch (QizxException e) { // no privileges?
                    children.clear();
                }
            }
            catch (Exception e) {
                app.showError("Cannot list XML Libraries: ", e);
            }
        }

        protected JPopupMenu getPopupMenu()
        {
            return (handler == null)?
                     null : handler.getDatabaseMenu(LibraryBrowser.this);
        }
    }
    
    public class LibraryNode extends TreePort.TNode
    {
        public String name;
        
        LibraryNode(String name)
        {
            super();
            this.name = name;
        }

        public Icon getIcon(boolean selected, boolean expanded) {
            return libIcon;
        }
        
        public String toString() {
            try {
                return "XML library '" + name +"'";
            }
            catch (Exception e) { return e.toString(); }
        }

        public boolean equals(Object obj)
        {
            if(obj == this)
                return true;
            if(!(obj instanceof LibraryNode))
                return false;
            LibraryNode that = (LibraryNode) obj;
            return that.name.equals(name);
        }

        public void procreate()
        {
            add(new CollectionNode("/", name));
        }
        
        protected void selected(TreeSelectionEvent e)
        {
            if (handler != null)
                handler.selectedLibrary(LibraryBrowser.this, name);
        }

        protected JPopupMenu getPopupMenu()
        {
            return (handler == null)? 
                       null : handler.getLibraryMenu(LibraryBrowser.this, name);            
        }
    }
    
    public class CollectionNode extends TreePort.TNode
    {
        String libraryName;
        String path;
        int pageStart;

        CollectionNode(String path, String libName)
        {
            this.path = path;
            this.libraryName = libName;
        }

        public Icon getIcon(boolean selected, boolean expanded)
        {
            return collectionIcon;
        }

        protected String getToolTip()
        {
            return "Collection('" + path + "')";
        }

        public String toString()
        {
            return PathUtil.getBaseName(path);
        }

        public boolean equals(Object obj)
        {
            if(obj == this)
                return true;
            if(!(obj instanceof CollectionNode))
                return false;
            CollectionNode that = (CollectionNode) obj;
            return that.path.equals(path);
        }

        public void procreate()
        {
            int end = pageStart + MAX_CHILDREN;
            if (connector == null)
                return;

            try {
                LibraryMemberIterator iter = 
                    connector.getChildren(libraryName, path, MAX_CHILDREN);

                if (pageStart > 0)
                    add(new NavigationNode("Previous "+ MAX_CHILDREN
                                           +" [of "+ pageStart +"]",
                                new BasicAction("", "cmdCollPrev", this)));
                
                for (int count = 0; iter.moveToNextMember(); ++count)
                {
                    if (count < pageStart)
                        continue;
                    LibraryMember member = iter.getCurrentMember();
                    
                    if(member.isDocument()) {
                        add(new DocumentNode(member.getPath(), libraryName, 
                                             member instanceof NonXMLDocument));
                    }
                    else {
                        add(new CollectionNode(member.getPath(), libraryName));
                    }
                    if (count > end) {
                        add(new NavigationNode("Next " + MAX_CHILDREN,
                                    new BasicAction("", "cmdCollNext", this)));
                        break;
                    }
                }
            }
            catch (Exception e) {
                app.getStatusBar().transientMessage("Got exception " + e);
                app.showError(e);
            }
        }

        protected void selected(TreeSelectionEvent e)
        {
            if (handler != null)
                handler.selectedCollection(LibraryBrowser.this, libraryName, path);            
        }

        public void cmdCollNext(ActionEvent e, BasicAction a)
        {
            removeAllChildren();
            pageStart += MAX_CHILDREN;
            refreshNodeContent(this);
        }
        
        public void cmdCollPrev(ActionEvent e, BasicAction a)
        {
            removeAllChildren();
            pageStart = Math.max(0, pageStart - MAX_CHILDREN);
            refreshNodeContent(this);
        }

        protected JPopupMenu getPopupMenu()
        {
            return (handler == null)? null
                      : handler.getCollectionMenu(LibraryBrowser.this, libraryName, path);                        
        }
    }
    
    
    public class DocumentNode extends TreePort.TLeaf
    {
        String libraryName;
        String path;
        boolean isNonXML;

        public DocumentNode(String path, String libName, boolean isNonXML)
        {
            this.path = path;
            this.libraryName = libName;
            this.isNonXML = isNonXML;
        }

        public Icon getIcon(boolean selected, boolean expanded)
        {
            return isNonXML? nxdataIcon : documentIcon;
        }

        public String toString()
        {
            return PathUtil.getBaseName(path);
        }

        public boolean equals(Object obj)
        {
            if(obj == this)
                return true;
            if(!(obj instanceof DocumentNode))
                return false;
            DocumentNode that = (DocumentNode) obj;
            return that.path.equals(path);
        }

        protected String getToolTip()
        {
            return "Document('" + path + "')";
        }

        protected JPopupMenu getPopupMenu()
        {
            return (handler == null)? null
               : handler.getDocumentMenu(LibraryBrowser.this, libraryName, path);  
        }

        protected void selected(TreeSelectionEvent e)
        {
            if (handler != null)
                handler.selectedDocument(LibraryBrowser.this, libraryName, path);            
        }
    }
}
