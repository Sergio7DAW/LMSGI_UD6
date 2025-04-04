/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of the Qizx application components
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.apps.studio.gui;

import java.io.File;
import javax.swing.filechooser.*;

public class GlobFileFilter extends FileFilter
{
    private String suffix;
    
    public GlobFileFilter(String extension) {
        suffix = "." + extension.toLowerCase();
    }
    
    public String getFileNameSuffix() {
        return suffix;
    }
    
    public boolean accept(File file)
    {
        if (file.isDirectory()) { 
            return true; // always wanted.
        }
        else {
            String name = file.getName().toLowerCase();
            return name.endsWith(suffix);
        }
    }
    
    public String getDescription() {
        return "*" + suffix;
    }
    
    public String toString() {
        return getDescription();
    }
    
    public int hashCode() {
        return suffix.hashCode();
    }
    
    public boolean equals(Object other) {
        if (other == null || !(other instanceof GlobFileFilter))
            return false;
        return suffix.equals(((GlobFileFilter) other).suffix);
    }
}
