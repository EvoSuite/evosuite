/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package com.examples.with.different.packagename;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * XMLElement is a representation of an XML object. The object is able to parse
 * XML code.
 * <P>
 * <DL>
 * <DT><B>Parsing XML Data</B></DT>
 * <DD>
 * You can parse XML data using the following code:
 * <UL>
 * <CODE>
 * XMLElement xml = new XMLElement();<BR>
 * FileReader reader = new FileReader("filename.xml");<BR>
 * xml.parseFromReader(reader);
 * </CODE>
 * </UL>
 * </DD>
 * </DL>
 * <DL>
 * <DT><B>Retrieving Attributes</B></DT>
 * <DD>
 * You can enumerate the attributes of an element using the method
 * {@link #enumerateAttributeNames() enumerateAttributeNames}. The attribute
 * values can be retrieved using the method
 * {@link #getStringAttribute(java.lang.String) getStringAttribute}. The
 * following example shows how to list the attributes of an element:
 * <UL>
 * <CODE>
 * XMLElement element = ...;<BR>
 * Enumeration enum = element.getAttributeNames();<BR>
 * while (enum.hasMoreElements()) {<BR>
 * &nbsp;&nbsp;&nbsp;&nbsp;String key = (String) enum.nextElement();<BR>
 * &nbsp;&nbsp;&nbsp;&nbsp;String value = element.getStringAttribute(key);<BR>
 * &nbsp;&nbsp;&nbsp;&nbsp;System.out.println(key + " = " + value);<BR>
 * }
 * </CODE>
 * </UL>
 * </DD>
 * </DL>
 * <DL>
 * <DT><B>Retrieving Child Elements</B></DT>
 * <DD>
 * You can enumerate the children of an element using
 * {@link #enumerateChildren() enumerateChildren}. The number of child elements
 * can be retrieved using {@link #countChildren() countChildren}.</DD>
 * </DL>
 * <DL>
 * <DT><B>Elements Containing Character Data</B></DT>
 * <DD>
 * If an elements contains character data, like in the following example:
 * <UL>
 * <CODE>
 * &lt;title&gt;The Title&lt;/title&gt;
 * </CODE>
 * </UL>
 * you can retrieve that data using the method {@link #getContent() getContent}.
 * </DD>
 * </DL>
 * <DL>
 * <DT><B>Subclassing XMLElement</B></DT>
 * <DD>
 * When subclassing XMLElement, you need to override the method
 * {@link #createAnotherElement() createAnotherElement} which has to return a
 * new copy of the receiver.</DD>
 * </DL>
 * <P>
 * 
 * @see nanoxml.XMLParseException
 * 
 * @author Marc De Scheemaecker &lt;<A
 *         href="mailto:cyberelf@mac.com">cyberelf@mac.com</A>&gt;
 * @version $Name: RELEASE_2_2_1 $, $Revision: 1.4 $
 */
public class XMLElement2 {

	public static void testCase(String string0) {
		XMLElement2 xmlElement = new XMLElement2();
		xmlElement.parseString(string0);
	}

	public static class XMLParseException extends RuntimeException {

		public XMLParseException(String msg, int line, String ex) {
		}

		public XMLParseException(String msg, String ex) {
		}

	}

	/**
	 * Serialization serial version ID.
	 */
	static final long serialVersionUID = 6685035139346394777L;

	/**
	 * Major version of NanoXML. Classes with the same major and minor version
	 * are binary compatible. Classes with the same major version are source
	 * compatible. If the major version is different, you may need to modify the
	 * client source code.
	 * 
	 * @see nanoxml.XMLElement2#NANOXML_MINOR_VERSION
	 */
	public static final int NANOXML_MAJOR_VERSION = 2;

	/**
	 * Minor version of NanoXML. Classes with the same major and minor version
	 * are binary compatible. Classes with the same major version are source
	 * compatible. If the major version is different, you may need to modify the
	 * client source code.
	 * 
	 * @see nanoxml.XMLElement2#NANOXML_MAJOR_VERSION
	 */
	public static final int NANOXML_MINOR_VERSION = 2;

	/**
	 * The attributes given to the element.
	 * 
	 * <dl>
	 * <dt><b>Invariants:</b></dt>
	 * <dd>
	 * <ul>
	 * <li>The field can be empty.
	 * <li>The field is never <code>null</code>.
	 * <li>The keys and the values are strings.
	 * </ul>
	 * </dd>
	 * </dl>
	 */
	private Hashtable attributes;

	/**
	 * Child elements of the element.
	 * 
	 * <dl>
	 * <dt><b>Invariants:</b></dt>
	 * <dd>
	 * <ul>
	 * <li>The field can be empty.
	 * <li>The field is never <code>null</code>.
	 * <li>The elements are instances of <code>XMLElement</code> or a subclass
	 * of <code>XMLElement</code>.
	 * </ul>
	 * </dd>
	 * </dl>
	 */
	private Vector children;

	/**
	 * The name of the element.
	 * 
	 * <dl>
	 * <dt><b>Invariants:</b></dt>
	 * <dd>
	 * <ul>
	 * <li>The field is <code>null</code> iff the element is not initialized by
	 * either parse or setName.
	 * <li>If the field is not <code>null</code>, it's not empty.
	 * <li>If the field is not <code>null</code>, it contains a valid XML
	 * identifier.
	 * </ul>
	 * </dd>
	 * </dl>
	 */
	private String name;

	/**
	 * The #PCDATA content of the object.
	 * 
	 * <dl>
	 * <dt><b>Invariants:</b></dt>
	 * <dd>
	 * <ul>
	 * <li>The field is <code>null</code> iff the element is not a #PCDATA
	 * element.
	 * <li>The field can be any string, including the empty string.
	 * </ul>
	 * </dd>
	 * </dl>
	 */
	private String contents;

	/**
	 * Conversion table for &amp;...; entities. The keys are the entity names
	 * without the &amp; and ; delimiters.
	 * 
	 * <dl>
	 * <dt><b>Invariants:</b></dt>
	 * <dd>
	 * <ul>
	 * <li>The field is never <code>null</code>.
	 * <li>The field always contains the following associations:
	 * "lt"&nbsp;=&gt;&nbsp;"&lt;", "gt"&nbsp;=&gt;&nbsp;"&gt;",
	 * "quot"&nbsp;=&gt;&nbsp;"\"", "apos"&nbsp;=&gt;&nbsp;"'",
	 * "amp"&nbsp;=&gt;&nbsp;"&amp;"
	 * <li>The keys are strings
	 * <li>The values are char arrays
	 * </ul>
	 * </dd>
	 * </dl>
	 */
	private Hashtable entities;

	/**
	 * The line number where the element starts.
	 * 
	 * <dl>
	 * <dt><b>Invariants:</b></dt>
	 * <dd>
	 * <ul>
	 * <li><code>lineNr &gt= 0</code>
	 * </ul>
	 * </dd>
	 * </dl>
	 */
	private int lineNr;

	/**
	 * <code>true</code> if the case of the element and attribute names are case
	 * insensitive.
	 */
	private boolean ignoreCase;

	/**
	 * <code>true</code> if the leading and trailing whitespace of #PCDATA
	 * sections have to be ignored.
	 */
	private boolean ignoreWhitespace;

	/**
	 * Character read too much. This character provides push-back functionality
	 * to the input reader without having to use a PushbackReader. If there is
	 * no such character, this field is '\0'.
	 */
	private char charReadTooMuch;

	/**
	 * The reader provided by the caller of the parse method.
	 * 
	 * <dl>
	 * <dt><b>Invariants:</b></dt>
	 * <dd>
	 * <ul>
	 * <li>The field is not <code>null</code> while the parse method is running.
	 * </ul>
	 * </dd>
	 * </dl>
	 */
	private String reader;
	private int readerIndex;

	/**
	 * The current line number in the source content.
	 * 
	 * <dl>
	 * <dt><b>Invariants:</b></dt>
	 * <dd>
	 * <ul>
	 * <li>parserLineNr &gt; 0 while the parse method is running.
	 * </ul>
	 * </dd>
	 * </dl>
	 */
	private int parserLineNr;

	/**
	 * Creates and initializes a new XML element. Calling the construction is
	 * equivalent to:
	 * <ul>
	 * <code>new XMLElement(new Hashtable(), false, true)
	 * </code>
	 * </ul>
	 * 
	 * <dl>
	 * <dt><b>Postconditions:</b></dt>
	 * <dd>
	 * <ul>
	 * <li>countChildren() => 0
	 * <li>enumerateChildren() => empty enumeration
	 * <li>enumeratePropertyNames() => empty enumeration
	 * <li>getChildren() => empty vector
	 * <li>getContent() => ""
	 * <li>getLineNr() => 0
	 * <li>getName() => null
	 * </ul>
	 * </dd>
	 * </dl>
	 * 
	 * @see nanoxml.XMLElement2#XMLElement(java.util.Hashtable)
	 *      XMLElement(Hashtable)
	 * @see nanoxml.XMLElement2#XMLElement(boolean)
	 * @see nanoxml.XMLElement2#XMLElement(java.util.Hashtable,boolean)
	 *      XMLElement(Hashtable, boolean)
	 */
	public XMLElement2() {
		this(new Hashtable(), false, true, true);
	}

	/**
	 * Creates and initializes a new XML element.
	 * <P>
	 * This constructor should <I>only</I> be called from
	 * {@link #createAnotherElement() createAnotherElement} to create child
	 * elements.
	 * 
	 * @param entities
	 *            The entity conversion table.
	 * @param skipLeadingWhitespace
	 *            <code>true</code> if leading and trailing whitespace in PCDATA
	 *            content has to be removed.
	 * @param fillBasicConversionTable
	 *            <code>true</code> if the basic entities need to be added to
	 *            the entity list.
	 * @param ignoreCase
	 *            <code>true</code> if the case of element and attribute names
	 *            have to be ignored.
	 * 
	 *            </dl>
	 *            <dl>
	 *            <dt><b>Preconditions:</b></dt><dd>
	 *            <ul>
	 *            <li><code>entities != null</code> <li>if <code>
	 *            fillBasicConversionTable == false</code> then <code>entities
	 *            </code> contains at least the following entries: <code>amp
	 *            </code>, <code>lt</code>, <code>gt</code>, <code>apos</code>
	 *            and <code>quot</code>
	 *            </ul>
	 *            </dd>
	 *            </dl>
	 * 
	 *            <dl>
	 *            <dt><b>Postconditions:</b></dt><dd>
	 *            <ul>
	 *            <li>countChildren() => 0 <li>enumerateChildren() => empty
	 *            enumeration <li>enumeratePropertyNames() => empty enumeration
	 *            <li>getChildren() => empty vector <li>getContent() => "" <li>
	 *            getLineNr() => 0 <li>getName() => null
	 *            </ul>
	 *            </dd>
	 *            </dl>
	 *            <dl>
	 * 
	 * @see nanoxml.XMLElement2#createAnotherElement()
	 */
	private XMLElement2(Hashtable entities, boolean skipLeadingWhitespace,
			boolean fillBasicConversionTable, boolean ignoreCase) {
		this.ignoreWhitespace = skipLeadingWhitespace;
		this.ignoreCase = ignoreCase;
		this.name = null;
		this.contents = "";
		this.attributes = new Hashtable();
		this.children = new Vector();
		this.entities = entities;
		this.lineNr = 0;

		this.entities.put("amp", new char[] { '&' });
		this.entities.put("quot", new char[] { '"' });
		this.entities.put("apos", new char[] { '\'' });
		this.entities.put("lt", new char[] { '<' });
		this.entities.put("gt", new char[] { '>' });
	}

	/**
	 * Returns the name of the element.
	 * 
	 * @see nanoxml.XMLElement2#setName(java.lang.String) setName(String)
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Scans the data for literal text. Scanning stops when a character does not
	 * match or after the complete text has been checked, whichever comes first.
	 * 
	 * @param literal
	 *            the literal to check.
	 * 
	 *            </dl>
	 *            <dl>
	 *            <dt><b>Preconditions:</b></dt>
	 *            <dd>
	 *            <ul>
	 *            <li><code>literal != null</code>
	 *            </ul>
	 *            </dd>
	 *            </dl>
	 */
	private boolean checkLiteral(String literal) {
		int length = literal.length();
		for (int i = 0; i < length; i += 1) {
			if (this.readChar() != literal.charAt(i)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Reads a character from a reader.
	 */
	private char readChar() {
		if (this.charReadTooMuch != '\0') {
			char ch = this.charReadTooMuch;
			this.charReadTooMuch = '\0';
			return ch;
		} else {
			if (readerIndex > this.reader.length() - 1) {
				throw this.unexpectedEndOfData();
			}
			int i = this.reader.charAt(readerIndex);
			readerIndex++;
			if (i == 10) { // line feed
				this.parserLineNr += 1;
				return '\n';
			} else {
				return (char) i;
			}
		}
	}

	/**
	 * Creates a parse exception for when the end of the data input has been
	 * reached.
	 */
	private XMLParseException unexpectedEndOfData() {
		String msg = "Unexpected end of data reached";
		return new XMLParseException(this.getName(), this.parserLineNr, msg);
	}

	/**
	 * Reads one XML element from a String and parses it.
	 * 
	 * @param reader
	 *            The reader from which to retrieve the XML data.
	 * 
	 *            </dl>
	 *            <dl>
	 *            <dt><b>Preconditions:</b></dt>
	 *            <dd>
	 *            <ul>
	 *            <li><code>string != null</code>
	 *            <li><code>string.length() &gt; 0</code>
	 *            </ul>
	 *            </dd>
	 *            </dl>
	 * 
	 *            <dl>
	 *            <dt><b>Postconditions:</b></dt>
	 *            <dd>
	 *            <ul>
	 *            <li>the state of the receiver is updated to reflect the XML
	 *            element parsed from the reader
	 *            </ul>
	 *            </dd>
	 *            </dl>
	 *            <dl>
	 * 
	 * @throws nanoxml.XMLParseException
	 *             If an error occured while parsing the string.
	 */
	public void parseString(String string) throws XMLParseException {
		this.parseFromReader(string,
		/* startingLineNr */1);

	}

	/**
	 * Reads one XML element from a java.io.Reader and parses it.
	 * 
	 * @param reader
	 *            The reader from which to retrieve the XML data.
	 * @param startingLineNr
	 *            The line number of the first line in the data.
	 * 
	 *            </dl>
	 *            <dl>
	 *            <dt><b>Preconditions:</b></dt>
	 *            <dd>
	 *            <ul>
	 *            <li><code>reader != null</code>
	 *            <li><code>reader</code> is not closed
	 *            </ul>
	 *            </dd>
	 *            </dl>
	 * 
	 *            <dl>
	 *            <dt><b>Postconditions:</b></dt>
	 *            <dd>
	 *            <ul>
	 *            <li>the state of the receiver is updated to reflect the XML
	 *            element parsed from the reader
	 *            <li>the reader points to the first character following the
	 *            last '&gt;' character of the XML element
	 *            </ul>
	 *            </dd>
	 *            </dl>
	 *            <dl>
	 * 
	 * @throws java.io.IOException
	 *             If an error occured while reading the input.
	 * @throws nanoxml.XMLParseException
	 *             If an error occured while parsing the read data.
	 */
	private void parseFromReader(String reader, int startingLineNr)
			throws XMLParseException {
		this.name = null;
		this.contents = "";
		this.attributes = new Hashtable();
		this.children = new Vector();
		this.charReadTooMuch = '\0';
		this.reader = reader;
		this.readerIndex = 0;
		this.parserLineNr = startingLineNr;

		for (;;) {
			char ch = this.scanWhitespace();

			if (ch != '<') {
				throw this.expectedInput("<");
			}

			ch = this.readChar();

			if ((ch == '!') || (ch == '?')) {
				this.skipSpecialTag(0);
			} else {
				this.unreadChar(ch);
				this.scanElement(this);
				return;
			}
		}
	}

	/**
	 * This method scans an identifier from the current reader.
	 * 
	 * @return the next character following the whitespace.
	 */
	private char scanWhitespace()

	{
		for (;;) {
			char ch = this.readChar();
			switch (ch) {
			case ' ':
			case '\t':
			case '\n':
			case '\r':
				break;
			default:
				return ch;
			}
		}
	}

	/**
	 * Creates a parse exception for when the next character read is not the
	 * character that was expected.
	 * 
	 * @param charSet
	 *            The set of characters (in human readable form) that was
	 *            expected.
	 * 
	 *            </dl>
	 *            <dl>
	 *            <dt><b>Preconditions:</b></dt>
	 *            <dd>
	 *            <ul>
	 *            <li><code>charSet != null</code>
	 *            <li><code>charSet.length() &gt; 0</code>
	 *            </ul>
	 *            </dd>
	 *            </dl>
	 */
	private XMLParseException expectedInput(String charSet) {
		String msg = "Expected: " + charSet;
		return new XMLParseException(this.getName(), this.parserLineNr, msg);
	}

	/**
	 * Skips a special tag or comment.
	 * 
	 * @param bracketLevel
	 *            The number of open square brackets ([) that have already been
	 *            read.
	 * 
	 *            </dl>
	 *            <dl>
	 *            <dt><b>Preconditions:</b></dt>
	 *            <dd>
	 *            <ul>
	 *            <li>The first &lt;! has already been read.
	 *            <li><code>bracketLevel >= 0</code>
	 *            </ul>
	 *            </dd>
	 *            </dl>
	 */
	private void skipSpecialTag(int bracketLevel) {
		int tagLevel = 1; // <
		char stringDelimiter = '\0';
		if (bracketLevel == 0) {
			char ch = this.readChar();
			if (ch == '[') {
				bracketLevel += 1;
			} else if (ch == '-') {
				ch = this.readChar();
				if (ch == '[') {
					bracketLevel += 1;
				} else if (ch == ']') {
					bracketLevel -= 1;
				} else if (ch == '-') {
					this.skipComment();
					return;
				}
			}
		}
		while (tagLevel > 0) {
			char ch = this.readChar();
			if (stringDelimiter == '\0') {
				if ((ch == '"') || (ch == '\'')) {
					stringDelimiter = ch;
				} else if (bracketLevel <= 0) {
					if (ch == '<') {
						tagLevel += 1;
					} else if (ch == '>') {
						tagLevel -= 1;
					}
				}
				if (ch == '[') {
					bracketLevel += 1;
				} else if (ch == ']') {
					bracketLevel -= 1;
				}
			} else {
				if (ch == stringDelimiter) {
					stringDelimiter = '\0';
				}
			}
		}
	}

	/**
	 * Pushes a character back to the read-back buffer.
	 * 
	 * @param ch
	 *            The character to push back.
	 * 
	 *            </dl>
	 *            <dl>
	 *            <dt><b>Preconditions:</b></dt>
	 *            <dd>
	 *            <ul>
	 *            <li>The read-back buffer is empty.
	 *            <li><code>ch != '\0'</code>
	 *            </ul>
	 *            </dd>
	 *            </dl>
	 */
	private void unreadChar(char ch) {
		this.charReadTooMuch = ch;
	}

	/**
	 * Scans an XML element.
	 * 
	 * @param elt
	 *            The element that will contain the result.
	 * 
	 *            </dl>
	 *            <dl>
	 *            <dt><b>Preconditions:</b></dt>
	 *            <dd>
	 *            <ul>
	 *            <li>The first &lt; has already been read.
	 *            <li><code>elt != null</code>
	 *            </ul>
	 *            </dd>
	 *            </dl>
	 */
	private void scanElement(XMLElement2 elt) {

		StringBuffer buf = new StringBuffer();

		this.scanIdentifier(buf);

		String name = buf.toString();

		if (name.length() == 0) {
			throw new XMLParseException("", "");
		}

		elt.setName(name);

		char ch = this.scanWhitespace();

		while ((ch != '>') && (ch != '/')) {
			// buf.setLength(0);
			buf = new StringBuffer();

			this.unreadChar(ch);
			this.scanIdentifier(buf);

			String key = buf.toString();

			ch = this.scanWhitespace();

			if (ch != '=') {
				throw this.expectedInput("=");
			}

			this.unreadChar(this.scanWhitespace());

			// buf.setLength(0);
			buf = new StringBuffer();

			this.scanString(buf);

			if (key.length() == 0) {
				throw new XMLParseException("", "");
			}

			elt.setAttribute(key, buf);

			ch = this.scanWhitespace();
		}
		if (ch == '/') {
			ch = this.readChar();
			if (ch != '>') {
				throw this.expectedInput(">");
			} else {
				return;
			}
		}
		// buf.setLength(0);
		buf = new StringBuffer();
		ch = this.scanWhitespace(buf);
		if (ch != '<') {
			this.unreadChar(ch);

			this.scanPCData(buf);

		} else {
			for (;;) {
				ch = this.readChar();
				if (ch == '!') {
					if (this.checkCDATA(buf)) {

						this.scanPCData(buf);

						break;
					} else {
						ch = this.scanWhitespace(buf);

						if (ch != '<') {
							this.unreadChar(ch);

							this.scanPCData(buf);

							break;
						}
					}
				} else {
					if ((ch != '/') || this.ignoreWhitespace) {
						// buf.setLength(0);
						buf = new StringBuffer();
					}
					if (ch == '/') {
						this.unreadChar(ch);

					}
					break;
				}
			}
		}
		if (buf.length() == 0) {
			while (ch != '/') {
				if (ch == '!') {
					ch = this.readChar();

					if (ch != '-') {
						throw this.expectedInput("Comment or Element");
					}
					ch = this.readChar();

					if (ch != '-') {
						throw this.expectedInput("Comment or Element");
					}
					this.skipComment();

				} else {
					this.unreadChar(ch);

					XMLElement2 child = this.createAnotherElement();
					this.scanElement(child);

					elt.addChild(child);

				}
				ch = this.scanWhitespace();

				if (ch != '<') {
					throw this.expectedInput("<");
				}
				ch = this.readChar();

			}
			this.unreadChar(ch);

		} else {
			if (this.ignoreWhitespace) {
				elt.setContent(buf.toString().trim());
			} else {
				elt.setContent(buf.toString());
			}
		}
		ch = this.readChar();

		if (ch != '/') {
			throw this.expectedInput("/");
		}
		this.unreadChar(this.scanWhitespace());

		if (!this.checkLiteral(name)) {
			throw this.expectedInput(name);
		}
		if (this.scanWhitespace() != '>') {
			throw this.expectedInput(">");
		} else {


		}
	}

	/**
	 * Skips a comment.
	 * 
	 * </dl>
	 * <dl>
	 * <dt><b>Preconditions:</b></dt>
	 * <dd>
	 * <ul>
	 * <li>The first &lt;!-- has already been read.
	 * </ul>
	 * </dd>
	 * </dl>
	 */
	private void skipComment() {
		int dashesToRead = 2;
		while (dashesToRead > 0) {
			char ch = this.readChar();
			if (ch == '-') {
				dashesToRead -= 1;
			} else {
				dashesToRead = 2;
			}
		}
		if (this.readChar() != '>') {
			throw this.expectedInput(">");
		}
	}

	/**
	 * Scans an identifier from the current reader. The scanned identifier is
	 * appended to <code>result</code>.
	 * 
	 * @param result
	 *            The buffer in which the scanned identifier will be put.
	 * 
	 *            </dl>
	 *            <dl>
	 *            <dt><b>Preconditions:</b></dt>
	 *            <dd>
	 *            <ul>
	 *            <li><code>result != null</code>
	 *            <li>The next character read from the reader is a valid first
	 *            character of an XML identifier.
	 *            </ul>
	 *            </dd>
	 *            </dl>
	 * 
	 *            <dl>
	 *            <dt><b>Postconditions:</b></dt>
	 *            <dd>
	 *            <ul>
	 *            <li>The next character read from the reader won't be an
	 *            identifier character.
	 *            </ul>
	 *            </dd>
	 *            </dl>
	 *            <dl>
	 */
	private void scanIdentifier(StringBuffer result) {
		for (;;) {
			char ch = this.readChar();
			if (((ch < 'A') || (ch > 'Z')) && ((ch < 'a') || (ch > 'z'))
					&& ((ch < '0') || (ch > '9')) && (ch != '_') && (ch != '.')
					&& (ch != ':') && (ch != '-') && (ch <= '\u007E')) {
				this.unreadChar(ch);
				return;
			}
			result.append(ch);
		}
	}

	/**
	 * Changes the name of the element.
	 * 
	 * @param name
	 *            The new name.
	 * 
	 *            </dl>
	 *            <dl>
	 *            <dt><b>Preconditions:</b></dt>
	 *            <dd>
	 *            <ul>
	 *            <li><code>name != null</code>
	 *            <li><code>name</code> is a valid XML identifier
	 *            </ul>
	 *            </dd>
	 *            </dl>
	 * 
	 * @see nanoxml.XMLElement#getName()
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * This method scans a delimited string from the current reader. The scanned
	 * string without delimiters is appended to <code>string</code>.
	 * 
	 * </dl>
	 * <dl>
	 * <dt><b>Preconditions:</b></dt>
	 * <dd>
	 * <ul>
	 * <li><code>string != null</code>
	 * <li>the next char read is the string delimiter
	 * </ul>
	 * </dd>
	 * </dl>
	 */
	private void scanString(StringBuffer string) {
		char delimiter = this.readChar();
		if ((delimiter != '\'') && (delimiter != '"')) {
			throw this.expectedInput("' or \"");
		}
		for (;;) {
			char ch = this.readChar();
			if (ch == delimiter) {
				return;
			} else if (ch == '&') {
				this.resolveEntity(string);
			} else {
				string.append(ch);
			}
		}
	}

	/**
	 * Resolves an entity. The name of the entity is read from the reader. The
	 * value of the entity is appended to <code>buf</code>.
	 * 
	 * @param buf
	 *            Where to put the entity value.
	 * 
	 *            </dl>
	 *            <dl>
	 *            <dt><b>Preconditions:</b></dt>
	 *            <dd>
	 *            <ul>
	 *            <li>The first &amp; has already been read.
	 *            <li><code>buf != null</code>
	 *            </ul>
	 *            </dd>
	 *            </dl>
	 */
	private void resolveEntity(StringBuffer buf) {
		char ch = '\0';
		StringBuffer keyBuf = new StringBuffer();
		for (;;) {
			ch = this.readChar();
			if (ch == ';') {
				break;
			}
			keyBuf.append(ch);
		}
		String key = keyBuf.toString();
		if (key.charAt(0) == '#') {
			try {
				if (key.charAt(1) == 'x') {
					ch = (char) Integer.parseInt(key.substring(2), 16);
				} else {
					ch = (char) Integer.parseInt(key.substring(1), 10);
				}
			} catch (NumberFormatException e) {
				throw this.unknownEntity(key);
			}
			buf.append(ch);
		} else {
			char[] value = (char[]) this.entities.get(key);
			if (value == null) {
				throw this.unknownEntity(key);
			}
			buf.append(value);
		}
	}

	/**
	 * Creates a parse exception for when an entity could not be resolved.
	 * 
	 * @param name
	 *            The name of the entity.
	 * 
	 *            </dl>
	 *            <dl>
	 *            <dt><b>Preconditions:</b></dt>
	 *            <dd>
	 *            <ul>
	 *            <li><code>name != null</code>
	 *            <li><code>name.length() &gt; 0</code>
	 *            </ul>
	 *            </dd>
	 *            </dl>
	 */
	private XMLParseException unknownEntity(String name) {
		String msg = "Unknown or invalid entity: &" + name + ";";
		return new XMLParseException(this.getName(), this.parserLineNr, msg);
	}

	/**
	 * Adds or modifies an attribute.
	 * 
	 * @param name
	 *            The name of the attribute.
	 * @param value
	 *            The value of the attribute.
	 * 
	 *            </dl>
	 *            <dl>
	 *            <dt><b>Preconditions:</b></dt>
	 *            <dd>
	 *            <ul>
	 *            <li><code>name != null</code>
	 *            <li><code>name</code> is a valid XML identifier
	 *            <li><code>value != null</code>
	 *            </ul>
	 *            </dd>
	 *            </dl>
	 * 
	 *            <dl>
	 *            <dt><b>Postconditions:</b></dt>
	 *            <dd>
	 *            <ul>
	 *            <li>enumerateAttributeNames() => old.enumerateAttributeNames()
	 *            + name
	 *            <li>getAttribute(name) => value
	 *            </ul>
	 *            </dd>
	 *            </dl>
	 *            <dl>
	 * 
	 * @see nanoxml.XMLElement#setDoubleAttribute(java.lang.String, double)
	 *      setDoubleAttribute(String, double)
	 * @see nanoxml.XMLElement#setIntAttribute(java.lang.String, int)
	 *      setIntAttribute(String, int)
	 * @see nanoxml.XMLElement#enumerateAttributeNames()
	 * @see nanoxml.XMLElement#getAttribute(java.lang.String)
	 *      getAttribute(String)
	 * @see nanoxml.XMLElement#getAttribute(java.lang.String, java.lang.Object)
	 *      getAttribute(String, Object)
	 * @see nanoxml.XMLElement#getAttribute(java.lang.String,
	 *      java.util.Hashtable, java.lang.String, boolean) getAttribute(String,
	 *      Hashtable, String, boolean)
	 * @see nanoxml.XMLElement#getStringAttribute(java.lang.String)
	 *      getStringAttribute(String)
	 * @see nanoxml.XMLElement#getStringAttribute(java.lang.String,
	 *      java.lang.String) getStringAttribute(String, String)
	 * @see nanoxml.XMLElement#getStringAttribute(java.lang.String,
	 *      java.util.Hashtable, java.lang.String, boolean)
	 *      getStringAttribute(String, Hashtable, String, boolean)
	 */
	public void setAttribute(String name, Object value) {
		if (this.ignoreCase) {
			name = name.toUpperCase();
		}
		this.attributes.put(name, value.toString());
	}

	/**
	 * This method scans an identifier from the current reader. The scanned
	 * whitespace is appended to <code>result</code>.
	 * 
	 * @return the next character following the whitespace.
	 * 
	 *         </dl>
	 *         <dl>
	 *         <dt><b>Preconditions:</b></dt>
	 *         <dd>
	 *         <ul>
	 *         <li><code>result != null</code>
	 *         </ul>
	 *         </dd>
	 *         </dl>
	 */
	private char scanWhitespace(StringBuffer result) {
		for (;;) {
			char ch = this.readChar();
			switch (ch) {
			case ' ':
			case '\t':
			case '\n':
				result.append(ch);
			case '\r':
				break;
			default:
				return ch;
			}
		}
	}

	/**
	 * Scans a #PCDATA element. CDATA sections and entities are resolved. The
	 * next &lt; char is skipped. The scanned data is appended to
	 * <code>data</code>.
	 * 
	 * </dl>
	 * <dl>
	 * <dt><b>Preconditions:</b></dt>
	 * <dd>
	 * <ul>
	 * <li><code>data != null</code>
	 * </ul>
	 * </dd>
	 * </dl>
	 */
	private void scanPCData(StringBuffer data) {
		for (;;) {
			char ch = this.readChar();
			if (ch == '<') {
				ch = this.readChar();
				if (ch == '!') {
					this.checkCDATA(data);
				} else {
					this.unreadChar(ch);
					return;
				}
			} else if (ch == '&') {
				this.resolveEntity(data);
			} else {
				data.append(ch);
			}
		}
	}

	/**
	 * Scans a special tag and if the tag is a CDATA section, append its content
	 * to <code>buf</code>.
	 * 
	 * </dl>
	 * <dl>
	 * <dt><b>Preconditions:</b></dt>
	 * <dd>
	 * <ul>
	 * <li><code>buf != null</code>
	 * <li>The first &lt; has already been read.
	 * </ul>
	 * </dd>
	 * </dl>
	 */
	private boolean checkCDATA(StringBuffer buf) {
		char ch = this.readChar();
		if (ch != '[') {
			this.unreadChar(ch);
			this.skipSpecialTag(0);
			return false;
		} else if (!this.checkLiteral("CDATA[")) {
			this.skipSpecialTag(1); // one [ has already been read
			return false;
		} else {
			int delimiterCharsSkipped = 0;
			while (delimiterCharsSkipped < 3) {
				ch = this.readChar();
				switch (ch) {
				case ']':
					if (delimiterCharsSkipped < 2) {
						delimiterCharsSkipped += 1;
					} else {
						buf.append(']');
						buf.append(']');
						delimiterCharsSkipped = 0;
					}
					break;
				case '>':
					if (delimiterCharsSkipped < 2) {
						for (int i = 0; i < delimiterCharsSkipped; i++) {
							buf.append(']');
						}
						delimiterCharsSkipped = 0;
						buf.append('>');
					} else {
						delimiterCharsSkipped = 3;
					}
					break;
				default:
					for (int i = 0; i < delimiterCharsSkipped; i += 1) {
						buf.append(']');
					}
					buf.append(ch);
					delimiterCharsSkipped = 0;
				}
			}
			return true;
		}
	}

	/**
	 * Creates a new similar XML element.
	 * <P>
	 * You should override this method when subclassing XMLElement.
	 */
	private XMLElement2 createAnotherElement() {
		return new XMLElement2(this.entities, this.ignoreWhitespace, false,
				this.ignoreCase);
	}

	/**
	 * Changes the content string.
	 * 
	 * @param content
	 *            The new content string.
	 */
	public void setContent(String content) {
		this.contents = content;
	}

	/**
	 * Adds a child element.
	 * 
	 * @param child
	 *            The child element to add.
	 * 
	 *            </dl>
	 *            <dl>
	 *            <dt><b>Preconditions:</b></dt>
	 *            <dd>
	 *            <ul>
	 *            <li><code>child != null</code>
	 *            <li><code>child.getName() != null</code>
	 *            <li><code>child</code> does not have a parent element
	 *            </ul>
	 *            </dd>
	 *            </dl>
	 * 
	 *            <dl>
	 *            <dt><b>Postconditions:</b></dt>
	 *            <dd>
	 *            <ul>
	 *            <li>countChildren() => old.countChildren() + 1
	 *            <li>enumerateChildren() => old.enumerateChildren() + child
	 *            <li>getChildren() => old.enumerateChildren() + child
	 *            </ul>
	 *            </dd>
	 *            </dl>
	 *            <dl>
	 * 
	 * @see nanoxml.XMLElement#countChildren()
	 * @see nanoxml.XMLElement#enumerateChildren()
	 * @see nanoxml.XMLElement#getChildren()
	 * @see nanoxml.XMLElement#removeChild(nanoxml.XMLElement)
	 *      removeChild(XMLElement)
	 */
	public void addChild(XMLElement2 child) {
		this.children.addElement(child);
	}

}
