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
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A labelled combo-box, which remembers typed-in values if editable.
 */
public class ComboSelector extends JPanel implements ActionListener
{
    private JLabel label;
    private JComboBox comboBox;

    public ComboSelector(String title, Object[] values, boolean editable)
    {
//        GridBagger grid = new GridBagger(this);
//        grid.newRow();
        new BoxLayout(this, BoxLayout.LINE_AXIS);

        label = new JLabel(title);
        add(label);

        comboBox = new JComboBox(values);
        comboBox.setEditable(editable);
        comboBox.addActionListener(this);
        add(comboBox);
    }

    public void setEnabled(boolean e)
    {
        comboBox.setEnabled(e);
    }

    /**
     * Returns the selected or edited value. If the text field is editable
     */
    public Object getValue()
    {
        return comboBox.getSelectedItem();
    }

    public void actionPerformed(ActionEvent e)
    {
        String value = (String) comboBox.getSelectedItem();
        addItem(value);
    }

    public void addItem(String value)
    {
        DefaultComboBoxModel model = (DefaultComboBoxModel) comboBox.getModel();
        for (int j = model.getSize(); --j >= 0;) {
            Object item = model.getElementAt(j);
            if(value.equals(item))
                return;
        }
        model.addElement(value);
    }

    public JComboBox getComboBox()
    {
        return comboBox;
    }
}
