/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of the Qizx application components
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.apps.studio;

import com.qizx.api.EvaluationException;
import com.qizx.api.Item;
import com.qizx.api.ItemSequence;
import com.qizx.api.ItemType;
import com.qizx.api.Node;
import com.qizx.api.QizxException;
import com.qizx.api.util.XMLSerializer;
import com.qizx.apps.studio.dialogs.ExportDialog;
import com.qizx.apps.studio.gui.BasicAction;
import com.qizx.apps.studio.gui.TreePort;
import com.qizx.apps.studio.gui.XdmNode;
import com.qizx.apps.studio.gui.XmlNode;
import com.qizx.apps.util.QizxConnector;
import com.qizx.util.basic.QueueWorker;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Icon;
import javax.swing.SwingUtilities;

public class XQResultView extends XQDataView
{
    private static final String SETT_RESULTS_PATH = "results_path";
    private static final String RESULTS_USE_MARKUP = "results.use_markup";
    private Icon atomIcon;
    private BasicAction prevPageAction;
    private BasicAction nextPageAction;
    private BasicAction firstPageAction;
    private BasicAction lastPageAction;
    private BasicAction exportAction;

    private QizxStudio studio;
    private QueueWorker waiter;
    private BasicAction finishAction;
    
    private QizxConnector connector;
    private StatusHandler statusHandler;
    
    private QizxConnector.Query cachedQuery;
    private Throwable execError;
    private float execTime;
    private boolean initialExec;
    private int  pageSize = 100;
    private int  pageStart;
    private List<Item> pageItems;
    private long  totalItemCount;   // 


    public XQResultView(QizxStudio app, String title, boolean showRoot)
    {
        super(app, title, showRoot);
        studio = app;
        atomIcon = app.getIcon("atom.gif");
        
        useMarkup = app.getBoolSetting(RESULTS_USE_MARKUP, true);
        markupButton.setSelected(useMarkup);
        
        exportAction = new BasicAction(null, app.getIcon("save.png"),
                                       "cmdSaveResults", this);
        addToolSpace(10, 0);
        addToolbarButton(exportAction, "Save results", "results_save");

        nextPageAction = new BasicAction(null, app.getIcon("resultset_next.png"),
                                         "cmdNextPage", this);
        prevPageAction = new BasicAction(null, app.getIcon("resultset_prev.png"),
                                         "cmdPrevPage", this);
        firstPageAction= new BasicAction(null, app.getIcon("resultset_first.png"),
                                         "cmdFirstPage", this);
        lastPageAction = new BasicAction(null, app.getIcon("resultset_last.png"),
                                         "cmdLastPage", this);
        actionStatus();
        
        addToolSpace(10, 0);
        addToolbarButton(lastPageAction, "Display last results", "results_lp");
        addToolbarButton(nextPageAction, "Display next results", "results_np");
        addToolbarButton(prevPageAction, "Display previous results", "results_pp");
        addToolbarButton(firstPageAction, "Display first results", "results_fp");
    }

    public StatusHandler getStatusHandler()
    {
        return statusHandler;
    }

    public void setStatusHandler(StatusHandler statusHandler)
    {
        this.statusHandler = statusHandler;
    }

    public void saveSettings()
    {
        app.saveSetting(RESULTS_USE_MARKUP, useMarkup);
    }
    
    /**
     * Executes a query and displays the results (or errors).
     * Execution is done in a service thread, so this method returns before
     * the end of actual display.
     */
    public void execQuery(String query, QizxConnector connector,
                          String libraryName, String queryDomain,
                          String mode,
                          BasicAction finishAction)
    {
        this.connector = connector;
        this.finishAction = finishAction;
        cachedQuery = new QizxConnector.Query(query, libraryName, mode);
        cachedQuery.setQueryDomain(queryDomain);
        
        initialExec = true;
        pageStart = 0;
        totalItemCount = 0;
        displayPageItems();
    }
    
    public void cancelQuery()
    {
        if(cachedQuery != null)
            cachedQuery.cancel();
    }

    public void cmdFirstPage(ActionEvent e, BasicAction action)
    {
        pageStart = 0;
        displayPageItems();
    }
    
    public void cmdPrevPage(ActionEvent e, BasicAction action)
    {
        pageStart -= pageSize;
        displayPageItems();
    }
    
    public void cmdNextPage(ActionEvent e, BasicAction action)
    {
        pageStart += pageSize;
        displayPageItems();
    }

    public void cmdLastPage(ActionEvent e, BasicAction action)
    {
        pageStart = (int) ((totalItemCount - 1) / pageSize) * pageSize;
        displayPageItems();
    }
    
    @Override
    public void cmdMarkupView(ActionEvent e, BasicAction a)
    {
        useMarkup = true;
        refreshPage();
    }
    
    @Override
    public void cmdDMView(ActionEvent e, BasicAction a)
    {
        useMarkup = false;
        refreshPage();
    }

    public void cmdSaveResults(ActionEvent e, BasicAction a)
    {
        String title = "Export results to file";
        Item sit = pageItems.size() > 0? pageItems.get(0) : null;
        boolean wellFormed = (totalItemCount == 1) && sit != null &&  sit.isNode();
        String suggested = app.getSetting(SETT_RESULTS_PATH);
        if(suggested == null)
            suggested = "results.xml";

        ExportDialog exportDialog = studio.getExportDialog();
        XMLSerializer sout =  exportDialog.showUp(suggested, wellFormed, title);
        app.saveSetting(SETT_RESULTS_PATH, exportDialog.getFilePath());

        if(sout == null)
            return;
        
        try {
            ItemSequence items = connector.execute(cachedQuery, 0, -1);
            for ( ; items.moveToNextItem(); ) {
                Item item = items.getCurrentItem();
                if(item.isNode())
                    sout.putNodeCopy(item.getNode(), 0);
                else
                    sout.putAtomText(item.getString());
                sout.println();
            }
            sout.flush();
            sout.getOutput().close();
        }
        catch (Throwable ex) {
            app.showError(ex);
            return;
        }
    }

    /**
     * Requests a page of items from Qizx connector, in a separate thread.
     * Invokes the real execution in local mode.
     */
    private void displayPageItems()
    {
        if(waiter == null) {
            waiter = new QueueWorker("XQExecutor");
            waiter.start();
        }
        app.waitCursor(true);
        waiter.queueTask(executionTask);
    }

    // executed in service thread
    private Runnable executionTask = new Runnable() {
        public void run()
        {
            execError = null;
            execTime = 0;
            long t0 = System.nanoTime();
            
            try {
                pageItems = connector.executeExpand(cachedQuery, 
                                                    pageStart, pageSize);
            }
            catch (Throwable e) {
                execError = e;
                if(studio.showStack()) // of each (aka Dimitri)
                    e.printStackTrace();
                pageItems = null;
            }
            execTime = ((System.nanoTime() - t0) / 100000) / 10.0f;
            
            // refresh in the GUI thread:
            SwingUtilities.invokeLater(displayTask);
        }
    };
    
    // executed in Swing thread
    private Runnable displayTask = new Runnable() {

        public void run()
        {
            app.waitCursor(false);

            totalItemCount = cachedQuery.getTotalItemCount();

            refreshPage();
            actionStatus();

            if(finishAction != null) {
                finishAction.actionPerformed(null);
                finishAction = null;
            }
            
            if(statusHandler != null) {
                statusHandler.executionStatus(totalItemCount, initialExec,
                                              execTime, execError);
//                System.err.println("compile time " + cachedQuery.getCompileTime()/1000);

            }
            
            if (cachedQuery.hasProfiling()) {
                studio.displayProfiling(cachedQuery.profiling);
            }
                
            initialExec = false;
        }
    };
    
    private void actionStatus()
    {
        lastPageAction.setEnabled(pageStart + pageSize < totalItemCount);
        nextPageAction.setEnabled(pageStart + pageSize < totalItemCount);
        prevPageAction.setEnabled(pageStart > 0);
        firstPageAction.setEnabled(pageStart > 0);
        
        exportAction.setEnabled(pageItems != null);
    }
    
    private void refreshPage()
    {
        long end = Math.min(pageStart + pageSize, totalItemCount);
        if (totalItemCount == 0)
            changeTitle(" Result items: none");
        else
            changeTitle(" Result items " + (pageStart + 1)
                        + " to " + end + " of " + totalItemCount);
        
        changeRoot(new ResultItems());
        expandAllVisible(40);
        getTree().scrollRowToVisible(0);
    }

    public interface StatusHandler
    {
        void executionStatus(long itemCount, boolean firstExecution,
                             float execTime /*ms*/, Throwable error);
    }
    
    // Top node for result display
    public class ResultItems extends TreePort.TNode
    {
        public void procreate()
        {
            if(pageItems != null) {
                try {
                    for(int i = 0, size = pageItems.size(); i < size; i++) {
                        Item item = pageItems.get(i);
                        if(item == null || !item.isNode()) {
                            add(new ItemNode(item));    // must be local
                        }
                        else { // node: can be wrapped if remote
                            Node node = item.getNode();
                            if(useMarkup)
                                add(new XmlNode(node, XQResultView.this));
                            else
                                add(new XdmNode(node));
                        }
                    }
                }
                catch (EvaluationException e) {
                    app.showError(e);
                }
            }
        }
    }
    
    // displays an Item in the item tree view (EXCEPT nodes)
    public class ItemNode extends TNode
    {
        Item item;
        String type;    // explicit type (item is TEXT node)
        
        ItemNode(Item item)
        {
            this.item = item;
        }

        public ItemNode(Node child, String type)
        {
            this.item = child;
            this.type = type;
            try {
                if(type == null && item.getType() != null)
                    this.type = item.getType().getShortName();
            }
            catch (EvaluationException e) {
                System.err.println("cannot get type of item: "+e);
            }
        }

        public Icon getIcon(boolean selected, boolean expanded)
        {
            return atomIcon;
        }

        protected String getToolTip()
        {
            if (item == null)
                return null;
            return "an item of type " + type;
        }

        public String toString()
        {
            if (item == null)
                return "...";
            try {
                if(type == null) {
                    ItemType itype = item.getType();
//                    if (itype.isSubTypeOf(execQuery.getType("string")))
//                        return itype + " = '" + item.getString() + "'";
                    return itype + " = " + item.getString();
                }
                else
                    return type + " = " + item.getString();
            }
            catch (Exception e) {
                return e.toString();
            }
        }

        public boolean isLeaf()
        {
            try {
                return (item == null || !item.isNode()
                        || item.getNode().getFirstChild() == null);
            }
            catch (QizxException ignored) {
            }
            return true;
        }

        // get children:
        public void procreate()
        {
        }
    }
}
