/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.util;

import com.qizx.api.QName;
import com.qizx.xdm.IQName;

import java.util.*;

/**
 * Management of a local table of qualified names (for documents or
 * collections).
 * <p>
 * Names are accessed by index or by value (namespace+localname).
 */
public class QNameTable
{
    // instance variables:
    protected ArrayList<IQName> names_ = new ArrayList<IQName>(32);
    protected HashMap<Key, Key> nameMap_ = new HashMap<Key, Key>();
    protected Key probe = new Key(null, null);

    /**
     * Searches a name and returns its index in the table. If the name is not
     * found, it is inserted.
     * 
     * @param uri Namespace URI.
     * @param localName
     */
    public synchronized int enter(String uri, String localName) // BUG FIX
    {
        probe.uri = uri;
        probe.localName = localName;
        Key key = nameMap_.get(probe);
        if (key != null)
            return key.code;
        return addName(uri, localName);
    }

    public int enter(QName name)
    {
        return enter(name.getNamespaceURI(), name.getLocalPart());
    }

    /**
     * Lookup of a qualified name.
     * 
     * @return the index of the name in the table, or -1 if not found.
     */
    public synchronized int find(String uri, String localName) // BUG FIX
    {
        probe.uri = uri;
        probe.localName = localName;
        Key key = nameMap_.get(probe);
        return key != null ? key.code : -1;
    }

    /**
     * Lookup of a qualified name.
     * 
     * @return the index of the name in the table, or -1 if not found.
     */
    public int find(QName name)
    {
        return find(name.getNamespaceURI(), name.getLocalPart());
    }

    /**
     * Inserts a name without check (for loading).
     */
    public int addName(String uri, String localName)
    {
        Key key = new Key(uri, localName);
        key.code = names_.size();
        names_.add(IQName.get(uri, localName));
        nameMap_.put(key, key);
        // System.out.println("addName "+uri+" "+localName+" "+key.code);
        return key.code;
    }

    /**
     * Gets the unique name associated with an index.
     */
    public IQName getName(int rank)
    {
        if (rank < 0 || rank >= names_.size())
            return null;
        return names_.get(rank);
    }

    /**
     * Returns the number of names stored in this table.
     */
    public int size()
    {
        return names_.size();
    }

    /**
     * Clears all the contents.
     */
    public synchronized void clear()
    {
        names_.clear();
        nameMap_ = new HashMap<Key, Key>();
    }

    public synchronized String[] getNamespaces()
    {
        Vector<String> nst = new Vector<String>();
        for (int n = names_.size(), ns; --n >= 0;) {
            String ens = getName(n).getNamespaceURI();
            for (ns = nst.size(); --ns >= 0;)
                if (ens == nst.elementAt(ns))
                    break;
            if (ns < 0)
                nst.addElement(ens);
        }
        return nst.toArray(new String[nst.size()]);
    }

    static class Key
    {
        String uri;
        String localName;

        int code;

        Key(String uri, String localName)
        {
            this.uri = uri;
            this.localName = localName;
        }

        public int hashCode()
        {
            return uri.hashCode() ^ localName.hashCode();
        }

        public boolean equals(Object other)
        {
            if (other == null || !(other instanceof Key))
                return false;
            Key n = (Key) other;
            // System.out.println("equals {"+uri+"}"+localName+"
            // to{"+n.uri+"}"+n.localName);
            return uri.equals(n.uri) && localName.equals(n.localName);
        }
    }
}
