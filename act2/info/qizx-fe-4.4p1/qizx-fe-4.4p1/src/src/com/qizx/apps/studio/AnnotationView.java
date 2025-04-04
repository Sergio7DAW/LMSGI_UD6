/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of the Qizx application components
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.apps.studio;

import com.qizx.api.admin.Profiling;
import com.qizx.apps.studio.gui.ListSelectionAction;
import com.qizx.apps.studio.gui.SortedTable;
import com.qizx.apps.studio.gui.ToolView;

import java.awt.Component;
import java.util.List;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

public class AnnotationView extends ToolView 
{
    private static final int[] defColWidths = { 100, 30, 200 };
    private List<Profiling> annotations;
    private SortedTable table;
    private XQEditor editor;
    boolean blockedSelect;
    
    public AnnotationView(XQEditor editor)
    {
        super("Profiling annotations", null);
        this.editor = editor;
        table = new SortedTable(defColWidths) {
            @Override
            public TableCellRenderer getCellRenderer(int row, int column)
            {
                if (column == 0)
                    return renderer;
                return super.getCellRenderer(row, column);
            }
        };
        setView(table);
        table.addSelectionListener(new ListSelectionAction("cmdSelectLine", this, table));
        table.setInitialSortColumn(0);
    }

    public void setModel(List<Profiling> annotations)
    {
        this.annotations = annotations;
        table.setModel(new AModel());
        revalidate();
    }
    
    public void cmdSelectLine(ListSelectionEvent e, ListSelectionAction a)
    {
        int row = table.getSelectedRow();
        if (row >= 0)
            row = table.convertRowIndexToModel(row);
        if (editor != null && annotations != null && row >= 0 && !blockedSelect) {
            Profiling annot = annotations.get(row);
            editor.select(annot.startPoint(), annot.endPoint());
        }
    }
    
    public void selectRow(int row)
    {
        if (row < 0 || row >= table.getRowCount())
            return;
        blockedSelect = true;
        row = table.convertRowIndexToView(row);
        table.getSelectionModel().setSelectionInterval(row, row);
        blockedSelect = false;
    }
    
    
    class AModel extends AbstractTableModel
    {
        public int getRowCount()
        {
            return annotations == null? 0 : annotations.size();
        }

        public int getColumnCount()
        {
            return 3;
        }

        public String getColumnName(int columnIndex)
        {
            switch(columnIndex) {
            case 0:
                return "Type";
            case 1:
                return "Count";
            case 2:
                return "Description";
            }
            return "?";
        }
        
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            if (annotations == null)
                return "?";
            Profiling profiling = annotations.get(rowIndex);
            switch(columnIndex) {
            case 0:
                return profiling.getType();
            case 1:
                return profiling.getCount();
            case 2:
                return profiling.getMessage();
            }
            return "?";
        }        
    }
    
    protected TableCellRenderer renderer = new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row, int column)
        {
            Component c = super.getTableCellRendererComponent(table, 
                                 value, isSelected, hasFocus, row, column);
            c.setBackground(null);
            if (column == 0 && row >= 0) {
                Profiling annot = annotations.get(table.convertRowIndexToModel(row));
                Style style = editor.getAnnotStyle(annot.getType());
                c.setBackground(StyleConstants.getBackground(style));
            }                    
            return c;
        }
    };

}

