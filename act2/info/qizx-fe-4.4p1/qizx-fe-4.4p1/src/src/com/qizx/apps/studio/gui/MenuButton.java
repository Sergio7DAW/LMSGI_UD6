/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of the Qizx application components
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.apps.studio.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;

/**
 * Drop-down menu. A button that brings up a popup menu.
 * Different Look and feel than JMenu in JMenubar.
 */
public class MenuButton extends JButton
{
    private static ImageIcon dropIcon = GUI.getIcon("dropdown.png");
    private JPopupMenu popup;
    
    public MenuButton(Action a)
    {
        super(a);
        init();
    }

    public MenuButton(Icon icon)
    {
        super(icon);
        init();
    }

    public MenuButton(String text, Icon icon)
    {
        super(text, icon);
        init();
    }

    public MenuButton(String text)
    {
        super(text);
        init();
    }

    public MenuButton() {
        init();
    }
    
    private void init()
    {
        popup = new JPopupMenu();
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                popupHook();
                popup.show(MenuButton.this, 0, getHeight());
            }
        });
        setHorizontalTextPosition(SwingConstants.LEFT);
        setIcon(dropIcon);
        setIconTextGap(3);
    }

    public JPopupMenu getPopupMenu() {
        return popup;
    }
    
    /**
     * Called just before the popup appears. Can be redefined for updating
     * the menu.
     */
    protected void popupHook() { }
    
}
