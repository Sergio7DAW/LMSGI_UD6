/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of the Qizx application components
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.apps.studio.gui;

import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * Extension of JTable for fixing deficient aspects.
 */
public class SortedTable extends JTable
{
    private int[] defaultColWidths;
    private int initialSortColumn = Integer.MAX_VALUE;
    private AbstractTableModel data;
    
    public SortedTable(int[] defaultColWidths)
    {
        setFont(getFont().deriveFont(Font.BOLD));
        this.defaultColWidths = defaultColWidths;
        setRowMargin(3);
        setRowHeight(8 + getRowHeight());
        columnModel.setColumnMargin(6);
        setAutoCreateRowSorter(true);
        setAutoCreateColumnsFromModel(false);
        setModel(new Model());
                
        // right-click menu:
        addMouseListener(new PopupListener());
    }

    public void addSelectionListener(ListSelectionListener listener)
    {
        getSelectionModel().addListSelectionListener(listener);
    }

    public void changeData(AbstractTableModel dataModel)
    {
        int colCount = getColumnCount();    // old model
        this.data = dataModel;
        if (colCount != dataModel.getColumnCount())
        {
            createDefaultColumnsFromModel();

            int columnCount = columnModel.getColumnCount();
            if (defaultColWidths != null && columnCount == defaultColWidths.length)
                for (int c = 0; c < columnCount; c++) {
                    
                    columnModel.getColumn(c).setPreferredWidth(defaultColWidths[c]);
                }
            if (initialSortColumn != Integer.MAX_VALUE) {
                // define on first display:
                int col = Math.abs(initialSortColumn);
                boolean desc = initialSortColumn < 0;
                RowSorter<? extends TableModel> rowSorter = getRowSorter();
                if (col >= columnCount)
                    rowSorter.setSortKeys(null);
                else if (rowSorter != null && col < columnCount) {
                    rowSorter.toggleSortOrder(col);
                    if (desc)
                        rowSorter.toggleSortOrder(col);
                }
            }
            data.fireTableStructureChanged();
        }
        
        data.fireTableStructureChanged();
        // now remember data changed...
        ((AbstractTableModel) getModel()).fireTableDataChanged();
    }

    public int getInitialSortColumn()
    {
        return initialSortColumn;
    }

    public void setInitialSortColumn(int initialSortColumn)
    {
        this.initialSortColumn = initialSortColumn;
    }


    class PopupListener extends MouseAdapter
    {
        public void mousePressed(MouseEvent e)
        {
            showPopup(e);
        }

        public void mouseReleased(MouseEvent e)
        {
            showPopup(e);
        }

        private void showPopup(MouseEvent e)
        {
            if (e.isPopupTrigger()) {
                JPopupMenu menu = getPopupMenu();
                if(menu != null) {
                    int row = rowAtPoint(e.getPoint());
//                    if(row >= 0)
//                        row = convertRowIndexToModel(row);
                    if (row >= 0)
                        getSelectionModel().setSelectionInterval(row, row);

                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        }
    }

    class Model extends AbstractTableModel
    {
        @Override
        public int getRowCount()
        {
            int rc = data == null? 0 : data.getRowCount();
            
            return rc;
        }

        @Override
        public int getColumnCount()
        {
            int cc = data == null? 0 : data.getColumnCount();
            
            return cc;
        }

        @Override
        public String getColumnName(int column)
        {
            return data == null? null : data.getColumnName(column);
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            return data == null? null : data.getValueAt(rowIndex, columnIndex);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return data == null? false : data.isCellEditable(rowIndex, columnIndex);
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            if (data != null)
                data.setValueAt(aValue, rowIndex, columnIndex);
        }
    }
    
    /**
     * To be overridden
     */
    protected JPopupMenu getPopupMenu()
    {
        return null;
    }
}
