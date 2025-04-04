/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of the Qizx application components
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.apps.studio.dialogs;

import com.qizx.apps.studio.Help;
import com.qizx.apps.studio.QizxStudio;
import com.qizx.apps.studio.QizxStudio.BarObserver;
import com.qizx.apps.studio.QizxStudio.MemberAction;
import com.qizx.apps.studio.gui.AppFrame;
import com.qizx.apps.studio.gui.BasicAction;
import com.qizx.apps.studio.gui.DialogBase;
import com.qizx.apps.studio.gui.GUI;
import com.qizx.apps.studio.gui.GridBagger;
import com.qizx.apps.studio.gui.Localization;
import com.qizx.apps.util.QizxConnector;

import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JProgressBar;

/**
 * 
 */
public class ReindexingDialog extends DialogBase
{
    private static Localization local =
        new Localization(QizxStudio.class, "ReindexingDialog");

    private JButton button;
    private JProgressBar progressBar;

    private AppFrame app;

    private MemberAction target;
    
    public ReindexingDialog(AppFrame parent)
    {
        super(parent, local.text("Library_Reindexing"));
        this.app = parent;
        buildContents();
        haveOnlyCloseButton();
        Help.setDialogHelp(this, "reindexing_dialog");
    }
    
    public void showUpFor(MemberAction target)
    {
        this.target = target;
        button.setEnabled(true);
        progressBar.setValue(0);
        setTitle("Reindex Library " + target.library);
        showUp();
    }

    private void buildContents()
    {
        setHint(local.text("hint"), true);
        
        GridBagger grid = new GridBagger(form, HGAP, 0);

        grid.newRow();
        progressBar = new JProgressBar(0, 100);
        grid.add(progressBar, grid.prop("xfill").leftMargin(HGAP));       

        button = new JButton(new BasicAction(local.text("Rebuild"),
                                             "cmdReindex", this));
        grid.add(button);       
    }
    
    public void cmdReindex(ActionEvent ev, BasicAction a) 
    {
        try {
            new Task().start();            
        }
        catch (Exception e) {
            e.printStackTrace();
            GUI.error(e.getMessage());
        }
    }
    
    class Task extends Thread
    {
        public void run()
        {
            try {
                button.setEnabled(false);
                QizxConnector ctor = target.browser.getConnector();
                ctor.reindex(target.library, new BarObserver(progressBar));
                cmdOK(null, null);
            }
            catch (Exception e) {
                app.showError(e);
            }
            finally {
                button.setEnabled(true);
            }            
        }
    }
}
