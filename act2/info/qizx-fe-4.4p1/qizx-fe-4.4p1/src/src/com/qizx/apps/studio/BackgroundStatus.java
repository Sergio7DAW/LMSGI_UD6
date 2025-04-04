/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of the Qizx application components
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */

package com.qizx.apps.studio;

import com.qizx.api.LibraryManager;
import com.qizx.apps.studio.gui.GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

/**
 * Displays the background activity of XLib server at regular intervals.
 */
public class BackgroundStatus extends Box
{
    private JProgressBar display;
    private JLabel task;
    private LibraryManager manager;
    
    public BackgroundStatus(LibraryManager manager, int period)
    {
        super(BoxLayout.X_AXIS);
        
        task = new JLabel(" ");
        Font smallFont = GUI.changeSize(task.getFont(), -2);
        task.setFont(smallFont);
        task.setForeground(Color.blue);
        add(task);
        add(Box.createHorizontalStrut(4));
        
        display = new JProgressBar(0, 20);
        display.setIndeterminate(false);
        display.setPreferredSize(new Dimension(30, 12));
        display.setToolTipText("Background activity");
        add(display);

        
        setManager(manager);
        
        TimerTask task = new TimerTask() {
            public void run()
            {
                 displayState();
            }
        };
        new Timer().schedule(task, 0, period);
    }
    
    public void displayState()
    {
        if(manager == null)
            return;
        //TODO
//        Gauge gauge = engine.monitor(XLib.MON_BACKGROUND);
//        display.setIndeterminate(gauge != null);
//        task.setText((gauge != null)? gauge.getName() : " ");
    }

    public void setManager(LibraryManager manager)
    {
        this.manager = manager;
    }
}
