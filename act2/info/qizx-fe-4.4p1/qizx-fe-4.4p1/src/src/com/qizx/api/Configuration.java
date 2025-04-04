/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.api;

import com.qizx.api.fulltext.FullTextFactory;
import com.qizx.util.basic.Check;
import com.qizx.util.basic.FileUtil;

import com.qizx.xquery.LibraryManagerImpl;
import com.qizx.xlib.XMLLibraryEngineFactory;

import com.qizx.xlib.lick.EvaluationKey;
import java.util.Date;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Handler;

/**
 * Bootstrap Configuration for Qizx.
 * <p>
 * Replaces the deprecated {@link LibraryManagerFactory}.
 * <p>
 * Methods of this class allow:
 * <ul>
 * <li>opening or creating a group of databases (XML Library): see
 * {@link #openLibraryGroup(File)} and {@link #createLibraryGroup(File)}.
 * <li>Opening plain sessions (without database): see
 * {@link #createSessionManager}
 * <li>Manage addons and options.
 * </ul>
 */
public class Configuration
{
    private static Configuration singleInstance;
    private static Addons addonMan;
    
    private static HashMap<String, String> importers =
         new HashMap<String, String>();
    static {

      System.setProperty("com.qizx.fe", "");
      EvaluationKey evalKey = null;
      try {
          evalKey = EvaluationKey.load("qze");
          Date now = new Date();
          if(evalKey != null && now.getTime() < evalKey.expires.getTime())
              System.setProperty("com.qizx.fe.expires",
                                 evalKey.expires.toString());
      }
      catch (Exception e) { ; } 
        for (String imp : ContentImporter.DEFAULT_IMPORTERS) {
            try {
                addContentImporter(imp);
            }
            catch (InstantiationException e) {
                System.err.println("cannot instantiate ContentImporter: " + e.getMessage());
            }
        }
    }

    private HashMap<File, LibraryManagerImpl> allManagers =
        new HashMap<File, LibraryManagerImpl>();
    private Handler bootstrapLogHandler;
    private HashMap<Property, Object> setup;

    private Configuration()
    {
    }
    
    /**
     * Defines a set of properties which will be inherited by LibraryManagers
     * created by {@link #createLibraryGroup(File)},
     * {@link #openLibraryGroup(File)} or {@link #createLibraryManager()}.
     * @param properties
     * @throws IllegalArgumentException
     */
    public static void setup(Properties properties)
        throws IllegalArgumentException
    {
        instance().doSetup(properties);
    }

    /**
     * Sets a property, which will be inherited by LibraryManagers created by
     * {@link #createLibraryGroup(File)}, {@link #openLibraryGroup(File)} or
     * {@link #createLibraryManager()}.
     * @param property see {@link Property}
     * @param value  see each {@link Property}
     * @throws IllegalArgumentException
     */
    public static void set(Property property, Object value)
        throws IllegalArgumentException
    {
        instance().doSet(property, value);
    }

    /**
     * Returns a list of classes allowed for Java Binding.
     * <p>These classes are defined by set({@link Property#ALLOWED_CLASSES}, names).
     * Notice that this property value is cumulative: repeated calls add new names
     * to the current list. To erase the lsit, set a value of <code>null</code>.
     * @return a comma-separated list of full class names.
     */
    public static String getAllowedClasses()
    {
        HashMap<Property, Object> conf = instance().setup;
        if (conf == null)
            return "";  // not null!
        String classes = (String) conf.get(ALLOWED_CLASSES);
        return (classes == null)? "" : classes;
    }

    /**
     * Defines a bootstrap Log Handler used when creating LibraryManagers
     * or plain sessions.
     * @param handler
     */
    public static void setLogHandler(Handler handler)
    {
        instance().bootstrapLogHandler = handler;
    }

    /**
     * Defines an extension jars for add-ons.
     * @param jarFile a jar file.
     */
    public static void defineExtensionJar(File jarFile)
    {
        addons().addJar(jarFile);
    }
    
    /**
     * Defines a directory containing extension jars.
     * Recursively scans this directory.
     * @param directory a readable directory
     */
    public static void defineExtensionDirectory(File directory)
    {
        addons().addDirectory(directory);
    }

    /**
     * (Internal) finds a class name using extended classpath.
     * @param className full name of a class
     */
    public static Class<?> getExtensionClass(String className)
        throws ClassNotFoundException
    {
        return addons().getClass(className);
    }
    
    /**
     * (Internal) instantiates a class name using extended classpath.
     * @param className full name of a class
     */
    public static Object instantiate(String className)
        throws InstantiationException
    {
        return addons().instantiate(className);
    }

    /**
     * (Internal) instantiates a class name using extended classpath.
     * Checks the implements the 'implemented' class.
     * @param className full name of a class
     * @param implemented a superclass or interface
     */
    public static Object instantiate(String className, Class<?> implemented)
        throws InstantiationException
    {
        return addons().instantiate(className, implemented);
    }

    /**
     * Creates a plain Session Manager. This method is to be preferred to
     * direct creation with <code>new XQuerySessionManager()</code>.
     * <p>
     * Sets a default Module Resolver with a base URI and
     * a default cache for parsed documents.
     * <p>
     * @param baseURI a base URI for resolution of modules.
     * @return a XQuerySessionManager.
     * @exception QizxException if <tt>storageDir</tt> exists and is not empty.
     */
    public static XQuerySessionManager createSessionManager(String baseURI)
        throws QizxException
    {
        return instance().createSessionMan(baseURI);
    }


    /**
     * Tests whether a directory contains an XML Library.
     * @param location a directory supposedly containing an
     * XML Library. It can also be any file or directory within an XML Library.
     * @return the directory containing the Qizx XML Library, or null if the
     * location does not correspond to an XML Library.
     * @exception FileNotFoundException if parent directory cannot be accessed
     * @since 4.0 
     */
    public static synchronized File locateLibrary(File location)
        throws FileNotFoundException
    {
        return XMLLibraryEngineFactory.findLibraryRoot(location);
    }
    
    /**
     * Creates a LibraryManager without group directory and without XML Libraries.
     * <p>
     * Libraries can then be managed through {@link LibraryManager#manageLibrary}
     * or created through {@link LibraryManager#createLibrary}.
     * <p>
     * @return an active LibraryManager managing the new group.
     * @exception QizxException if <tt>storageDir</tt> exists and is not empty.
     */
    public static LibraryManager createLibraryManager()
        throws QizxException
    {
        return instance().createManager();
    }

    /**
     * Creates a group of XML Libraries and the LibraryManager controlling it.
     * <p>
     * There is initially no XML Library in the created group. A Library can be
     * added by {@link LibraryManager#createLibrary}.
     * @param groupDirectory a directory which is the location of the Library
     *        Group. This directory is created if it does not exist.
     * @return an active LibraryManager managing the new group.
     * @exception QizxException if <tt>storageDir</tt> exists and is not empty.
     */
    public static LibraryManager createLibraryGroup(File groupDirectory)
        throws QizxException
    {
        return instance().createGroup(groupDirectory);
    }

    /**
     * Starts a LibraryManager controlling an existing group of XML Libraries.
     * <p>XML Libraries are located as sub-directories of the 'group directory'. 
     * They are found by scanning the group directory (the
     * descriptor file group.qlg is deprecated).
     * @param groupDirectory a directory which is the root location of the group.
     * @return an active LibraryManager managing the group.
     * @exception QizxException if <tt>storageDir</tt> has not been initialized by
     *            invoking {@link #createLibraryGroup(File)}.
     */
    public static LibraryManager openLibraryGroup(File groupDirectory)
        throws QizxException
    {
        return instance().openGroup(groupDirectory);
    }
    
    private LibraryManager createManager()
        throws QizxException
    {
        try {
            LibraryManagerImpl man = new LibraryManagerImpl(null);
            if(setup != null)
                man.configure(setup);
            return man;
        }
        catch (Exception e) {
            throw new LibraryException(e.getMessage()); // should not happen
        }
    }

    private LibraryManager createGroup(File groupDirectory)
        throws QizxException
    {        
        Check.nonNull(groupDirectory, "groupDirectory");
        try {
            File keyDir = groupDirectory.getCanonicalFile();
            checkNotAlreadyOpen(keyDir);
            LibraryManagerImpl libMan = new LibraryManagerImpl(groupDirectory);
            if(setup != null)
                libMan.configure(setup);
            libMan.create();
            allManagers.put(keyDir, libMan);
            return libMan;
        }
        catch (Exception e) {
            String message = e.getMessage();
            if(message == null)
                message = e.toString();
            throw new QizxException("error creating Library group " + groupDirectory
                                    + ": " + message, e);
        }
    }

    private LibraryManager openGroup(File groupDirectory)
        throws QizxException
    {
        Check.nonNull(groupDirectory, "groupDirectory");
        LibraryManagerImpl libMan = null;
        try {
            File rootDir = groupDirectory.getCanonicalFile();
            checkNotAlreadyOpen(rootDir);
            File libDir = XMLLibraryEngineFactory.findLibraryRoot(rootDir);

            if (libDir == null) {
                // not a Library root: normal open
                libMan = new LibraryManagerImpl(rootDir);
            }
            else {
                // Library: rootless group with one lib
                libMan = new LibraryManagerImpl(null);
                libMan.addLibrary(libDir.getName(), libDir);
            }
            if(setup != null)
                libMan.configure(setup);
            if (bootstrapLogHandler != null)
                libMan.addLogHandler(bootstrapLogHandler);

            libMan.open();
            allManagers.put(rootDir, libMan);
            return libMan;
        }
        catch (Exception e) {
            if (libMan != null)
                libMan.closeAllLibraries(0); // to avoid hanging
            String message = e.getMessage();
            if (message == null)
                message = e.toString();
            throw new DataModelException("error opening XML Library group "
                                         + groupDirectory + ": " + message, e);
        }

    }

    // prevent a recurring user issue:
    private void checkNotAlreadyOpen(File storageDir)
        throws DataModelException
    {
        LibraryManagerImpl man = allManagers.get(storageDir);
        if(man != null && !man.isClosed())
            throw new DataModelException(
               "an instance of LibraryManager already exists for this group");
        allManagers.put(storageDir, null);
    }

    private XQuerySessionManager createSessionMan(String baseURI)
        throws QizxException
    {
        try {
            XQuerySessionManager man =
                new XQuerySessionManager(FileUtil.uriToURL(baseURI));
            if (setup != null)
                man.configure(setup);
            return man;
        }
        catch (Exception e) {
            throw new DataModelException(e.getMessage()); // should not happen
        }
    }

    /**
     * Defines a set of default property values, from a property file.
     * @param properties set of properties in String form.
     * @throws Exception
     */
    void doSetup(Properties properties)
        throws IllegalArgumentException
    {
        Enumeration props = properties.keys();
        for (; props.hasMoreElements();) {
            String name = (String) props.nextElement();
            Property prop = findProperty(name);
            if(prop == null)
                continue;   // ignore
            doSet(prop, properties.getProperty(name));
        }    
    }

    private void doSet(Property property, Object value)
        throws IllegalArgumentException
    {
        if (property == null)
            return;
        // special case: restore default value
        if (value == null) {
            if(setup != null)
                setup.remove(property);
            return;
        }
        value = property.checkValue(value);
        if(setup == null)
            setup = new HashMap<Property, Object>();
        if (property == ALLOWED_CLASSES)
        {            
            String classes = (String) setup.get(property);
            if(classes != null)
                value = classes + "," + (String) value;
        }
        setup.put(property, value);
    }

    /**
     * Creates an instance of the factory, which can be used to configure a
     * LibraryManager.
     * @return a unique instance of this factory
     */
    static synchronized Configuration instance()
    {
        if (singleInstance == null) {
            singleInstance = new Configuration();
        }
        return singleInstance;
    }

    static synchronized Addons addons()
    {
        if(addonMan == null)
            addonMan = new Addons(new URL[0], Configuration.class.getClassLoader());
        return addonMan;
    }


    static class Addons extends URLClassLoader
    {
        public boolean trace;

        public Addons(URL[] urls, ClassLoader parent)
        {
            super(urls, parent);
        }

        public Addons(URL[] urls)
        {
            super(urls);
        }

        public void addJar(File jarFile)
        {
            addURL(FileUtil.fileToURL(jarFile));
        }

        // all the jars inside
        public void addDirectory(File lib)
        {
            File[] files = lib.listFiles();
            for(File file : files) {
                if(file.getName().endsWith(".jar"))
                    addJar(file);
                else if(file.isDirectory())
                    addDirectory(file);
            }
        }

        @Override
        protected synchronized Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException
        {
            if(trace) {
                System.err.println("Load Addon class "+ name +" using:");
                for(URL u : getURLs()) {
                    System.err.println("   " + u);
                }
            }
            
            try {
                return super.loadClass(name, resolve); 
            }
            catch (ClassNotFoundException e) {
                if(trace) 
                    e.printStackTrace();
                throw e;
            }
        }

        public Class<?> getClass(String className)
            throws ClassNotFoundException
        {
            return loadClass(className);
        }
        
        /**
         * Instantiates a class name as an object (needs a default constructor).
         * @param className
         */
        public Object instantiate(String className)
            throws InstantiationException
        {
            try {
                return java.beans.Beans.instantiate(this, className);
            }
            catch (IOException e) {
                throw new InstantiationException("cannot instantiate class " + className);
            }
            catch (ClassNotFoundException e) { // should not happen, checked
                throw new InstantiationException("unknown class " + className
                                                 +": "+e);
            }
        }

        public Object instantiate(String className, Class<?> implemented)
            throws InstantiationException
        {
            Object obj = instantiate(className);
            if(!implemented.isAssignableFrom(obj.getClass()))
                throw new InstantiationException(className + 
                                    " is not an implementation of " + implemented);
            return obj;
        }
    }
    
    // ---------- builtin properties -----------------------------------------
    
    /**
     * Logging level applied to all XML Libraries.
     * Default value is "info", accepted values are "error", "warning", "info",
     * and "debug".
     */
    public static final Property LOGGING_LEVEL =
        new Property("logging_level", "Database", "admin",
                     "Logging level applied to all XML Libraries",
                     "INFO");

    /**
     * Maximum memory size used by a LibraryManager (in bytes). Setting this
     * property on a {@link LibraryManager} is similar to use setMemoryLimit().
     */
    public static final Property MEMORY_LIMIT =
        new Property("memory_limit", "Database", "admin",
                     "Maximum memory size used by a LibraryManager",
                     -1L);

    /**
     * Control of file-system sync on each commit for data safety. Caution: this
     * can slow down commits on some file-systems like ext4.
     */
    public static final Property SYNC_COMMIT =
        new Property("sync_commit", "Database", "admin",
                     "File-system sync performed on each commit for data safety ",
                     false);
    /**
     * Cut-off time on shutdown (closeAllLibraries) granted to cleanup and index
     * optimization tasks. Value in seconds
     */
    public static final Property SHUTDOWN_COMPACTER_DELAY =
        new Property("shutdown_compacter_delay", "Database", "expert",
                     "Cut-off time in milliseconds on shutdown (closeAllLibraries)",
                     4000, 1, null);
    /**
     * Size in bytes of the cache of parsed (or transient) XML documents. This is
     * equivalent to calling setTransientDocumentCacheSize on
     * {@link LibraryManager} or {@link XQuerySessionManager}
     */
    public static final Property TRANSIENT_DOCUMENT_CACHE_SIZE =
        new Property("transient_document_cache_size", "XQuery", "admin",
                     "Size in bytes of the cache of parsed/transient XML documents",
                     10000000L, 0, null);
    /**
     * Class used as a default {@link FullTextFactory}.
     */
    public static final Property FULLTEXT_FACTORY =
        new Property("fulltext_factory", "XQuery", "admin",
                     "Class used as a default FullTextFactory",
                     "com.qizx.api.util.fulltext.DefaultFullTextFactory");
    /**
     * List of extension classes for import of semistructured content into an XML
     * Library.
     * <p>
     * The value is a comma-separated list of class names, each class must
     * implement {@link ContentImporter}.
     * <p>
     * Example: "com.qizx.xmodule.importer.json.JsonImporter
     */
    public static final Property CONTENT_IMPORTERS =
        new Property("content_importers", "Extensions", "admin",
                     "List of extension classes that import semistructured content into an XML Library",
                     "");
    /**
     * List of classes allowed for Java function Binding.
     * <p>
     * The value is a comma-separated list of class names.
     * <p>
     * Example: "org.expath.zip.qizx.Functions"
     */
    public static final Property ALLOWED_CLASSES =
        new Property("allowed_classes", "Extensions", "admin",
                     "List of extension classes allowed for Java function Binding",
                     "");

    /**
     * Addition of Data-Model statistics as meta-properties of Documents
     */
    public static final Property DOC_NODE_STATS =
        new Property("doc_node_stats", "Database", "expert",
                     "Addition of Data-Model statistics (number of XML elements, "
                     + "attributes etc) as meta-properties of Documents",
                     false);

    /**
     * Compression for XML document storage.
     * <p>
     * Value is boolean, default value is true.
     */
    public static final Property DOC_COMPRESS =
        new Property("doc_compress", "Database", "expert",
                     "Compression level for XML document storage.",
                     0, 0, 9);
    /**
     * Compression for XML dmocument indexes.
     * <p>
     * Value is boolean, default value is true.
     */
    public static final Property DOC_INDEX_COMPRESS =
        new Property("doc_index_compress", "Database", "expert",
                     "Compression level for XML document indexes.",
                     1, 0, 9);
    /**
     * Maximum size in bytes for document index segments. (segment compaction will
     * not create larger segments than this size)
     * <p>
     * Value is Long, default value is 6 Gigabytes.
     */
    public static final Property DOC_SEGMENT_MAX_SIZE =
        new Property("doc_segment_max_size", "Database", "expert",
                     "Maximum size in bytes for a document index segment",
                     6000000000L, 10000000L, 1000000000000L);
    /**
     * Number of index segments of similar size that triggers a segment compaction
     * (unless this would create a segment larger than specified property
     * DOC_SEGMENT_MAX_SIZE).
     * <p>
     * Value is integer, default value is 8.
     */
    public static final Property DOC_SEGMENT_MAX_COUNT =
        new Property("doc_segment_max_count", "Database", "expert",
                     "Number of index segments that triggers a segment compaction",
                     16, 4, 32);
    /**
     * Size of blocks used for XML document storage.
     */
    public static final Property DOC_BLOCK_SIZE =
        new Property("doc_block_size", "Database", "expert",
                     "Size of blocks used for documents",
                     4096);

    /**
     * Compression for metadata properties.
     * <p>
     * Value is boolean, default value is true.
     */
    public static final Property META_COMPRESS =
        new Property("meta_compress", "Database", "expert",
                     "Compression level for meta-properties storage.",
                     1, 0, 9);
    /**
     * Compression for metadata indexes.
     * <p>
     * Value is boolean, default value is true.
     */
    public static final Property META_INDEX_COMPRESS =
        new Property("meta_index_compress", "Database", "expert",
                     "Compression level for meta-properties indexes.",
                     1, 0, 9);
    /**
     * Maximum size in bytes for metadata index segments (segment compaction will
     * not create larger segments than this size)
     * <p>
     * Value is Long, default value is 0.5 Gigabytes.
     */
    public static final Property META_SEGMENT_MAX_SIZE =
        new Property("meta_segment_max_size", "Database", "expert",
                     "Maximum size in bytes for a meta index segment.",
                     500000000L, 10000000L, 1000000000000L);
    /**
     * Number of index segments of similar size that triggers a segment compaction
     * (unless this would create a segment larger than specified property
     * META_SEGMENT_MAX_SIZE).
     * <p>
     */
    public static final Property META_SEGMENT_MAX_COUNT =
        new Property("meta_segment_max_count", "Database", "expert",
                     "Number of index segments that triggers a segment compaction",
                     10, 4, 32);
    /**
     * Size of blocks used for meta-properties storage.
     */
    public static final Property META_BLOCK_SIZE =
        new Property("meta_block_size", "Database", "expert",
                     "Size of blocks used for meta-properties",
                     1024);

    /**
     * Checking of duplicate document paths inside large transactions
     * (uses more memory).
     * <p>
     * Value is boolean, default value is true.
     */
    public static final Property LARGE_TX_DUPLICATE_CHECK =
        new Property("large_tx_duplicate_check", "Database", "expert",
                     "Check duplicate document paths inside large transactions (uses more memory)",
                     true);
    
    /**
     * Size threshold in index compaction for using a separate thread.
     */
    public static final Property LONG_COMPACTION_SIZE =
        new Property("long_compaction_size", "Database", "expert",
                     "Index compaction size beyond which a separate thread is used",
                     2000000L);
    
    public static final Property XQUERY_IMPLICIT_TIMEZONE =
        new Property("xquery_implicit_timezone", "XQuery", "admin",
                     "implicit timezone for dates in XQuery scripts (defaults to local)",
                     null);
    
    public static final Property XQUERY_DEFAULT_COLLATION =
        new Property("xquery_default_collation", "XQuery", "admin",
                     "default collation used in text comparisons",
                     null);

    public static final Property XQUERY_STRICT_TYPING =
        new Property("xquery_strict_typing", "XQuery", "expert",
                     "enforce stronger type checking (partially implemented)",
                     false);

    public static final Property XQUERY_STRICT_COMPLIANCE =
        new Property("xquery_strict_compliance", "XQuery", "expert",
                     "enforce strict XQuery compliance (remove extensions)",
                     false);
    
    public static Property[] properties = {
        LOGGING_LEVEL,
        TRANSIENT_DOCUMENT_CACHE_SIZE,
        FULLTEXT_FACTORY,
        CONTENT_IMPORTERS,
        ALLOWED_CLASSES, 
        
        XQUERY_IMPLICIT_TIMEZONE, XQUERY_DEFAULT_COLLATION,
        XQUERY_STRICT_TYPING, XQUERY_STRICT_COMPLIANCE,
        
        MEMORY_LIMIT,
        SYNC_COMMIT,
        SHUTDOWN_COMPACTER_DELAY,
        DOC_NODE_STATS,
        LARGE_TX_DUPLICATE_CHECK,
        LONG_COMPACTION_SIZE,
        
        DOC_COMPRESS,
        DOC_INDEX_COMPRESS,
        DOC_SEGMENT_MAX_SIZE,
        DOC_SEGMENT_MAX_COUNT, 
        DOC_BLOCK_SIZE,
        META_COMPRESS,
        META_INDEX_COMPRESS,
        META_SEGMENT_MAX_SIZE,
        META_SEGMENT_MAX_COUNT, 
        META_BLOCK_SIZE
    };
    
    /**
     * Properties of Qizx Configuration.
     */
    public static class Property
        implements Comparable<Property>
    {
        String name;
        String category;
        String level;
        String description;
        Object defaultValue;
        Object minValue;
        Object maxValue;

        public Property(String name, String category, String level,
                        String description, Object defaultValue)
        {
            this(name, category, level, description, defaultValue, null, null);
        }

        public Property(String name, String category, String level,
                        String description,
                        Object defaultValue, Object minValue, Object maxValue)
        {
            this.name = name;
            this.description = description;
            this.category = category;
            this.level = level;
            this.defaultValue = defaultValue;
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        public String getName()
        {
            return name;
        }

        public Class<?> getType()
        {
            return getDefaultValue() != null ? getDefaultValue().getClass() : String.class;
        }

        public String getDescription()
        {
            return description;
        }

        public String getCategory()
        {
            return category;
        }

        public String getLevel()
        {
            return level;
        }

        public Object checkValue(Object value)
            throws IllegalArgumentException
        {
            if (value == null)
                return value;
            Class<?> type = getType();
            try {
                if(Number.class.isAssignableFrom(type))
                    return checkLong(value);
                else if(Boolean.class == type)
                    return booleanValue(value);
            }
            catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Property " + name + ": " + e.getMessage(), e);
            }
            return value;
        }
        
        public long checkLong(Object value)
            throws IllegalArgumentException
        {
            long v = longValue(value);
            if (minValue != null) {
                long min = longValue(minValue);
                if(v < min)
                    throw new IllegalArgumentException("value of property " + name 
                                                       + " is less than minimum " + min);
            }
            if (maxValue != null) {
                long max = longValue(maxValue);
                if(v > max)
                    throw new IllegalArgumentException("value of property " + name 
                                                       + " is greater than maximum " + max);
            }
            return v;
        }
        
        public String stringValue(Object value)
        {
            return (value == null) ? null : value.toString();
        }
        
        public boolean booleanValue(Object value)
        {
            if (value instanceof String)
                return Boolean.parseBoolean((String) value);
            if (value instanceof Boolean)
                return ((Boolean) value).booleanValue();
            throw new IllegalArgumentException("Property " + name +
                              " requires boolean value: gets '" + value + "'");
        }

        public int intValue(Object value)
        {
            return (int) longValue(value);  // dubious
        }
        
        public long longValue(Object value)
        {
            if (value instanceof Long)
                return ((Long) value).longValue();
            if (value instanceof Integer)
                return ((Integer) value).longValue();
            if (value instanceof String)
                return Long.parseLong((String) value);
            if(value == null)
                return 0;
            throw new IllegalArgumentException("Property " + name +
            		" requires integer value: gets '" + value + "'");
        }

        public Object getDefaultValue()
        {
            return defaultValue;
        }

        public int compareTo(Property p)
        {
            return name.compareTo(p.name);
        }
    }
    
    protected static Property findProperty(String uname)
    {
        for(Property p : properties) {
            if(uname.equalsIgnoreCase(p.name))
                return p;
        }
        return null;
    }

    /**
     * Defines a ContentImporter by its class name.
     * @param className full class name.
     * @throws InstantiationException
     */
    public static synchronized void addContentImporter(String className)
        throws InstantiationException
    {
        try {
            // check it is a ContentImporter
            ContentImporter imp =
                (ContentImporter) instantiate(className, ContentImporter.class);
            for (String name : imp.getNames()) {
                importers.put(name.toLowerCase(), className);
            }
        }
        catch (NoClassDefFoundError e) {
            throw new InstantiationException("class not found " + e.getMessage());
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * (Internal) instantiates a ContentImporter by its symbolic name.
     * @param formatName symbolic name, e.g "json".
     */
    public static synchronized ContentImporter instantiateImporter(String formatName)
        throws InstantiationException
    {
        String lname = formatName.toLowerCase();
        String iclass = importers.get(lname);
        if(iclass == null)
            throw new InstantiationException(
                   "no content importer found for format '" + formatName +"'");
        try {
            ContentImporter imp = (ContentImporter) instantiate(iclass);
            return imp;
        }
        catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }    
}
