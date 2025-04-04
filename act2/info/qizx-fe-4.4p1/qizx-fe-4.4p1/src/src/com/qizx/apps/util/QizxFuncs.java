/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of the Qizx application components
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.apps.util;


import com.qizx.api.Configuration;
import com.qizx.api.LibraryManager;
import com.qizx.api.QizxException;

import java.io.File;


/**
 * Utility functions that can be called in XQuery through Java binding.
 */
public class QizxFuncs
{

    /**
     * Creates a XML Library group and included Libraries
     */
    public static void createGroup(String rootPath, String [] libraries)
        throws QizxException
    {
        LibraryManager libMan = Configuration.createLibraryGroup(new File(rootPath));
        for(String lib : libraries) {
            libMan.createLibrary(lib, null);
        }
        libMan.closeAllLibraries(100);
    }
    
    public static void fatal(String message)
    {
        System.err.println(message);
        System.exit(1);
    }
    
    public static void usage(String message)
    {
        System.err.println("usage: " + message);
        System.exit(2);
    }
}
