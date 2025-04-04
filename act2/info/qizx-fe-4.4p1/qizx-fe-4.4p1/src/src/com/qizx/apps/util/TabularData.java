/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of the Qizx application components
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.apps.util;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

/**
 * Tabular Data Model: a list of rows made of named cells.
 */
public class TabularData extends AbstractTableModel
{
    private String className;
    private ArrayList<Row> rows;
    private String[] columnNames;
    private boolean[] editable;
    private boolean rememberChanges;


    public TabularData(String className, String[] columnNames)
    {
        this.className = className;
        this.columnNames = columnNames;
        rows = new ArrayList<Row>();
        editable = new boolean[columnNames.length];
    }


    class Row
    {
        boolean modified;
        Object[] cells;

        Row()
        {
            cells = new Object[columnNames.length];
        }
    }

    public String getClassName()
    {
        return className;
    }

    public void setColumnNames(String[] columnNames)
    {
        this.columnNames = columnNames;
    }

    public int getRowCount()
    {
        return rows.size();
    }

    public int getColumnCount()
    {
        return columnNames.length;
    }

    public String getColumnName(int columnIndex)
    {
        return columnNames[columnIndex];
    }

    public int getColumnIndex(String columnName)
    {
        for (int i = 0; i < columnNames.length; i++) {
            if (columnName.equals(columnNames[i])) {
                return i;
            }
        }
        return -1;
    }

    public Class<?> getColumnClass(int columnIndex)
    {
        return Object.class;
    }

    public void setEditable(int column, boolean editable)
    {
        this.editable[column] = editable;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return editable[columnIndex];
    }

    public boolean isModified()
    {
        for(Row r : rows)
            if (r.modified)
                return true;
        return false;
    }

    public boolean isRowModified(int rowIndex)
    {
        Row row = rows.get(rowIndex);
        return row.modified;
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        // System.err.println("at "+rowIndex+" c"+columnIndex+" : ");
        if(rowIndex >= rows.size()) {
            System.err.println("bad row "+rowIndex);
            return null;
        }
        Row row = rows.get(rowIndex);
        if (columnIndex < 0 || columnIndex >= row.cells.length) {
            System.err.println("bad col "+columnIndex);
            return null;
        }
        return row.cells[columnIndex];
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        while (rowIndex >= rows.size())
            rows.add(new Row());
        Row row = rows.get(rowIndex);
        Object oldValue = row.cells[columnIndex];
        if(rememberChanges) {
            boolean change = false;
            if (aValue == null || oldValue == null)
                change = aValue != oldValue;
            else {
                change = !aValue.toString().equals(oldValue.toString());
                
            }
            if (change) {
                row.modified = true;
                fireTableCellUpdated(rowIndex, columnIndex);
                
            }
        }
        row.cells[columnIndex] = aValue;
    }
    
    public boolean getRememberChanges()
    {
        return rememberChanges;
    }

    public void setRememberChanges(boolean rememberChanges)
    {
        this.rememberChanges = rememberChanges;
    }
}
