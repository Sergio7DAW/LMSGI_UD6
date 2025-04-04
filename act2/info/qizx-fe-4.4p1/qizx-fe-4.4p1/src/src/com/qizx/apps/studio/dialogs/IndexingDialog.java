/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of the Qizx application components
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.apps.studio.dialogs;

import com.qizx.api.Indexing;
import com.qizx.api.Indexing.Rule;
import com.qizx.api.util.text.FormatDateSieve;
import com.qizx.api.util.text.FormatNumberSieve;
import com.qizx.api.util.text.ISODateSieve;
import com.qizx.apps.studio.Help;
import com.qizx.apps.studio.QizxStudio;
import com.qizx.apps.studio.QizxStudio.MemberAction;
import com.qizx.apps.studio.gui.*;
import com.qizx.apps.util.QizxConnector;

import org.xml.sax.InputSource;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 *   Manage indexing properties for a XML Library, 
 *   run index rebuilding or optimization.
 */
public class IndexingDialog extends DialogBase
{
    private static Localization local =
        new Localization(QizxStudio.class, "IndexingDialog");

    private AppFrame app;
    private Indexing indexing;
    private MemberAction target;

    private JFileChooser fileChooser;
    private JButton loadButton;
    private JButton resetButton;

    
    public IndexingDialog(AppFrame parent)
    {
        super(parent, local.text("Edit_Indexing_Specification"));
        app = parent;
        buildContents();

        getOkButton().setText(local.text("Apply"));
        getCancelButton().setText(local.text("Close"));
        
        Help.setDialogHelp(this, "indexing_dialog");
        
    }
    
    public void showUpFor(MemberAction target)
        throws Exception
    {
        QizxConnector ctor = target.browser.getConnector();
        setIndexing(ctor.getIndexing(target.library));
        this.target = target;
        showUp();
    }

    private void setIndexing(Indexing indexing)
    {
        this.indexing = indexing;
        formController.modelChanged();
    }

    private void buildContents()
    {
        setHint(local.text("hint"), true);
        
        GridBagger grid = new GridBagger(form, 0, VGAP);
        
        grid.newRow();
        
        loadButton = GUI.newBasicButton(local.text("Load_From_File..."),
                                        "cmdLoad", this);
                
        resetButton = GUI.newBasicButton(local.text("Reset_To_Default"),
                                         "cmdReset", this);
        addButton(loadButton, 1);
        addButton(resetButton, 2);
        
        // general property editor:
        grid.newRow();
        grid.add(generalPropertyEditor(), grid.prop("xfill"));
        
        grid.newRow();
        grid.add(defaultElementEditor(), grid.prop("xfill"));

        grid.newRow();
        grid.add(defaultAttributeEditor(), grid.prop("xfill"));
    }
    
    public JComponent generalPropertyEditor()
    {
        JPanel box = new JPanel();
        box.setBorder(new TitledBorder("General properties"));
        GridBagger grid = new GridBagger(box);
        grid.setInsets(5, 2);
        
        //FormController form = new GeneralEditor();
        
        grid.newRow();
        grid.addLabel(local.text("Full-Text_globally_enabled"), 1);
        grid.add(formController.addCheckbox("FullText"), grid.prop("left"));
        
        grid.newRow();
        grid.addLabel(local.text("Full-text_min._word_length"), 1);
        grid.add(formController.addIntSpinner("MinWord", 0, 199, 1));
        
        grid.newRow();
        grid.addLabel(local.text("Full-text_max._word_length"), 1);
        grid.add(formController.addIntSpinner("MaxWord", 0, 199, 1));
        
        grid.newRow();
        grid.addLabel(local.text("Simple_content_max._length"), 1);
        grid.add(formController.addIntSpinner("MaxContent", 0, 999, 1));

        return box;
    }
    
    public JComponent defaultElementEditor()
    {
        JPanel box = new JPanel();
        box.setBorder(new TitledBorder("Default Element Rules"));
        GridBagger grid = new GridBagger(box);
        grid.setInsets(5, 2);
       
        grid.newRow();
        grid.addLabel(local.text("Numeric_Format"), 1);
        grid.add(formController.addTextField("ElementNumFormat", 12), grid.prop("left"));
        
        grid.newRow();
        grid.addLabel(local.text("Date_Format"), 1);
        grid.add(formController.addTextField("ElementDateFormat", 12), grid.prop("left"));

        return box;
    }
    
    public JComponent defaultAttributeEditor()
    {
        JPanel box = new JPanel();
        box.setBorder(new TitledBorder("Default Attribute Rules"));
        GridBagger grid = new GridBagger(box);
        grid.setInsets(5, 2);
        
        grid.newRow();
        grid.addLabel(local.text("Numeric_Format"), 1);
        grid.add(formController.addTextField("AttributeNumFormat", 12), grid.prop("left"));
        
        grid.newRow();
        grid.addLabel(local.text("Date_Format"), 1);
        grid.add(formController.addTextField("AttributeDateFormat", 12), grid.prop("left"));

        return box;
    }


    public void cmdOK(ActionEvent ae, BasicAction a)
    {
        try {
            QizxConnector ctor = target.browser.getConnector();
            ctor.setIndexing(target.library, indexing);
            
            ReindexingDialog dialog = new ReindexingDialog(app);
            dialog.showUpFor(target);
            if(!dialog.isCancelled())
                super.cmdOK(ae, a);
        }
        catch (Exception e) {
            app.showError(e);
        }
    }

    public void cmdLoad(ActionEvent ev, BasicAction a) 
    {
        if(fileChooser == null)
            fileChooser = app.newFileChooser("indexing");
        if(!app.showOpenDialog(fileChooser, this))
            return;
        File sheetFile = fileChooser.getSelectedFile();
        if(sheetFile == null)
            return;
        try {
            Indexing indexing = new Indexing();
            indexing.parse(new InputSource(sheetFile.getPath()));
            setIndexing(indexing);
        }
        catch (Exception e) {
            GUI.error(e.getMessage());
        }
    }

    public void cmdReset(ActionEvent ev, BasicAction a)
    {
        Indexing indexing = Indexing.defaultRules();
        setIndexing(indexing);
    }

    private FormController formController = new FormController()
    {
        public void setFullText(boolean selected)
        {
            indexing.setFulltextEnabled(selected);
        }
        public boolean getFullText()
        {
            return indexing.isFulltextEnabled();
        }

        public void setMinWord(int value) {
            indexing.setMinWordLength(value);
        }
        public int getMinWord() {
            return indexing.getMinWordLength();
        }

        public void setMaxWord(int value) {
            indexing.setMaxWordLength(value);
        }
        public int getMaxWord() {
            return indexing.getMaxWordLength();
        }

        public void setMaxContent(int value) {
            indexing.setMaxStringLength(value);
        }
        public int getMaxContent() {
            return indexing.getMaxStringLength();
        }
        
        public String getElementDateFormat()
        {
            //form.enableControl("ElementDateFormat", true);
            Rule rule = findRule(true, true);
            if(rule != null && rule.getSieve() instanceof FormatDateSieve) {
                FormatDateSieve sieve = (FormatDateSieve) rule.getSieve();
                return sieve.getFormat();
            }
            return null;
        }
        
        public void setElementDateFormat(String format)
        {
            setDateFormat(true, format);
        }
        
        public String getElementNumFormat()
        {
            Rule rule = findRule(true, false);
            if(rule != null && rule.getSieve() instanceof FormatNumberSieve) {
                FormatNumberSieve sieve = (FormatNumberSieve) rule.getSieve();
                return sieve.getFormat();
            }
            return null;
        }
        public void setElementNumFormat(String format)
        {
            setNumFormat(true, format);
        }
        
        public String getAttributeDateFormat()
        {
            Rule rule = findRule(!true, true);
            if(rule != null && rule.getSieve() instanceof FormatDateSieve) {
                FormatDateSieve sieve = (FormatDateSieve) rule.getSieve();
                return sieve.getFormat();
            }
            return null;
        }
        
        public void setAttributeDateFormat(String format)
        {
            setDateFormat(false, format);
        }

        public String getAttributeNumFormat()
        {
            Rule rule = findRule(!true, false);
            if(rule != null && rule.getSieve() instanceof FormatNumberSieve) {
                FormatNumberSieve sieve = (FormatNumberSieve) rule.getSieve();
                return sieve.getFormat();
            }
            return null;
        }
        public void setAttributeNumFormat(String format)
        {
            setNumFormat(false, format);
        }
    };
    
    
    private void setDateFormat(boolean element, String format)
    {
        Rule rule = findRule(element, true);
        if(rule == null)
            return;
        if(format.trim().length() == 0) {
            rule.setSieve(new ISODateSieve());
        }
        else {
            FormatDateSieve sieve = new FormatDateSieve();
            rule.setSieve(sieve);
            sieve.setFormat(format, null);
        }
    }
    
    private void setNumFormat(boolean element, String format)
    {
        Rule rule = findRule(element, false);
        if(rule == null)
            return;
        if(format.trim().length() > 0) {
            FormatNumberSieve sieve = new FormatNumberSieve();
            rule.setSieve(sieve);
            sieve.setFormat(format, null);
        }
    }
    private Indexing.Rule findRule(boolean elementRule, boolean date)
    {
        int type = date? Indexing.DATE_AND_STRING : Indexing.NUMERIC_AND_STRING;
        int nr = elementRule? indexing.getElementRuleCount()
                            : indexing.getAttributeRuleCount();
        for(int r = 0; r < nr; r++)  {
            Rule rule = elementRule? indexing.getElementRule(r)
                                   : indexing.getAttributeRule(r);
            if(rule.getName() == null && rule.getContext() == null &&
                 rule.getIndexingType() == type)
                return rule;
        }
        return null;
    }
}
