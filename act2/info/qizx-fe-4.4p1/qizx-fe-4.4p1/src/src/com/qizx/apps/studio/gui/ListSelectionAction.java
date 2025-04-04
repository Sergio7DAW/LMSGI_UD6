/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of the Qizx application components
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.apps.studio.gui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JComponent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Action based on reflection...
 */
public class ListSelectionAction
    implements ListSelectionListener
{
    protected Method action;
    protected Object target;
    protected JComponent source;
    
    public JComponent getSource()
    {
        return source;
    }

    /**
     * Creates an action that calls the specified method on the object.
     * @param method name of a public method in the target object. Must have one
     *        argument of class ActionEvent.
     * @param target target of this action.
     */
    public ListSelectionAction(String method, Object target, JComponent source)
    {
        this.source = source;
        this.target = target;
        try {
            this.action = target.getClass().getMethod(method, new Class[] {
                ListSelectionEvent.class, getClass()
            });
        }
        catch (Exception e) { // not runtime
            System.err.println("*** " + e);
        }
    }

    public void valueChanged(ListSelectionEvent e)
    {
        if (e.getValueIsAdjusting())
            return;
        Throwable caught;
        try {
            if (action != null)
                action.invoke(target, new Object[] {
                    e, this
                });
            return;
        }
        catch (InvocationTargetException e1) {
            caught = e1.getCause();
        }
        catch (Exception e1) {
            caught = e1;
        }
        // signal error: TODO log
        String msg = "<html><b>" + caught.toString() + "</b>";
        StackTraceElement[] st = caught.getStackTrace();
        for (int s = 0; s < st.length; s++) {
            msg += "<br>" + st[s];
        }
        GUI.message("Internal Error", msg);
        caught.printStackTrace();
    }
}
