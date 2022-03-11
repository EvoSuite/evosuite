/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package com.examples.with.different.packagename.testcarver;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.IOException;
import java.lang.reflect.Array;

import com.examples.with.different.packagename.testcarver.ConversionException;
import com.examples.with.different.packagename.testcarver.Converter;

/**
 * Generic {@link Converter} implementaion that handles conversion
 * to and from <b>array</b> objects.
 * <p>
 * Can be configured to either return a <i>default value</i> or throw a
 * <code>ConversionException</code> if a conversion error occurs.
 * <p>
 * The main features of this implementation are:
 * <ul>
 *     <li><b>Element Conversion</b> - delegates to a {@link Converter},
 *         appropriate for the type, to convert individual elements
 *         of the array. This leverages the power of existing converters
 *         without having to replicate their functionality for converting
 *         to the element type and removes the need to create a specifc
 *         array type converters.</li>
 *     <li><b>Arrays or Collections</b> - can convert from either arrays or
 *         Collections to an array, limited only by the capability
 *         of the delegate {@link Converter}.</li>
 *     <li><b>Delimited Lists</b> - can Convert <b>to</b> and <b>from</b> a
 *         delimited list in String format.</li>
 *     <li><b>Conversion to String</b> - converts an array to a
 *         <code>String</code> in one of two ways: as a <i>delimited list</i>
 *         or by converting the first element in the array to a String - this
 *         is controlled by the {@link ArrayConverter#setOnlyFirstToString(boolean)}
 *         parameter.</li>
 *     <li><b>Multi Dimensional Arrays</b> - its possible to convert a <code>String</code>
 *         to a multi-dimensional arrays, by embedding {@link ArrayConverter}
 *         within each other - see example below.</li>
 *     <li><b>Default Value</b></li>
 *         <ul>
 *             <li><b><i>No Default</b></i> - use the
 *                 {@link ArrayConverter#ArrayConverter(Class, Converter)}
 *                 constructor to create a converter which throws a
 *                 {@link ConversionException} if the value is missing or
 *                 invalid.</li>
 *             <li><b><i>Default values</b></i> - use the
 *                 {@link ArrayConverter#ArrayConverter(Class, Converter, int)}
 *                 constructor to create a converter which returns a <i>default
 *                 value</i>. The <i>defaultSize</i> parameter controls the
 *                 <i>default value</i> in the following way:</li>
 *                 <ul>
 *                    <li><i>defaultSize &lt; 0</i> - default is <code>null</code></li>
 *                    <li><i>defaultSize = 0</i> - default is an array of length zero</li>
 *                    <li><i>defaultSize &gt; 0</i> - default is an array with a
 *                        length specified by <code>defaultSize</code> (N.B. elements
 *                        in the array will be <code>null</code>)</li>
 *                 </ul>
 *         </ul>
 * </ul>
 *
 * <h3>Parsing Delimited Lists</h3>
 * This implementation can convert a delimited list in <code>String</code> format
 * into an array of the appropriate type. By default, it uses a comma as the delimiter
 * but the following methods can be used to configure parsing:
 * <ul>
 *     <li><code>setDelimiter(char)</code> - allows the character used as
 *         the delimiter to be configured [default is a comma].</li>
 *     <li><code>setAllowedChars(char[])</code> - adds additional characters
 *         (to the default alphabetic/numeric) to those considered to be
 *         valid token characters.
 * </ul>
 *
 * <h3>Multi Dimensional Arrays</h3>
 * It is possible to convert a <code>String</code> to mulit-dimensional arrays by using
 * {@link ArrayConverter} as the element {@link Converter}
 * within another {@link ArrayConverter}.
 * <p>
 * For example, the following code demonstrates how to construct a {@link Converter}
 * to convert a delimited <code>String</code> into a two dimensional integer array:
 * <p>
 * <pre>
 *    // Construct an Integer Converter
 *    IntegerConverter integerConverter = new IntegerConverter();
 *
 *    // Construct an array Converter for an integer array (i.e. int[]) using
 *    // an IntegerConverter as the element converter.
 *    // N.B. Uses the default comma (i.e. ",") as the delimiter between individual numbers
 *    ArrayConverter arrayConverter = new ArrayConverter(int[].class, integerConverter);
 *
 *    // Construct a "Matrix" Converter which converts arrays of integer arrays using
 *    // the pre-ceeding ArrayConverter as the element Converter.
 *    // N.B. Uses a semi-colon (i.e. ";") as the delimiter to separate the different sets of numbers.
 *    //      Also the delimiter used by the first ArrayConverter needs to be added to the
 *    //      "allowed characters" for this one.
 *    ArrayConverter matrixConverter = new ArrayConverter(int[][].class, arrayConverter);
 *    matrixConverter.setDelimiter(';');
 *    matrixConverter.setAllowedChars(new char[] {','});
 *
 *    // Do the Conversion
 *    String matrixString = "11,12,13 ; 21,22,23 ; 31,32,33 ; 41,42,43";
 *    int[][] result = (int[][])matrixConverter.convert(int[][].class, matrixString);
 * </pre>
 *
 * @version $Revision: 640131 $ $Date: 2008-03-23 02:10:31 +0000 (Sun, 23 Mar 2008) $
 * @since 1.8.0
 */
public class ArrayConverter extends AbstractConverter {

    private Object defaultTypeInstance;
    private Converter elementConverter;
    private int defaultSize;
    private char delimiter = ',';
    private char[] allowedChars = new char[]{'.', '-'};
    private boolean onlyFirstToString = true;

    // ----------------------------------------------------------- Constructors

    /**
     * Construct an <b>array</b> <code>Converter</code> with the specified
     * <b>component</b> <code>Converter</code> that throws a
     * <code>ConversionException</code> if an error occurs.
     *
     * @param defaultType      The default array type this
     *                         <code>Converter</code> handles
     * @param elementConverter Converter used to convert
     *                         individual array elements.
     */
    public ArrayConverter(Class defaultType, Converter elementConverter) {
        super();
        if (defaultType == null) {
            throw new IllegalArgumentException("Default type is missing");
        }
        if (!defaultType.isArray()) {
            throw new IllegalArgumentException("Default type must be an array.");
        }
        if (elementConverter == null) {
            throw new IllegalArgumentException("Component Converter is missing.");
        }
        this.defaultTypeInstance = Array.newInstance(defaultType.getComponentType(), 0);
        this.elementConverter = elementConverter;
    }

    /**
     * Construct an <b>array</b> <code>Converter</code> with the specified
     * <b>component</b> <code>Converter</code> that returns a default
     * array of the specified size (or <code>null</code>) if an error occurs.
     *
     * @param defaultType      The default array type this
     *                         <code>Converter</code> handles
     * @param elementConverter Converter used to convert
     *                         individual array elements.
     * @param defaultSize      Specifies the size of the default array value or if less
     *                         than zero indicates that a <code>null</code> default value should be used.
     */
    public ArrayConverter(Class defaultType, Converter elementConverter, int defaultSize) {
        this(defaultType, elementConverter);
        this.defaultSize = defaultSize;
        Object defaultValue = null;
        if (defaultSize >= 0) {
            defaultValue = Array.newInstance(defaultType.getComponentType(), defaultSize);
        }
        setDefaultValue(defaultValue);
    }

    /**
     * Set the delimiter to be used for parsing a delimited String.
     *
     * @param delimiter The delimiter [default ',']
     */
    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }

    /**
     * Set the allowed characters to be used for parsing a delimited String.
     *
     * @param allowedChars Characters which are to be considered as part of
     *                     the tokens when parsing a delimited String [default is '.' and '-']
     */
    public void setAllowedChars(char[] allowedChars) {
        this.allowedChars = allowedChars;
    }

    /**
     * Indicates whether converting to a String should create
     * a delimited list or just convert the first value.
     *
     * @param onlyFirstToString <code>true</code> converts only
     *                          the first value in the array to a String, <code>false</code>
     *                          converts all values in the array into a delimited list (default
     *                          is <code>true</code>
     */
    public void setOnlyFirstToString(boolean onlyFirstToString) {
        this.onlyFirstToString = onlyFirstToString;
    }

    /**
     * Return the default type this <code>Converter</code> handles.
     *
     * @return The default type this <code>Converter</code> handles.
     */
    protected Class getDefaultType() {
        return defaultTypeInstance.getClass();
    }

    /**
     * Handles conversion to a String.
     *
     * @param value The value to be converted.
     * @return the converted String value.
     * @throws Throwable if an error occurs converting to a String
     */
    protected String convertToString(Object value) throws Throwable {

        int size = 0;
        Iterator iterator = null;
        Class type = value.getClass();
        if (type.isArray()) {
            size = Array.getLength(value);
        } else {
            Collection collection = convertToCollection(type, value);
            size = collection.size();
            iterator = collection.iterator();
        }

        if (size == 0) {
            return (String) getDefault(String.class);
        }

        if (onlyFirstToString) {
            size = 1;
        }

        // Create a StringBuffer containing a delimited list of the values
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                buffer.append(delimiter);
            }
            Object element = iterator == null ? Array.get(value, i) : iterator.next();
            element = elementConverter.convert(String.class, element);
            if (element != null) {
                buffer.append(element);
            }
        }

        return buffer.toString();

    }

    /**
     * Handles conversion to an array of the specified type.
     *
     * @param type  The type to which this value should be converted.
     * @param value The input value to be converted.
     * @return The converted value.
     * @throws Throwable if an error occurs converting to the specified type
     */
    protected Object convertToType(Class type, Object value) throws Throwable {

        if (!type.isArray()) {
            throw new ConversionException(toString(getClass())
                    + " cannot handle conversion to '"
                    + toString(type) + "' (not an array).");
        }

        // Handle the source
        int size = 0;
        Iterator iterator = null;
        if (value.getClass().isArray()) {
            size = Array.getLength(value);
        } else {
            Collection collection = convertToCollection(type, value);
            size = collection.size();
            iterator = collection.iterator();
        }

        // Allocate a new Array
        Class componentType = type.getComponentType();
        Object newArray = Array.newInstance(componentType, size);

        // Convert and set each element in the new Array
        for (int i = 0; i < size; i++) {
            Object element = iterator == null ? Array.get(value, i) : iterator.next();
            // TODO - probably should catch conversion errors and throw
            //        new exception providing better info back to the user
            element = elementConverter.convert(componentType, element);
            Array.set(newArray, i, element);
        }

        return newArray;
    }

    /**
     * Returns the value unchanged.
     *
     * @param value The value to convert
     * @return The value unchanged
     */
    protected Object convertArray(Object value) {
        return value;
    }

    /**
     * Converts non-array values to a Collection prior
     * to being converted either to an array or a String.
     * </p>
     * <ul>
     *   <li>{@link Collection} values are returned unchanged</li>
     *   <li>{@link Number}, {@link Boolean}  and {@link java.util.Date}
     *       values returned as a the only element in a List.</li>
     *   <li>All other types are converted to a String and parsed
     *       as a delimited list.</li>
     * </ul>
     *
     * <strong>N.B.</strong> The method is called by both the
     * {@link ArrayConverter#convertToType(Class, Object)} and
     * {@link ArrayConverter#convertToString(Object)} methods for
     * <i>non-array</i> types.
     *
     * @param type  The type to convert the value to
     * @param value value to be converted
     * @return Collection elements.
     */
    protected Collection convertToCollection(Class type, Object value) {
        if (value instanceof Collection) {
            return (Collection) value;
        }
        if (value instanceof Number ||
                value instanceof Boolean ||
                value instanceof java.util.Date) {
            List list = new ArrayList(1);
            list.add(value);
            return list;
        }

        return parseElements(type, value.toString());
    }

    /**
     * Return the default value for conversions to the specified
     * type.
     *
     * @param type Data type to which this value should be converted.
     * @return The default value for the specified type.
     */
    protected Object getDefault(Class type) {
        if (type.equals(String.class)) {
            return null;
        }

        Object defaultValue = super.getDefault(type);
        if (defaultValue == null) {
            return null;
        }

        if (defaultValue.getClass().equals(type)) {
            return defaultValue;
        } else {
            return Array.newInstance(type.getComponentType(), defaultSize);
        }

    }

    /**
     * Provide a String representation of this array converter.
     *
     * @return A String representation of this array converter
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(toString(getClass()));
        buffer.append("[UseDefault=");
        buffer.append(isUseDefault());
        buffer.append(", ");
        buffer.append(elementConverter.toString());
        buffer.append(']');
        return buffer.toString();
    }

    /**
     * <p>Parse an incoming String of the form similar to an array initializer
     * in the Java language into a <code>List</code> individual Strings
     * for each element, according to the following rules.</p>
     * <ul>
     * <li>The string is expected to be a comma-separated list of values.</li>
     * <li>The string may optionally have matching '{' and '}' delimiters
     *   around the list.</li>
     * <li>Whitespace before and after each element is stripped.</li>
     * <li>Elements in the list may be delimited by single or double quotes.
     *  Within a quoted elements, the normal Java escape sequences are valid.</li>
     * </ul>
     *
     * @param type  The type to convert the value to
     * @param value String value to be parsed
     * @return List of parsed elements.
     * @throws ConversionException  if the syntax of <code>svalue</code>
     *                              is not syntactically valid
     * @throws NullPointerException if <code>svalue</code>
     *                              is <code>null</code>
     */
    private List parseElements(Class type, String value) {

        // Trim any matching '{' and '}' delimiters
        value = value.trim();
        if (value.startsWith("{") && value.endsWith("}")) {
            value = value.substring(1, value.length() - 1);
        }

        try {

            // Set up a StreamTokenizer on the characters in this String
            StreamTokenizer st = new StreamTokenizer(new StringReader(value));
            st.whitespaceChars(delimiter, delimiter); // Set the delimiters
            st.ordinaryChars('0', '9');  // Needed to turn off numeric flag
            st.wordChars('0', '9');      // Needed to make part of tokens
            for (int i = 0; i < allowedChars.length; i++) {
                st.ordinaryChars(allowedChars[i], allowedChars[i]);
                st.wordChars(allowedChars[i], allowedChars[i]);
            }

            // Split comma-delimited tokens into a List
            List list = null;
            while (true) {
                int ttype = st.nextToken();
                if ((ttype == StreamTokenizer.TT_WORD) || (ttype > 0)) {
                    if (st.sval != null) {
                        if (list == null) {
                            list = new ArrayList();
                        }
                        list.add(st.sval);
                    }
                } else if (ttype == StreamTokenizer.TT_EOF) {
                    break;
                } else {
                    throw new ConversionException("Encountered token of type "
                            + ttype + " parsing elements to '" + toString(type) + ".");
                }
            }

            if (list == null) {
                list = Collections.EMPTY_LIST;
            }

            // Return the completed list
            return (list);

        } catch (IOException e) {

            throw new ConversionException("Error converting from String to '"
                    + toString(type) + "': " + e.getMessage(), e);

        }

    }

}
