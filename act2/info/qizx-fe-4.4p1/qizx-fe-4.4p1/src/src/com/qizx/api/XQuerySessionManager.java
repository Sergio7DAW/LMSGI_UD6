/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.api;

import com.qizx.api.Configuration.Property;
import com.qizx.api.fulltext.FullTextFactory;
import com.qizx.api.util.DefaultModuleResolver;
import com.qizx.util.basic.Check;
import com.qizx.xdm.DocumentPool;
import com.qizx.xquery.ModuleManager;
import com.qizx.xquery.XQuerySessionImpl;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Manager of simple XQuery sessions without access to a XML Library.
 * <p>Provides a cache of Modules and a cache of transient documents, shared
 * among the sessions created on this manager. This cache avoids reparsing XML
 * documents if different sessions access it. It can detect a change on
 * documents stored in the file-system and reload the document.
 */
public class XQuerySessionManager
{
    String COMMIT_SYNCS = "commit_syncs";
    String CONTENT_IMPORTERS = "content_importers";

    private ModuleManager moduleMan;
    private DocumentPool  documentCache;
    private int sessionIds;
    private volatile WeakReference<XQuerySession>[] sessions = new WeakReference[4];
    private Object sessionsMutex = new Object();
    private FullTextFactory defaultFTFactory;
    // Configuration properties:
    protected HashMap<Property,Object> config = new HashMap<Property,Object>();

    /**
     * Creates a session manager with a default Module Resolver and a default
     * cache for parsed documents.
     * <p><b>Attention: </b> this method does not inherit setup from
     *  {@link Configuration}. It is likely to be deprecated in the future.
     * @param moduleBaseURI base URI for the default Module Resolver
     */
    public XQuerySessionManager(URL moduleBaseURI)
    {
        this(new DefaultModuleResolver(moduleBaseURI), -1);
    }

    /**
     * Creates a session manager.
     * <p><b>Attention: </b> this method does not inherit setup from
     *  {@link Configuration}. It is likely to be deprecated in the future.
     * 
     * @param moduleResolver resolver used for modules
     * @param transientDocumentCacheSize size in bytes of the document cache
     */
    public XQuerySessionManager(ModuleResolver moduleResolver,
                                int transientDocumentCacheSize)
    {
        moduleMan = new ModuleManager(moduleResolver);
        documentCache = new DocumentPool();
        if(transientDocumentCacheSize >= 0)
            documentCache.setCacheSize(transientDocumentCacheSize);

        predefined(Configuration.ALLOWED_CLASSES);
        predefined(Configuration.CONTENT_IMPORTERS);
        predefined(Configuration.TRANSIENT_DOCUMENT_CACHE_SIZE);
        predefined(Configuration.MEMORY_LIMIT);
    }
    
    /**
     * Gets the current configuration properties of the Session Manager.
     * @return a mapping of configuration Property descriptors to values.
     */
    public Map<Property, Object> getConfiguration()
    {
        return (Map<Property, Object>) config.clone();
    }

    /**
     * Configures a XQuerySessionManager with a set of properties.
     * @param properties a set of properties. Recognized properties are:
     *        <ul>
     *        <li>"<b>TRANSIENT_DOCUMENT_CACHE_SIZE</b>": the value must represent
     *        an integer number of bytes. This is equivalent to calling
     *        setTransientDocumentCacheSize.
     *        <li>"<b>CONTENT_IMPORTERS</b>": a comma-separated list of classes
     *        that can be used for importing miscellaneous content formats into an
     *        XML Library. These classes must implement ContentImporter.
     *        </ul>
     * @exception QizxException <p>
     *            When one or several invalid property values are specified. An
     *            invalid value does not prevent valid properties to be taken into
     *            account.
     * @since 4.2
     */
    public void configure(Properties properties)
        throws QizxException
    {
        StringBuilder errors = null;

        Enumeration props = properties.keys();
        for (; props.hasMoreElements();) {
            String name = (String) props.nextElement();
            String value = properties.getProperty(name);
            Property prop = Configuration.findProperty(name);
            if(prop == null)
                continue;   // ignore
            try {
                configure(prop, value);
            }
            catch (Exception e) {
                String msg = " property '" + name + "': " + e.getMessage();
                if (errors == null)
                    errors = new StringBuilder(msg);
                else
                    errors.append("\n" + msg);
            }
        }
        if (errors != null)
            throw new QizxException(errors.toString());
    }
   
    /**
     * (Internal use)
     */
    public void configure(Map<Property,Object> setup)
        throws Exception
    {
        for( Map.Entry<Property, Object> e : setup.entrySet()) {
            configure(e.getKey(), e.getValue());
        }
    }

    /**
     * Configures a XQuerySessionManager with a Property.
     * @param property a Property. Recognized properties are:
     *        <ul> 
     *        <li>"<b>{@link Property#TRANSIENT_DOCUMENT_CACHE_SIZE}</b>": the value must represent
     *        an integer number of bytes. This is equivalent to calling
     *        setTransientDocumentCacheSize.
     *        <li>"<b>{@link Property#CONTENT_IMPORTERS}</b>": a comma-separated list of classes
     *        that can be used for importing miscellaneous content formats into an
     *        XML Library. These classes must implement ContentImporter.
     *        </ul>
     * @param value see each Property
     * @return true if Property is recognized.
     * @throws Exception
     */
    public boolean configure(Property property, Object value)
        throws Exception
    {
        value = property.checkValue(value);

        // special case: restore default value
        if (value == null)
            value = property.getDefaultValue();
        
        Object oldValue = config.get(property);
        if (oldValue == null && !config.containsKey(property))
            return false;   // not recognized: all known properties are in map
        config.put(property, value);
        
        // special processing for properties needing immediate effect:

        if (property == Configuration.CONTENT_IMPORTERS) {
            // not pretty: seems local but actually global
            String[] names = property.stringValue(value).split("[ \t\r\n;,]+");
            for (String className : names) {
                if (className.length() > 0) {
                    Configuration.addContentImporter(className);
                }
            }
        }
        else if (property == Configuration.TRANSIENT_DOCUMENT_CACHE_SIZE) {
            setTransientDocumentCacheSize(property.longValue(value));
        }
        else if (property == Configuration.FULLTEXT_FACTORY) {
            String svalue = (String) value;
            setFullTextFactory((FullTextFactory) Configuration
                                   .instantiate(svalue.trim(), FullTextFactory.class));
        }

        return !value.equals(oldValue);
    }

    protected void predefined(Property prop)
    {
         config.put(prop, prop.getDefaultValue());
    }

    /**
     * Creates a new XQuery session.
     * 
     * @return a new XQuery session using the resources of this session
     * manager
     */
    public synchronized XQuerySession createSession()
    {
        XQuerySessionImpl session = new XQuerySessionImpl(this, ++ sessionIds);

        String classes = config.get(Configuration.ALLOWED_CLASSES).toString();
        if(classes != null) {
            String[] names = classes.split("[ \t\r\n;,]+");
            for (String className : names) {
                if (className.length() > 0) {
                    session.enableJavaBinding(className);
                }
            }
        }

        FullTextFactory ftf;
        if(defaultFTFactory != null) {
            try {
                ftf = (FullTextFactory) defaultFTFactory.getClass().newInstance();
                session.setFullTextFactory(ftf);
            }
            catch (Exception e) {
                //throw new LibraryException("cannot instantiate FullTextFactory", e);
            }
        }

        synchronized (sessionsMutex ) {
            // create a weak reference to the view
            int v = sessions.length;
            WeakReference<XQuerySession> newRef = new WeakReference<XQuerySession>(session);
            for (; --v >= 0;)
                if (sessions[v] == null || sessions[v].get() == null) {
                    sessions[v] = newRef; // reuse slot
                    break;
                }
            if (v < 0) {
                WeakReference<XQuerySession>[] old = sessions;
                sessions = new WeakReference[old.length * 2];
                System.arraycopy(old, 0, sessions, 0, old.length);
                sessions[old.length] = newRef;
            }
        }
        
        return session;
    }

    public List<XQuerySession> listSessions()
    {
        ArrayList<XQuerySession> list = new ArrayList<XQuerySession>();
        synchronized (sessions) {
            for (int v = sessions.length; --v >= 0;)
                if (sessions[v] != null) {
                    XQuerySession view = (XQuerySession) sessions[v].get();
                    if (view != null)
                        list.add((XQuerySession) view);
                }
        }
        return list;
    }

    /**
     * Finds a session by its public identifier.
     * @param id a public identifier returned by {@link XQuerySession#getIdentifier()}.
     * @return a session, or null if not found.
     * @since 4.4
     */
    public XQuerySession findSession(int id)
    {
        synchronized (sessions) {
            for (int v = sessions.length; --v >= 0;)
                if (sessions[v] != null) {
                    XQuerySession s = (XQuerySession) sessions[v].get();
                    if (s != null && id == s.getIdentifier())
                        return s;
                }
        }
        return null;
    }

    /**
     * For internal use.
     */
    public ModuleManager getModuleManager()
    {
        return moduleMan;
    }

    /**
     * Sets the maximum memory size for the document cache. The document cache
     * stores transient documents which are parsed in memory.
     * 
     * @param size maximum memory size in bytes. Decreasing this size will
     * flush the cache accordingly.
     * @return the former maximum memory size in bytes
     */
    public long setTransientDocumentCacheSize(long size)
    {
        long oldSize = documentCache.getCacheSize();
        documentCache.setCacheSize(size);
        return oldSize;
    }

    /**
     * Gets the current maximum memory size for the document cache.
     * 
     * @return a size in bytes
     */
    public long getTransientDocumentCacheSize()
    {
        return documentCache.getCacheSize();
    }

    /**
     * For internal use.
     */
    public DocumentPool getDocumentCache()
    {
        return documentCache;
    }

    /**
     * For internal use.
     */
    public void setDocumentCache(DocumentPool documentCache)
    {
        this.documentCache = documentCache;
    }

    /**
     * Defines a resolver of XQuery modules.
     * @param resolver a module resolver
     */
    public void setModuleResolver(ModuleResolver resolver)
    {
        Check.nonNull(resolver, "resolver");
        moduleMan.setResolver(resolver);
    }

    /**
     * Returns the current Resolver of XQuery modules.
     * @return the current module resolver
     */
    public ModuleResolver getModuleResolver()
    {
        return moduleMan.getResolver();
    }

    public FullTextFactory getFullTextFactory()
    {
        return defaultFTFactory;
    }

    public void setFullTextFactory(FullTextFactory factory)
    {
        defaultFTFactory = factory;
    }

    public static void bind(XQuerySession session, String prefix, Class classe)
    {
        String name = classe.getName();
        session.getContext().declarePrefix(prefix, "java:" + name);
        session.enableJavaBinding(name);
    }
}
