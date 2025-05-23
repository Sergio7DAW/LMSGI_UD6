/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.util;

import com.qizx.api.Configuration.Property;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigTable extends HashMap<Property,Object>
{
    private String title;

    public ConfigTable(String title, Property[] predefined)
    {
        this.title = title;
        for(Property prop : predefined)
            predefine(prop);
    }

    public synchronized void predefine(Property prop)
    {
        put(prop, prop.getDefaultValue());
    }

    public synchronized ConfigTable copy()
    {
        return (ConfigTable) clone();
    }

    public synchronized void save(File file) throws IOException
    {
        Properties props = new Properties();
        for (Map.Entry<Property, Object> e : entrySet()) {
            Object v = e.getValue();
            if (v == null || v.equals(e.getKey().getDefaultValue()))
                continue; // not changed
            props.setProperty(e.getKey().getName(), v.toString());
        }

        File save0 = new File(file.getPath() + "tmp");
        File old = new File(file.getPath() + "~");

        FileOutputStream out = new FileOutputStream(save0);
        props.store(out, "##\n## DO NOT EDIT\n" + 
           "## File generated by saving the configuration of " + title + "\n");
        out.close();
        old.delete();
        if (file.exists())
            file.renameTo(old);
        save0.renameTo(file);
    }

    public synchronized void load(File confFile) throws IOException
    {
        Properties props = new Properties();
        FileInputStream in = new FileInputStream(confFile);
        props.load(in);
        in.close();

        for (Enumeration<?> e = props.keys(); e.hasMoreElements();) {
            String name = (String) e.nextElement();
            Property prop = findProperty(name);
            if(prop == null)
                continue;   // ignore
            put(prop, props.getProperty(name));
        }
    }
    
    public synchronized Property findProperty(String name)
    {
        for(Property p : keySet()) {
            if(name.equalsIgnoreCase(p.getName()))
                return p;
        }
        return null;
    }
    
    public synchronized boolean booleanProp(Property prop)
    {
        return prop.booleanValue(get(prop));
    }

    public synchronized long longProp(Property prop)
    {
        return prop.longValue(get(prop));
    }

    public synchronized int intProp(Property prop)
    {
        return prop.intValue(get(prop));
    }

    public synchronized String stringProp(Property prop)
    {
        return prop.stringValue(get(prop));
    }
}
