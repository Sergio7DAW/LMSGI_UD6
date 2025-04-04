/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.util.basic;



/**
 * Home-made assertions, used both internally and for API checks
 */
public final class Check
{

    public static void equals(long v1, long v2, String message) {
        if(v1 != v2)
            throw new AssertionError(message + ": " + v1 + " != " + v2);
    }

    public static void equals(int v1, int v2, String message) {
        if(v1 != v2)
            throw new AssertionError(message + ": " + v1 + " != " + v2);
    }
   
    public static void condition(boolean condition, String message) {
        if(!condition)
            throw new AssertionError(message);
    }
   
    public static void nonNull(Object obj, String name) {
        if(obj == null)
            throw new AssertionError(name + " should not be null");
    }

    public static void mustBe(String that)
    {
        throw new AssertionError("argument must be " + that);
    }

    public static void implementation(Object object, Class classe, Class interf)
    {
         if(!(classe.isInstance(object)))
             throw new AssertionError("unsupported implementation of interface "
                                      + interf.getName());
    }

    public static void bug(String message)
    {
        System.err.println("BUG: "+message);
        Thread.dumpStack();
    }
   
}
