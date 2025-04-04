/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of the Qizx application components
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.apps.util;

import com.qizx.api.*;
import com.qizx.api.admin.BackgroundTask;
import com.qizx.api.admin.Profiling;
import com.qizx.api.util.XMLSerializer;
import com.qizx.api.util.logging.Statistic;
import com.qizx.api.util.logging.Statistics;
import com.qizx.apps.restapi.RestAPI;
import com.qizx.apps.restapi.RestAPIConnection;
import com.qizx.util.basic.Check;
import com.qizx.util.basic.FileUtil;
import com.qizx.util.basic.Util;
import com.qizx.util.rest.RESTClient.Response;
import com.qizx.util.rest.RESTException;
import com.qizx.xdm.DocumentParser;
import com.qizx.xdm.IQName;
import com.qizx.xquery.ExpressionImpl;

import com.qizx.xquery.ext.AdminFunctions;

import org.xml.sax.InputSource;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Abstraction of access to a Qizx database, either local or remote.
 * <p>Most operations are performed with a library name and a library member path.
 */
public class QizxConnector
{
    private static final long MAX_IMPORT_SIZE = 1024 * 2000;
    private static final int MAX_IMPORT_DOCS = 100;
    private static final QName TYPE = IQName.get("type");
    private static final String[] PROP_NATURE = { "nature" };
    private static final int MAX_QUERY_LENGTH = 50;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private RestAPIConnection clientCx;
    private XQuerySessionManager plainSessionManager;
    private XQuerySession plainSession;

    private long totalImportSize;
    private int importCount;
    private boolean putNonXML;

    private LibraryManager libMan;
    private Library importLibrary;
    private HashMap<String, Library> libMap;
    private LibraryProgressObserver progressObserver;
    private PrintWriter compileTrace;

    /**
     * Connected to a plain local XQuery session.
     */
    public QizxConnector(String moduleBaseURI)
    {
        // use a plain XQuerySession
        URL baseURL = FileUtil.fileToURL(moduleBaseURI == null? "." : moduleBaseURI);
        plainSessionManager = new XQuerySessionManager(baseURL);
    }

    /**
     * Connected to a Qizx server.
     */
    public QizxConnector(RestAPIConnection server)
    {
        this.clientCx = server;
    }


    /**
     * Connected to a local XML Library or Library group.
     */
    public QizxConnector(LibraryManager manager)
    {
        this.libMan = manager;
        libMap = new HashMap<String, Library>();
    }

    /**
     * Shutdown.
     * @throws Exception
     */
    public void close() throws Exception
    {

        if(libMan != null) {
            libMan.closeAllLibraries(0);
            libMan = null;
        }
        clientCx = null;
    }

    public boolean isClosed()
    {
        return 

                libMan == null &&
                clientCx == null ;
    }

    public String getDisplay()
    {
        if(clientCx != null)
            return "Server at " + clientCx.getBaseURL();

        if(libMan == null)
            return "[No Library Group]";

        if(libMan.getGroupDirectory() == null)
            return "XML Library Group"; // rootless
        return "XML Library Group at " + libMan.getGroupDirectory();
    }

    /**
     * Works only in remote mode.
     * @return a Map of properties, or null
     */
    public Map<String, Property> getInfo() throws Exception
    {
        if(clientCx != null)
            return clientCx.info();
        return null;
    }

    public boolean isLocal()
    {
        return clientCx == null;
    }


    public boolean canCreateLocalLibrary()
    {
        return libMan != null && libMan.getGroupDirectory() != null;
    }

    public String serverCommand(String command)
        throws Exception
    {
        if (clientCx == null)
            throw new IllegalStateException("not connected to a server");
        return clientCx.serverControl(command);
    }

    /**
     * Execute on server and return directly the response as a stream.
     * @param format can be only "items", "xml", "xhtml", or "html"
     */
    public InputStream executeRemote(String query, String libraryName,
                                     String format, String encoding)
        throws QizxException, IOException
    {
        if (clientCx != null) {
            clientCx.setLibraryName(libraryName);
            Response resp = clientCx.rawEval(query, format, encoding, -1,
                                             RestAPIConnection.COUNTING_NONE,
                                             null, -1, 0);
            return resp.getStream();
        }
        return null;
    }

 
    /**
     * Execute and return items as Nodes.
     * An item in remote mode is special: only its string value is available.
     * <b>Caution</b>: the ItemSequence returned in remote mode is bounded by 
     * the count, while it local mode it is not.
     * <p>Profiling is performed by setting the 'mode' field in Query.
     */
    public ItemSequence execute(Query query, int startItem, int count)
        throws QizxException, IOException
    {
        ItemSequence seq = null;
        query.compileTime = -1;

        if (clientCx != null) {
            // send query here:
            clientCx.setLibraryName(query.libraryName);
            // profiling is done there:
            seq = clientCx.evalAsItems(query.query, query.mode, count, startItem);
        }
        else {
            XQuerySession session = null;

            if(libMan != null)
                session = getLibrary(query.libraryName, true);
            else 
            {
                if(plainSession == null)
                    plainSession = plainSessionManager.createSession();
                session = plainSession;
            }

            // execute query if not cached locally:
            seq = query.results;
            if(seq == null)
            {
                long ctime = System.nanoTime();
                Expression exp = query.expr =
                    session.compileExpression(query.query);
                ctime = System.nanoTime() - ctime;
                
                if(query.domain != null) {
                    // Any expression that can be passed to fn:collection
                    // Valid both for Qizx/db and Qizx/open
                    Expression ic = 
                      session.compileExpression("fn:collection('" + query.domain + "')");
                    seq = ic.evaluate();
                    exp.bindImplicitCollection(seq);
                }

                ((ExpressionImpl) exp).setProperty(AdminFunctions.CTX_PROP_LIB_MANAGER,
                                                   libMan);

                if(compileTrace != null)
                    ((ExpressionImpl) exp).setCompilationTrace(compileTrace);
                // execute or profile:
                if(query.mode == null)
                    query.results = seq = exp.evaluate();
                else if (RestAPIConnection.PROFILE.equals(query.mode))
                    query.results = seq = exp.profile();
                
                query.compileTime = ctime;
            }
            seq.moveTo(startItem);
        }
        query.results = seq;
        query.totalItemCount = seq.countItems();

        return seq;
    }
    
    public List<Item> executeExpand(Query query, int startItem, int count)
        throws QizxException, IOException
    {
        ItemSequence iter = execute(query, startItem, count);
        ArrayList<Item> pageItems = new ArrayList<Item>();
        for( ; --count >= 0 && iter.moveToNextItem(); ) {
            pageItems.add(iter.getCurrentItem());
        }
        query.profiling = iter.getProfilingAnnotations();
        return pageItems;
    }
    
    /**
     * Enables or disables binding of a Java class in XQuery
     * @param javaClass full name of a class, or null to enable all classes
     */
    public void enableBinding(String javaClass) throws QizxException
    {
        if(plainSession != null)
            plainSession.enableJavaBinding(javaClass);

        else if (libMan != null) {
            for(String libName : libMan.listLibraries())
               getLibrary(libName).enableJavaBinding(javaClass);
        }
    }

    public void configure(Properties config) throws Exception
    {
        if (plainSessionManager != null)
            plainSessionManager.configure(config);

        else if (libMan != null)
            libMan.configure(config);
        }

    public void setModuleResolver(ModuleResolver resolver)
    {
        if (plainSessionManager != null)
            plainSessionManager.setModuleResolver(resolver);  

        else if (libMan != null)
            libMan.setModuleResolver(resolver);
    }
   
    /**
     * Query with results.
     * In local mode, the result ItemSequence is cached and reused.
     * 
     */
    public static class Query
    {
        private String query;
        private String libraryName;
        private String domain;
        private String mode; // null (normal), "profile", "debug" (later)
        
        public List<Profiling> profiling;
        
        // local mode:
        private Expression expr;
        private ItemSequence results;
        private long totalItemCount;
        public long compileTime;

        /**
         * @param mode used for profiling (and later: debugging)
         */
        public Query(String query, String libraryName, String mode)
        {
            this.query = query;
            this.libraryName = libraryName;
            this.mode = mode;
        }

        public String getQuery() {
            return query;
        }

        public long getTotalItemCount() {
            return totalItemCount;
        }

        public String getQueryDomain()
        {
            return domain;
        }

        public void setQueryDomain(String queryDomain)
        {
            this.domain = queryDomain;
        }

        public long getCompileTime()
        {
            return compileTime;
        }

        public void setCompileTime(long compileTime)
        {
            this.compileTime = compileTime;
        }

        public void cancel()
        {
            if(expr != null) {
                expr.cancelEvaluation();
            }
        }

        public boolean hasProfiling()
        {
            return profiling != null;
        }
    }


    public String[] listLibraries()
        throws Exception
    {
        String[] libs = null;
        if(clientCx != null)
            libs = clientCx.listLibraries();

        else if(libMan != null)
            libs = libMan.listLibraries();
        return libs;
    }
    

    public Library getLibrary(String libraryName)
        throws QizxException
    {
        return getLibrary(libraryName, false);
    }
    
    public Library getLibrary(String libraryName, boolean required)
        throws QizxException
    {
        if(libMan == null)
            return null;
        Library lib = libMap.get(libraryName);
        if(lib == null) {
            lib = libMan.openLibrary(libraryName);
            if(lib != null) {
                libMap.put(libraryName, lib);
                // everything allowed in local mode anyway:
                XQuerySessionManager.bind(lib, "admin", AdminFunctions.class);
            }
            else if(required)
                throw new QizxException("no such Library '" + libraryName + "'");
        }
        return lib;
    }

    public PrintWriter getCompileTrace()
    {
        return compileTrace;
    }

    public void setCompileTrace(PrintWriter compileTrace)
    {
        this.compileTrace = compileTrace;
    }

    
    public void createLibrary(String libraryName) throws Exception
    {
        if (clientCx != null) {
            clientCx.createLibrary(libraryName);
        }

        else if(libMan != null)
            libMan.createLibrary(libraryName, null);
    }

    public void deleteLibrary(String libraryName)
        throws Exception
    {
        if (clientCx != null) {
            clientCx.deleteLibrary(libraryName);
        }

        else if (libMan != null) {
            libMan.deleteLibrary(libraryName);
        }
    }

    
    private static class Member
    {
        String path;
        boolean isDoc;
        boolean isNonXML;

        Member(String path, boolean isDoc) {
            this(path, isDoc, false);
        }

        public Member(String path, boolean isDoc, boolean isNonXML)
        {
            this.path = path;
            this.isDoc = isDoc;
            this.isNonXML = isNonXML;
        }
    }
    
    private Comparator<Member> memberComparator = new Comparator<Member>() {
        public int compare(Member o1, Member o2)
        {
            if(o1.isDoc != o2.isDoc)
                return o1.isDoc? 1 : -1;
            return o1.path.compareTo(o2.path);
        }
    };

    public void commit(String libraryName) throws Exception
    {

        if(libMan != null) {
            Library lib = getLibrary(libraryName, true);
            lib.setProgressObserver(progressObserver);
            lib.commit();
        }
        else
            flushImport();
    }
    
    public void rollback(String libraryName) throws Exception
    {

        if(libMan != null) {
            Library lib = getLibrary(libraryName, true);
            lib.rollback();
        }
        else {
            // cant do nuttin: the current put is simply discarded
            // but it's only a partial rollback
        }
    }

    public void refresh(String libraryName) throws Exception
    {

        if(libMan != null && libraryName != null) {
            Library lib = getLibrary(libraryName, true);
            lib.refresh();
        }
    }

    public Node getXMLContents(String libraryName, String docPath)
        throws Exception
    {
        if (clientCx != null) {
            clientCx.setLibraryName(libraryName);
            Response resp = clientCx.get(docPath);
            return resp.getNode();
        }

        else if (libMan != null) {
            Library lib = getLibrary(libraryName, true);
            Document doc = lib.getDocument(docPath);
            return doc == null? null : doc.getDocumentNode();
        }
        return null;
    }
    
    public InputStream getNonXMLContents(String libraryName, String docPath)
        throws Exception
    {
        if (clientCx != null) {
            clientCx.setLibraryName(libraryName);
            Response resp = clientCx.get(docPath);
            return resp.getStream();
        }

        else if (libMan != null) {
            Library lib = getLibrary(libraryName, true);
            NonXMLDocument doc = lib.getNonXMLDocument(docPath);
            return doc == null ? null : doc.open();
        }
        return null;
    }

    public LibraryMemberIterator getChildren(String libraryName,
                                             String memberPath, int limit)
        throws Exception
    {
        if (clientCx != null) {
            clientCx.setLibraryName(libraryName);
            return clientCx.getChildren(memberPath, limit);
        }

        else if (libMan != null) {
            Library lib = getLibrary(libraryName, true);
            Collection collection = lib.getCollection(memberPath);
            return collection == null? null : collection.getChildren();
        }
        return null;
    }

    /**
     * Returns "collection", "document", "non-xml", or null
     */
    public String getMemberNature(String libraryName, String path)
        throws Exception
    {
        Map<String, Property> props = getMemberProperties(libraryName, path,
                                                          PROP_NATURE);
        if(props != null) {
            Property p = props.get("nature");
            if (p != null)
                return p.value;
        }
        return null;
    }
    
    public Map<String, Property> getMemberProperties(String libraryName,
                                                     String path, String[] propNames)
        throws Exception
    {
        if (clientCx != null) {
            clientCx.setLibraryName(libraryName);
            return clientCx.getMemberProperties(path, propNames);
        }

        else if (libMan != null) {
            Library lib = getLibrary(libraryName, true);
            LibraryMember member = lib.getMember(path);
            if(member == null)
                return null;
            String[] pnames = (propNames != null)?
                                  propNames : member.getPropertyNames();
            Map<String, Property> props = new HashMap<String, Property>();
            for (int i = 0; i < pnames.length; i++) {
                Property prop = new Property(pnames[i],
                                             member.getProperty(pnames[i]));
                props.put(pnames[i], prop);
            }
            return props;
        }
        return null;
    }

    /**
     * Stores a property of a Library member
     */
    public void setMemberProperty(String libraryName, String path,
                                  Property property)
        throws Exception
    {
        Check.nonNull(path, "path");
        Check.nonNull(property, "property");
        
        if (clientCx != null) {
            clientCx.setLibraryName(libraryName);
            // TODO? serialize actual node to String
            // pseudo-types node() and "<expression>" processed in server
            clientCx.startSetProp(path, property.name, property.type, property.value);
            clientCx.finishProperties();
        }

        else if (libMan != null) {
            Library lib = getLibrary(libraryName, true);
            LibraryMember member = lib.getMember(path);
            
            // special treatment of node() "expression"  pseudo-types
            if(Property.EXPRESSION.equals(property.type)) {
                Expression exp = lib.compileExpression(property.value);
                ItemSequence res = exp.evaluate();
                if(res.moveToNextItem()) {
                    property.itemValue = res.getCurrentItem();
                } // ignore remaining items...
            }
            else if(Property.NODE.equals(property.type)) {
                property.nodeValue = DocumentParser.parse(
                             new InputSource(new StringReader(property.value)));
            }
            member.setProperty(property.name, property.toObject());
            lib.commit();
        }
    }

    
    public String copyMember(String libraryName, String path, String newPath)
        throws Exception
    {
        if (clientCx != null) {
            clientCx.setLibraryName(libraryName);
            return clientCx.copy(path, newPath);
        }

        else if(libMan != null) {
            Library lib = getLibrary(libraryName, true);
            LibraryMember member = lib.copyMember(path, newPath);
            lib.commit();
            return member.getPath();
        }
        return null;
    }
    
    public String renameMember(String libraryName, String path, String newPath)
        throws Exception
    {
        if (clientCx != null) {
            clientCx.setLibraryName(libraryName);
            return clientCx.move(path, newPath);
        }

        else if(libMan != null) {
            Library lib = getLibrary(libraryName, true);
            LibraryMember member = lib.renameMember(path, newPath);
            lib.commit();
            return member.getPath();
        }
        return null;
    }

    public void createCollection(String libraryName, String path)
        throws Exception
    {
        if (clientCx != null) {
            clientCx.setLibraryName(libraryName);
            clientCx.createCollection(path, true);
        }

        else if (libMan != null) {
            Library lib = getLibrary(libraryName, true);
            lib.createCollection(path);
            lib.commit();
        }
    }
    
    public boolean deleteMember(String libraryName, String path)
        throws Exception
    {
        if (clientCx != null) {
            clientCx.setLibraryName(libraryName);
            return clientCx.delete(path);
        }

        else if (libMan != null) {
            Library lib = getLibrary(libraryName, true);
            boolean ok = lib.deleteMember(path);
            lib.commit();
            return ok;
        }
        return false;
    }
    
    
    public void importStart(String libraryName)
        throws Exception
    {
        putNonXML = false;
        if (clientCx != null) {
            clientCx.startPut();
        }

        else {
            importLibrary = getLibrary(libraryName, true);
            importLibrary.setProgressObserver(progressObserver);
        }
    }
    
    /**
     * Import a document from a file.
     * @param docPath path in the Library
     * @param file source file
     * @throws LibraryException messagewith a list of documents that generated an error
     * each on a line, with
     */
    public void importDocument(String docPath, File file)
        throws Exception
    {
        if(clientCx != null) {
            serverPut(docPath, file, null);
        }

        else if (importLibrary != null) {
            Document doc = importLibrary.importDocument(docPath, file);
            doc.setProperty("source", file.getCanonicalPath());
        }
    }

    /**
         * Import a document from data.
         * @param docPath path in the Library
         * @param data XML data as String
         * @param sourceURL optional URL, ignored in remote mode
         */
        public void importDocument(String docPath, String data, String sourceURL)
            throws Exception
        {
            if(clientCx != null) {
                long fileSize = data.length();
                if (totalImportSize + fileSize >= MAX_IMPORT_SIZE
                         || importCount >= MAX_IMPORT_DOCS)
                    flushImport();
                clientCx.putDocument(docPath, data);
                totalImportSize += fileSize;
                ++importCount;
            }

            else if (importLibrary != null) {
                Document doc = importLibrary.importDocument(docPath, data);
                if (sourceURL != null)
                    doc.setProperty("source", sourceURL);
            }
        }

    public void importNonXMLStart(String libraryName)
            throws Exception
        {
            putNonXML = true;
            if (clientCx != null) {
                clientCx.startPutNonXML();
            }

            else {
                importLibrary = getLibrary(libraryName, true);
                importLibrary.setProgressObserver(progressObserver);
            }
        }

    /**
     * Import a non-XML document from a file.
     * @param docPath path in the Library
     * @param file source file
     * @param contentType mime type such as "image/jpeg". Can be null, but it is
     * strongly recommended to provide a value.
     * @throws LibraryException messagewith a list of documents that generated an error
     * each on a line, with
     */
    public void importNonXMLDocument(String docPath, File file, String contentType)
        throws Exception
    {
        if(clientCx != null) {
            serverPut(docPath, file, contentType);   // same as XML
        }

        else if (importLibrary != null) {
            FileInputStream input = new FileInputStream(file);
            NonXMLDocument doc = importLibrary.importNonXMLDocument(docPath, true, input);
            input.close();
            doc.setProperty("source", file.getCanonicalPath());
            doc.setProperty("content-type", contentType);
        }
    }

    public void flushImport() throws Exception
    {
        if(clientCx != null && importCount > 0) {
            try {
                String[] errors = clientCx.finishPut();
                if(errors[0].startsWith("IMPORT ERRORS 0"))
                    return;
                // TODO trim last one
                throw new ImportException(errors);
            }
            finally {
                if(putNonXML)
                    clientCx.startPutNonXML();
                else
                    clientCx.startPut();
                totalImportSize = 0;
                importCount = 0;
            }
        }
    }
    
    private void serverPut(String docPath, File file, String contentType)
        throws IOException, Exception
    {
        long fileSize = file.length();
        clientCx.putDocument(docPath, file, contentType);
        totalImportSize += fileSize;
        ++importCount;
        
        if (totalImportSize + fileSize >= MAX_IMPORT_SIZE
            || importCount >= MAX_IMPORT_DOCS)
            flushImport();
    }

    public static class ImportException extends QizxException
    {
        private String[] errors;

        public ImportException(String[] errors)
        {
            super("Parsing");
            this.errors = errors;
        }

        public String[] getErrors()
        {
            return errors;
        }
    }


    public void optimize(String libraryName, LibraryProgressObserver progress)
        throws Exception
    {
        if(clientCx != null) {
            clientCx.setLibraryName(libraryName);
            String pid = clientCx.optimize();
            displayProgress(pid, progress, 'O');
        }
        else if(libMan != null) {
            Library library = getLibrary(libraryName, true);
            if(progress != null)
                library.setProgressObserver(progress);
            library.optimize();
            library.refresh();  // long standing issue
        }
    }

    public void quickOptimize(String libraryName, int timeHint, boolean blocking,
                              LibraryProgressObserver progress)
        throws Exception
    {
        if (clientCx != null) {
            clientCx.setLibraryName(libraryName);
            String pid = clientCx.quickOptimize(timeHint, blocking);
            displayProgress(pid, progress, 'O');
        }
        else if (libMan != null) {
            Library library = getLibrary(libraryName, true);
            if (progress != null)
                library.setProgressObserver(progress);
            library.quickOptimize(timeHint, blocking);
            library.refresh(); // long standing issue
        }
    }

    public void reindex(String libraryName, LibraryProgressObserver progress)
        throws Exception
    {
        if (clientCx != null) {
            clientCx.setLibraryName(libraryName);
            String pid = clientCx.reindex();
            displayProgress(pid, progress, 'R');
        }
        else if (libMan != null) {
            Library library = getLibrary(libraryName, true);
            if (progress != null)
                library.setProgressObserver(progress);
            library.reIndex();
            library.refresh(); // long standing issue
        }
    }

    public void backup(String libraryName, File backupDir,
                       LibraryProgressObserver progress)
        throws Exception
    {
        if (clientCx != null) {
            clientCx.setLibraryName(libraryName);
            String pid = clientCx.backup(backupDir.getAbsolutePath());
            displayProgress(pid, progress, 'B');
        }
        else if (libMan != null) {
            Library library = getLibrary(libraryName, true);
            if (progress != null)
                library.setProgressObserver(progress);
            library.backup(backupDir);
        }
    }

    public void incrementalBackup(String libraryName, File backupDir,
                                  LibraryProgressObserver progress)
        throws Exception
    {
        if(clientCx != null) {
            clientCx.setLibraryName(libraryName);
            String pid = clientCx.incrementalBackup(backupDir.getAbsolutePath());
            displayProgress(pid, progress, 'B');            
        }
        else if (libMan != null) {
            Library library = getLibrary(libraryName, true);
            if(progress != null)
                library.setProgressObserver(progress);
            library.incrementalBackup(backupDir);
        }
    }

    public Indexing getIndexing(String libraryName)
        throws Exception
    {
        if (clientCx != null) {
            clientCx.setLibraryName(libraryName);
            return clientCx.getIndexing();
        }
        else if (libMan != null) {
            Library library = getLibrary(libraryName, true);
            return library.getIndexing();
        }
        return null;
    }

    public void setIndexing(String libraryName, Indexing indexing)
        throws Exception
    {
        if (clientCx != null) {
            clientCx.setLibraryName(libraryName);
            StringWriter sw = new StringWriter();
            XMLSerializer sout = new XMLSerializer(sw);
            indexing.export(sout);
            sout.flush();
            clientCx.setIndexing(sw.toString());
        }
        else if (libMan != null) {
            Library library = getLibrary(libraryName, true);
            library.setIndexing(indexing);
        }
    }

    private void displayProgress(String pid, LibraryProgressObserver observer,
                                 char mode)
        throws RESTException
    {
        double progress = 0;
        for (; progress != 1;) {

            if (observer != null) {
                switch (mode) {
                case 'B':
                    observer.backupProgress(progress);
                    break;
                case 'O':
                    observer.optimizationProgress(progress);
                    break;
                case 'R':
                    observer.reindexingProgress(progress);
                    break;
                }
            }
            Util.sleep(1000);
            progress = clientCx.getProgress(pid);
        }
    }

    public void setProgressObserver(LibraryProgressObserver observer)
    {
        progressObserver = observer;
    }
    
    public String getACL(String libraryName, String path, boolean inherited)
        throws Exception
    {
        if (clientCx != null) {
            clientCx.setLibraryName(libraryName);
            return clientCx.getAcl(path, inherited);
        }
        return null;
    }

    public void setACL(String libraryName, String acls)
        throws Exception
    {
        if (clientCx != null) {
            clientCx.setLibraryName(libraryName);
            clientCx.setAcl(acls);
        }
    }

    public TabularData getStatistics(String mapping)
        throws Exception
    {
        if (clientCx != null) {
            return clientCx.getStats(mapping);
        }

        else if (libMan != null) {
            Statistic.Map stats = new Statistic.Map();
            if ("expert".equalsIgnoreCase(mapping))
                stats.setMapping(Statistics.SHORT_MAPPING);
            else if (!"full".equalsIgnoreCase(mapping))
                stats.setMapping(Statistics.ADMIN_MAPPING);
            libMan.collectStatistics(stats);
            TabularData data = new TabularData("Statistics", RestAPI.STATS_FIELDS);
            int row = 0;
            for (Statistic s : stats.values()) {
                int col = 0;
                data.setValueAt(s.getId(), row, col++);
                String type = s.getType();
                data.setValueAt(type, row, col++);
                data.setValueAt(Statistics.decorate(s.getValue(), type), row, col++);
                data.setValueAt(s.getFamily(), row, col++);
                data.setValueAt(s.getDescription(), row, col++);
                ++row;
            }
            return data;
        }
        return null;
    }

    public TabularData getConfiguration(boolean expert)
        throws Exception
    {
        if (clientCx != null) {
            return clientCx.getConfiguration(expert);
        }

        else if (libMan != null)
        {
            Map<Configuration.Property, Object> configuration =
                libMan.getConfiguration();
            TabularData data = new TabularData("Configuration", RestAPI.CONFIGURATION_FIELDS);
            int row = 0;
            for(Map.Entry<Configuration.Property,Object> e : configuration.entrySet())
            {
                Configuration.Property p = e.getKey();
                Class<?> type = p.getType();
                if(!expert && "expert".equals(p.getLevel()))
                    continue;
                data.setValueAt(p.getName(), row, 0);
                data.setValueAt(p.getCategory(), row, 1);
                data.setValueAt(p.getLevel(), row, 2);
                data.setValueAt(type.getSimpleName(), row, 3);
                data.setValueAt(e.getValue(), row, 4);
                data.setValueAt(p.getDefaultValue(), row, 5);
                data.setValueAt(p.getDescription(), row, 6);
                ++ row;
            }
            return data ;
        }
        return null;
    }
    
    /**
     * Updates and changes the configuration.
     * @param properties
     * @return true if actual change happened.
     * @throws Exception
     */
    public boolean changeConfiguration(Properties properties)
        throws Exception
    {
        if (clientCx != null) {
            return clientCx.changeConfiguration(properties);
        }

        else if (libMan != null)
        {
            if (!libMan.configure(properties))
                return false;
            libMan.saveConfiguration();
            return true;
        }
        return false;  
    }

    public TabularData listRunningQueries()
        throws Exception
    {
        if (clientCx != null) {
            return clientCx.listRunningQueries();
        }

        else if (libMan != null) {
            TabularData data = new TabularData("Running Queries",
                                               RestAPI.RUNNING_QUERIES_FIELDS);
            long now = System.currentTimeMillis();

            int row = 0;
            for (Library lib : libMan.listSessions()) {
                User u = lib.getUser();
                String userName = (u == null) ? "?" : u.getName();

                List<Expression> queries = lib.listExpressions();
                for (Expression query : queries)
                {
                    long time = query.getStartTime();
                    if (time <= 0)
                        continue;
                    String src = query.getSource();
                    if (src.length() > MAX_QUERY_LENGTH)
                        src = src.substring(0, MAX_QUERY_LENGTH) + "...";
                    src = src.replace('\n', ' ').replace('\t', ' ');
                    
                    data.setValueAt(query.getIdentifier(), row, 0);
                    data.setValueAt(userName, row, 1);
                    data.setValueAt((now - time) / 1000000L / 1000.0, row, 2);
                    data.setValueAt(src, row, 3);
                }
            }
            return data;
        }
        return null;
    }

    public String cancelQuery(String id)
        throws Exception
    {
        if (clientCx != null) {
            return clientCx.cancelRunningQuery(id);
        }

        else if (libMan != null) {
            Expression exp = findExpression(libMan, id);
            if (exp == null)
                return "unknown";
            else {
                boolean running = exp.getStartTime() > 0;
                exp.cancelEvaluation();
                return (running ? "OK" : "idle");
            }
        }
        return null;
    }


    private Expression findExpression(LibraryManager engine, String xid)
        throws IOException
    {
        List<Library> sessions = engine.listSessions();
        for (Library s : sessions) {
            List<Expression> expressions = s.listExpressions();
            for (Expression e : expressions)
                if (e.getIdentifier().equals(xid))
                    return e;
        }
        return null;
    }

    public TabularData listMaintenanceTasks(int timeline)
        throws Exception
    {
        if (clientCx != null) {
            return clientCx.listMaintenanceTasks(timeline);
        }

        else if (libMan != null) {
            TabularData data = new TabularData("Background Tasks", 
                                               RestAPI.TASKS_FIELDS);
            int row = 0;
            for (BackgroundTask t : libMan.listBackgroundTasks(timeline)) {
                int percent = (int) (t.getDone() * 1000 + 0.5);
                data.setValueAt(t.getTaskName(), row, 0);
                data.setValueAt(t.getLibraryName(), row, 1);
                long startTime = t.getStartTime();
                data.setValueAt(dateFormat.format(startTime), row, 2);
                long endTime = t.getEndTime();
                String fend = (endTime == 0)? "-" : dateFormat.format(endTime);
                data.setValueAt(fend, row, 3);
                long dur = (endTime == 0)? 0 : (endTime - startTime);
                data.setValueAt(dur / 1000.0, row, 4);
                data.setValueAt(percent / 10.0 + "%", row, 5);
                row ++;
            }
            return data;
        }
        return null;
    }
}
