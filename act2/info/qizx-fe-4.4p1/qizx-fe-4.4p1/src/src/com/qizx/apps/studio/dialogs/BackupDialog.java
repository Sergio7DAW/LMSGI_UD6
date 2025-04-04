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
import com.qizx.apps.studio.gui.*;
import com.qizx.apps.util.QizxConnector;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JProgressBar;

/**
 * Manage indexing properties for a XML Library, run index rebuilding or
 * optimization.
 */
public class BackupDialog extends DialogBase
{
    private static Localization local =
        new Localization(QizxStudio.class, "BackupDialog");

    private JButton backupButton;
    private FileSelector fileSelector;
    private JProgressBar progressBar;

    private AppFrame app;
    private MemberAction target;

    public BackupDialog(AppFrame parent)
    {
        super(parent, local.text("Library_Backup"));
        this.app = parent;
        buildContents();
        haveOnlyCloseButton();
        Help.setDialogHelp(this, "backup_dialog");
    }

    public void showUpFor(MemberAction target)
    {
        this.target = target;
        backupButton.setEnabled(true);
        progressBar.setValue(0);

        fileSelector.setPath(app.getSetting(QizxStudio.SETTING_BACKUP_DIR));
        showUp();
        app.saveSetting(QizxStudio.SETTING_BACKUP_DIR, fileSelector.getPath());
    }

    protected void buildContents()
    {
        GridBagger grid = new GridBagger(form, 0, VGAP);

        setHint(local.text("hint"), true);
        
        grid.newRow();
        fileSelector = new FileSelector(20, false, true);
        fileSelector.getFileChooser().setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        grid.add(fileSelector, grid.prop("xfill").spans(2, 1));

        grid.newRow();

        progressBar = new JProgressBar(0, 100);
        grid.add(progressBar, grid.prop("xfill"));

        backupButton = new JButton(new BasicAction(local.text("Backup"),
                                                   "cmdBackup", this));
        grid.add(backupButton, grid.leftMargin(HGAP));
    }

    public void cmdBackup(ActionEvent ev, BasicAction a)
    {
        String backupDir = fileSelector.getPath();
        if (backupDir == null) {
            GUI.error("Please select a directory");
            return;
        }
        try {
            new BackupTask(new File(backupDir)).start();
        }
        catch (Exception e) {
            e.printStackTrace();
            GUI.error(e.getMessage());
        }
    }


    class BackupTask extends Thread
    {
        File backupDir;

        public BackupTask(File backupDir)
        {
            this.backupDir = backupDir;
        }

        public void run()
        {
            try {
                backupButton.setEnabled(false);
                QizxConnector ctor = target.browser.getConnector();
                ctor.backup(target.library, backupDir,
                              new BarObserver(progressBar));
                cmdOK(null, null);
            }
            catch (Exception e) {
                app.showError(e);
            }
            finally {
                backupButton.setEnabled(true);
            }
        }
    }
}
