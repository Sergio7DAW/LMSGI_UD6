/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of the Qizx application components
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */

package com.qizx.apps.studio.gui;

import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.AbstractAction;
import javax.swing.Icon;

/**
 * Action based on reflection...
 */
public class BasicAction extends AbstractAction
{   
    protected Method action;
    protected Object target;
    
    public BasicAction(String label, String action, Object target)
    {
        this(label, null, action, target);
    }
    
    /**
     * Creates an action that calls the specified method on the object.
     * @param label
     * @param icon
     * @param method name of a public method in the target object. Must have
     * one argument of class ActionEvent.
     * @param target target of this action.
     */
    public BasicAction(String label, Icon icon, String method, Object target) 
    {
        super(label, icon);
        this.target = target;
        try {
            this.action = target.getClass().getMethod(method,
                                  new Class[] { ActionEvent.class, getClass() });
        }
        catch (Exception e) {   // not runtime
            System.err.println("*** "+e);
        }
    }
    
    public void actionPerformed(ActionEvent e)
    {
        Throwable caught;
        try {
            if(action != null)
                action.invoke(target, new Object[] { e, this });
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
