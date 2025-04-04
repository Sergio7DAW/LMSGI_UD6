/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of the Qizx application components
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */

package com.qizx.apps.studio.gui;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * A menu that shows an an history with tool tips.
 */
public class HistoryMenu extends MenuButton
    implements ListDataListener
{
    private HistoryModel model;
    private int maxWidth = 25;

    public HistoryMenu(String title, HistoryModel model)
    {
        super(title);
        this.model = model;
        model.addListDataListener(this);
        rebuild();
    }
    
    // Redefinable callback.
    protected void selected(Object item) { }

    public void rebuild()
    {
        JPopupMenu pop = getPopupMenu();
        pop.removeAll();
        int size = Math.min(model.getSize(), 40);
        for(int it = 0; it < size; it++) {
            Object entry = model.getElementAt(it);
            pop.add(new Item(entry));
        }
        pop.validate();
    }
    
    class Item extends JMenuItem 
    {
        Object item;
        
        Item(Object item) {
            this.item = item;
            String text = item.toString();
            String shortText = text;
            if(shortText.length() > maxWidth)
                shortText = shortText.substring(0, maxWidth) + "...";
            setText(shortText);
            if(shortText != text)
                setToolTipText("<html><pre>" + text);
            addActionListener(selectAction);
        }

        public Point getToolTipLocation(MouseEvent event)
        {   // try to be beside, not over!
            return new Point(0, 25);
        }
        
    }
    
    public void cmdSelect(ActionEvent e, BasicAction a)
    {
        Item source = (Item) e.getSource();
        selected(source.item);
    }

    private BasicAction selectAction = 
        new BasicAction("", "cmdSelect", this);
    
    // ----------- ListDataListener ---------------------------
    
    public void contentsChanged(ListDataEvent e)
    {
        rebuild();  // not lazy
    }

    public void intervalAdded(ListDataEvent e)
    {
        rebuild();  // not lazy
    }

    public void intervalRemoved(ListDataEvent e)
    {
        rebuild();  // not lazy
    }    
}
