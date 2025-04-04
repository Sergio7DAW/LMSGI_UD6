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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Form management: an object containing diverse kinds of controls.
 * Based on reflection: changes to the model are performed through calls
 * to Bean-like methods of the FormController.
 */
public abstract class FormController implements ChangeListener
{
    private static Class<?>[] BOOLEAN_ARG = { boolean.class };
    private static Class<?>[] OBJECT_ARG = { Object.class };
    private static Class<?>[] STRING_ARG = { String.class };
    private static Class<?>[] INT_ARG = { int.class };
    
    protected Method action;
    protected Object target;
    
    protected HashMap<String, JComponent> controls;
    protected HashSet<Object> modifiedFields = new HashSet<Object>();
    
    public FormController()
    {
        controls = new HashMap<String, JComponent>();
    }
    
    public void modelChanged()
    {
        for (Iterator<String> iter = controls.keySet().iterator(); iter.hasNext();) {
            String name = iter.next();
            JComponent c = controls.get(name);
            // try to call the form's get method with this name:
            Object value;
            try {
                value = callGetMethod(name, c);
            }
            catch (NoSuchMethodException e) {
                value = getField(name);
            }
            
            if(c instanceof JCheckBox) {
                JCheckBox box = (JCheckBox) c;
                box.setSelected(Boolean.TRUE.equals(value));
            }
            else if (c instanceof JSpinner) {
                JSpinner box = (JSpinner) c;
                box.setValue(value);
            }
            else if (c instanceof JTextField) {
                JTextField box = (JTextField) c;
                box.setText((value == null)? "" : value.toString());
            }
            else if (c instanceof XComboBox) {
                XComboBox box = (XComboBox) c;
                box.select(value);
            }
            else if (c instanceof JComboBox) {
                JComboBox box = (JComboBox) c;
                box.setSelectedItem(value);
            }
        }
    }

    private KeyListener keyListener = new KeyListener() {

        public void keyReleased(KeyEvent e)
        {
             modifiedFields.add(e.getSource());
        }

        public void keyPressed(KeyEvent e) { }
        public void keyTyped(KeyEvent e) { }
    };

    private FocusListener textFocusListener = new FocusListener() {
        public void focusGained(FocusEvent e) { }

        public void focusLost(FocusEvent e)
        {
             if(modifiedFields.contains(e.getSource())) {
                 textFieldCB(new ActionEvent(e.getSource(), 0, ""), null);
                 modifiedFields.remove(e.getSource());
             }
        }
    };

    /**
     * This method can be overriden instead of using reflection
     */
    public Object getField(String name)
    {
        return null;
    }

    /**
     * This method can be overriden instead of using reflection
     */
    public void setField(String name, Object value)
    {
    }

    public void addControl(String name, JComponent control)
    {
        control.setName(name);
        controls.put(name, control);
    }

    public void enableControls(boolean enabled)
    {
         for(JComponent c : controls.values()) {
             c.setEnabled(enabled);
         }
    }

    public void enableControl(String name, boolean enabled)
    {
         JComponent c = controls.get(name);
         if(c != null)
             c.setEnabled(enabled);
    }

    public JCheckBox addCheckbox(String name)
    {
        JCheckBox c = new JCheckBox(new BasicAction(null, "checkboxCB", this));
        addControl(name, c);
        return c;
    }

    public JTextField addTextField(String name, int columns)
    {
        JTextField c = new JTextField(columns);
        GUI.setMinWidth(c, columns * 9);    // shit with GridBag
        addControl(name, c);
        c.addActionListener(new BasicAction("", "textFieldCB", this));
        c.addFocusListener(textFocusListener);
        c.addKeyListener(keyListener);
        return c;
    }
    
    public FileSelector addFileField(String name, boolean directory, boolean open)
    {
        FileSelector f = new FileSelector(30, !directory, !open);
        f.addActionListener(new BasicAction("", "fileCB", this));
        return f;
    }
    
    public JTextField addIntTextField(String name, int min, int max)
    {
        JTextField c = addTextField(name, 4);
        return c;
    }

    public JSpinner addIntSpinner(String name, int min, int max, int step)
    {
        JSpinner c = new JSpinner(new SpinnerNumberModel(0, min, max, step));
        addControl(name, c);
        c.addChangeListener(this);
        return c;
    }

    public JTextField addDoubleTextField(String name, double min, double max)
    {
        JTextField c = new JTextField(8);
        addControl(name, c);
        return c;
    }

    public XComboBox addComboBox(String name, String[] labels, Object[] items)
    {
        XComboBox cbox = new XComboBox(items, labels);
        cbox.addActionListener(new BasicAction(null, "comboBoxCB", this));
        addControl(name, cbox);
        return cbox;
    }

    public static class XComboBox extends JComboBox
    {
        Object[] items;
        String[] labels;
        public boolean userAction;
        
        public XComboBox(Object[] items, String[] labels)
        {
            super(labels);
            this.labels = labels;
            this.items = items;
        }
        
        void select(Object item)
        {
            if(item == null) {
                setSelectedIndex(-1);
                return;
            }
            int index = labels.length;
            for( ; --index >= 0; ) {
                if(items != null && item.equals(items[index])
                        || item.equals(labels[index]))
                    break;
            }
            userAction = false;
            setSelectedIndex(index);
            userAction = true;
        }

        public void changeModel(String[] labels)
        {
            super.setModel(new DefaultComboBoxModel(labels));
            this.labels = labels;
        }
    }


    public void comboBoxCB(ActionEvent e, BasicAction a)
    {
        XComboBox c = (XComboBox) e.getSource();
        if (c.userAction) {
            Object value = c.getSelectedItem();
            if (c.items != null)
                value = c.items[c.getSelectedIndex()];
            if (!callSetMethod(c.getName(), INT_ARG, value))
                 callSetMethod(c.getName(), OBJECT_ARG, value);
        }
    }

    public void textFieldCB(ActionEvent e, BasicAction a)
    {
        JTextField c = (JTextField) e.getSource();
        callSetMethod(c.getName(), STRING_ARG, c.getText());
    }

    public void checkboxCB(ActionEvent e, BasicAction a)
    {
        JCheckBox c = (JCheckBox) e.getSource();
        callSetMethod(c.getName(), BOOLEAN_ARG, Boolean.valueOf(c.isSelected()));
    }
    
    public void stateChanged(ChangeEvent e)
    {
        JSpinner c = (JSpinner) e.getSource();
        if(callSetMethod(c.getName(), OBJECT_ARG, c.getValue()))
            return;
        callSetMethod(c.getName(), INT_ARG, c.getValue());       
    }

    private Object callGetMethod(String name, JComponent c)
        throws NoSuchMethodException
    {
        try {
            String methodName = "get" + name;
            Method meth = getClass().getMethod(methodName, (Class[]) null);
            meth.setAccessible(true);
            return meth.invoke(this, (Object[]) null);
        }
        catch (NoSuchMethodException e) {
            throw e;
        }
        catch (InvocationTargetException e1) {
            showHandlerError(e1.getCause());
        }
        catch (Exception e1) {
            showHandlerError(e1);
        }
        return null;
    }

    private boolean callSetMethod(String name, Class[] prototype, Object value)
    {
        try {
            String methodName = "set" + name;
            Method meth = getClass().getMethod(methodName, prototype);
            meth.setAccessible(true);
            meth.invoke(this, new Object[] { value });
            return true;
        }
        catch(NoSuchMethodException ex) {
            setField(name, value); 
            return true;
        }
        catch (InvocationTargetException e1) {
            GUI.error(e1.getCause().getMessage());
        }
        catch (Exception e1) {
            showHandlerError(e1);
        }
        return false;
    }
    
    private void showHandlerError(Throwable caught)
    {
        // signal error:
        String msg = "<html><b>" + caught.toString() + "</b>";
        StackTraceElement[] st = caught.getStackTrace();
        for (int s = 0; s < st.length && s < 15; s++) {
            msg += "<br>" + st[s];
        }
        GUI.message("Internal Error", msg);
        caught.printStackTrace();
    }

    public void compCB(ActionEvent e, BasicAction a)
    {
        Throwable caught;
        try {
            if(action != null)
                action.invoke(target, new Object[] { e });
            return;
        }
        catch (InvocationTargetException e1) {
            caught = e1.getCause();
        }
        catch (Exception e1) {
            caught = e1;
        }
        showHandlerError(caught);
    }
}
