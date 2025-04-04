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

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

/**
 * General scrolling view, with title and tool area.
 */
public class ToolView extends JPanel
{
    private GridBagger grid;
    private JPanel header;
    private JComponent title;
    private JToolBar tools;
    protected JScrollPane scrollPane;

    private JComponent view;
    private GridBagger headerGrid;

    public ToolView() {
        this("View", null);
    }

    public ToolView(String titleText, JComponent view)
    {
        grid = new GridBagger(this);
        grid.newRow();
        header = new JPanel();
        grid.add(header, grid.prop("xfill"));

        headerGrid = new GridBagger(header);
        headerGrid.newRow();
        title = new JLabel(titleText == null? null : (" " + titleText));
        title.setMinimumSize(new Dimension(10, 15));
        // tools on the right: xfill
        headerGrid.add(title, headerGrid.prop("xfill"));
        
        grid.newRow();
        scrollPane = new JScrollPane();
        if(view != null)
            scrollPane.setViewportView(view);
        grid.add(scrollPane, grid.prop("fill"));
    }


    public JComponent getTitle()
    {
        return title;
    }

    public void setTitle(JComponent title)
    {
        header.remove(this.title);
        this.title = title;
        header.add(title, new GridBagger(header).prop("xfill"), 0);
    }

    public void changeTitle(String titleText)
    {
        ((JLabel) title).setText(titleText);
    }

    public JComponent getTools()
    {
        return tools;
    }

    public JComponent getView()
    {
        return view;
    }

    public void setView(JComponent newView)
    {
        if(view != null)
            scrollPane.remove(view);
        scrollPane.setViewportView(newView);
        this.view = newView;
    }
    
    public void setDirectView(JComponent newView)
    {
        if(scrollPane != null) {            
            if(view != null)
                scrollPane.remove(view);
            remove(scrollPane);
            scrollPane = null;
        }
        grid.newRow();
        grid.add(newView, grid.prop("fill"));
        this.view = newView;
    }


    public void addToolSpace(int width, int position)
    {
        haveToolBar();
        tools.add(Box.createHorizontalStrut(width), position);
    }
    
    public void addTool(JComponent tool, int position)
    {
        haveToolBar();
        tools.add(tool, position);
    }

    private void haveToolBar()
    {
        if(tools == null) {
            tools = new JToolBar(); // new Box(BoxLayout.LINE_AXIS);
            tools.setFloatable(false);
            headerGrid.add(tools, headerGrid.weighted(0, 0));
        }
    }

    public void removeTool(JComponent tool)
    {
        if(tools != null)
            tools.remove(tool);
    }
}
