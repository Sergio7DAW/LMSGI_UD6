/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of the Qizx application components
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.apps;

import com.qizx.api.*;
import com.qizx.api.util.DefaultModuleResolver;
import com.qizx.api.util.XMLSerializer;
import com.qizx.api.util.logging.Log;
import com.qizx.api.util.logging.StreamLog;
import com.qizx.apps.restapi.RestAPIConnection;
import com.qizx.apps.util.ConsoleLogger;
import com.qizx.apps.util.Property;
import com.qizx.apps.util.QizxConnector;
import com.qizx.apps.util.TabularData;
import com.qizx.expath.pkg.QizxRepoResolver;
import com.qizx.expath.pkg.QizxRepository;
import com.qizx.util.basic.CLOptions;
import com.qizx.util.basic.CLOptions.Error;
import com.qizx.util.basic.FileUtil;
import com.qizx.util.basic.PathUtil;
import com.qizx.util.basic.Util;
import com.qizx.xquery.ExpressionImpl;
import com.qizx.xquery.XMLExprDisplay;
import com.qizx.xquery.impl.Lexer;
import com.qizx.xquery.impl.NewLexer;

import org.expath.pkg.repo.FileSystemStorage;
import org.expath.pkg.repo.PackageException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.*;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 * A simple command line interface application that executes XQuery scripts
 * from files.
 * <p>
 */
public class QizxCLI
{
    private static final int PUT_MAX_SIZE = 20 * 1024 * 1024;    // 20 Mb

    static String APP_NAME = Product.PRODUCT_NAME;
    static final String MISSING_LIBRARY = "Library should be specified: use option -l";

    // ---------------- command-line options ---------------------------------
    
    static CLOptions options = new CLOptions(APP_NAME);

    private static QizxCLI app;
    
    static {
        final String PLAIN_FILE_HELP = "file containing a query to execute.\n";
        options.define(null, "<xquery file>", "!fileAction",
                       PLAIN_FILE_HELP);
        options.define("-q_", "<xquery file>", "!queryFileAction",
                       PLAIN_FILE_HELP + "If file is '-', read from the standard input.\n");
        options.define("-.", "", "!fileAction", null);
        
        options.defineSection("Server options:");

        options.define("-login_", "<username[:password]>", "!setLoginAction",
            "define the login name. If authentication is required and\n" +
            "the password is not present, it will be read on the console.\n" +
            "See also -auth");
        options.define("-auth_", "<file>", "!secretAction",
                       "define the login credentials in a file for security.\n" +
                       "If authentication is required, credentials will be read from this file.\n" +
                       "This avoids exposing the password in the command line (with -login).");


        options.define("-group_", "<directory>", "=groupOption",
                       "select a group of XML Libraries specified by its root directory");
        options.define("-g_", "<directory>", "=groupOption",
                       "short form of -group");
        
        options.defineSection("XML Library options:");
        options.define("-library_", "<name>", "=libraryOption",
                       "select this XML Library");
        options.define("-l_", "<name>", "=libraryOption",
                       "short form of -library");
        
        options.define("-import_", "<collection_path> <files...>", "!importPointAction",
            "\nimport XML documents into the specified Collection.\n"+
            "directories are also accepted, and scanned for XML documents.\n"+
            "'collection_path' can be a pattern for generating document paths.");
        
        options.define("-include_", "<suffix>", "+includeSuffixes",
                       "import XML documents whose filename ends with this suffix");
        options.define("-exclude_", "<suffix>", "+excludeSuffixes",
                       "do not import XML documents whose filename ends with this suffix");

        options.define("-export_", "<member_path>", "+membersToExport",
                       "export the specified Collection or Document");

        options.define("-delete_", "<member_path>", "+membersToDelete",
                       "delete the specified Collection or Document");
 
        options.defineSection("Administration:");
        options.define("-create.", "", "=createAction",
                       "create the specified group or library if necessary");
    
        options.define("-delete-library.", "", "=libraryDelete",
                       "delete the selected XML Library");

        options.define("-set-acl_", "", "=aclData",
                       "define Access Control List from a file");
        
        options.define("-backup_", "<path>", "=backupLocation",
                       "backup the current Library to location specified.");

        options.define("-ibackup_", "<path>", "=incrBackupLocation",
            "incrementally backup the current Library to location specified.");

        options.define("-indexing_", "<path>", "=xsheetPath",
                       "upload an Indexing Specification read from a file");

        options.define("-reindex.", "", "=rebuildIndexes",
                       "rebuild the indexes of the current Library");

        options.define("-optimize.", "", "=optimize",
                       "optimize the storage and the indexes of the current Library");

        options.define("-check_", "<log_file>", "=checkLogFile",
                       "perform a structure check on all Libraries and log to file");
        options.define("-deep.", "", "=deepCheck",
                        "perform a deep check (in combination with -check)");
        options.define("-nost.", "", "=nostorageCheck", null);

        options.define("-server_", "<command>", "=serverCommand",
                       "control the access to XML Libraries in a server.\n" +
                       "Command can be 'status', 'start', 'stop', 'restart'.");


        // --------- XQuery: -------
        options.defineSection("XQuery settings (ignored in client mode):");
        
        options.define("-i_", "<collection>", "=implicitDomain",
            "define implicit root for Path expressions: a path expression\n" +
            "without explicit root, like \"//elem\", takes this value as root.\n" +
            "This allows writing queries independent of the input data.\n" +
            "The value is the same as the argument of function fn:collection():\n" +

            "- the path of a Document or a Collection inside an XML Library\n" +
            "- a file path or an URL: for example \"dir1/doc1.xml\"\n" +
            "- a file pattern: \"dir/*.xml\"\n" +
            "- a semicolon-separated list of the above elements: \"dir1/*.xml;dir2/doc2.xsl\"\n"
            );

        options.define("-domain_", "<collection>", "=implicitDomain",
            "alias for option -i (implicit path root)");
        
        options.define("-D:", "variable_name=value", "+globals",
                       "initialize a XQuery global variable.");

        options.define("--*", "...", "+appArgs",
            "put all following arguments into XQuery variable '$arguments'");
        
        options.define("-base-uri_", "<path>", "+baseURI",
            "define the base URI for locating parsed XML documents.");

        options.define("-module-base-uri_", "<path>", "+moduleBaseURI",
            "define base URI for XQuery modules");
        options.define("-config_", "<property_file>", "+configFile",
                       "read file containing configuration properties");
        options.define("-xrepo_", "<repository_path>", "+expathRepos",
            "use an EXPath repository for XQuery modules");
        
        options.define("-timezone_", "<time zone>", "+timezone",
                       "implicit timezone in duration format");
        options.define("-collation_", "<collation>", "+collation",
                       "name of default collation");
        options.define("-doc-cache_", "<size_in_Kb>", "=docCache",
            "define the parsed document cache size (default 8 Mb)");
        
        // --------- Output: -------
        options.defineSection("Output options:");
        
        options.define("-out_", "<output file>", "+outputFile",
            "output file (defaults to standard output)");
        options.define("-X:", "option=value", "+xmlOptions",
                       "set a XML serialization option.\n" +
                       "For example -Xmethod=html -Xencoding=ISO-8859-1");
        options.define("-wrap.", "", "=wrapDisplay",
                       "wrap query results in Item description tags");


        options.define("-version.", "", "=printVersion", "display version");
        options.define("-v.", "", "=verbose", "verbose mode");
        options.define("-jt.", "", "=traceJava", 
            "trace Java extension functions");
        options.define("-tex.", "", "=traceExceptions",
            "display trace of exceptions");
        
        options.define("-help@", "", "?", "print this help");
        
        // undocumented options:
        options.define("-r_", "count", "=repeats", null);
        options.define("-mr_", "count", "=muteRepeats", null);

        options.define("-dq.", "", "=traceQ", null);
        options.define("-dx.", "", "=traceExpr", null);
        options.define("-dlex.", "", "=traceLex", null);
    }

    static public void main(String args[])
    {
        app = new QizxCLI();
        try {
            options.parse(args, app);
            
            // changed in 4.0: options are unordered (except filenames)
            app.run();
        }
        catch (CLOptions.Error e) {
            System.exit(2);
        }
        catch (Throwable e) {
            if (app.traceExceptions)
                e.printStackTrace();
            else
                System.err.println("*** " + e.getClass().getSimpleName() +
                                   ": " + e.getMessage());
            try {
                if(app.connection != null)
                    app.connection.close();
            }
            catch (Exception e1) {
                System.err.println("emergency close fails: " + e1);
            }
            System.exit(1);
        }
        
        // if any error: exit code
        if(app.errorCount > 0)
            System.exit(1);
    }

    // -------- public variables for cl options: -----------------------------
 
    // actions with parameters:
    public String importPoint;
    
    public String[] membersToExport = new String[0];
    public String[] membersToDelete = new String[0];
    public String backupLocation;
    public String incrBackupLocation;
    public String checkLogFile;
    public String serverCommand;
    public boolean libraryDelete;
    public boolean createAction;
    public boolean rebuildIndexes;
    public boolean optimize;
    public boolean printVersion;
    public boolean deepCheck;
    public boolean nostorageCheck;
    
    // simple settings:
    public String groupOption;
    public String libraryOption;
    public String login;
    public String baseURI;
    public String moduleBaseURI;
    public String expathRepos;
    public String implicitDomain;
    public String xsheetPath;
    public String aclData;
    public String[] includeSuffixes;
    public String[] excludeSuffixes;

    public String[] queryFiles = new String[0];
    public String[] importFiles = new String[0];
    public String[] globals = new String[0];
    public String[] appArgs = new String[0];
    public String timezone;
    public String collation;
    public String[] xmlOptions = new String[0];
    public String outputFile = null;
    public String configFile;
    public String checkOptions;

    public int    docCache;    
    public int    repeats = 1;  
    public int    muteRepeats;
    
    public boolean wrapDisplay;
    public boolean verbose;
    public boolean traceJava;
    public boolean traceExceptions;
    public boolean traceQ;
    public boolean traceExpr;
    public boolean traceLex;

    private boolean afterImport;
    
    // -------- private variables: -------------------------------------------
    
    // wraps local/remote connections:
    private QizxConnector connection;
    

    private LibraryManager libMan;
    private XQuerySessionManager sessionMan;
    private XQuerySession session;
    
    private XMLSerializer resultOutput;
    private PrintWriter stderr = new PrintWriter(System.err, true);
    private String currentFile;
    private int errorCount = 0;
    
    private Properties secrets;
    private String password;
 
    public QizxCLI()
    {
        resultOutput = new XMLSerializer();
        resultOutput.setIndent(2);
        

        ConsoleLogger handler = new ConsoleLogger();
        handler.setLevel(Level.WARNING);
        Configuration.setLogHandler(handler);
    }

    public void secretAction(String path) throws Exception
    {
        secrets = new Properties();
        try {
            FileInputStream input = new FileInputStream(path);
            secrets.load(input);
            input.close();
            login = secrets.getProperty("login");
            password = secrets.getProperty("password");
            if(login == null || password == null)
                fatal("secrets file should define properties 'login' and 'password'");
        }
        catch (IOException e) {
            fatal("cannot read secret file: "+e.getMessage());
        }
    }

    public void setLoginAction(String cred) throws Exception
    {
        int colon = cred.indexOf(':');
        if(colon > 0) {
            password = cred.substring(colon + 1);
            login = cred.substring(0, colon);
        }
        else {
            login = cred;
        }       
    }

    public void queryFileAction(String path)
        throws Exception
    {
        options.addToField(this, "", "queryFiles", path);
        afterImport = false;
    }
    
    public void fileAction(Boolean dummy) // stdin
        throws Exception
    {
        options.addToField(this, "", "queryFiles", "-");
    }

    // self-standing command-line argument: import path / query file
    public void fileAction(String path)
        throws Exception
    {
        if(afterImport)
            options.addToField(this, "", "importFiles", path); 
        else
            options.addToField(this, "", "queryFiles", path);   
    }

    public void importPointAction(String path) throws Error
    {
        if(importPoint != null)
            throw new CLOptions.Error("option -import can be used only once\n");
        importPoint = path;
        afterImport = true;
    }
    
    private static void fatal(String message)
    {
        System.err.println("*** " + message);
        System.exit(2);
    }

    private void error(String message, Exception e)
    {
        stderr.println("*** " + message);
        if (traceExceptions && e != null)
            e.printStackTrace();
        ++ errorCount;
    }

    private void warning(String message)
    {
        stderr.println("* warning: " + message);
    }
   
    // -------- implementation -----------------------------------------------
    
    private void run() throws Exception
    {       
        // check there is an actual action:
        if (!libraryDelete &&
            !createAction &&
            importPoint == null &&
            queryFiles.length == 0 &&
            membersToDelete.length == 0 &&
            membersToExport.length == 0 &&
           backupLocation == null && incrBackupLocation == null && 
           !rebuildIndexes && !optimize &&
            checkLogFile == null &&
            serverCommand == null &&
            aclData == null &&
            !printVersion)
        {
            fatal("no command: specify a XQuery file\n" +

           	      "    or -import -export -create -delete -server -backup -reindex -optimize -check\n" +
           	      " Use option -help for more information.");
        }

        if(printVersion)
            doPrintVersion();
        

        if (serverCommand != null)
            doServerAction(serverCommand);
        
        if(createAction) {
            if(libraryOption != null)
                libraryConnection();
            else // only group
                groupConnection();
        }
        
        if (groupOption != null) {
            groupConnection();
        }

        if (libraryDelete) {
            doDeleteLibrary();
        }

        if (membersToDelete.length > 0) {
            for (String path : membersToDelete)
                doDeleteMember(path);
            ensureCommit();
        }

        if (importPoint != null) {
            doStartImport(importPoint);
            for (String path : importFiles)
                doImportDocument(path);
            ensureCommit();
        }

        if (optimize)
            doOptimize();
        
        if (rebuildIndexes)
            doReindex();

        if(aclData != null)
            doSetACL(aclData);
        
        if(checkLogFile != null)
            doCheckDatabase(checkLogFile);
        
        if (backupLocation != null)
            doBackup(backupLocation);
        else if (incrBackupLocation != null)
            doIncrBackup(incrBackupLocation);

        if (membersToExport.length > 0) {
            for (String path : membersToExport)
                doExportMember(path);
        }

        // XQuery files are executed at last
        for(int p = 0; p < queryFiles.length; p++)
        {
            currentFile = queryFiles[p];
            Reader input = "-".equals(currentFile)
                    ? (Reader) (new InputStreamReader(System.in))
                    : new FileReader(currentFile);

            if(connection == null || connection.isLocal())
                doExecuteFile(input, currentFile);
            else
                doExecuteFileRemote(input);
            input.close();
        }

        // close local group
        if(connection != null)
            connection.close();
    }

    private void doPrintVersion()
        throws Exception
    {
        if (groupOption != null)
            groupConnection();
        if(connection == null || connection.isLocal())
            stderr.println(Product.PRODUCT_NAME + " " + Product.FULL_VERSION);
        else {
            Map<String, Property> info = connection.getInfo();
            Property prop = info.get("product-name");
            String name = (prop == null)? "?" : prop.value;
            prop = info.get("product-version");
            String version = (prop == null)? "?" : prop.value;
            stderr.println(name + " " + version);
        }
    }



    private void doServerAction(String cmd) throws Exception
    {
        groupConnection();
        stderr.println("" + connection.serverCommand(cmd));
    }

    private void doDeleteLibrary() throws Exception
    {
        groupConnection();
        if(libraryOption == null)
            error("please specify library to delete with option -l", null);
        connection.deleteLibrary(libraryOption);
    }

    private void doDeleteMember(String path) throws Exception
    {
        libraryConnection();
        if(!connection.deleteMember(libraryOption, path) && verbose)
            error("cannot delete Library member " + path, null);
    }

    private void ensureCommit()
        throws Exception
    {
        if (connection != null)
        try {
            connection.commit(libraryOption);
        }
        catch(QizxConnector.ImportException ex) {
            for(String err : ex.getErrors())
                if(!err.startsWith("IMPORT ERRORS"))
                    error(err.replace('\t', ' '), null);
        }
    }

    private void doExportMember(String path) throws Exception
    {
        path = PathUtil.normalizePath(path, false);
        if(outputFile == null)
            outputFile = PathUtil.getBaseName(path);

        libraryConnection();
        String nat = connection.getMemberNature(libraryOption, path);
        if("document".equals(nat)) {
            // document or non-existent
            exportDocument(path, outputFile);
        }
        else if("collection".equals(nat)) {
            exportCollection(path, new File(outputFile));
        }
        else error("no such Library member " + path, null);
    }

    private void doSetACL(String aclFile) throws Exception
    {
        String data = FileUtil.loadString(new File(aclFile));
        libraryConnection();
        connection.setACL(libraryOption, data);
    }

    private void doBackup(String backupLocation)
        throws Exception
    {
        groupConnection();
        connection.backup(libraryOption, new File(backupLocation),
                          progressHandler);
    }

    private void doIncrBackup(String backupLocation)
        throws Exception
    {
        groupConnection();
        connection.incrementalBackup(libraryOption, new File(backupLocation),
                                     progressHandler);
    }

    private void doReindex()
        throws Exception
    {
        groupConnection();
        connection.reindex(libraryOption, progressHandler);
    }

    private void doOptimize()
        throws Exception
    {
        groupConnection();
        connection.optimize(libraryOption, progressHandler);
    }

    private LibraryProgressObserver progressHandler = new LibraryProgressObserver() {
        public void importProgress(double size) {
            displayProgress(size);
        }
        
        public void commitProgress(double fraction) {
            displayProgress(fraction);
        }
        
        public void backupProgress(double fraction) {
            displayProgress(fraction);
        }
        
        public void reindexingProgress(double fraction) {
            displayProgress(fraction);
        }
        
        public void optimizationProgress(double fraction) {
            displayProgress(fraction);
        }
    };

    // Start importing into a collection:
    private void doStartImport(String collection)
        throws Exception
    {
        if(collection.endsWith(".xml") || collection.endsWith(".xml"))
            warning("-import requires the path of a Collection, seems you " +
                    "provide a path for a Document: " + collection);
        libraryConnection();
        
        collection = PathUtil.normalizePath(collection, true);
        if(collection.indexOf('*') < 0)
            connection.createCollection(libraryOption, collection);

        // indexing specs specified ?
        if(xsheetPath != null)
            setIndexing(xsheetPath);
        
        connection.importStart(libraryOption);
    }

    private void doImportDocument(String path) throws Exception
    {
        importEntity(path, importPoint);
    }

    
    private void doCheckDatabase(String logFile) throws Exception
    {

        groupConnection();
        if(!connection.isLocal()) {
            error("db check available only in local mode", null);
            return;
        }
        OutputStream logStream = new FileOutputStream(logFile);
        Log log = new StreamLog(logStream );
        // now in API
        int mask = deepCheck? LibraryManager.CHECK_CONTENTS : 0;
        if (!nostorageCheck)
            mask |= LibraryManager.CHECK_STORAGE;
        libMan.sanityCheck(log, mask, true);
        logStream.close();
        if (log.getErrorCount() > 0)
            System.err.println("*** " + log.getErrorCount() + " errors found");
    }


    // -----------------------------------------------------------------------
    
    private void doExecuteFileRemote(Reader input)
        throws IOException, QizxException
    {
        String query = FileUtil.loadString(input);
        String format = wrapDisplay? "items" : "xml";
        String encoding = "UTF-8";
        for(String option : xmlOptions) {
            if(option.startsWith("method="))
                format = option.substring(7);
            else if(option.startsWith("encoding="))
                encoding = option.substring(9);
        }
        
        InputStream stream = 
            connection.executeRemote(query, libraryOption, format, encoding);
                
        if (outputFile != null) {
            FileOutputStream out = new FileOutputStream(outputFile);
            FileUtil.copy(stream, out, null);
            out.close();
        }
        else {
            FileUtil.copy(stream, System.out, null);
            System.out.flush();
        }
        stream.close();
    }

    /*
     * Parses and executes a single XQuery file on local xlib or as pure XQuery.
     */
    private void doExecuteFile(Reader input, String path)
        throws Exception
    {
        // ensure we have a XQ session (can be a Library)
        if(session == null) {

            if(groupOption != null || libraryOption != null) {
                libraryConnection();    // creates a connection
                session = connection.getLibrary(libraryOption);
            }

            if(session == null) { // fallback to pure XQ
                String defModBase = (moduleBaseURI != null)? moduleBaseURI : ".";
                sessionMan = Configuration.createSessionManager(defModBase);
                if(configFile != null) {
                    sessionMan.configure(Util.loadProperties(configFile));
                }
                setupModules();
                session = sessionMan.createSession();
            }
            // enable all:
            session.enableJavaBinding(null);
        }

        try {
            if (timezone != null) {
                TimeZone tz = TimeZone.getTimeZone(timezone);
                session.getContext().setImplicitTimeZone(tz);
            }
            if (collation != null)
                session.getContext().setDefaultCollation(collation);

            if(baseURI != null)
                session.getContext().setBaseURI(baseURI);

            // Load modules relatively to this script (if no EXPath repository)
            String parentPath = PathUtil.getParentPath(path);
            if(parentPath != null && expathRepos == null) {
                URL modBase = FileUtil.fileToURL(parentPath);
                session.setModuleResolver(new DefaultModuleResolver(modBase));
            }


            if(traceLex) {
                Lexer.debug = true;
                NewLexer.debug = 1;
            }
                    
            String src = FileUtil.loadString(input);
            Expression query = session.compileExpression(src);
            // create & init a global 'arguments' with args from cmd line:
            QName argVar = session.getQName("arguments");
            query.getContext().declareVariable(argVar, null);
            query.bindVariable(argVar, appArgs, null);

            for (int g = 0; g < globals.length; g++) {
                int eq = globals[g].indexOf('=');
                if (eq < 0)
                    throw new IllegalArgumentException("illegal variable init: "
                                                       + globals[g]);
                String name = globals[g].substring(0, eq);
                query.bindVariable(session.getQName(name),
                                   globals[g].substring(eq + 1), null);
            }

            // Any expression that can be passed to fn:collection
            // Valid both for Qizx/db and Qizx/open
            if (implicitDomain != null) {
                Expression icol = session.compileExpression("fn:collection('" + implicitDomain + "')");
                ItemSequence seq = icol.evaluate();
                query.bindImplicitCollection(seq);
            }

            executeAndPrint(query);
        }
        catch (CompilationException e) {
            error(e.getErrorCount() + " parsing/static error(s):", e);
            printMessages(e.getMessages());
        }
        catch (Exception ex) {
            ++ errorCount;
            stderr.println("*** " + ex);
            if (traceExceptions)
                ex.printStackTrace();
        }
    }

    // -----------------------------------------------------------------------

    // Ensures connection to a group
    private void groupConnection() throws Exception
    {
        if (connection != null)
            return;
        if(groupOption == null)
            fatal("Library Group should be specified: use option -group or -g");
        
        if (remoteServer()) {
            RestAPIConnection server = new RestAPIConnection(groupOption);
            server.setCredentials(login, password);
            connection = new QizxConnector(server);
            server.login();
        }

        else {
            File groupDir = new File(groupOption);
            if(!groupDir.exists() && createAction) {
                libMan = Configuration.createLibraryGroup(groupDir);
            }
            if(libMan == null)
                libMan = Configuration.openLibraryGroup(groupDir);
            if(configFile != null) {
                libMan.configure(Util.loadProperties(configFile));
            }
            setupModules();
            connection = new QizxConnector(libMan);
        }
    }
    
    // Ensures we have a Library (can be null in remote mode)
    private void libraryConnection() throws Exception
    {
        groupConnection(); // a minimum
        if(connection.isLocal()) {
            if(libraryOption == null)
                throw new QizxException(MISSING_LIBRARY);
            if(createAction)
                connection.createLibrary(libraryOption);

            else
                connection.getLibrary(libraryOption); 
        }
        else if(createAction) {
            if(libraryOption == null)
                throw new QizxException(MISSING_LIBRARY);
            connection.createLibrary(libraryOption);
        }
        createAction = false; // avoid doing it several times
    }

    private boolean remoteServer()
    {
        return groupOption != null && groupOption.startsWith("http://");
    }

    private void setupModules()
    {
        if(expathRepos == null) {
            // is it set from env variable?
            expathRepos = System.getenv("EXPATH_REPO");                    
        }
        if(expathRepos != null) { // EXPath aboard
            File repo_dir = new File(expathRepos);
            try {
                // plug an extended EXPath Repository
                FileSystemStorage storage = new FileSystemStorage(repo_dir);
                QizxRepository repo = new QizxRepository(storage);
                ModuleResolver resolver = new QizxRepoResolver(repo);
                if(sessionMan != null)
                    sessionMan.setModuleResolver(resolver);

                else if(libMan != null)
                    libMan.setModuleResolver(resolver);
            }
            catch (PackageException e) {
                error("EXPath repository error: " + e.getMessage(), e);
            }
        }
        else if(moduleBaseURI != null) {
            URL moduleURL = FileUtil.fileToURL(moduleBaseURI);
            if(sessionMan != null)
                sessionMan.setModuleResolver(new DefaultModuleResolver(moduleURL));

            else if (libMan != null)
                libMan.setModuleResolver(new DefaultModuleResolver(moduleURL));
        }
    }

    protected PasswordAuthentication authenticate()
    {
        if(login == null)
            fatal("authentication required: see options -auth or -login");

        if(password == null) {
            // Java 1.6 has a Console.readPassword method TODO
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("password: ");
            System.out.flush();
            try {
                password = console.readLine();
                if(password == null)
                    fatal("password required");
            }
            catch (Exception e) {
                fatal(e.getMessage());
            }
        }
        return new PasswordAuthentication(login, password.toCharArray()); 
    }

    private void displayProgress(double progress)
    {
        long percent = Math.round(progress * 100);
        stderr.print(percent + " % done\r");
        stderr.flush();
        if(progress == 1) {
            Util.sleep(300);
            stderr.println("              ");
        }
    }

    private void printMessages(Message[] errors)
    {
         for (int i = 0; i < errors.length; i++) {
            Message m = errors[i];
            String module = m.getModuleURI();
            if(module != null && module.startsWith("file:"))
                module = module.substring(5);
            
            stderr.print("Error on line " + m.getLineNumber()
                         + " col " + m.getColumnNumber());
            if(module != null)
                stderr.print(" module " + module);
            stderr.println(":");
            QName errorCode = m.getErrorCode();
            String code =
                (errorCode == null)? "<no-code>" : errorCode.getLocalPart();
            stderr.println(" "+ code + ": " + m.getText());
        }
    }



    private void setIndexing(String path) throws Exception
    {
        libraryConnection();
        Indexing sheet = new Indexing();
        sheet.parse(new InputSource(path));
        connection.setIndexing(libraryOption, sheet);
    }

    // Import a file, a whole directory, or an URL into a collection
    // root directory name is kept
    private void importEntity(String uri, String collectionPath)
        throws Exception
    {
        // URL of a document ?
        URL url = FileUtil.toURL(uri);
        if (url != null) { // remote url
            try {
                // Use the URL as path of the document in the library
                connection.importDocument(url.getPath(), FileUtil.loadString(url),
                                         url.toString());
            }
            catch (Throwable t) {
                error(t.getClass().getSimpleName() + " on " + uri
                      + ": " + t.getMessage(), null);
            }
        }
        else { // simple file or directory
            File docfile = new File(uri);
            importDocument(docfile, collectionPath, true);
        }
    }

    //  Import a file or a whole directory into a collection
    private Document importDocument(File file, String collectionPath, boolean flat)
        throws Exception
    {
        if (file.isFile()) {
            if (!acceptedFile(file))
                return null;

            boolean pattern = (collectionPath.indexOf('*') >= 0);
            String docPath = pattern ? collectionPath
                    : (collectionPath + '/' + file.getName());
            
            if(verbose)
                stderr.println(" import " +file+" into "+docPath);

            long docSize = file.length();
            if (!connection.isLocal() && docSize > PUT_MAX_SIZE)
                warning("large document import may fail: " + file
                        + " " + docSize + " bytes");
            try {
                connection.importDocument(docPath, file);
            }
            catch(QizxConnector.ImportException ex) {
                for(String err : ex.getErrors())
                    if(!err.startsWith("IMPORT ERRORS"))
                        error(err.replace('\t', ' '), null);
            }
            catch (LibraryException e) {
                Exception ex = e;
                if (e.getCause() instanceof SAXException)
                    ex = (SAXException) e.getCause();
                StringBuilder msg = new StringBuilder(ex.getClass().getSimpleName());
                msg.append(" on ").append(file);
                if(ex instanceof SAXParseException) {
                    SAXParseException sax = (SAXParseException) ex;
                    msg.append("[line " + sax.getLineNumber() + "] ");
                }
                msg.append(": ").append(ex.getMessage());
                error(msg.toString(), null);
            }
            catch (Throwable t) {
                error(t.getClass().getSimpleName() + " on " + docPath
                      + ": " + t.getMessage(), null);
                t.printStackTrace();
            }
        }
        else if (file.isDirectory()) {
            String newPath = collectionPath;
            if(!flat)
                newPath = collectionPath + "/" + file.getName();
            File[] files = file.listFiles();
            for (int f = 0; f < files.length; f++)
                importDocument(files[f], newPath, false);
        }
        else {
            stderr.println("cannot find document " + file);
        }
        return null;
    }

    /**
     * Apply file filters (-include and -exclude)
     */
    private boolean acceptedFile(File file)
    {
        String name = file.getName().toLowerCase();
        return (includeSuffixes == null || hasSuffix(name, includeSuffixes))
            && (excludeSuffixes == null || !hasSuffix(name, excludeSuffixes));
    }

    private boolean hasSuffix(String name, String[] suffixes)
    {
        for (int i = 0; i < suffixes.length; i++) {
            if(name.endsWith(suffixes[i]))
                return true;
        }
        return false;
    }

    private void exportDocument(String memberPath, String path)
        throws Exception
    {
        // use normal output specification:
        XMLSerializer sout = new XMLSerializer();
        prepareOutput(sout, path);
        Node tree = connection.getXMLContents(libraryOption, memberPath);
        sout.putNodeCopy(tree, 0);
        sout.println();
        sout.flush();
        sout.getOutput().close();
    }

    private void exportCollection(String memberPath, File output)
        throws Exception
    {
        if (!output.isDirectory()) {
            if(output.exists())
                throw new QizxException("collection export location is not a directory: "
                                        + outputFile);
            output.mkdirs();
        }
        
        LibraryMemberIterator iter = connection.getChildren(libraryOption,
                                                            memberPath, -1);        
        for(; iter.moveToNextMember(); )
        {
            LibraryMember member = iter.getCurrentMember();
            String childPath = member.getPath();
            File childFile = new File(output, PathUtil.getBaseName(childPath));
            if(member.isDocument()) {
                exportDocument(childPath, childFile.getPath());
            }
            else {
                exportCollection(childPath, childFile);
            }            
        }
    }

    private void prepareOutput(XMLSerializer serial, String outputFile)
        throws QizxException, IOException
    {
        serial.reset();
        
        // XML options:
        for (int g = 0; g < xmlOptions.length; g++)
        {
            int eq = xmlOptions[g].indexOf('=');
            if (eq < 0) {
                throw new QizxException("invalid XML option: " + xmlOptions[g]);
            }
            serial.setOption(xmlOptions[g].substring(0, eq),
                             xmlOptions[g].substring(eq + 1));
        }
    
        if (outputFile != null) {
            serial.setOutput(new FileOutputStream(outputFile),
                             serial.getEncoding());
        }
    }
    
    // Executes a compiled expression
    private void executeAndPrint(Expression query)
        throws Exception
    {
        QName RESULTS = query.getQName("query-results");
        QName ITEM = query.getQName("item"), ITEMTYPE = query.getQName("type");
    
        prepareOutput(resultOutput, outputFile);
        if(repeats > 1)
            verbose = true;

        if(traceQ) {
            ((ExpressionImpl) query).setCompilationTrace(new PrintWriter(System.err, true));
        }
        if(traceExpr) {
            XMLSerializer dout = new XMLSerializer(new PrintWriter(System.err, true));
            dout.setIndent(2);
            XMLExprDisplay display = new XMLExprDisplay(dout);
            ((ExpressionImpl) query).dump(display);
        }
        if(muteRepeats > 1) {
            long T0 = System.currentTimeMillis();
            int total = 0;
            for(int r = 0; r < muteRepeats; r++) {
                ItemSequence v = query.evaluate();
                total += v.countItems();
            }
            stderr.println("evaluation time " +
                           (System.currentTimeMillis() - T0) / (float) muteRepeats);
        }

        for (int rep = 0; rep < repeats; rep++) {
            try {
                long T1, T0 = System.currentTimeMillis();
    
                // Pure evaluation, without retrieving results (returns an
                // iterator).
                // Due to lazy evaluation, this is sometimes much shorter
                // than the actual retrieval.
                ItemSequence v = query.evaluate();
                
                T1 = System.currentTimeMillis();
                int itemCount = 0;
    
                resultOutput.reset();
    
                if (wrapDisplay) {
                    resultOutput.putDocumentStart();
                    resultOutput.putElementStart(RESULTS);
                }
    
                for (; v.moveToNextItem(); itemCount++)
                {
                    Item item = v.getCurrentItem();
                    if (wrapDisplay) {
                        resultOutput.putElementStart(ITEM);
                        resultOutput.putAttribute(ITEMTYPE,
                                               v.getType().toString(), null);
                    }
                    if (item.isNode()) {
                        v.export(resultOutput);
                        if(resultOutput.getIndent() > 0)
                            resultOutput.println();
                    }
                    else {
                        if (itemCount > 0 && !wrapDisplay)
                            resultOutput.putText(" "); // some space
                        resultOutput.putAtomText(v.getString());
                    }
                    if (wrapDisplay)
                        resultOutput.putElementEnd(ITEM);
                    // dont want many <?xml?> if several items:
                    resultOutput.setOmitXMLDeclaration(true);
                }
    
                if (wrapDisplay) {
                    resultOutput.putElementEnd(RESULTS);
                    resultOutput.putDocumentEnd();
                }
                
                resultOutput.flush();
               
                if (verbose)
                    stderr.println("-> " + itemCount + " item(s)");
                long T2 = System.currentTimeMillis();
                if (verbose)
                    stderr.println("evaluation time: " + (T1 - T0)
                                   + " ms, display time: " + (T2 - T1)
                                   + " ms");
            }
            catch (EvaluationException e) {
                ++ errorCount;
                QName errCode = e.getErrorCode();
                stderr.println("*** execution error "
                               + (errCode != null? errCode.getLocalPart() : "<unknown code>")
                               + ": " + e.getMessage());
                if (e.getCause() != null && !traceExceptions) {
                    stderr.println("  caused by: " + e.getCause());
                }
                EvaluationStackTrace[] stack = e.getStack();
                for (int i = 0; i < stack.length; ++i) {
                    EvaluationStackTrace frame = stack[i];
                    String sig = frame.getSignature();
                    sig = (sig == null)? "" : ("in " + sig);
                    stderr.print(sig + " at line " + frame.getLineNumber()
                                     + " col " + frame.getColumnNumber()
                                     + " in ");
                    if(frame.getModuleURI() != null)
                        stderr.print(frame.getModuleURI());
                    else 
                        stderr.print(currentFile);
                    stderr.println();
                }
                if (traceExceptions)
                    e.printStackTrace();
            }
        }
    }
}

