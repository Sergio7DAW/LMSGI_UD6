/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of the Qizx application components
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.apps.studio.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;


/**
 *	Scrollable text view, with tool bar.
 *<p> Supports text styles.
 */
public class TextPort extends ToolView
    implements CaretListener, UndoableEditListener
{
    private static final String LINK_ATTR = "#link#";
    private static final int MIN_CHAR_WIDTH = 7;
    
    protected JTextPane  text;
    protected StyledDocument doc;
    protected StyleContext styles = new StyleContext();
    protected AttributeSet BLANK_STYLE = null;
    protected UndoManager undoer;
    protected BasicAction undoAction, redoAction;
    protected JPopupMenu popup;
    
    private boolean recordUndoEdits;

    private int columns;
    private boolean blockedSelect;
    
    public TextPort(String title)
    {
        this(title, -1);
    }
    
    /**
     * @param columns if negative: variable width w/ wrapping.
     */
    public TextPort(String title, final int columns)
    {
        super(title, null);
        doc = new DefaultStyledDocument();
        this.columns = columns;
        BLANK_STYLE = doc.addStyle("", null);
        
        text = new JTextPane(doc) {
           
            // just crappy this way of telling it wraps...
            public boolean getScrollableTracksViewportWidth()
            {
                if( TextPort.this.columns <= 0)
                    return true;
                //getParent().setBackground(Color.white);
                return false;
            }
            
            public Dimension getPreferredSize()
            {
                int cols = TextPort.this.columns;
                if(cols <= 0)
                    return super.getPreferredSize();
                
                View view = (View) getUI().getRootView(this);

                float w = Math.max(cols * MIN_CHAR_WIDTH,
                                   view.getPreferredSpan(View.X_AXIS));
                float h = view.getPreferredSpan(View.Y_AXIS);

                return new Dimension((int) Math.ceil(w + 2 * MIN_CHAR_WIDTH),
                                     (int) Math.ceil(h));
            }

        };
        
        setView(text);
        scrollPane.getViewport().setOpaque(true);
        scrollPane.getViewport().setBackground(Color.white);
    }
    
    public void changeFont(int style, boolean fixedWidth) {
        Font ft = text.getFont();
        if(fixedWidth)
             ft = new Font("Monospaced", style, ft.getSize());
        else ft = ft.deriveFont(style);
        text.setFont(ft);
        //resetWidth();
    }
    
    public void changeFontSize(int size) {
        Font ft = text.getFont();
        text.setFont(ft.deriveFont((float) size));
    }
    
//    private void resetWidth()
//    { 
//        if(columns <= 0)
//            return;
//        Dimension size = new Dimension(columns * GUI.getFontWidth(text), 0);
//        text.setMinimumSize(size);
//        text.setPreferredSize(size);
//    }

    public void clearText()
    {
        try {
            doc.remove(0, doc.getLength());
        }
        catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    public String getText()
    {
        String txt = text.getText();
        return txt == null? "" : txt;
    }
    
    public void setText(String value)
    {
        text.setText(value);
    }
    
    public JTextPane getTextPane() {
        return text;
    }

    public int appendText( String txt, AttributeSet style ) {
        try {
            int pos = doc.getLength();
            doc.insertString( pos, txt, style );
            //Rectangle r = text.modelToView(doc.getLength());
            //getViewport().scrollRectToVisible(r);
            pos = doc.getLength();
            selectPosition(pos);
            return pos;
        }
        catch(BadLocationException e) {
            e.printStackTrace();
            return doc.getLength();
        }
    }
    
    public void displaySpan(int startPoint, int endPoint,
                            AttributeSet style, boolean replace)
    {
        if (endPoint > startPoint && style != null)
            doc.setCharacterAttributes(startPoint, endPoint - startPoint,
                                       style, replace);
    } 


    public void clearStyles()
    {
        doc.setCharacterAttributes(0, doc.getLength(), BLANK_STYLE, true);
    }

    public void selectPosition(int pos)
    {
        text.select(pos, pos);
    }

    public void select(int position, int endPos)
    {
        blockedSelect = true;
        text.requestFocusInWindow();
        text.select(position, endPos);
        blockedSelect = false;
    }

    public JButton addButton(Action action, int position)
    {
        JButton button = new JButton(action);
        addTool(button, position);
        return button;
    }

    public Style getStyle(String name) {
        return doc.getStyle(name);
    }
    
    public Style addStyle(String name, Color fontColor) {
        return addStyle(name, fontColor, true);
    }
    
    public Style addStyle(String name, Color fontColor, boolean bold) {
        Style style = doc.addStyle(name, null);
        StyleConstants.setForeground( style, fontColor);
        StyleConstants.setBold(style, bold);
        return style;
    }

    public Style addStyle(String name, Color fontColor, Color bgColor)
    {
        Style style = doc.addStyle(name, null);
        if (fontColor != null)
            StyleConstants.setForeground(style, fontColor);
        if (bgColor != null)
            StyleConstants.setBackground(style, bgColor);
        return style;
    }
    
    public static MutableAttributeSet mutableStyle( Style parent ) {
        return new SimpleAttributeSet(parent);
    }

    public static MutableAttributeSet linkStyle(Style style, Object link)
    {
        MutableAttributeSet must = mutableStyle(style);
        must.addAttribute(LINK_ATTR, link);
        return must;
    }

    public interface HyperlinkAction
    {
        void click(TextPort source, int textOffset);
    }
    
    public void addLink(String text, Style style, HyperlinkAction action)
    {
        appendText(text, linkStyle(style, action));
    }

    public void caretUpdate(CaretEvent e)
    {
        if (blockedSelect)
            return;
        Element elem = doc.getCharacterElement(e.getDot());
        if(elem == null)
            return;
        HyperlinkAction link =
            (HyperlinkAction) elem.getAttributes().getAttribute(LINK_ATTR);
        if(link != null)
            link.click(this, e.getDot());
    }
    
    public void enableUndo()
    {
        undoer = new UndoManager();
        doc.addUndoableEditListener(this);
        undoAction = new BasicAction("Undo", GUI.getIcon("undo.png"), "cmdUndo", this);
        GUI.bindAction(this, "control Z", "undo", undoAction);
        redoAction = new BasicAction("Redo", GUI.getIcon("redo.png"), "cmdRedo", this);
        GUI.bindAction(this, "control Y", "redo", redoAction);
    }
    
    public Action getAction(String name)
    {
        if("undo".equals(name))
            return undoAction;
        if("redo".equals(name))
            return redoAction;
        return GUI.getAction(text, name);
    }

    protected void enableUndoEvents(boolean enable)
    {
        recordUndoEdits = enable;
    }

    public void undoableEditHappened(UndoableEditEvent e)
    {
        UndoableEdit edit = e.getEdit();
        if(!recordUndoEdits)
            return;
        undoer.addEdit(edit);
        undoActionsEnable();
    }

    public void cmdUndo(ActionEvent e, BasicAction a)
    {
        if(undoer.canUndo())
            undoer.undo();
        undoActionsEnable();
    }
    
    public void cmdRedo(ActionEvent e, BasicAction a)
    {
        if(undoer.canRedo())
            undoer.redo();
        undoActionsEnable();    
    }

    private void undoActionsEnable()
    {
        undoAction.setEnabled(undoer.canUndo());
        redoAction.setEnabled(undoer.canRedo());
    }
}
