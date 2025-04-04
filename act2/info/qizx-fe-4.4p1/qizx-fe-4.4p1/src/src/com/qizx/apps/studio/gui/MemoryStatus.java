/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of the Qizx application components
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.apps.studio.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.*;

/**
 * Displays the memory use at regular intervals.
 */
public class MemoryStatus extends Box
{
    private static final int MB = 1048076;
    private JProgressBar display;
    Runtime runtime = Runtime.getRuntime();
    
    public MemoryStatus()
    {
        this(-1);
    }
    
    public MemoryStatus(int period)
    {
        super(BoxLayout.X_AXIS);
        JLabel label = new JLabel("Memory: ");
        Font smallFont = GUI.changeSize(label.getFont(), -2);
        label.setFont(smallFont);
        add(label);
        
        int maxMemory = (int) (runtime.maxMemory() / MB);

        display = new JProgressBar(0, maxMemory);
        display.setStringPainted(true);
        display.setPreferredSize(new Dimension(100, 15));
        display.setFont(smallFont);
        add(display);

        if(period > 0)
            setBeat(period);
    }
    
    public void displayState()
    {
        long totalMemory = runtime.totalMemory();
        long usedMemory = (totalMemory - runtime.freeMemory()) / MB;
        int maxMemory = (int) (runtime.maxMemory() / MB);
        display.setValue((int) usedMemory);
        display.setString((usedMemory * 100 / maxMemory) + "% of "
                          + maxMemory + "Mb");
    }
    
    public void setBeat(int period)
    {
        TimerTask task = new TimerTask() {
            public void run()
            {
                 displayState();
            }
        };
        new Timer().schedule(task, 0, period);
    }
}
