/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of the Qizx application components
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.apps.studio;

import com.qizx.apps.studio.gui.*;
import com.qizx.apps.util.QizxConnector;
import com.qizx.apps.util.TabularData;
import com.qizx.util.basic.FileUtil;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class AdminTab extends JTabbedPane
{
    private QizxStudio app;
    private ConfigTab configTab;
    private StatsTab statsTab;
    private RunningQueriesTab runningTab;
    private TasksTab tasksTab;
    private Timer tick;
    private Icon tabIcon1;

    private QizxConnector connector;
    private ExecutorService executor;
    
    private static final String[] REFRESH_ITEMS = new String[] {
        "none", "1 second", "2 seconds", "5 seconds", "10 seconds",
        "30 seconds", "1 minute"
    };
    private static final int[] REFRESH_VALUES = new int[] {
        0, 1000, 2000, 5000, 10000, 30000, 60000
    };

    public AdminTab(QizxStudio app)
    {
        super(JTabbedPane.LEFT, JTabbedPane.WRAP_TAB_LAYOUT);
        this.app = app;
        Help.setHelpId(this, "admin_tab");
        tabIcon1 = app.getIcon("tab.png");
        
        configTab = new ConfigTab("configuration");
        addTab("Configuration", tabIcon1, configTab,
               "Visualise and edit Server or Database configuration");

        tasksTab = new TasksTab("Maintenance Tasks");
        addTab("Maintenance Tasks", tabIcon1, tasksTab, "");

        statsTab = new StatsTab("Statistics");
        addTab("Statistics", tabIcon1, statsTab,
               "Visualise Server or Database rutime statistics");

        runningTab = new RunningQueriesTab("Running Queries");
        addTab("Running Queries", tabIcon1, runningTab, "");

        setSelectedIndex(1);

        tick = new Timer(1000, new BasicAction("timer", "cmdTimer", this));
        tick.start();

        executor = Executors.newSingleThreadExecutor();
    }

    public void setConnector(QizxConnector connector)
    {
        this.connector = connector;
        configTab.refresh(false);
        statsTab.refresh(false);
        runningTab.refresh(false);
        tasksTab.refresh(false);
    }

    public void saveConfig(TabularData data)
        throws Exception
    {
        Properties props = new Properties();
        for(int r = 0; r < data.getRowCount(); r++) {
            if(data.isRowModified(r)) {
                String key = (String) data.getValueAt(r, 0);
                Object value = data.getValueAt(r, configTab.VALUE_COL);
                props.setProperty(key, value.toString());
            }
        }
        connector.changeConfiguration(props);
    }

    abstract class Tab extends ToolView
    {
        JComboBox autoRefreshCBox;
        long autoRefreshLastTime;
        int forcedInterval;

        public Tab(String title)
        {
            super(title, null);
        }

        abstract void refresh(boolean auto);

        void autoRefresh()
        {
            int index = (autoRefreshCBox == null)? -1 : autoRefreshCBox.getSelectedIndex();
            int interval = (index <= 0) ? forcedInterval : REFRESH_VALUES[index];
            
            if (interval <= 0)
                return; // none
            
            long now = System.currentTimeMillis();
            if (now + 100 < autoRefreshLastTime + interval)
                return;
            autoRefreshLastTime = now;
            // we are in the Swing thread
            refresh(true);
        }

        void setupAutoRefresh()
        {
            autoRefreshCBox = new JComboBox(REFRESH_ITEMS);
            addToolSpace(20, -1);
            addTool(new JLabel("Auto refresh: "), -1);
            addTool(autoRefreshCBox, -1);
        }

        protected TabularData makeErrorModel(String message)
        {
            TabularData data = new TabularData("", new String[] { "Error" });
            data.setValueAt(message, 0, 0);
            return data;
        }
    }


    class ConfigTab extends Tab 
        implements CellEditorListener
    {
        final int[] defaultColWidths = {
            150, 30, 25, 25, 80, 80, 300
        };
        static final int ID_COL = 0;
        static final int TYPE_COL = 3;
        static final int VALUE_COL = 4;
        final Color EDITED = new Color(255, 150, 100);
        
        TabularData configData;
        SortedTable tableView;
        JCheckBox expertToggle;
        JButton refreshButton;
        JButton saveButton;
        
        protected DefaultCellEditor boolCellEditor =
                                    new DefaultCellEditor(new JCheckBox());
        protected DefaultCellEditor defCellEditor =
                                    new DefaultCellEditor(new JTextField());
        
        protected TableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected,
                                                           boolean hasFocus, int row,
                                                           int column)
            {
                Component c = super.getTableCellRendererComponent(table, 
                                     value, isSelected, hasFocus, row, column);
                c.setBackground(null);
                if (column == VALUE_COL && row >= 0) {
                    if (configData.isRowModified(table.convertRowIndexToModel(row)))
                        c.setBackground(EDITED);
                }                    
                return c;
            }
        };

        public ConfigTab(String title)
        {
            super(title);
            Help.setHelpId(this, "config_tab");
            
            tableView = new SortedTable(defaultColWidths) {
                @Override
                public TableCellEditor getCellEditor(int row, int column)
                {
                    row = convertRowIndexToModel(row);
                    if (configData != null
                        && "Boolean".equals(configData.getValueAt(row, TYPE_COL)))
                        return boolCellEditor;
                    return defCellEditor;
                }

                @Override
                public TableCellRenderer getCellRenderer(int row, int column)
                {
                    if (column == VALUE_COL)
                        return renderer;
                    return super.getCellRenderer(row, column);
                }

                @Override
                protected JPopupMenu getPopupMenu()
                {
                    JPopupMenu m = new JPopupMenu();
                    m.add(new BasicAction("Restore default value",
                                          "cmdRestore", AdminTab.this));
                    return m;
                }
            };
            // sort on Name column:
            tableView.setInitialSortColumn(ID_COL);

            setView(tableView);

            boolCellEditor = new DefaultCellEditor(new JComboBox(new String[] {
                "false", "true"
            }));
            boolCellEditor.addCellEditorListener(this);
            defCellEditor.setClickCountToStart(1);
            defCellEditor.addCellEditorListener(this);

            BasicAction refreshAction =
                new BasicAction("Refresh", "cmdRefreshConfig", AdminTab.this);
            refreshButton = new JButton(refreshAction);
            addTool(refreshButton, -1);
            BasicAction saveAction =
                new BasicAction("Save", "cmdSaveConfig", AdminTab.this);
            addToolSpace(10, -1);
            saveButton = new JButton(saveAction);
            addTool(saveButton, -1);
            
            addToolSpace(20, -1);
            expertToggle = new JCheckBox("Expert");
            expertToggle.addActionListener(refreshAction);
            addTool(expertToggle, -1);
        }

        void refresh(boolean auto)
        {            
            boolean expert = expertToggle.isSelected();
            if (connector == null)
                changeTitle("");
            else
                changeTitle(" Configuration of " + connector.getDisplay());

            try {
                configData = (connector == null) ? null
                                           : connector.getConfiguration(expert);
                if (configData == null)
                    tableView.changeData(new DefaultTableModel());
                else {
                    // value is editable
                    configData.setEditable(4, true);
                    tableView.changeData(configData);
                    configData.setRememberChanges(true);
                }
            }
            catch (Exception e) {
                configData = makeErrorModel(e.getMessage());
                tableView.changeData(configData);
            }
            tableView.revalidate();
        }

        private void enableRefresh(boolean enabled)
        {
            expertToggle.setEnabled(enabled);
            refreshButton.setEnabled(enabled);
        }

        @Override
        public void editingStopped(ChangeEvent e)
        {
            //enableRefresh(!data.isModified());
        }
        
        @Override
        public void editingCanceled(ChangeEvent e) // doesnt work...
        {
        }

        public void save() throws Exception
        {
            saveConfig(configData);
        }

        public void restore()
        {
            for(int row : tableView.getSelectedRows()) {
                int r = tableView.convertRowIndexToModel(row);
                configData.setValueAt(configData.getValueAt(r, VALUE_COL + 1), r, VALUE_COL);
            }
        }
    }

    public void cmdRefreshConfig(ActionEvent e, BasicAction a)
    {
        if(configTab.configData != null &&
           configTab.configData.isModified())
            if(!GUI.confirmation("Configuration modified",
                                 "Some properties are modified:\n" +
                                 "Discard changes and proceed?"))
                return;
        configTab.refresh(false);
    }
    
    public void cmdSaveConfig(ActionEvent e, BasicAction a)
    { 
        try {
            configTab.save();
            configTab.refresh(false);
        }
        catch (Exception ex) {
           app.showError("cannot save configuration:", ex);
        }

        //configTab.enableRefresh(true);
    }
    
    public void cmdRestore(ActionEvent e, BasicAction a)
    { 
        configTab.restore();
    }


    class StatsTab extends Tab
    {
        SortedTable tableView;
        JCheckBox expertToggle;

        final int[] defaultColWidths = {
            250, 100, 100, 100, 400
        };

        public StatsTab(String title)
        {
            super(title);
            Help.setHelpId(this, "stats_tab");
            
            tableView = new SortedTable(defaultColWidths);
            setView(tableView);   // no scrollbar
            // sort on Id column:
            tableView.setInitialSortColumn(0);

            BasicAction refreshAction =
                new BasicAction("Refresh", "cmdRefreshStats", AdminTab.this);

            JButton refreshButton = new JButton(refreshAction);
            addTool(refreshButton, -1);

            setupAutoRefresh();

            addToolSpace(20, -1);
            expertToggle = new JCheckBox("Expert");
            expertToggle.addActionListener(refreshAction);
            addTool(expertToggle, -1);
        }

        void refresh(boolean auto)
        {
            boolean expert = expertToggle.isSelected();
            if (connector == null)
                changeTitle("");
            else
                changeTitle(" Execution statistics of "
                            + connector.getDisplay());

            try {
                TabularData data = (connector == null) ? null
                                   : connector.getStatistics(expert? "expert" : "admin");
                if (data == null)
                    tableView.changeData(new DefaultTableModel());
                else {
                    tableView.changeData(data);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                tableView.changeData(makeErrorModel(e.getMessage()));
            }
            tableView.revalidate();
        }
    }

    public void cmdRefreshStats(ActionEvent e, BasicAction a)
    {
        statsTab.refresh(false);
    }


    class RunningQueriesTab extends Tab
    {
        SortedTable tableView;
        final int[] defaultColWidths = {
            100, 100, 100, 400
        };
        private TabularData data;

        public RunningQueriesTab(String title)
        {
            super(title);
            Help.setHelpId(this, "queries_tab");

            tableView = new SortedTable(defaultColWidths) {
                @Override
                protected JPopupMenu getPopupMenu()
                {
                    JPopupMenu m = new JPopupMenu();
                    m.add(new BasicAction("Cancel query",
                                          "cmdCancelQuery", AdminTab.this));
                    return m;
                }
            };
            setView(tableView);
            tableView.setInitialSortColumn(2);
            Help.setHelpId(tableView, "queries_table");

            BasicAction refreshAction =
                new BasicAction("Refresh", "cmdRefreshQueries", AdminTab.this);

            JButton refreshButton = new JButton(refreshAction);
            addTool(refreshButton, -1);

            setupAutoRefresh();
        }

        void refresh(boolean auto)
        {
            if (connector == null)
                changeTitle("");
            else
                changeTitle(" Running Queries in " + connector.getDisplay());

            try {
                data = (connector == null) ? null : connector.listRunningQueries();
                if (data == null)
                    tableView.changeData(new DefaultTableModel());
                else {
                    tableView.changeData(data);
                }
            }
            catch (Exception e) {
                tableView.changeData(makeErrorModel(e.getMessage()));
            }
            tableView.revalidate();
        }

        public void cancelQuery()
        {
            for(int row : tableView.getSelectedRows()) {
                int r = tableView.convertRowIndexToModel(row);
                String id = (String) data.getValueAt(r, 0);
                try {
                    connector.cancelQuery(id);
                }
                catch (Exception e) {
                    app.showError("Cancelling query " + id + ": " + e.getMessage(), e);
                }
            }
        }
    }

    public void cmdRefreshQueries(ActionEvent e, BasicAction a)
    {
        runningTab.refresh(false);
    }

    public void cmdCancelQuery(ActionEvent e, BasicAction a)
    {
        runningTab.cancelQuery();
    }

    private static final String[] INTERVALS = {
        "none", "every hour", "every 12 hours", "daily", "weekly" };
    private static final Object[] INTERVAL_HRS = { 0,  1, 12, 24, 24 * 7 };

    private static final String[] TIMELINE_LABELS = {
        "past 24 hrs", "past 48 hrs", "past week", "past month" };
    private static final int[] TIMELINE_VALUES = { 24, 48, 24 * 7, 31 * 24 };


    public class TasksTab extends Tab
    {
        final String[] CUR_TASK_FIELDS = {
          "Type", "Database", "Start Time", "Estim. Finish Time", "Est. Duration", "Progress"
        };
        final String[] TASK_FIELDS = {
          "Type", "Database", "Start Time", "Finish Time", "Duration", "Progress"
        };
       
        SortedTable pastJobsTable;
        SortedTable currentJobsTable;

        final int[] defaultColWidths = {
            100, 80, 150, 150, 60, 50
        };
        private TabularData configData;
        private String[] libNameList;
        private FormController backupForm;
        private FormController ibackupForm;
        private FormController optimForm;
        private JComboBox timelineCBox;
        //private JTextField locCountField;
        private JLabel dirDisplayLabel;

        private JComboBox manualLibField;
        private JButton manualOptimizeButton;
        private JButton manualReindexButton;
        private JButton manualBackupButton;
        private JCheckBox incrementalCheckBox;
        private JProgressBar manualProgressBar;
        private FileSelector backupDirSelector;

        
        private Runnable refreshTask = new Runnable() {
            public void run()
            {
                refresh(true);
            }
        };

        public TasksTab(String title)
        {
            super(title);
            Help.setHelpId(this, "tasks_tab");
            
            JPanel pane = new JPanel();
            GridBagger grid = new GridBagger(pane, 8, 6);
            setDirectView(pane);   // no scrollbar

            grid.newRow();
            JPanel mform = createManualOpForm();
            setBorder(mform, "Manual operation");
            grid.add(mform, grid.prop("xfill").prop("top").spans(2, 1));

            grid.newRow();
            JPanel form1 = createScheduledBackupForm(false);
            setBorder(form1, "Scheduled Full Backup");
            grid.add(form1, grid.prop("xfill").prop("top"));

            grid.newRow();
            JPanel form3 = createScheduledBackupForm(true);
            setBorder(form3, "Scheduled Incremental Backup");
            grid.add(form3, grid.prop("xfill").prop("top"));

            grid.newRow();
            JPanel form2 = createScheduledOptimizeForm();
            setBorder(form2, "Scheduled Database Optimization");
            grid.add(form2, grid.prop("xfill").prop("top"));
            
            grid.newRow();
            currentJobsTable = new SortedTable(defaultColWidths);
            currentJobsTable.setInitialSortColumn(-2);
            JScrollPane scr = new JScrollPane(currentJobsTable);
            setBorder(scr, "Running jobs");
            grid.add(scr, grid.prop("fill").spans(2, 1));
            Help.setHelpId(scr, "current_tasks_table");

            grid.newRow();
            JPanel pastPanel = new JPanel();
            grid.add(pastPanel, grid.prop("fill").spans(2, 1).weighted(1, 3));
            setBorder(pastPanel, "Past jobs");
            
            GridBagger pgrid = new GridBagger(pastPanel, 0, 4);
            pgrid.newColumn();
            timelineCBox = new JComboBox(TIMELINE_LABELS);
            timelineCBox.setAction(new BasicAction("Change timeline",
                                            "cmdRefreshTasks", AdminTab.this));
            pgrid.add(timelineCBox, pgrid.prop("left"));

            pastJobsTable = new SortedTable(defaultColWidths);
            pastJobsTable.setInitialSortColumn(-2);
            scr = new JScrollPane(pastJobsTable);
            pgrid.add(scr, pgrid.prop("fill").spans(2, 2));
            Help.setHelpId(scr, "past_tasks_table");

            BasicAction refreshAction =
                new BasicAction("Refresh", "cmdRefreshTasks", AdminTab.this);

            JButton refreshButton = new JButton(refreshAction);
            addTool(refreshButton, -1);
            
            enableForms(false);
            setupAutoRefresh();
        }

        private JPanel createManualOpForm()
        {
            JPanel panel = new JPanel();
            GridBagger grid = new GridBagger(panel, 8, 3);
            Help.setHelpId(panel, "manual_task_form");
            
            grid.newRow();
            grid.add(new JLabel("XML Library:"), grid.prop("right"));
            manualLibField = new JComboBox();
            grid.add(manualLibField, grid.prop("left"));

            grid.add((JComponent) Box.createHorizontalStrut(8));
            manualBackupButton = new JButton(new BasicAction("Backup",
                                                             "cmdManualBackup", this));
            grid.add(manualBackupButton, grid.prop("right"));
            
            incrementalCheckBox = new JCheckBox("incremental");
            grid.add(incrementalCheckBox, grid.prop("left"));
            
            grid.add(new JLabel("to:"), grid.prop("right").weighted(0, 1));
            
            backupDirSelector = new FileSelector(20, false, true);
            grid.add(backupDirSelector, grid.prop("left"));
            String fileSetting = app.getSetting(QizxStudio.SETTING_BACKUP_DIR);
            if (fileSetting != null)
                backupDirSelector.setSuggestedFile(fileSetting);

            //grid.add((JComponent) Box.createHorizontalStrut(16));

            // ------ second row:
            grid.newRow();
            grid.skip(3);
            manualReindexButton = new JButton(new BasicAction("Reindex",
                                                              "cmdManualReindex", this));
            grid.add(manualReindexButton, grid.prop("right"));

            // ------ third row:
            grid.newRow();
            
            manualProgressBar = new JProgressBar(0, 100);
            manualProgressBar.setStringPainted(true);
            GUI.setMinWidth(manualProgressBar, 160);
            grid.add(manualProgressBar, grid.prop("xfill").spans(2, 1));
            
            grid.skip(1);
            manualOptimizeButton = new JButton(new BasicAction("Optimize",
                                                               "cmdManualOptimize", this));
            grid.add(manualOptimizeButton, grid.prop("right"));

            return panel;
        }
        
        public void cmdManualBackup(ActionEvent e, BasicAction a)
        {
            String path = backupDirSelector.getPath();
            if (path != null && path.trim().length() > 0) {
                boolean incr = incrementalCheckBox.isSelected();
                manualOp(incr? 'I' : 'B', new File(path));
                app.saveSetting(QizxStudio.SETTING_BACKUP_DIR, path);
            }
        }
        
        public void cmdManualReindex(ActionEvent e, BasicAction a)
        {
            manualOp('R', null);
        }

        public void cmdManualOptimize(ActionEvent e, BasicAction a)
        {
            manualOp('O', null);
        }

        private void manualOp(final char op, final File dir)
        {
            final String libName = (String) manualLibField.getSelectedItem();
            if (libName == null)
                return;
            enableButtons(false);
            forcedInterval = 1000;
            executor.submit(new Runnable() {
                public void run()
                {
                    try {
                        QizxStudio.BarObserver progress =
                            new QizxStudio.BarObserver(manualProgressBar);
                        if(op == 'B') {
                            connector.backup(libName, dir, progress);
                            progress.backupProgress(1); // oops
                        }
                        else if(op == 'I') {
                            connector.incrementalBackup(libName, dir, progress);
                            progress.backupProgress(1); // oops
                        }
                        else if(op == 'R') {
                            connector.reindex(libName, progress);
                            progress.reindexingProgress(1); // oops
                        }
                        else if(op == 'O') {
                            connector.optimize(libName, progress);
                            progress.optimizationProgress(1); 
                        }
                    }
                    catch (Exception e1) {
                        app.showError(e1.getMessage());
                    }
                    finally {
                        enableButtons(true); 
                        forcedInterval = -1;
                    }
                    // beware not to do refresh in task thread:
                    SwingUtilities.invokeLater(refreshTask);
                }
            });
        }

        private void enableButtons(boolean enable)
        {
             manualBackupButton.setEnabled(enable);
             manualOptimizeButton.setEnabled(enable);
             manualReindexButton.setEnabled(enable);
        }

        private JPanel createScheduledBackupForm(boolean increm)
        {
            JPanel panel = new JPanel();
            GridBagger grid = new GridBagger(panel, 8, 3);
            Help.setHelpId(panel, "sched_" + (increm? "i" : "") + "backup_form");

            String backup = "scheduled_" + (increm? "ibackup" : "backup");
            final String intervalProp = backup + "_interval";
            
            FormController form = new FormController() {
                public Object getField(String name) {
                    
                    Object prop = getProp(name);
                    if(prop != null && intervalProp.equals(name))
                        prop = Integer.parseInt((String) prop);
                    return prop;
                }
                public void setField(String name, Object value) {
                    
                    setProp(name, value);
                }
            };
            if (increm)
                ibackupForm = form;
            else
                backupForm = form;
            grid.newRow();
            JComboBox periodicityField = form.addComboBox(intervalProp, 
                                                          INTERVALS, INTERVAL_HRS);
            grid.add(new JLabel("Backup frequency:"), grid.prop("right"));
            grid.add(periodicityField, grid.prop("left"));
            grid.skip(1);
            JButton saveButton = new JButton(new BasicAction("Save", 
                                                "cmdSaveBackup", AdminTab.this));
            grid.add(saveButton, grid.prop("right"));
            saveButton.setMinimumSize(saveButton.getPreferredSize());
            form.addControl("save", saveButton);
            
            grid.newRow();
            JTextField timeField = form.addTextField(backup + "_start", 6);
            grid.add(new JLabel("Start time:"), grid.prop("right"));
            grid.add(timeField, grid.prop("left").weighted(1, 1));
            
            //grid.newRow();
            final JTextField locCountField;
            if (!increm) {
                grid.add(new JLabel("Rolling backup count:"), grid.prop("right"));
                locCountField = form.addIntTextField(backup + "_dir_count", 1, 10);
                grid.add(locCountField, grid.prop("left"));
            }
            else locCountField = null;
            
            grid.newRow();
            final JTextField locationField =
                form.addTextField(backup + "_dir", 35);
            grid.add(new JLabel("Backup Directory:"), grid.prop("right"));
            grid.add(locationField, grid.prop("xfill").spans(3, 1));
            
            grid.newRow();
            final JLabel dirDisplay = new JLabel("\u00a0");
            dirDisplay.setForeground(new Color(0x404080));
            grid.add(new JLabel("dir. example:"), grid.prop("right"));
            grid.add(dirDisplay, grid.prop("xfill").spans(2, 1));

            KeyListener listener = new KeyListener() {
                public void keyReleased(KeyEvent e)
                {
                    dirDisplay.setText(computeBackupPath(locationField.getText(),
                                                         locCountField));
                }
                public void keyPressed(KeyEvent e) { }
                public void keyTyped(KeyEvent e) { }
            };
            locationField.addKeyListener(listener);
            if (!increm)
                locCountField.addKeyListener(listener);
            
            return panel;
        }

        private JPanel createScheduledOptimizeForm()
        {
            JPanel form = new JPanel();
            Help.setHelpId(form, "sched_optim_form");
            GridBagger grid = new GridBagger(form, 8, 3);
            
            optimForm = new FormController() {
                public Object getField(String name) {
                    Object prop = getProp(name);
                    if(prop != null && "scheduled_optimize_interval".equals(name))
                        prop = Integer.parseInt((String) prop);
                    return prop;
                }
                public void setField(String name, Object value) {
                    setProp(name, value);
                }
            };

            grid.newRow();
            JComboBox periodicityField =
                optimForm.addComboBox("scheduled_optimize_interval",
                                      INTERVALS, INTERVAL_HRS);
            grid.add(new JLabel("Frequency:"), grid.prop("right"));
            grid.add(periodicityField, grid.prop("left"));
            
            JButton saveButton = new JButton(new BasicAction("Save", 
                                                "cmdSaveOptim", AdminTab.this));
            grid.add(saveButton, grid.prop("right"));
            optimForm.addControl("save", saveButton);
            
            grid.newRow();
            JTextField timeField = optimForm.addTextField("scheduled_optimize_start", 6);
            grid.add(new JLabel("Start time:"), grid.prop("right"));
            grid.add(timeField, grid.prop("left").weighted(1, 1));
            
            grid.newRow();
            JTextField locCountField = optimForm.addIntTextField("scheduled_optimize_max_time", 1, 10);
            grid.add(new JLabel("Max time in minutes:"), grid.prop("right"));
            grid.add(locCountField, grid.prop("left"));
            
            return form;
        }

        private void setBorder(JComponent panel, String title)
        {
            TitledBorder border = new TitledBorder(title);
            border.setTitleColor(new Color(40, 40, 180));
            //panel.setBackground(getBackground().brighter());
            panel.setBorder(border);
        }

        protected void setProp(String name, Object value)
        {
             if (configData != null)
                 for(int p = configData.getRowCount(); --p >= 0; ) {
                     if(name.equals(configData.getValueAt(p, ConfigTab.ID_COL))) {
                         
                         configData.setValueAt(value, p, ConfigTab.VALUE_COL);
                         return;
                     }
                 }
        }

        protected Object getProp(String name)
        {
            if (configData != null)
                for(int p = configData.getRowCount(); --p >= 0; ) {
                    if(name.equals(configData.getValueAt(p, ConfigTab.ID_COL))) {
                        Object value = configData.getValueAt(p, ConfigTab.VALUE_COL);
                        
                        return value;
                    }
                }
            return null;
        }

        void refresh(boolean auto)
        {
            if (connector == null)
                changeTitle("");
            else
                changeTitle(" Maintenance tasks of " + connector.getDisplay());

            try {
                int ti = timelineCBox.getSelectedIndex();
                int time = (ti < 0)? 24 : TIMELINE_VALUES[ti];
                TabularData data =
                    (connector == null) ? null : connector.listMaintenanceTasks(time);
                if (data == null)
                    pastJobsTable.changeData(new DefaultTableModel());
                else {
                    data.setColumnNames(TASK_FIELDS);
                    pastJobsTable.changeData(data);
                }

                data = (connector == null) ? null : connector.listMaintenanceTasks(0);
                if (data == null)
                    currentJobsTable.changeData(new DefaultTableModel());
                else {
                    data.setColumnNames(CUR_TASK_FIELDS);
                    currentJobsTable.changeData(data);
                }
                
                libNameList = (connector == null) ?
                                    new String[0] : connector.listLibraries();
                int libIndex = manualLibField.getSelectedIndex();
                manualLibField.setModel(new DefaultComboBoxModel(libNameList));
                if (libIndex >= 0 && libIndex < libNameList.length)
                    manualLibField.setSelectedIndex(libIndex);
                    
                    
                if(!auto) {
                    boolean enable = (connector != null) && !connector.isLocal();
                    configData = enable ? connector.getConfiguration(false) : null;
                    if(configData != null) {
                        configData.setRememberChanges(true);
                        backupForm.modelChanged();
                        if (ibackupForm != null)
                            ibackupForm.modelChanged();
                        optimForm.modelChanged();
                    }
                    
                    boolean enabled = data != null;
                    enableForms(enabled);                    
                }
            }
            catch (Exception e) {
                currentJobsTable.changeData(makeErrorModel(e.getMessage()));
                pastJobsTable.changeData(makeErrorModel(e.getMessage()));
            }
            pastJobsTable.revalidate();
        }

        protected String computeBackupPath(String path, JTextField countField)
        {
            int index = -1;
            String count = countField == null? "" : countField.getText().trim();
            if (count.length() > 0)
                try {
                    index = Integer.parseInt(count);
                }
                catch (NumberFormatException e) { ; }
            File parent = FileUtil.indexFile(new File(path), index > 1? 0 : -1);
            
            return new File(parent, "<database>").toString();
        }

        protected void enableForms(boolean enabled)
        {
            backupForm.enableControls(enabled && !connector.isLocal());
            if (ibackupForm != null)
                ibackupForm.enableControls(enabled && !connector.isLocal());
            optimForm.enableControls(enabled && !connector.isLocal());
            manualLibField.setEnabled(enabled);
            manualBackupButton.setEnabled(enabled);
            manualOptimizeButton.setEnabled(enabled);
            manualReindexButton.setEnabled(enabled);
        }
    }

    public void cmdRefreshTasks(ActionEvent e, BasicAction a)
    {
        tasksTab.refresh(false);
    }
    
    public void cmdSaveBackup(ActionEvent e, BasicAction a)
    {
        try {
            if (tasksTab.configData != null)
                saveConfig(tasksTab.configData);
        }
        catch (Exception e1) {
            app.showError("saving configuration: " + e1.getMessage());
        }
    }
    
    public void cmdSaveOptim(ActionEvent e, BasicAction a)
    {
        try {
            if (tasksTab.configData != null)
                saveConfig(tasksTab.configData);
        }
        catch (Exception e1) {
            app.showError("saving configuration: " + e1.getMessage());
        }
    }

    public void cmdTimer(ActionEvent e, BasicAction a)
    {
        int selectedIndex = getSelectedIndex();
        Component tabAbove = getComponentAt(selectedIndex);
        

        statsTab.autoRefresh();
        if(tabAbove == runningTab)
            runningTab.autoRefresh();
        if(tabAbove == tasksTab)
            tasksTab.autoRefresh();
    }
}
