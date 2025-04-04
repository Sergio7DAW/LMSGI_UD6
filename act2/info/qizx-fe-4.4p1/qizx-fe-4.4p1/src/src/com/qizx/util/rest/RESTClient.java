/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.util.rest;

import com.qizx.api.DataModelException;
import com.qizx.api.Node;
import com.qizx.util.basic.FileUtil;
import com.qizx.util.io.ByteInput;
import com.qizx.util.io.ByteInputStream;
import com.qizx.xdm.DocumentParser;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A generic HTTP REST client.
 * <p>
 * Caveat: based on Apache HTT Client 4.1.x , which requires response stream
 * to BE ENTIRELY CONSUMED up to the last byte, otherwise the send() methods
 * will just hang forever! A definite design bug.
 */
public class RESTClient
{
    public static final String DEFAULT_CHAR_ENCODING = "UTF-8";
    public static final String PATH = "path";
    public static final String ERROR_TYPE = "text/x-qizx-error";
    private static final String OPER = "op";
    // Sun makes a confusion between charset and encoding... :
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private String baseURL;
    private AbstractHttpClient connection;
    private String hostName;
    private int hostPort;
    protected int curParamRank;
    protected static ThreadSafeClientConnManager sharedConnMan;
    
    static {
        sharedConnMan = new ThreadSafeClientConnManager();
        sharedConnMan.setDefaultMaxPerRoute(5);
    }
    
    public RESTClient(String URL) throws MalformedURLException
    {
        connection = new DefaultHttpClient(sharedConnMan);
        URL uri = new URL(URL);
        String protocol = uri.getProtocol();
        if (!"http".equals(protocol) && !"https".equals(protocol))
            throw new MalformedURLException("invalid protocol: " + protocol
                                            + ", expect 'http:'");
        baseURL = uri.toString();
        hostName = uri.getHost();
        hostPort = uri.getPort();
    }

    /**
     * Returns the URL of the server.
     */
    public String getBaseURL()
    {
        return baseURL; 
    }

    /**
     * Defines user credentials for Basic and Digest authentication schemes.
     * @param username
     * @param password
     */
    public void setCredentials(String username, String password)
    {
        CredentialsProvider creds = connection.getCredentialsProvider();
        creds.setCredentials(new AuthScope(hostName, hostPort),
                             new UsernamePasswordCredentials(username,
                                                             password));
    }

    /**
     * Creates a raw Get request
     */
    public Get newGet()
    {
        Get req = new Get(null);
        return req;
    }

    /**
     * Internal.
     */
    public Get newGet(String name, String path)
    {
        Get req = new Get(name);
        req.setParameter(OPER, name);
        if (path != null)
            req.setParameter(PATH, path);
        return req;
    }

    /**
     * Internal.
     */
    public Put newPut()
    {
        Put put = new Put(null);
        return put;
    }
    
    /**
     * Internal.
     */
    public Put newPut(String name, String path)
    {
        Put req = new Put(null);
        req.setParameter(OPER, name);
        if (path != null)
            req.setParameter(PATH, path);
        return req;
    }
    
    /**
     * Internal.
     */
    public Post newPost()
    {
        Post post = new Post(null);
        return post;
    }
    
    /**
     * Internal.
     */
    public Post newPost(String name, String path)
    {
        Post post = new Post(name);
        if (path != null)
            post.setParameter(PATH, path);
        return post;
    }

    protected void resetRank()
    {
        curParamRank = 1;
    }
    
    protected String ranked(String name)
    {
        String rname = (curParamRank <= 1) ? name : (name + curParamRank);
        return rname;
    }

    protected String nameList(String[] names)
    {
        StringBuilder buf = new StringBuilder(names.length * 8);
        for (int i = 0; i < names.length; i++) {
            buf.append(names[i]);
        }
        return buf.toString();
    }

    protected static RESTException wrapped(Exception e)
    {
        return new RESTException(e.getMessage(), e);
    }

    public abstract class Request
    {
        String opName;
        String charEncoding;
        HashMap<String, String[]> headers;
        ArrayList<BasicNameValuePair> parameters;

        public Request(String name)
        {
            this.opName = name;
            parameters = new ArrayList<BasicNameValuePair>();
        }

        /**
         * Specifies the character encoding used to escape accented characters in
         * " <tt>application/x-www-form-urlencoded</tt>".
         * @param encoding a character encoding: "<tt>ISO-8859-1</tt>" or "
         *        <tt>UTF-8</tt>". Specifying <code>null</code> is equivalent to
         *        resetting the encoding to the default value.
         * @see #getCharacterEncoding
         */
        public void setCharacterEncoding(String encoding)
        {
            if (encoding == null) {
                encoding = DEFAULT_CHAR_ENCODING;
            }
            charEncoding = encoding;
        }

        /**
         * Returns the character encoding used to escape accented characters in "
         * <tt>application/x-www-form-urlencoded</tt>".
         * @see #setCharacterEncoding
         */
        public String getCharacterEncoding()
        {
            return charEncoding;
        }

        /**
         * Replaces the previous values, if any, of specified header by specified
         * value.
         * @param name name of the header
         * @param value value of the header
         * @see #addHeader
         */
        public void setHeader(String name, String value)
        {
            if (headers == null)
                headers = new HashMap<String, String[]>();
            headers.put(name, new String[] {
                value
            });
        }

        /**
         * Add specified header. Note that the same header may be added several
         * times with different values. That is, adding a header does overwrite
         * its previous value, instead values accumulate.
         * @param name name of the header
         * @param value value of the header
         * @see #setHeader
         */
        public void addHeader(String name, String value)
        {
            if (headers == null)
                headers = new HashMap<String, String[]>();
            String[] values = headers.get(name);
            if (values == null) {
                values = new String[] {
                    value
                };
            }
            else {
                int count = values.length;
                String[] values2 = new String[count + 1];
                System.arraycopy(values, 0, values2, 0, count);
                values2[count] = value;
                values = values2;
            }
            headers.put(name, values);
        }

        /**
         * Return the names of all headers added using {@link #addHeader} or
         * {@link #setHeader}.
         * @see #addHeader
         */
        public String[] getHeaderNames()
        {
            if (headers == null)
                return new String[0];
            String[] names = new String[headers.size()];
            return headers.keySet().toArray(names);
        }

        /**
         * Return the values of specified header. Returns <code>null</code> if
         * specified header is absent.
         * @see #addHeader
         */
        public String[] getHeader(String name)
        {
            if (headers == null)
                return null;
            return (String[]) headers.get(name);
        }

        public void setParameter(String name, String value)
        {
            BasicNameValuePair pair = new BasicNameValuePair(name, value);
            int p = 0, pcnt = parameters.size();
            for (; p < pcnt; p++) {
                if (name.equals(parameters.get(p).getName())) {
                    parameters.set(p, pair);
                    return;
                }
            }
            parameters.add(pair);
        }

        public void setParameter(String name, long value)
        {
            setParameter(name, Long.toString(value));
        }

        public String getParameter(String name)
        {
            for (int p = 0, pcnt = parameters.size(); p < pcnt; p++) {
                BasicNameValuePair pair = parameters.get(p);
                if (name.equals(pair.getName()))
                    return pair.getValue();
            }
            return null;
        }

        public void appendParameter(String name, String value)
        {
            String pv = getParameter(name);
            if (pv == null)
                setParameter(name, value);
            else
                setParameter(name, pv + value);
        }

        protected void prepareHeaders(HttpUriRequest req)
        {
            if (headers == null)
                return;
            for (Map.Entry<String, String[]> e : headers.entrySet()) {
                String name = e.getKey();
                String[] values = e.getValue();
                for (int i = 0; i < values.length; ++i) {
                    req.addHeader(name, values[i]);
                }
            }
        }

        /**
         * Sends the request and returns the Response. 
         */
        abstract Response send()
            throws RESTException;

        /**
         * Sends the request and checks the response. If an error is returned,
         * then this methods throws a RESTException with the error message.
         * @throws RESTException
         */
        public Response sendAndCheck()
            throws RESTException
        {
            Response resp = send();
            if(resp.isError()) {
                String reason = resp.getErrorString();
                throw new RESTException(reason);
            }
            return resp;
        }
    }


    /**
     * a Get request.
     * <p>Parameters are passed through the URL.
     */
    public class Get extends Request
    {
        HttpGet req;

        public Get(String name)
        {
            super(name);
        }

        public Response send()
            throws RESTException
        {
            // Apache HTTPClient is crappy!
            String query = URLEncodedUtils.format(parameters, charEncoding);
            req = new HttpGet(baseURL + "?" + query);
            prepareHeaders(req);
            try {
                
                HttpResponse resp = connection.execute(req);
                return new Response(resp, this);
            }
            catch (Exception e) {
                throw wrapped(e);
            }
        }
    }

    public class Put extends Request
    {
        HttpPut req;
        private InputStream content;

        public Put(String name)
        {
            super(name);
        }

        public void setContent(InputStream content)
        {
            this.content = content;
        }
        
        @Override
        public Response send()
            throws RESTException
        {
            String query = URLEncodedUtils.format(parameters, charEncoding);
            req = new HttpPut(baseURL + "?" + query);
            prepareHeaders(req);
            req.setEntity(new InputStreamEntity(content, -1));
            
            try {
                
                HttpResponse resp = connection.execute(req);
                return new Response(resp, this);
            }
            catch (Exception e) {
                throw wrapped(e);
            }
        }
    }
    
    /**
     * A POST request, multipart or url-encoded.
     * <p>
     * If only {@link #setParameter(String, String)} is used, then the request is url-encoded.
     * <p>
     * If the put*Part methods are used, the request uses multipart encoding.
     */
    public class Post extends Request
    {
        HttpPost req;
        MultipartEntity multipart;

        public Post(String name)
        {
            super(name);
        }

        /**
         * Send simple POST, not multipart.
         */
        public Response send()
            throws RESTException
        {
            req = new HttpPost(baseURL);
            prepareHeaders(req);
            try {
                if (multipart != null) {
                    req.setEntity(multipart);
                }
                else {
                    setParameter("op", opName);
                    UrlEncodedFormEntity ent = new UrlEncodedFormEntity(parameters);
                    
                    req.setEntity(ent);
                }

                HttpResponse resp = connection.execute(req);
                return new Response(resp, this);
            }
            catch (Exception e) {
                throw wrapped(e);
            }
        }

        public void putStringPart(String name, String value)
            throws RESTException
        {
            try {
                ensureMultipart();
                StringBody body = new StringBody(value, UTF8);
                multipart.addPart(new FormBodyPart(name, body));
            }
            catch (IOException e) {
                throw new RESTException("creating Text part: "
                                        + e.getMessage(), e);
            }
        }

        public void putFilePart(String name, File file, String contentType)
            throws RESTException
        {
            try {
                
                ensureMultipart();
                if (contentType == null)
                    contentType = "application/octet-stream";
                multipart.addPart(new FormBodyPart(name,
                                           new FileBody(file, contentType, "UTF-8")));
            }
            catch (Exception e) {
                throw new RESTException("creating File part: "
                                        + e.getMessage(), e);
            }
        }

        public void putBytePart(String name, byte[] value)
            throws RESTException
        {
            try {
                ensureMultipart();
                multipart.addPart(new FormBodyPart(name,
                                             new ByteArrayBody(value, name)));
            }
            catch (IOException e) {
                throw new RESTException("creating Text part: "
                                        + e.getMessage(), e);
            }
        }
        private void ensureMultipart()
            throws UnsupportedEncodingException
        {
            if (multipart != null)
                return;
            multipart = new MultipartEntity();
            if(opName != null)
                multipart.addPart("op", new StringBody(opName));
            String path = getParameter(PATH);
            if (path != null)
                multipart.addPart("path", new StringBody(path));
        }
    }


    /**
     * Wraps a response from the server.
     * <p>The contents can be tested with {@link #getContentType()} and
     *  {@link #getContentEncoding}.
     *  <p>Contents can be obtained as byte stream, char stream, or parsed Node
     *  through specialized methods.
     */
    public static class Response
    {
        private HttpResponse resp;
        private HttpEntity entity;
        private Request request;
        
        private InputStream stream;
        private ByteInputStream byteInput;
        private BufferedReader reader;
        private Node rootNode;      // root of XML result
        private Node curNode;   // current node of nextNode()

        public Response(HttpResponse resp, Request request)
        {
            this.resp = resp;
            this.entity = resp.getEntity();
            this.request = request;
        }

        /**
         * Return the names of all headers.
         */
        public String[] getHeaderNames()
        {
            Header[] h = resp.getAllHeaders();
            String[] names = new String[h.length];
            for (int i = h.length; --i >= 0;)
                names[i] = h[i].getName();
            return names;
        }

        /**
         * Return the values of specified header. Returns <code>null</code> if
         * specified header is absent.
         */
        public String getHeader(String name)
        {
            Header h = resp.getFirstHeader(name);
            return (h == null) ? null : h.getValue();
        }

        /**
         * Returns the value of the "Content-type" header.
         */
        public String getContentType()
        {
            return getHeader("content-type");
        }

        /**
         * Returns the name of the encoding used in the response.
         */
        public String getContentEncoding()
        {
            Header hdr = entity.getContentEncoding();
            return hdr == null ? DEFAULT_CHAR_ENCODING : hdr.getValue();
        }

        /**
         * Returns the HTTP error code.
         * <p>Caution: non-HTTP errors returned by a Qizx Server use a code 200 OK,
         * and a content-type "text/x-qizx-error". This is for dealing with 
         * broken clients that do not handles the HTTP codes correctly.
         * <p>Error codes over 400 represent an actual HTTP error (such as
         * authentication, or severe server error).
         * @see isError
         */
        public int getStatusCode()
        {
            return resp.getStatusLine().getStatusCode();
        }

        /**
         * Returns true if the request returned an error.
         * <p>This method takes into account the particular way errors are
         * returned by a Qizx Server: non-HTTP errors returned use a code 200 OK,
         * and a content-type "text/x-qizx-error". This is for dealing with 
         * broken clients that do not handles the HTTP codes correctly.
         */
        public boolean isError()
        {
            checkState();
            int code = getStatusCode();
            if (code >= 400)
                return true;
            String ctype = getContentType();
            return ctype != null && ctype.startsWith(ERROR_TYPE);
        }

        /**
         * Returns a String describing a REST error (it is assumed the isError()
         * method return true)
         * @throws RESTException
         */
        public String getErrorString()
            throws RESTException
        {
            checkState();
            int code = getStatusCode();
            if (code >= 400)
                return resp.getStatusLine().getReasonPhrase();

            return getString();
        }

        /**
         * Returns the response contents as a single String.
         * @throws IOException
         */
        public String getString()
            throws RESTException
        {
            checkState();
            InputStream in = getStream();
            try {
                try {
                    return FileUtil.loadString(in, getContentEncoding());
           	    }
            	finally {
                	in.close();
            	}
        	}
            catch (IOException e) {
                throw wrapped(e);
            }
        }

        /**
         * Returns the response contents as a byte Stream.
         * @throws RESTException
         */
        public InputStream getStream()
            throws RESTException
        {
            try {
            	if (stream == null)
                	stream = entity.getContent();
            }
            catch (Exception e) {
                throw wrapped(e);
            }
            return stream;
        }

        /**
         * Returns the response contents as a byte Stream.
         */
        public ByteInputStream getByteInputStream()
            throws RESTException
        {
            if (byteInput == null)
                byteInput = new ByteInputStream(getStream());
            return byteInput;
        }

        /**
         * Returns the response contents as a character stream. Uses the
         * encoding returned by the response if any.
         * @throws IOException
         */
        private BufferedReader getReader()
            throws RESTException
        {
            if (reader == null) {
                try {
                InputStreamReader ins = new InputStreamReader(getStream(),
                                                           getContentEncoding());
                reader = new BufferedReader(ins);
            }
                catch (UnsupportedEncodingException e) {
                    throw wrapped(e);
                }
            }
            return reader;
        }
        
        /**
         * Consumes the contents of the response to the last byte.
         * This may be necessary, depending on the underlying HTTP library.
         */
        public void consumeContent()
        {
            try {
                getReader().close();
            }
            catch (Exception e) {
                System.err.println("consumeContent: " + e);
            }
        }
        
        /**
         * Get result as a list of text lines. Iterate on each line.
         */
        public String getNextLine()
            throws RESTException
        {
            try {
                return getReader().readLine();
            }
            catch (IOException e) {
                throw wrapped(e);
            }
        }

        /**
         * Returns the first line (or the next line) and consumes the rest of the
         *  response.
         * @throws RESTException
         */
        public String getSingleLine()
            throws RESTException
        {
            String line = getNextLine();
            consumeContent();
            return line;
        }
        
        /**
         * Returns the response contents as a list of text lines.
         * @throws RESTException
         */
        public String[] getLines()
            throws RESTException
        {
            return getLines(false);
        }
        
        /**
         * Returns the response contents as a list of text lines.
         * @throws RESTException
         */
        public String[] getLines(boolean skipFirst)
            throws RESTException
        {
            ArrayList<String> lines = new ArrayList<String>();
            try {
                BufferedReader in = getReader();
                String aline = in.readLine();
                for (; aline != null;) {
                    if (!skipFirst)
                        lines.add(aline);
                    skipFirst = false;
                    aline = in.readLine();
                }
                return lines.toArray(new String[lines.size()]);
            }
            catch (Exception e) {
                throw wrapped(e);
            }
        }

        /**
         * Returns the response contents as a parsed XML node, represented
         * according to the XPath/XQuery Data Model.
         * <p>Assumes that the response contains one well-formed document or XML fragment.
         * @throws DataModelException
         */
        public Node getNode()
            throws DataModelException
        {
            if (rootNode == null)
                try {
                    rootNode = DocumentParser.parse(new InputSource(getStream()));
                }
                catch (Exception e) {
                    throw new DataModelException(e.getMessage(), e);
                }
            return rootNode;
        }

        /**
         * Returns the first "item" of the response as a parsed XML node, represented
         * according to the XPath/XQuery Data Model.
         * <p>Assumes that the response is a list of XQuery Items, each wrapped in an
         * element describing its type.
         * @throws DataModelException
         */
        public Node firstItemNode()
            throws DataModelException
        {
            getNode();
            if (rootNode == null)
                return null;
            curNode = rootNode.getFirstChild();
            if (curNode != null)
                curNode = curNode.getFirstChild();

            return curNode;
        }

        private void checkState()
        {
            // TODO
        }
    }

//    public static void main(String[] args)
//    {
//        try {
//            RESTClient cli = new RESTClient("http://localhost:8080//qizx/api");
//            cli.setCredentials("admin", "nimda");
//
//            Get get = cli.newGet("get", "/my/doc.xml");
//            get.setParameter("library", "xlib");
//            Response resp = get.send();
//            String enc = resp.getContentEncoding();
//            String ctype = resp.getContentType();
//            System.err.println("response " + ctype + " " + enc + "\n"
//                               + resp.getString());
//
//            Post post = cli.newPost("put", null);
//            for(int i = 1; i <= 100; i++) {
//                post.putStringPart(post.ranked("path", i), "/my/doc" + i + ".xml");
//                post.putStringPart(post.ranked("data", i), 
//                                   "<doc id='" + i + "'>blabla</doc>");
//            }
//            File[] files = new File("/home/xml/shake").listFiles();
//            cli.resetRank();
//            for (int i = 0; i < files.length; i++) {
//                post.putStringPart(cli.ranked("path"), "/shake/play"
//                                                               + i + ".xml");
//                post.putFilePart(cli.ranked("data"), files[i], null);
//            }
//            resp = post.send();
//            System.err.println("put: " + resp.getString());
//            
//            post = cli.newPost("mklib", null);
//            post.setParameter("name", "lili");
//            resp = post.sendAndCheck();
//            System.err.println("put: " + resp.getString());
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
