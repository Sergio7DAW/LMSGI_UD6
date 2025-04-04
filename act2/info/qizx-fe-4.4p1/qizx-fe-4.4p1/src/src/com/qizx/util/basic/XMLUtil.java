/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of Qizx XQuery engine
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
/*
 * Copyright (c) 2001-2003 Pixware. 
 *
 * Author: Hussein Shafie
 * Modified by Xavier Franc
 */
package com.qizx.util.basic;

import java.util.StringTokenizer;

/**
 * A collection of utility functions (static methods) related to XML.
 */
public class XMLUtil
{
    // supplemental character range:
    public static final int SUPPLEM_LB = 0x10000;
    public static final int SUPPLEM_UB = 0x10FFFF;
    // High surrogate range:
    private static final char HI_SURROG_LB = 0xd800;   // lower bound
    private static final char HI_SURROG_UB = 0xdbff;   // upper bound
    private static final char LO_SURROG_LB = 0xdc00;
    private static final char LO_SURROG_UB = 0xdf00;
    private static final char SURROG_LB = HI_SURROG_LB; // lower bound
    private static final char SURROG_UB = LO_SURROG_UB; // upper bound

    /**
     * Tests if specified string is a lexically correct target for a process
     * instruction.
     * <p>
     * Note that Names starting with "<tt>xml</tt>" (case-insensitive) are
     * rejected.
     * 
     * @param s string to be tested
     * @return <code>true</code> if test is successful; <code>false</code>
     *         otherwise
     */
    public static final boolean isPITarget(String s)
    {
        if (s == null || s.length() == 0)
            return false;
        return (isName(s) &&
                !s.regionMatches(/* ignoreCase */true, 0, "xml", 0, 3));
    }

    /**
     * Tests if specified string is a lexically correct NMTOKEN.
     * 
     * @param s string to be tested
     * @return <code>true</code> if test is successful; <code>false</code>
     *         otherwise
     */
    public static final boolean isNmtoken(String s)
    {
        int count;
        if (s == null || (count = s.length()) == 0)
            return false;

        for (int i = 0; i < count; ++i) {
            if (!isNameChar(s.charAt(i)))
                return false;
        }
        return true;
    }
    
    /**
     * Tests if specified string is a lexically correct NCName.
     * 
     * @param s string to be tested
     * @return <code>true</code> if test is successful; <code>false</code>
     * otherwise
     */
    public static final boolean isNCName(String s)
    {
        int count;
        if (s == null || (count = s.length()) == 0)
            return false;
        
        char c = s.charAt(0);
        if(!XMLUtil.isNameStartChar(c))
            return false;

        for (int i = 1; i < count; ++i) {
            c = s.charAt(i);
            if (!XMLUtil.isNameChar(c))
                return false;
        }
        return true;
    }
    
    /**
     * Tests if specified string is a lexically correct Name.
     * 
     * @param s string to be tested
     * @return <code>true</code> if test is successful; <code>false</code>
     *         otherwise
     */
    public static final boolean isName(String s)
    {
        int count;
        if (s == null || (count = s.length()) == 0)
            return false;

        int c = s.charAt(0);
        if(!XMLUtil.isNameStartChar(c) && c != ':')
            return false;

        for (int i = 1; i < count; ++i) {
            c = s.charAt(i);
            if (!XMLUtil.isNameChar(c) && c != ':')
                return false;
        }
        return true;
    }
    
    /**
     * Tests whether this is a NC name start character.
     */
    public static boolean isNameStartChar(int c)
    {
        if(c < 0x300) {
            return c >= 'A' && c <= 'Z'
                || c >= 'a' && c <= 'z'
                || c == '_'
                || c >= 0xC0 && c != 0xF7 && c != 0xD7;
        }
        return c >= 0x370 && c <= 0x37D
            || c >= 0x37F && c <= 0x1FFF
            || c >= 0x200C && c <= 0x200D
            || c >= 0x2070 && c <= 0x218F
            || c >= 0x2C00 && c <= 0x2EFF
            || c >= 0x3001 && c <= 0xD7FF
            || c >= 0xF900 && c <= 0xFDCF
            || c >= 0xFDF0 && c <= 0xFFFD
            || c >= 0x10000 && c <= 0xEFFFF;
    }

    /**
     * Tests whether this is a NC name start character.
     */
    public static boolean isNameChar(int c)
    {
        return isNameStartChar(c)
            || c == '-' || c == '.' || c == 0xB7
            || c >= '0' && c <= '9'
            || c >= 0x0300 && c <= 0x036F
            || c >= 0x203F && c <= 0x2040;
    }
    
    /**
     * Tests if specified character is a XML space (<tt>'\t'</tt>,
     * <tt>'\r'</tt>, <tt>'\n'</tt>, <tt>' '</tt>).
     * 
     * @param c character to be tested
     * @return <code>true</code> if test is successful; <code>false</code>
     *         otherwise
     */
    public static final boolean isXMLSpace(char c)
    {
        switch (c) {
        case ' ':
        case '\n':
        case '\r':
        case '\t':
            return true;
        default:
            return false;
        }
    }

    /**
     * Tests if all characters are XML space (<tt>'\t'</tt>, <tt>'\r'</tt>,
     * <tt>'\n'</tt>, <tt>' '</tt>).
     * 
     * @param s characters to be tested
     * @return <code>true</code> if test is successful; <code>false</code>
     *         otherwise
     */
    public static final boolean isXMLSpace(String s)
    {
        for (int c = s.length(); --c >= 0;)
            if (!isXMLSpace(s.charAt(c)))
                return false;
        return true;
    }

    /**
     * Tests if all characters are XML space (<tt>'\t'</tt>, <tt>'\r'</tt>,
     * <tt>'\n'</tt>, <tt>' '</tt>).
     * 
     * @param s characters to be tested
     * @return <code>true</code> if test is successful; <code>false</code>
     *         otherwise
     */
    public static final boolean isXMLSpace(char[] s, int start, int length)
    {
        for (int c = 0; c < length; c++)
            if (!isXMLSpace(s[c + start]))
                return false;
        return true;
    }
    
    /**
     * Tests if specified character is a character which can be contained in a
     * XML document.
     * 
     * @param c character to be tested
     * @return <code>true</code> if test is successful; <code>false</code>
     * otherwise
     */
    public static final boolean isXMLChar(int c)
    {
        // Char ::= #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] |
        // [#x10000-#x10FFFF]
        switch (c) {
        case 0x9:
        case 0xA:
        case 0xD:
            return true;
        default:
            if ((c >= 0x20 && c <= 0xD7FF) || (c >= 0xE000 && c <= 0xFFFD))
                return true;
        }

        return false;
    }
    
    /**
     * Tests whether the codepoint is a supplemental character (not in BMP).
     */
    public static boolean isSupplementalChar(int c)
    {
        return c >= SUPPLEM_LB && c <= SUPPLEM_UB;
    }

    /**
     * Tests whether the codepoint is a low or high surrogate code.
     */
    public static boolean isSurrogateChar(char c)
    {
        return c >= SURROG_LB && c <= SURROG_UB;
    }

    /**
     * Returns the high surrogate code of a supplemental character.
     */
    public static char highSurrogate(int c)
    {
        return (char) (((c - SUPPLEM_LB) >> 10) + HI_SURROG_LB);
    }

    /**
     * Returns the low surrogate code of a supplemental character.
     */
    public static char lowSurrogate(int c)
    {
        return (char) (((c - SUPPLEM_LB) & 0x3FF) + LO_SURROG_LB);
    }

    /**
     * Rebuilds a supplemental character codepoint from surrogate pair.
     * 
     * @param hiSurrog a valid hi surrogate code
     * @param loSurrog
     * @return supplemental character on 32 bits
     */
    public static int supplementalChar(char hiSurrog, char loSurrog)
    {
        return ((hiSurrog - HI_SURROG_LB) << 10) + (loSurrog - LO_SURROG_LB);
    }
    
    // -----------------------------------------------------------------------
    
    /**
     * Replaces successive XML space characters by a single space character (<tt>' '</tt>)
     * then removes leading and trailing space characters if any.
     * 
     * @param value string to be processed
     * @return processed string
     */
    public static final String collapseWhiteSpace(CharSequence value)
    {
        StringBuffer buffer = new StringBuffer();
        compressWhiteSpace(value, buffer);

        int last = buffer.length() - 1;
        if (last >= 0) {
            if (buffer.charAt(last) == ' ') {
                buffer.deleteCharAt(last);
                --last;
            }

            if (last >= 0 && buffer.charAt(0) == ' ')
                buffer.deleteCharAt(0);
        }

        return buffer.toString();
    }

    /**
     * Replaces successive XML space characters (<tt>'\t'</tt>,
     * <tt>'\r'</tt>, <tt>'\n'</tt>, <tt>' '</tt>) by a single space
     * character (<tt>' '</tt>).
     * 
     * @param value string to be processed
     * @return processed string
     */
    public static final String compressWhiteSpace(String value)
    {
        StringBuffer buffer = new StringBuffer();
        compressWhiteSpace(value, buffer);
        return buffer.toString();
    }

    /**
     * Replaces successive XML space characters (<tt>'\t'</tt>,
     * <tt>'\r'</tt>, <tt>'\n'</tt>, <tt>' '</tt>) by a single space
     * character (<tt>' '</tt>).
     * 
     * @param value string to be processed
     * @param buffer buffer used to store processed characters (characters are
     *        appended to this buffer)
     */
    private static void compressWhiteSpace(CharSequence value, StringBuffer buffer)
    {
        // No need to convert "\r\n" to a single '\n' because white spaces
        // are compressed.
        
        int length = value.length();
        char prevChar = '?';
        for (int i = 0; i < length; ++i) {
            char c = value.charAt(i);

            switch (c) {
            case '\t':
            case '\r':
            case '\n':
                c = ' ';
                break;
            }

            if (c == ' ') {
                if (prevChar != ' ') {
                    buffer.append(c);
                    prevChar = c;
                }
            }
            else {
                buffer.append(c);
                prevChar = c;
            }
        }
    }
    
    /**
     * Replaces sequence "<tt>\r\n</tt>" and characters <tt>'\t'</tt>,
     * <tt>'\r'</tt>, <tt>'\n'</tt> by a single space character <tt>' '</tt>.
     * 
     * @param value string to be processed
     * @return processed string
     */
    public static final String replaceWhiteSpace(String value)
    {
        StringBuffer buffer = new StringBuffer();

        int length = value.length();
        char prevChar = '?';
        for (int i = 0; i < length; ++i) {
            char c = value.charAt(i);
            switch (c) {
            case '\t':
            case '\r':
                buffer.append(' ');
                break;
            case '\n':
                // Equivalent to converting "\r\n" to a single '\n' then
                // converting '\n' to ' '.
                if (prevChar != '\r')
                    buffer.append(' ');
                break;
            default:
                buffer.append(c);
            }

            prevChar = c;
        }

        return buffer.toString();
    }
    
    /**
     * Splits specified string at XML space character boundaries (<tt>'\t'</tt>,
     * <tt>'\r'</tt>, <tt>'\n'</tt>, <tt>' '</tt>). Returns list of
     * parts.
     * 
     * @param s string to be split
     * @return list of parts
     */
    public static final String[] splitList(String s)
    {
        StringTokenizer tokens = new StringTokenizer(s, " \n\r\t");
        String[] split = new String[tokens.countTokens()];

        for (int i = 0; i < split.length; ++i)
            split[i] = tokens.nextToken();

        return split;
    }
    
    // -----------------------------------------------------------------------
    
    /**
     * Escapes specified string (that is, <tt>'&lt;'</tt> is replaced by "<tt>&amp;#60</tt>;",
     * <tt>'&amp;'</tt> is replaced by "<tt>&amp;#38;</tt>", etc).
     * 
     * @param string string to be escaped
     * @return escaped string
     */
    public static final String escapeXML(String string)
    {
        StringBuffer escaped = new StringBuffer();
        escapeXML(string, escaped);
        return escaped.toString();
    }

    /**
     * Escapes specified string (that is, <tt>'&lt;'</tt> is replaced by "<tt>&amp;#60</tt>;",
     * <tt>'&amp;'</tt> is replaced by "<tt>&amp;#38;</tt>", etc) then
     * quotes the escaped string.
     * 
     * @param string string to be escaped and quoted
     * @return escaped and quoted string
     */
    public static final String quoteXML(String string)
    {
        StringBuffer quoted = new StringBuffer();
        quoted.append('\"');
        escapeXML(string, quoted);
        quoted.append('\"');
        return quoted.toString();
    }

    /**
     * Escapes specified string (that is, <tt>'&lt;'</tt> is replaced by "<tt>&amp;#60</tt>;",
     * <tt>'&amp;'</tt> is replaced by "<tt>&amp;#38;</tt>", etc).
     * 
     * @param string string to be escaped
     * @param escaped buffer used to store escaped string (characters are
     *        appended to this buffer)
     */
    public static final void escapeXML(String string, StringBuffer escaped)
    {
        char[] chars = string.toCharArray();
        escapeXML(chars, 0, chars.length, escaped);
    }

    /**
     * Escapes specified character array (that is, <tt>'&lt;'</tt> is
     * replaced by "<tt>&amp;#60</tt>;", <tt>'&amp;'</tt> is replaced by "<tt>&amp;#38;</tt>",
     * etc).
     * 
     * @param chars character array to be escaped
     * @param offset specifies first character in array to be escaped
     * @param length number of characters in array to be escaped
     * @param escaped buffer used to store escaped string (characters are
     *        appended to this buffer)
     */
    public static final void escapeXML(char[] chars, int offset, int length,
                                       StringBuffer escaped)
    {
        escapeXML(chars, offset, length, escaped, false);
    }

    /**
     * Escapes specified character array (that is, <tt>'&lt;'</tt> is
     * replaced by "<tt>&amp;#60</tt>;", <tt>'&amp;'</tt> is replaced by "<tt>&amp;#38;</tt>",
     * etc).
     * 
     * @param chars character array to be escaped
     * @param offset specifies first character in array to be escaped
     * @param length number of characters in array to be escaped
     * @param escaped buffer used to store escaped string (characters are
     *        appended to this buffer)
     * @param ascii if true, characters with code &gt; 127 are escaped as
     *        <tt>&amp;#<i>code</i>;</tt>
     */
    public static final void escapeXML(char[] chars, int offset, int length,
                                       StringBuffer escaped, boolean ascii)
    {
        int end = offset + length;
        for (int i = offset; i < end; ++i) {
            char c = chars[i];
            switch (c) {
            case '\'':
                escaped.append("&#39;");
                break;
            case '\"':
                escaped.append("&#34;");
                break;
            case '<':
                escaped.append("&#60;");
                break;
            case '>':
                escaped.append("&#62;");
                break;
            case '&':
                escaped.append("&#38;");
                break;
            case 0x00A0:
                // &nbsp;
                escaped.append("&#x00A0;");
                break;
            default:
                if (ascii && c > 127) {
                    escaped.append("&#");
                    escaped.append(Integer.toString((int) c));
                    escaped.append(';');
                }
                else {
                    escaped.append(c);
                }
            }
        }
    }
    
    // -----------------------------------------------------------------------
    
    /**
     * Unescapes specified string. Inverse operation of escapeXML(...).
     * 
     * @param text string to be unescaped
     * @return unescaped string
     */
    public static final String unescapeXML(String text)
    {
        StringBuffer unescaped = new StringBuffer();
        unescapeXML(text, 0, text.length(), unescaped);
        return unescaped.toString();
    }

    /**
     * Unescapes specified string. Inverse operation of escapeXML().
     * 
     * @param text string to be unescaped
     * @param offset specifies first character in string to be unescaped
     * @param length number of characters in string to be unescaped
     * @param unescaped buffer used to store unescaped string (characters are
     *        appended to this buffer)
     */
    public static final void unescapeXML(String text, int offset, int length,
                                         StringBuffer unescaped)
    {
        int end = offset + length;

        for (int i = offset; i < end; ++i) {
            char c = text.charAt(i);

            if (c == '&') {
                StringBuffer charRef = new StringBuffer();

                ++i;
                while (i < end) {
                    c = text.charAt(i);
                    if (c == ';')
                        break;
                    charRef.append(c);
                    ++i;
                }

                c = parseCharRef(charRef.toString());
            }

            unescaped.append(c);
        }
    }

    private static char parseCharRef(String charRef)
    {
        if (charRef.length() >= 2 && charRef.charAt(0) == '#') {
            int i;

            try {
                if (charRef.charAt(1) == 'x')
                    i = Integer.parseInt(charRef.substring(2), 16);
                else
                    i = Integer.parseInt(charRef.substring(1));
            }
            catch (NumberFormatException e) {
                i = -1;
            }

            if (i < 0 || i > Character.MAX_VALUE)
                return '?';
            else
                return (char) i;
        }
        if (charRef.equals("amp")) {
            return '&';
        }
        if (charRef.equals("apos")) {
            return '\'';
        }
        if (charRef.equals("quot")) {
            return '\"';
        }
        if (charRef.equals("lt")) {
            return '<';
        }
        if (charRef.equals("gt")) {
            return '>';
        }
        else {
            return '?';
        }
    }
    
    // -----------------------------------------------------------------------
    
    private static int uniqueId = 0;
    
    /**
     * Returns a unique ID which can be used for example as the value of an
     * attribute of type ID.
     */
    public static final String getUniqueId()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("___");
        buffer.append(Long.toString(System.currentTimeMillis(),
                                    Character.MAX_RADIX));
        buffer.append(".");
        buffer.append(Integer.toString(uniqueId++, Character.MAX_RADIX));

        return buffer.toString();
    }

    public static String normalizePI(String contents)
    {
        if(contents == null || contents.indexOf("?>") >= 0)
            return null;
        int i = 0, len = contents.length();
        for(; i < len; i++)
            if(!Character.isWhitespace(contents.charAt(i)))
                break;
        return (i == 0)? contents : contents.substring(i);
    }

    public static boolean checkComment(String contents)
    {
        if(contents == null)
            return true; // ?
        return contents.indexOf("--") < 0 &&
               !contents.startsWith("-") && !contents.endsWith("-");
    }
}
