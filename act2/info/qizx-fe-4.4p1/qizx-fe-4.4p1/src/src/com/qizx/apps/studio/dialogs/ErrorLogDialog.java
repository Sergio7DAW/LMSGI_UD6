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
import com.qizx.apps.studio.gui.DialogBase;
import com.qizx.apps.studio.gui.GridBagger;
import com.qizx.apps.studio.gui.TextPort;
import com.qizx.apps.studio.gui.TextPortLogger;

import java.awt.Dimension;
import java.awt.Frame;
import java.util.logging.Level;

public class ErrorLogDialog extends DialogBase
{
    private TextPort textArea;
    private TextPortLogger appender;
    protected boolean autoShow = true;
    
    public ErrorLogDialog(Frame parent)
    {
        super(parent, parent.getTitle() + " Error log");
        haveOnlyCloseButton();
        
        setModal(false);
        Help.setDialogHelp(this, "error_dialog");
        
        textArea = new TextPort("Error messages", 60);
        GridBagger grid = new GridBagger(form, 0, 0);
        grid.add(textArea, grid.prop("fill"));
        textArea.setPreferredSize(new Dimension(600, 400));
        appender = new TextPortLogger(textArea) {
            protected void newEvent(Level level)
            {
                if(autoShow && level.intValue() >= Level.WARNING.intValue())
                    showUp();
            }
        };

        pack();
    }

    public boolean isAutoShowing()
    {
        return autoShow;
    }

    public void setAutoShowing(boolean autoShow)
    {
        this.autoShow = autoShow;
    }

    public TextPortLogger getLogHandler()
    {
        return appender;
    }

}
