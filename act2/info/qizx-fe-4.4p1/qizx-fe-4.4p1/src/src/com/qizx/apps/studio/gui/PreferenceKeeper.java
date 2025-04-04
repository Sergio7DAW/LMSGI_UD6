/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of the Qizx application components
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */

package com.qizx.apps.studio.gui;

import java.awt.Rectangle;
import java.util.prefs.Preferences;

/**
 * A Wrapper for preference management.
 * Manages 1 level: user (no more system preferences).
 */
public class PreferenceKeeper
{
    private static final char LF_SUBS = (char) 0xfff; // turnaround pb with XML storage
    
    private Preferences userPrefs;
    
    public PreferenceKeeper(Class mainClass)
    {
        userPrefs = Preferences.userNodeForPackage(mainClass);
    }
    
    /**
     * Gets a preference value first from user preferences, then from
     * system preferences.
     * @param key
     * @return null if not found.
     */
    public String get(String key)
    {
        String v = userPrefs.get(key, null);
        if(v != null)
            v = v.replace(LF_SUBS, '\n');
        return v;
    }

    public String get(String key, String defaultValue)
    {
        String v = userPrefs.get(key, null);
        return (v == null)? defaultValue : v;
    }

    /**
     * Store a preference into user preferences.
     */
    public void put(String key, String value)
    {
        userPrefs.put(key, value.replace('\n', LF_SUBS));
    }

    /**
     * Gets a preference value first from user preferences, then from
     * system preferences.
     * @param key
     */
    public int getInt(String key, int defaultValue)
    {
        try {
            String v = get(key);
            if(v != null)
                return Integer.parseInt(v);
        }
        catch (NumberFormatException e) {
        }
        return defaultValue;
    }

    public void put(String key, int value)
    {
        userPrefs.put(key, Integer.toString(value));
    }

    public boolean getBool(String key, boolean defaultValue)
    {
        String v = get(key);
        if(v != null)
            return "true".equals(v);
        return defaultValue;
    }

    public void put(String key, boolean value)
    {
        userPrefs.put(key, Boolean.toString(value));
    }

    
    public void putGeometry(String key, Rectangle r)
    {
        userPrefs.put(key, r.x + " " + r.y + " " + r.width + " " + r.height);
    }
    
    public Rectangle getGeometry(String key)
    {
        String g = userPrefs.get(key, null);
        if(g == null)
            return null;
        String[] values = g.split(" ");
        int x = 0, y = 0, w = 100, h = 100;
        try {
            x = Integer.parseInt(values[0]);
            y = Integer.parseInt(values[1]);
            w = Integer.parseInt(values[2]);
            h = Integer.parseInt(values[3]);
        }
        catch (NumberFormatException e) {
            ; // ignore
        }
        return new Rectangle(x, y, w, h);
    }
}
