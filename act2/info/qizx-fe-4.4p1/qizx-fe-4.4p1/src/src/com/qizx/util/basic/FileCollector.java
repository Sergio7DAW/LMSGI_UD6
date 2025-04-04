/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.util.basic;

import com.qizx.util.StringPattern;

import java.io.File;
import java.util.ArrayList;

/**
 * Expands a root-path and a pattern into a list of files.
 * <p>Does not support wildcard inside the root path. Only the NAME of
 * the file is used for matching the pattern.
 */
public class FileCollector
{
    private File root;
    private boolean descent;
    private ArrayList<File> files = new ArrayList<File>();
    private long byteSize;
    private StringPattern filter;
   
    public FileCollector(String root, boolean descent, StringPattern filter)
    {
        this(new File(root), descent, filter);
    }

    public FileCollector(File root, boolean descent, StringPattern filter)
    {
        this.root = root;
        this.descent = descent;
        this.filter = filter;
    }

    public File getRoot()
    {
        return root;
    }

    public boolean hasDirRoot()
    {
        return root.isDirectory();
    }

    public long getByteSize() {
        return byteSize;
    }

    public int getSize() {
        return files.size();
    }

    public void collect()
    {
         if(root.isDirectory())
             descent(root);
         else if(root.isFile())
             addFile(root);
    }

    private void addFile(File file)
    {
        files.add(file);
        byteSize += file.length();
    }

    private void descent(File dir)
    {
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if(file.isDirectory()) {
                if(descent)
                    descent(file);
            }
            else if(filter == null || filter.matches(file.getName()))
                addFile(file);
        }
    }
    
    public File getFile(int index) {
        return files.get(index);
    }

    public StringPattern getFilter()
    {
        return filter;
    }

    public void setFilter(StringPattern filter)
    {
        this.filter = filter;
    }
}
