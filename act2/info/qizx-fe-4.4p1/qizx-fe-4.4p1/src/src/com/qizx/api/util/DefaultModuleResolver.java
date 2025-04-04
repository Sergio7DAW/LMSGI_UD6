/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.api.util;

import com.qizx.api.ModuleResolver;
import com.qizx.util.basic.Check;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.transform.Source;

/**
 * Default ModuleResolver implementation using a base URL.
 * <p>
 * The strategy used consists of appending the module namespace URI to the base
 * URI, and (if possible) checking the existence of the pointed resource.
 * <p>
 * If the check fails, and if location hints are provided, then each hint is
 * appended to the base URL and put to the result list, unless it can be
 * verified that the URL formed this way points to a non-existent resource.
 */
public class DefaultModuleResolver
    implements ModuleResolver
{
    private URL base;

    /**
     * Creates a resolver using a base URL. 
     * @param base an URL used as a base for resolving module URI's. 
     */
    public DefaultModuleResolver(URL base)
    {
        Check.nonNull(base, "base URL");
        this.base = base;
        // due to weird behavior of URL class, need a trailing slash:
        if(base != null && !base.getPath().endsWith("/")) {
            try {
                this.base = new URL(base.getProtocol(), base.getHost(),
                                    base.getPath() + "/");
            }
            catch (MalformedURLException e) { // what to do ?
                e.printStackTrace();
            }
        }
    }

    public String toString()
    {
        return "DefaultModuleResolver [base=" + base + "]";
    }

    /** @see com.qizx.api.ModuleResolver#resolve
     */
    public URL[] resolve(String moduleNamespaceURI, String[] locationHints)
        throws MalformedURLException 
    {
        URL attempt = new URL(base, moduleNamespaceURI);

        if(existingFile(attempt))
            return new URL[] { attempt };
        // try hints:
        if(locationHints == null || locationHints.length == 0)
            return new URL[0]; // failure
        int hintCnt = locationHints.length;
        ArrayList<URL> result = new ArrayList<URL>(hintCnt);
        for (int i = 0; i < locationHints.length; i++) {
            URL url = new URL(base, locationHints[i]);
            
            if(existingFile(url))
                result.add(url);
        }
        return result.toArray(new URL[result.size()]);
    }
    
    public URL getBase()
    {
        return base;
    }

    // returns true if 'file:' and exists as a plain file
    private boolean existingFile(URL url)
    {
        if(!"file".equals(url.getProtocol()))
            return false;
        File file = new File(url.getPath());
        return file.isFile();
    }

    public Source resolveTemplates(String url)
    {
        return null; // not supported
    }
}
