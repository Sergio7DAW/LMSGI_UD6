/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of the Qizx application components
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.apps.restapi;

import com.qizx.api.*;
import com.qizx.api.admin.Profiling;
import com.qizx.api.util.XMLSerializer;
import com.qizx.apps.util.Property;
import com.qizx.apps.util.TabularData;
import com.qizx.util.basic.PathUtil;
import com.qizx.util.basic.Util;
import com.qizx.util.rest.RESTClient;
import com.qizx.util.rest.RESTException;
import com.qizx.xdm.Conversion;
import com.qizx.xdm.IQName;
import com.qizx.xdm.NodePullStream;
import com.qizx.xdm.XQName;
import com.qizx.xquery.XQType;
import com.qizx.xquery.dt.NodeType;

import org.xml.sax.InputSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * HTTP client for the Qizx REST API.
 * <p>Provides most of functionalities of {@link Library}, with a few
 * restrictions and slightly different semantics.
 * 
 * <p>This class is not thread-safe. As many instances as desired can be created.
 */
public class RestAPIConnection extends RESTClient
    implements RestAPI
{
    protected String libName;
    private Post curPost;
    private Put curPut;

    /**
     * Creates a Qizx REST API client.
     * @param URL an URL pointing to the API Service of a Qizx Server.
     * @throws MalformedURLException
     */
    public RestAPIConnection(String URL) throws MalformedURLException
    {
        super(URL);
    }

    /**
     * Sets the name of the current XML Library.
     * <p>Can be left to null if the server manages only one XML Library.
     * @param name name of an XML Library (database) managed by the server.
     */
    public void setLibraryName(String name)
    {
        libName = name;
    }

    @Override
    public void setCredentials(String username, String password)
    {
        super.setCredentials(username, password);
    }

    /**
     * This request checks that the client can access the server.
     * <p>User credentials should be set before attempting any connection.
     * See {@link #setCredentials(String, String)}.
     * @throws RESTException
     */
    public String login()
        throws RESTException
    {
        Get req = newGet("server", null);
        req.setParameter("command", "status");
        Response resp = req.send();
        
        if (resp.isError()) {
            if (resp.getStatusCode() == 401) {
                String hd = resp.getHeader("WWW-Authenticate");
                resp.consumeContent();
                return (hd != null)? Util.stringAfter(hd, "=") : "server";
            }
            throw new RESTException("login error: " + resp.getErrorString());
        }

        String r = resp.getSingleLine();
        if (!"online".equals(r) && !"offline".equals(r))
            throw new RESTException("invalid response from REST server: " + r);
        return null;
    }

    /**
     * Gets the contents of a Document or of a Collection.
     * @param path database path of the Document or Collection.
     * @param limit maximum number of members for collection; no limit if < 0
     * @return a Response. By testing the content-type of the Response, the
     * contents can be obtained as byte stream, char stream, String or parsed XML Node.
     * @see Response
     * @throws RESTException
     */
    public Response get(String path)
        throws RESTException
    {
        return get(path, null, -1);
    }

    /**
     * Gets the contents of a Document or of a Collection.
     * @param path database path of the Document or Collection.
     * @param limit maximum number of members for collection; no limit if < 0
     * @return a Response. By testing the content-type of the Response, the
     * contents can be obtained as byte stream, char stream, String or parsed XML Node.
     * @see Response
     * @throws RESTException
     */
    public Response get(String path, int limit)
        throws RESTException
    {
        return get(path, null, limit);
    }

    /**
     * Gets the contents of a Document or of a Collection.
     * @param path database path of the Document or Collection.
     * @param options serialization options for Documents: a comma or tab-separated
     * list of options in the form <code>option=value</code>, for example
     *  <code>method=XML, encoding=ISO8859-1</code>. See {@link XMLSerializer}
     *  for options details.
     * @param limit maximum number of members for collection; no limit if < 0
     * @return a Response. By testing the content-type of the Response, the
     * contents can be obtained as byte stream, char stream, String or parsed XML Node.
     * @see com.qizx.util.rest.RESTClient.Response
     * @throws RESTException
     */
    public Response get(String path, String options, int limit)
        throws RESTException
    {
        Get req = newGet("get", path);
        if (options != null)
            req.setParameter("options", options);
        req.setParameter("max", Integer.toString(limit));
        return req.sendAndCheck();
    }

    /**
     * Sends an evaluates an XQuery script, returns the response as a sequence
     * of XQuery Items.
     * @param query an XQuery script.
     * @param format a String with value "items", "XML" or "HTML".
     * @return a sequence of Items.
     * @throws RESTException
     * @throws EvaluationException
     */
    public ItemSequence eval(String query, String format)
        throws RESTException, EvaluationException
    {
        return eval(query, format, null, -1, COUNTING_EXACT, null, 0, -1);
    }

    /**
     * Sends an evaluates an XQuery script, returns the response as a page
     * of XQuery Items.
     * @param query an XQuery script.
     * @param mode null in normal execution, or "profile"
     * @param itemCount desired number of items.
     * @param firstItem the rank of the first desired item (first rank is 1).
     * @return a sequence of Items.
     * @throws RESTException
     * @throws EvaluationException
     */
    public ItemSequence evalAsItems(String query, String mode, 
                                    int itemCount, int firstItem)
        throws RESTException, EvaluationException
    {
        return eval(query, FORMAT_ITEMS, null, -1, COUNTING_EXACT, mode,
                    itemCount, firstItem);
    }

    /**
     * Sends an evaluates an XQuery script, returns the response as a page
     * of XQuery Items. Most general request.
     * @param query an XQuery script.
     * @param format a String with value "items", "XML" or "HTML".
     * @param encoding encoding used for the response
     * @param maxTime number of milliseconds the query is allowed to run. Ignored if <= 0.
     * @param counting counting method (applicable to format "items" only): value
     * can be: <ul>
     * <li>"exact" (the default) for an exact count (can be costly to evaluate),
     * <li>"estimated" in which case the count value is similar to
     * the count returned by XQuery function x:count-estimate().
     * <li>"none": no counting
     * </ul>
     * @param mode execution mode: null for normal execution, or "profile" for profiling
     * @param itemCount desired number of items.
     * @param firstItem the rank of the first desired item (first rank is 1).
     * @return a sequence of Items.
     * @throws RESTException
     * @throws EvaluationException
     */
    public ItemSequence eval(String query, String format, String encoding,
                             int maxTime, String counting, String mode,
                             int itemCount, int firstItem)
        throws RESTException, EvaluationException
    {
        try {
            Response resp = rawEval(query, format, encoding, maxTime, counting,
                                    mode, itemCount, firstItem);
            return new SeqImpl(resp.getNode(), RestAPI.PROFILE.equalsIgnoreCase(mode));
        }
        catch (DataModelException e) {
            throw wrap(e);
        }
    }

    /**
     * Evaluation without parsing of the response (internal use).
     */
    public Response rawEval(String query, String format, String encoding,
                            int maxTime, String counting, String mode,
                            int itemCount, int firstItem)
        throws RESTException, EvaluationException
    {
        Post req = newPost("eval", null);
        req.setParameter("format", format);
        req.setParameter("query", query);
        req.setParameter("counting", counting);
        if (mode != null)
            req.setParameter("mode", mode);
        if (encoding != null)
            req.setParameter("encoding", encoding);
        if (itemCount > 0)
            req.setParameter("count", Integer.toString(itemCount));
        req.setParameter("first", Integer.toString(firstItem));
        if (maxTime > 0)
            req.setParameter("maxtime", Integer.toString(maxTime));
        return req.sendAndCheck();
    }

    /**
     * Starts an import of XML documents.
     * <p>
     * A typical import snippet looks like:
     * <pre>
     * restCx.startPut();
     * restCx.addDocument(&quot;/docs/path1&quot;, stringData);
     * restCx.addDocument(&quot;/docs/path2&quot;, new File(myfile), null);
     * String[] status = restCx.finishPut();
     * </pre>
     * @see putDocument
     * @see #finishPut()
     * @throws RESTException
     */
    public void startPut()
        throws RESTException
    {
        curPost = newMultipartPost("put", null);
        resetRank();
    }
    
    /**
     * Starts an import of non-XML documents.
     * @see #startPut #putDocument #finishPut
     */
    public void startPutNonXML()
        throws RESTException
    {
        curPost = newMultipartPost("putnonxml", null);
        resetRank();
    }

    /**
     * Adds a document to the current import operation.
     * @param path database path of the document
     * @param contents a well-formed XML document, if called after startPut,
     * or any data if following startPutNonXML.
     * @param contentType can be null or "text/xml" for XML data, or an
     * appropriate Mime type for non-XML documents.
     * @see #startPut #startPutNonXML #finishPut
     * @throws RESTException
     */
    public void putDocument(String path, File contents, String contentType)
        throws RESTException
    {
        if (curPost == null)
            throw new RESTException("bad state: call startPut");
        curPost.putStringPart(ranked("path"), path);
        curPost.putFilePart(ranked("data"), contents, contentType);
        curParamRank++;
    }

    /**
     * Adds a document to the current import operation.
     * @param path database path of the document
     * @param contents a well-formed XML document as a String, if called after
     *        startPut, or any text data if following startPutNonXML.
     * @see #startPut #startPutNonXML
     * @throws RESTException
     */
    public void putDocument(String path, String contents)
        throws RESTException
    {
        if (curPost == null)
            throw new RESTException("bad state: call startPut first");
        curPost.putStringPart(ranked("path"), path);
        curPost.putStringPart(ranked("data"), contents);
        curParamRank++;
    }

    /**
     * Called to finish a bulk load (after startPut and a series of addDocument)
     * @return a status String giving the number of documents in error
     * @see #startPut #startPutNonXML #putDocument
     * @throws RESTException
     */
    public String[] finishPut()
        throws RESTException
    {
        if (curPost == null)
            throw new RESTException("bad state: call startPut first");
        Response resp = curPost.sendAndCheck();
        curPost = null;
        return resp.getLines();
    }

    /**
     * Store wrapped XML documents.
     * <p>Data is pulled from a stream: due to http-client library constraints,
     *  this cannot work in push mode.
     * <p>The format is:
     * <pre>
     *  <import>
     *   <document path="doc1.xml"> ... contents ... </document>
     *   <document path="doc2.xml"> ... contents ... </document>
     *  </import>
     * </pre>
     * @param data XML input stream
     * @return a status String giving the number of documents in error
     * @throws RESTException
     */
    public String[] streamingPut(InputStream data)
        throws RESTException
    {
        curPut = newPut("streamingput", null);
        curPut.setContent(data);
        Response resp = curPut.sendAndCheck();
        return resp.getLines();
    }

    /**
     * Wraps the <code>mkcol</code> request.
     * @param path database path of the collection
     * @param createParents true if parent collection are to be created if they
     * do not yet exist.
     * @return the path of the new collection
     * @throws RESTException
     */
    public String createCollection(String path, boolean createParents)
        throws RESTException
    {
        Post req = newPost("mkcol", path);
        req.setParameter("parents", Boolean.toString(createParents));
        Response resp = req.sendAndCheck();
        return resp.getSingleLine();
    }

    /**
     * Wraps the <code>move</code> request.
     * @param src path of a collection or document to be renamed
     * @param dst target path of a collection or document
     * @return target path
     * @throws RESTException
     */
    public String move(String src, String dst)
        throws RESTException
    {
        Post req = newPost("move", null);
        req.setParameter("src", src);
        req.setParameter("dst", dst);
        Response resp = req.sendAndCheck();
        return resp.getSingleLine();
    }

    /**
     * Copies a Document or Collection.
     * <p>Wraps the <code>copy</code> request.
     * @param src path of a collection or document to be copied
     * @param dst target path of a collection or document
     * @return target path
     */
    public String copy(String src, String dst)
        throws RESTException
    {
        Post req = newPost("copy", null);
        req.setParameter("src", src);
        req.setParameter("dst", dst);
        Response resp = req.sendAndCheck();
        return resp.getSingleLine();
    }

    /**
     * Deletes a Document or Collection given by its path.
     * @param path path of a collection or document to be deleted
     * @return true if actually deleted
     */
    public boolean delete(String path)
        throws RESTException
    {
        Post req = newPost("delete", path);
        Response resp = req.sendAndCheck();
        String s = resp.getSingleLine();
        return s != null && s.length() > 0;
    }
    
    /**
     * Returns children of Collection: documents or collections as an iterator.
     * @param collectionPath parent collection
     * @return an iterator
     * @throws RESTException
     */
    public LibraryMemberIterator getChildren(String collectionPath, int limit)
        throws RESTException
    {
        Response r = get(collectionPath, limit);
        ArrayList<MemberImpl> members = new ArrayList<MemberImpl>();
        
        for (;;) {
            String path = r.getNextLine();
            if (path == null)
                break;
            if(path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
                members.add(new CollecImpl(path));
            }
            else {
                members.add(new DocImpl(path));
            }
        }
        return new CollectionIterImpl(members);
    }
    
    /**
     * Returns an iterator over XML Library members
     * @param path root path (a Collection if depth > 0)
     * @param depth if 0, return only the properties of the root member, 
     *  if dept&nbsp;>&nbsp;0 then the root path must be that of a Collection,
     *  and the method returns members within that depth (depth=1 : children,
     *  depth=2: grand-children etc)
     * @param properties names of properties to return. By default, all properties
     *        are returned. The names are separated by commas or whitespace.
     * @return an iterator on members
     */
    public LibraryMemberIterator getMembers(String path, int depth,
                                            String[] properties)
        throws RESTException, EvaluationException
    {
        Get req = newGet("getprop", path);
        req.setParameter("depth", Integer.toString(depth));
        if (properties != null)
            req.setParameter("properties", nameList(properties));
        Response resp = req.sendAndCheck();
        try {
            return new MemberIterImpl(resp.firstItemNode());
        }
        catch (DataModelException e) {
            throw wrap(e);
        }
    }
    
    /**
     * Returns metadata properties of a Library Member (document or collection)
     * @param path path of the Library Member
     * @param propNames selected property names. If null or empty, all properties
     * are returned.
     * @return a map of name to Property descriptors.
     * @throws RESTException
     */
    public Map<String, Property> getMemberProperties(String path, String[] propNames)
        throws RESTException
    {
        Get req = newGet("getprop", path);
        req.setParameter("depth", "1");
        Response resp = req.sendAndCheck();
        try {
            return getProperties(resp.firstItemNode(), propNames);
        }
        catch (DataModelException e) {
            throw new RESTException(e.getMessage(), e);
        }
    }
    
    /**
     * Starts a batch of setprop operations on a Library Member (document or collection)
     * @param path path of the Library Member
     * @param name property name
     * @param type property type
     * @param value property value, matching the type
     * @throws RESTException
     */
    public void startSetProp(String path, 
                             String name, String type, String value)
        throws RESTException
    {
        curPost = newPost("setprop", path);
        resetRank();
        addProperty(name, type, value);
    }

    /**
     * Adds a Property to a batch of setprop operations
     * @param name property name
     * @param type property type
     * @param value property value, matching the type
     * @throws RESTException
     */
    public void addProperty(String name, String type, String value)
        throws RESTException
    {
        if (curPost == null)
            throw new RESTException("bad state: call startSetProp");
        curPost.setParameter(ranked("name"), name);
        if (type != null)
            curPost.setParameter(ranked("type"), type);
        if (value != null)
            curPost.setParameter(ranked("value"), value);
        curParamRank++;
    }

    /**
     * Finishes a batch  of setprop operations
     * @throws RESTException
     */
    public String finishProperties()
        throws RESTException
    {
        if (curPost == null)
            throw new RESTException("bad state: call startSetProp");
        Response resp = curPost.sendAndCheck();
        return resp.getSingleLine();
    }

    /**
     * Looks for Library members matching a query on metadata properties.
     * @param path root collection defining a search domain (null for the whole
     *        database)
     * @param query a query on metadata properties.
     * @param properties names of desired properties for each returned member (If
     *        null or empty, all properties are returned).
     * @return an iterator
     * @throws RESTException
     * @throws EvaluationException
     */
    public LibraryMemberIterator queryProp(String path, String query,
                                           String[] properties)
        throws RESTException, EvaluationException
    {
        Get req = newGet("queryprop", path);
        if (query != null)
            req.setParameter("query", query);
        if (properties != null)
            req.setParameter("properties", nameList(properties));
        Response resp = req.sendAndCheck();
        try {
            return new MemberIterImpl(resp.firstItemNode());
        }
        catch (DataModelException e) {
            throw wrap(e);
        }
    }

    /**
     * Returns a map of system properties describing the Qizx server.
     * @throws RESTException
     * @throws DataModelException
     */
    public Map<String, Property> info()
        throws RESTException, DataModelException
    {
        Get req = newGet("info", null);
        Response resp = req.sendAndCheck();
        Node root = resp.getNode();
        if (root == null)
            return null;
        return getProperties(root.getFirstChild(), null);
    }

    private Map<String,Property> getProperties(Node props, String[] names)
        throws DataModelException
    {
        HashMap<String,Property> map = new HashMap<String,Property>();
        Node child = props.getFirstChild();
        for (; child != null; child = child.getNextSibling()) {
            if (child.getNodeName() == null)
                continue;
            Node attr = child.getAttribute(NAME);
            if (attr == null || child.getNodeName() != PROPERTY)
                throw new DataModelException("improper property node " + child);
            String propName = attr.getStringValue();
            if (names != null && Util.indexOf(names, propName) < 0)
                continue;
            Property prop = new Property();
            prop.name = propName;
            Node typeAttr = child.getAttribute(TYPE_ATTR);
            prop.type = (typeAttr == null) ? "string" : typeAttr.getStringValue();
            Node value = child.getFirstChild();
            if (prop.type.endsWith("()"))
                prop.nodeValue = value;
            else
                prop.value = value.getStringValue();
            map.put(prop.name, prop);
        }
        return map;
    }

    /**
     * Wraps the "listlib" command.
     * @return a list of Library names
     * @throws RESTException
     */
    public String[] listLibraries()
        throws RESTException
    {
        Get req = newGet("listlib", null);
        Response resp = req.sendAndCheck();
        return resp.getLines();
    }

    // -------------------------------------------------------------------

    /**
     * Wraps the "server" command.
     * @param command
     * @return a String status
     * @throws RESTException
     */
    public String serverControl(String command)
        throws RESTException
    {
        Post req = newPost("server", null);
        req.setParameter("command", command);
        Response resp = req.sendAndCheck();
        return resp.getSingleLine();
    }

    /**
     * Wraps the "mklib" command. Applied on the current XML Library.
     * @throws RESTException
     */
    public void createLibrary(String libraryName)
        throws RESTException
    {
        Post req = newPost("mklib", null);
        req.setParameter("name", libraryName);
        Response resp = req.sendAndCheck();
        resp.getSingleLine();   // required (thanks to Apache)
    }

    /**
     * Wraps the "dellib" command. Applied on the current XML Library.
     * @param libraryName name of an existing XML Library
     * @throws RESTException
     */
    public void deleteLibrary(String libraryName)
        throws RESTException
    {
        Post req = newPost("dellib", null);
        req.setParameter("name", libraryName);
        Response resp = req.sendAndCheck();
        resp.getSingleLine();   // required (thanks to Apache)
    }

    /**
     * Wraps the "getindexing" command. Applied on the current XML Library.
     * @return a specification of Indexing options for the Library
     * @throws RESTException
     */
    public Indexing getIndexing()
        throws RESTException
    {
        Get req = newGet("getindexing", null);
        Response resp = req.sendAndCheck();
        
        Indexing ix = new Indexing();
        try {
            InputStream resultStream = resp.getStream();
            ix.parse(new InputSource(resultStream));
            resultStream.close();
            return ix;
        }
        catch (Exception e) {
            throw new RESTException(e.getMessage(), e);            
        }
    }

    /**
     * Wraps the "setindexing" command. Applied on the current XML Library.
     * @param spec a specification of Indexing options for the Library, as an XML file.
     * @throws RESTException
     */
    public void setIndexing(File spec)
        throws RESTException
    {
        Post req = newMultipartPost("setindexing", null);
        req.putFilePart("indexing", spec, null);
        Response resp = req.sendAndCheck();
        resp.getSingleLine();   // required (thanks to Apache)
    }

    /**
     * Wraps the "setindexing" command. Applied on the current XML Library.
     * @param spec spec a specification of Indexing options for the Library, as an XML fragment
     * @throws RESTException
     */
    public void setIndexing(String spec)
        throws RESTException
    {
        Post req = newPost("setindexing", null);
        req.setParameter("indexing", spec);
        Response resp = req.sendAndCheck();
        resp.getSingleLine();   // required (thanks to Apache)
    }

    /**
     * Wraps the "reindex" command. Applied on the current XML Library.
     * @return a Progress Identifier (for the getProgress method)
     * @throws RESTException
     */
    public String reindex()
        throws RESTException
    {
        Post req = newPost("reindex", null);
        Response resp = req.sendAndCheck();
        return resp.getSingleLine(); // progress id
    }

    /**
     * Wraps the "optimize" command. Applied on the current XML Library.
     * @return a Progress Identifier (for the getProgress method)
     * @throws RESTException
     */
    public String optimize()
        throws RESTException
    {
        Post req = newPost("optimize", null);
        Response resp = req.sendAndCheck();
        return resp.getSingleLine(); // progress id
    }

    /**
     * Wraps the "quickoptimize" command. Applied on the current XML Library.
     * @param timeHint maximum desired time to spend optimizing, in seconds
     * @param blocking true if method waits for a result
     * @return a Progress Identifier (for the getProgress method)
     * @throws RESTException
     */
    public String quickOptimize(int timeHint, boolean blocking)
        throws RESTException
    {
        Post req = newPost("quickoptimize", null);
        req.setParameter("timeHint", Integer.toString(timeHint));
        req.setParameter("blocking", Boolean.toString(blocking));
        Response resp = req.sendAndCheck();
        return resp.getSingleLine(); // progress id
    }

    /**
     * Wraps the "backup" command. Applied on the current XML Library.
     * @param path path of a Directory on the server host, target of the backup
     * @return a Progress Identifier (for the getProgress method)
     * @throws RESTException
     */
    public String backup(String path)
        throws RESTException
    {
        Post req = newPost("backup", path);
        Response resp = req.sendAndCheck();
        return resp.getSingleLine(); // progress id
    }

    /**
     * Wraps the "incrbackup" command. Applied on the current XML Library.
     * @param path path of a Directory on the server host, target of the backup
     * @return a Progress Identifier (for the getProgress method)
     * @throws RESTException
     */
    public String incrementalBackup(String path)
        throws RESTException
    {
        Post req = newPost("incrbackup", path);
        Response resp = req.sendAndCheck();
        return resp.getSingleLine(); // progress id
    }

    /**
     * Wraps the "progress" command.
     * @param id an identifier returned by backup reindex etc
     * @return fraction of the job achieved
     * @throws RESTException
     */
    public double getProgress(String id)
        throws RESTException
    {
        Get req = newGet("progress", null);
        req.setParameter("id", id);
        Response resp = req.sendAndCheck();
        resp.getNextLine(); // skip
        String s = resp.getNextLine();
        resp.consumeContent();
        return (s == null) ? Double.NaN : Double.parseDouble(s);
    }

    /**
     * Returns the ACL information for a Library member of the current XML Library.
     * Wraps the "getacl" command.
     * @param path
     * @param inherited
     * @throws IOException 
     */
    public String getAcl(String path, boolean inherited)
        throws RESTException
    {
        Get req = newGet("getacl", path);
        req.setParameter("inherited", Boolean.toString(inherited));
        return req.sendAndCheck().getString();
    }

    /** 
     * Adds ACL rules to the current XML Library
     * Wraps the "setacl" command.
     * @param data
     * @throws RESTException
     */
    public void setAcl(String data)
        throws RESTException
    {
        Post req = newPost("setacl", null);
        req.setParameter("acl", data);
        Response resp = req.sendAndCheck();
        resp.getSingleLine();   // required (thanks Apache)
    }

    /**
     * Returns a table of statistics from the server.
     * @param level "admin" or "expert"
     * @return a table with columns "Id", "Type", "Value", "Family".
     *          See the REST API documentation for more details.
     * @throws RESTException
     */
    public TabularData getStats(String level)
        throws RESTException
    {
        Get req = newGet("getstats", null);
        if (level != null)
            req.setParameter("level", level);
        Response resp = req.sendAndCheck();
        TabularData data = new TabularData("Statistics", STATS_FIELDS);
        int row = 0;
        for (String line : resp.getLines(true)) {
            if (line.length() == 0)
                continue;
            String[] fields = line.split("\t");
            int column = 0;
            for (String field : fields) {
                if (column >= fields.length)
                    break;
                data.setValueAt(field, row, column++);
            }
            ++row;
        }
        return data;
    }

    /**
     * Returns a table of configuration properties.
     * @param expert false for basic/admin configuration, true for most detailed
     *        configuration
     * @return a table with columns "Name", "Category", "Level", "Type", "Value",
     *         "DefaultValue", "Description".
     *          See the REST API documentation for more details.
     * @throws RESTException
     */
    public TabularData getConfiguration(boolean expert)
        throws RESTException
    {
        Get req = newGet("getconfig", null);
        if(expert)
            req.setParameter("level", "expert");
        Response resp = req.sendAndCheck();
        
        TabularData data = new TabularData("Configuration", CONFIGURATION_FIELDS);
        int row = 0;
        for(String line : resp.getLines(true)) {
            String[] fields = line.split("\t");
            int column = 0;
            for(String field : fields)
                data.setValueAt(field, row, column++);
            ++ row;
        }
        return data;
    }

    /**
     * Changes a set of configuration properties.
     * @param properties a map of properties with their value
     * @return true if at least one property was actually changed. Unknown
     *         properties are ignored.
     * @throws RESTException
     */
    public boolean changeConfiguration(Properties properties)
        throws RESTException
    {
        Post req = newPost("changeconfig", null);
        int rank = 0;
        for(Enumeration<Object> e = properties.keys(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            req.setParameter("property" + rank, key);
            req.setParameter("value" + rank, properties.getProperty(key));            
            ++ rank;
        }
        Response resp = req.sendAndCheck();
        return "true".equals(resp.getSingleLine());
    }

    /**
     * Returns a table of queries currently executing in one of the XML Libraries 
     * managed by the server.
     * @return a table with columns "Id", "User", "Elapsed Time", "Source".
     *          See the REST API documentation for more details.
     * @throws RESTException
     */
    public TabularData listRunningQueries()
        throws RESTException
    {
        Get req = newGet("listqueries", null);
        Response resp = req.sendAndCheck();
        
        TabularData data = new TabularData("Running Queries", RUNNING_QUERIES_FIELDS);
        int row = 0;
        for(String line : resp.getLines(true)) {
            String[] fields = line.split("\t");
            int column = 0;
            for(String field : fields)
                data.setValueAt(field, row, column++);
            ++ row;
        }
        return data;
    }

    /**
     * Cancels a currently executing XQuery.
     * @param exprId unique identifier of the query, as returned by
     *        {@link #listRunningQueries()}
     * @return a String containing "OK" if the query was found and cancelled,
     *         "idle" : if the query was found but not executing, and "unknown" if
     *         the query was not found.
     * @throws RESTException
     */
    public String cancelRunningQuery(String exprId)
        throws RESTException
    {
        Post req = newPost("cancelquery", null);
        req.setParameter("xid", exprId);
        Response resp = req.sendAndCheck();
        return resp.getSingleLine(); 
    }

    /**
     * Gets a list of background tasks
     * @param timeline if 0, return only currently running tasks, otherwise return
     *        tasks that started within this number of hours in the past. For
     *        example timeline=24 returns all tasks that started in the past 24
     *        hours.
     * @return a table with columns "Type", "Database", "Start Time",
     *         "Finish Time", "Duration", "Progress"
     */
    public TabularData listMaintenanceTasks(int timeline)
        throws RESTException
    {
        Get req = newGet("listtasks", null);
        req.setParameter("timeline", "" + timeline);
        Response resp = req.sendAndCheck();

        TabularData data = new TabularData("Background Tasks", TASKS_FIELDS);
        int row = 0;
        for(String line : resp.getLines(true)) {
            String[] fields = line.split("\t");
            int column = 0;
            for(String field : fields)
                data.setValueAt(field, row, column++);
            ++ row;
        }
        return data;
    }

    // -----------------------------------------------------------------------

    @Override
    public Get newGet(String name, String path)
    {
        Get req = super.newGet(name, path);
        if (libName != null)
            req.setParameter("library", libName);
        return req;
    }
    
    @Override
    public Put newPut(String name, String path)
    {
        Put req = super.newPut(name, path);
        if (libName != null)
            req.setParameter("library", libName);
        return req;
    }

    @Override
    public Post newPost(String name, String path)
    {
        Post req = super.newPost(name, path);
        if (libName != null)
            req.setParameter("library", libName);
        return req;
    }
    
    protected Post newMultipartPost(String name, String path)
    {
        Post req = newPost(name, path);
        try {
            if (libName != null)
                req.putStringPart("library", libName);
        }
        catch (RESTException e) {
            ;
        }
        return req;
    }

    private static EvaluationException wrap(DataModelException e)
    {
        return new EvaluationException(e.getMessage(), e);
    }

    // -----------------------------------------------------------------------

    static class ItemImpl
        implements Item
    {
        Node curItem;
        ItemType type; // on demand
        String value;
    
        public ItemImpl(Node item)
        {
            curItem = item;
        }
    
        public ItemType getType()
            throws EvaluationException
        {
            if (type == null && curItem != null) {
                try {
                    Node typeAttr = curItem.getAttribute(TYPE_ATTR);
                    if (typeAttr != null)
                        type = XQType.findItemType(typeAttr.getStringValue());
                }
                catch (DataModelException e) {
                    throw wrap(e);
                }
            }
            return type;
        }
    
        public boolean getBoolean()
            throws EvaluationException
        {
            return Conversion.toBoolean(getString());
        }
    
        public float getFloat()
            throws EvaluationException
        {
            return Conversion.toFloat(getString());
        }
    
        public double getDouble()
            throws EvaluationException
        {
            return Conversion.toDouble(getString());
        }
    
        public long getInteger()
            throws EvaluationException
        {
            return Conversion.toInteger(getString());
        }
    
        public BigDecimal getDecimal()
            throws EvaluationException
        {
            return Conversion.toDecimal(getString(), true);
        }
    
        public QName getQName()
            throws EvaluationException
        {
            try {
                String pname = curItem.getString();
                String prefix = IQName.extractPrefix(pname);
                String ncname = IQName.extractLocalName(pname);
                if (prefix.length() == 0)
                    return XQName.get("", ncname, "");

                // NS resolution:
                String uri = null;
                if (curItem != null)  // NS in-scope for the current node 
                    uri = curItem.getNamespaceUri(prefix);
                if (uri == null)
                    throw new EvaluationException("cannot resolve QName " + pname);
                return XQName.get(uri, ncname, prefix);
            }
            catch (DataModelException e) {
                throw wrap(e);
            }
        }

        public String getString()
            throws EvaluationException
        {
            try {
                return curItem.getStringValue();
            }
            catch (DataModelException e) {
                throw wrap(e);
            }
        }
    
        public Object getObject()
            throws QizxException
        {
            return getString();
        }

        public boolean isNode()
        {
            try {
                getType();
                if (type instanceof NodeType)
                    return true;
            }
            catch (EvaluationException e) { ; }
            return false;
        }
    
        public Node getNode()
            throws EvaluationException
        {
            try {
                return curItem.getFirstChild();
            }
            catch (DataModelException e) {
                throw wrap(e);
            }
        }
    
        public XMLPullStream exportNode()
            throws EvaluationException
        {
            return new NodePullStream(getNode());
        }
    
        public void export(XMLPushStream writer)
            throws QizxException
        {
            writer.putNodeCopy(getNode(), 0);
        }
    }


    static class SeqImpl extends ItemImpl
        implements ItemSequence
    {
        private long count;
        private long position;
        private boolean atFirst;
        private List<Profiling> profiling;
    
        public SeqImpl(Node root, boolean profiling)
            throws EvaluationException
        {
            super(null);
            try {
                root = root.getFirstChild();
                if (root != null) {
                    // get count
                    Node cntAttr = root.getAttribute(T_COUNT);
                    if (cntAttr == null)
                        cntAttr = root.getAttribute(E_COUNT);
                    if (cntAttr != null)
                        count = Long.parseLong(cntAttr.getStringValue());
                    
                    curItem = root.getFirstChild();
                    
                    if (profiling)
                        parseProfiling(curItem);
                    atFirst = true;
                }
            }
            catch (DataModelException e) {
                throw wrap(e);
            }
        }
    
        protected void setNodeImpl(Node node)
            throws DataModelException
        {
            curItem = node;
            type = null;
            value = null;
        }
    
        public boolean moveToNextItem()
            throws EvaluationException
        {
            try {
                if (curItem == null || curItem.getNodeName() == PROFILING)
                    return false;

                if (!atFirst) {
                    curItem = curItem.getNextSibling();
                    if (curItem == null || curItem.getNodeName() == PROFILING)
                        return false;
                }
                atFirst = false;
                setNodeImpl(curItem);
                ++ position;
                return true;
            }
            catch (DataModelException e) {
                throw wrap(e);
            }
        }

        public Item getCurrentItem()
        {
            return new ItemImpl(curItem);
        }
    
        public long countItems()
            throws EvaluationException
        {
            return count;
        }
    
        public long estimatedDocumentCount()
        {
            return count;
        }
    
        public long estimatedDocumentCount(int minimalPosition)
            throws EvaluationException
        {
            return count;
        }
    
        public int skip(int count)
            throws EvaluationException
        {
            long start = position;
            for(; --count >= 0; ) {
                if(!moveToNextItem())
                    break;
            }
            return (int) (position - start);
        }

        public void moveTo(long position)
            throws EvaluationException
        {
            // TODO
        }
    
        public long getPosition()
        {
            return position;
        }
    
        public Expression getExpression()
        {
            return null;
        }

        public double getFulltextScore()
            throws EvaluationException
        {
            return 0;
        }
    
        public void close()
        {
            curItem = null;
        }

        private void parseProfiling(Node first)
            throws DataModelException
        {
            profiling = new ArrayList<Profiling>();
            Node container = first;
            for( ; container != null; container = container.getNextSibling())
                if(container.getNodeName() == PROFILING)
                    break;
            if(container == null)
                return; // should not happen
            Node item = container.getFirstChild();
            for(; item != null; item = item.getNextSibling())
                if(item.getNodeName() != null) {
                    String type = getAttr(item, "type");
                    String cnt = getAttr(item, "count");
                    int count = cnt == null? -1 : Integer.parseInt(cnt);
                    int start = Integer.parseInt(getAttr(item, "start"));
                    int end = Integer.parseInt(getAttr(item, "end"));
                    String message = item.getStringValue();
                    profiling.add(new Profiling(type, count, start, end, message));
                    
                }
        }

        private String getAttr(Node item, String name) throws DataModelException
        {
            Node attr = item.getAttribute(IQName.get(name));
            return attr == null? null : attr.getStringValue();
        }

        public List<Profiling> getProfilingAnnotations()
        {
            return profiling;
        }
    }
    
    class MemberImpl implements LibraryMember
    {
        protected String path;
        protected Map<String, Property> props;

        public MemberImpl(String path)
        {
            this.path = path;
        }

        public MemberImpl(Map<String, Property> map)
        {
            props = map;
            Property pathProp = props.get("path");
            if(pathProp == null)
                throw new RuntimeException("missing 'path' property");
            this.path = pathProp.value;
        }

        public boolean exists() {
            return true; // by construction
        }

        public boolean isCollection()
        {
            return false;
        }

        public boolean isDocument()
        {
            return false;
        }

        public String getPath()
        {
            return path;
        }

        public String getName()
        {
            return PathUtil.getBaseName(path);
        }

        public String getParentPath()
        {
            return PathUtil.getParentPath(path);
        }

        public Collection getParent()
            throws DataModelException
        {
            throw new DataModelException("not supported");
        }


        public Library getLibrary() {
            return null;
        }

        public boolean contains(LibraryMember other)
        {
            return PathUtil.contains(path, false, other.getPath());
        }

        public boolean lock(int timeoutMillis)
            throws DataModelException
        {
            throw new DataModelException("not supported");
        }

        public void copyTo(String newPath)
            throws DataModelException
        {
            throw new DataModelException("not supported");
        }

        public void renameTo(String newPath)
            throws DataModelException
        {
            throw new DataModelException("not supported");
        }

        public void delete()
            throws DataModelException
        {
            throw new DataModelException("not supported");
        }

        public String[] getPropertyNames()
            throws DataModelException
        {
            haveProps();
            return null; // TODO
        }

        public boolean hasProperty(String propName)
            throws DataModelException
        {
            haveProps();
            return props.get(propName) != null;
        }

        public Object getProperty(String propName)
            throws DataModelException
        {
            haveProps();
            Property prop = props.get(propName);
            return prop == null? null : prop.value;
        }

        public long getIntegerProperty(String propName)
            throws DataModelException
        {
            String v = (String) getProperty(propName);
            if (v == null)
                return -1;
            return Long.parseLong(v);
        }

        public Object setProperty(String propName, Object propValue)
            throws DataModelException
        {
            throw new DataModelException("not supported");
        }

        public Object setIntegerProperty(String propName, long propValue)
            throws DataModelException
        {
            throw new DataModelException("not supported");
        }

        public Object removeProperty(String propName)
            throws DataModelException
        {
            throw new DataModelException("not supported");
        }

        private void haveProps() throws DataModelException
        {
            if(props == null)
                throw new DataModelException("not available");
             // TODO
        }        
    }
    
    class DocImpl extends MemberImpl
    {
        public DocImpl(Map<String, Property> map)
        {
            super(map);
        }
        public DocImpl(String path)
        {
            super(path);
        }

        @Override
        public boolean isDocument()
        {
            return true;
        }
    }

    class CollecImpl extends MemberImpl
    {
        public CollecImpl(Map<String, Property> map)
        {
            super(map);
        }
        public CollecImpl(String path)
        {
            super(path);
        }

        @Override
        public boolean isCollection()
        {
            return true;
        }
    }
    
    // created from getprop
    class MemberIterImpl implements LibraryMemberIterator
    {
        Node curMemb;   // element containing properties
        
        public MemberIterImpl(Node firstNode)
        {
            curMemb = firstNode;
        }

        public boolean moveToNextMember()
        {
            try {
                Node next = curMemb == null? null : curMemb.getFirstChild();
                if(next == null)
                    return false;
                curMemb = next;
                return true;
            }
            catch (DataModelException e) {
                return false;
            }
        }

        public LibraryMember getCurrentMember()
        {
            try {
                return new MemberImpl(getProperties(curMemb, null));
            }
            catch (DataModelException e) {
                return null;
            }
        }

        public LibraryMemberIterator reborn()
        {
            try {
                Node n = curMemb == null? null : curMemb.getParent();
                n = (n == null)? null : n.getFirstChild();
                return (n == null)? null : new MemberIterImpl(n);
            }
            catch (DataModelException e) {
                return null;
            }
        }
    }

    // created from get
    class CollectionIterImpl implements LibraryMemberIterator
    {
        ArrayList<MemberImpl> members;
        int memberIndex = -1;
        
        public CollectionIterImpl(ArrayList<MemberImpl> members)
        {
            this.members = members;
        }

        public boolean moveToNextMember()
        {
            return (++memberIndex < members.size());
        }

        public LibraryMember getCurrentMember()
        {
            return (memberIndex >= 0 && memberIndex < members.size())?
                    members.get(memberIndex) : null;
        }

        public LibraryMemberIterator reborn()
        {
            return new CollectionIterImpl(members);
        }
    }
}
