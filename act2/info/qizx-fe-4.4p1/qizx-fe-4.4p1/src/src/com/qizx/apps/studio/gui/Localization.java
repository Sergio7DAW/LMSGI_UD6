/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of the Qizx application components
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */

package com.qizx.apps.studio.gui;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Message localization helper.
 * <p>Uses a fallback strategy when the messages are not found: returns
 * the message key with underscores replaced by spaces.
 */
public class Localization
{
    private ResourceBundle bundle;
    private String prefix;
    
    /**
     * Localization properties must be located in mainClass.properties,
     * mainClass_fr.properties etc.
     */
    public Localization(Class mainClass) {
        try {
            bundle = ResourceBundle.getBundle(mainClass.getName());
        }
        catch (MissingResourceException ignored) {
            // use default formatting
        }
        
    }
    
    /**
     * Localization properties must be located in mainClass.properties,
     * mainClass_fr.properties etc.
     */
    public Localization(Class mainClass, String prefix) {
        this(mainClass);
        this.prefix = prefix;
    }

    private String get(String key)
    {
        if (bundle != null)
            try {
                return bundle.getString(key);
            }
            catch (MissingResourceException e) { // proceed
            }
        return null;
    }
    
    private String getKey(String key)
    {
        String fmt = (prefix == null)? get(key) : get(prefix + "." + key);
        if (fmt != null)
            return fmt;
        // default processing: replace underscores by spaces
        return key.replace('_', ' ');
    }
    
    /**
     * Returns a simple localized value from a resource key.
     * @param key resource key
     * @return The value found in locale resources. If not found returns
     * the key with underscores replaced by spaces.
     */
    public String text(String key) {
        return getKey(key);
    }
    
    public String msg(String key, Object[] args) {
        return MessageFormat.format(getKey(key), args);
    }
    
    public String msg(String key, Object arg1) {
        return MessageFormat.format(getKey(key), new Object[] { arg1 });
    }
    
    public String msg(String key, Object arg1, Object arg2) {
        return MessageFormat.format(getKey(key), new Object[] { arg1, arg2 });
    }
    
    public String msg(String key, Object arg1, Object arg2, Object arg3) {
        return MessageFormat.format(getKey(key),
                                    new Object[] { arg1, arg2, arg3 });
    }
    
    public String msg(String key, Object arg1, Object arg2, 
                      String arg3, String arg4) {
        return MessageFormat.format(getKey(key),
                                    new Object[] { arg1, arg2, arg3, arg4 });
    }
    
    public String msg(String key, Object arg1, int arg2) {
        return MessageFormat.format(getKey(key),
                                    new Object[] { arg1, new Integer(arg2) });
    }
}
