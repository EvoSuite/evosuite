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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Locale;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test Case for the ArrayConverter class.
 *
 * @version $Revision: 745078 $ $Date: 2009-02-17 14:03:10 +0000 (Tue, 17 Feb 2009) $
 */
public class ArrayConverterTestCase extends TestCase {

    /**
     * Construct a new Array Converter test case.
     *
     * @param name Test Name
     */
    public ArrayConverterTestCase(String name) {
        super(name);
    }

    // ------------------------------------------------------------------------

    /**
     * Create Test Suite
     *
     * @return test suite
     */
    public static TestSuite suite() {
        return new TestSuite(ArrayConverterTestCase.class);
    }

    /**
     * Set Up
     */
    public void setUp() throws Exception {
    }

    /**
     * Tear Down
     */
    public void tearDown() throws Exception {
    }


    // ------------------------------------------------------------------------

    /**
     * Test Converting using the IntegerConverter as the component Converter
     */
    public void testComponentIntegerConverter() {

        IntegerConverter intConverter = new IntegerConverter(0);
        intConverter.setPattern("#,###");
        intConverter.setLocale(Locale.US);
        ArrayConverter arrayConverter = new ArrayConverter(int[].class, intConverter, 0);
        arrayConverter.setAllowedChars(new char[]{',', '-'});
        arrayConverter.setDelimiter(';');

        // Expected results
        int[] intArray = new int[]{1111, 2222, 3333, 4444};
        String stringA = "1,111; 2,222; 3,333; 4,444";
        String stringB = intArray[0] + ";" + intArray[1] + ";" + intArray[2] + ";" + intArray[3];
        String[] strArray = new String[]{"" + intArray[0], "" + intArray[1], "" + intArray[2], "" + intArray[3]};
        long[] longArray = new long[]{intArray[0], intArray[1], intArray[2], intArray[3]};
        Long[] LONGArray = new Long[]{(long) intArray[0], (long) intArray[1], (long) intArray[2], (long) intArray[3]};
        Integer[] IntegerArray = new Integer[]{intArray[0], intArray[1], intArray[2], intArray[3]};
        ArrayList strList = new ArrayList();
        ArrayList longList = new ArrayList();
        for (int i = 0; i < strArray.length; i++) {
            strList.add(strArray[i]);
            longList.add(LONGArray[i]);
        }


        String msg = null;

        // String --> int[]
        try {
            msg = "String --> int[]";
            checkArray(msg, intArray, arrayConverter.convert(int[].class, stringA));
        } catch (Exception e) {
            fail(msg + " failed " + e);
        }

        // String --> int[] (with braces)
        try {
            msg = "String --> Integer[] (with braces)";
            checkArray(msg, IntegerArray, arrayConverter.convert(Integer[].class, "{" + stringA + "}"));
        } catch (Exception e) {
            fail(msg + " failed " + e);
        }

        // String[] --> int[]
        try {
            msg = "String[] --> int[]";
            checkArray(msg, intArray, arrayConverter.convert(int[].class, strArray));
        } catch (Exception e) {
            fail(msg + " failed " + e);
        }

        // String[] --> Integer[]
        try {
            msg = "String[] --> Integer[]";
            checkArray(msg, IntegerArray, arrayConverter.convert(Integer[].class, strArray));
        } catch (Exception e) {
            fail(msg + " failed " + e);
        }

        // long[] --> int[]
        try {
            msg = "long[] --> int[]";
            checkArray(msg, intArray, arrayConverter.convert(int[].class, longArray));
        } catch (Exception e) {
            fail(msg + " failed " + e);
        }

        // Long --> int[]
        try {
            msg = "Long --> int[]";
            checkArray(msg, new int[]{LONGArray[0].intValue()}, arrayConverter.convert(int[].class, LONGArray[0]));
        } catch (Exception e) {
            fail(msg + " failed " + e);
        }

        // LONG[] --> int[]
        try {
            msg = "LONG[] --> int[]";
            checkArray(msg, intArray, arrayConverter.convert(int[].class, LONGArray));
        } catch (Exception e) {
            fail(msg + " failed " + e);
        }

        // Long --> String
        try {
            msg = "Long --> String";
            assertEquals(msg, LONGArray[0] + "", arrayConverter.convert(String.class, LONGArray[0]));
        } catch (Exception e) {
            fail(msg + " failed " + e);
        }

        // LONG[] --> String (first)
        try {
            msg = "LONG[] --> String (first)";
            assertEquals(msg, LONGArray[0] + "", arrayConverter.convert(String.class, LONGArray));
        } catch (Exception e) {
            fail(msg + " failed " + e);
        }

        // LONG[] --> String (all)
        try {
            msg = "LONG[] --> String (all)";
            arrayConverter.setOnlyFirstToString(false);
            assertEquals(msg, stringB, arrayConverter.convert(String.class, LONGArray));
        } catch (Exception e) {
            fail(msg + " failed " + e);
        }

        // Collection of Long --> String
        try {
            msg = "Collection of Long --> String";
            assertEquals(msg, stringB, arrayConverter.convert(String.class, longList));
        } catch (Exception e) {
            fail(msg + " failed " + e);
        }

        // LONG[] --> String[]
        try {
            msg = "long[] --> String[]";
            checkArray(msg, strArray, arrayConverter.convert(String[].class, LONGArray));
        } catch (Exception e) {
            fail(msg + " failed " + e);
        }

        // Collection of String --> Integer[]
        try {
            msg = "Collection of String --> Integer[]";
            checkArray(msg, IntegerArray, arrayConverter.convert(Integer[].class, strList));
        } catch (Exception e) {
            fail(msg + " failed " + e);
        }

        // Collection of Long --> int[]
        try {
            msg = "Collection of Long --> int[]";
            checkArray(msg, intArray, arrayConverter.convert(int[].class, longList));
        } catch (Exception e) {
            fail(msg + " failed " + e);
        }
    }

    /**
     * Test Converting a String[] to integer array (with leading/trailing whitespace)
     */
    public void testStringArrayToNumber() {

        // Configure Converter
        IntegerConverter intConverter = new IntegerConverter();
        ArrayConverter arrayConverter = new ArrayConverter(int[].class, intConverter);

        // Test Data
        String[] array = new String[]{"10", "  11", "12  ", "  13  "};
        ArrayList list = new ArrayList();
        for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
        }

        // Expected results
        String msg = null;
        int[] expectedInt = new int[]{10, 11, 12, 13};
        Integer[] expectedInteger = new Integer[]{expectedInt[0], expectedInt[1], expectedInt[2], expectedInt[3]};

        // Test String[] --> int[]
        try {
            msg = "String[] --> int[]";
            checkArray(msg, expectedInt, arrayConverter.convert(int[].class, array));
        } catch (Exception e) {
            fail(msg + " failed " + e);
        }

        // Test String[] --> Integer[]
        try {
            msg = "String[] --> Integer[]";
            checkArray(msg, expectedInteger, arrayConverter.convert(Integer[].class, array));
        } catch (Exception e) {
            fail(msg + " failed " + e);
        }

        // Test List --> int[]
        try {
            msg = "List --> int[]";
            checkArray(msg, expectedInt, arrayConverter.convert(int[].class, list));
        } catch (Exception e) {
            fail(msg + " failed " + e);
        }

        // Test List --> Integer[]
        try {
            msg = "List --> Integer[]";
            checkArray(msg, expectedInteger, arrayConverter.convert(Integer[].class, list));
        } catch (Exception e) {
            fail(msg + " failed " + e);
        }
    }

    /**
     * Test the Matrix!!!! (parses a String into a 2 dimensional integer array or matrix)
     */
    public void testTheMatrix() {

        // Test Date - create the Matrix!!
        // Following String uses two delimiter:
        //     - comma (",") to separate individual numbers
        //     - semi-colon (";") to separate lists of numbers
        String matrixString = "11,12,13 ; 21,22,23 ; 31,32,33 ; 41,42,43";
        int[][] expected = new int[][]{new int[]{11, 12, 13},
                new int[]{21, 22, 23},
                new int[]{31, 32, 33},
                new int[]{41, 42, 43}};

        // Construct an Integer Converter
        IntegerConverter integerConverter = new IntegerConverter();

        // Construct an array Converter for an integer array (i.e. int[]) using
        // an IntegerConverter as the element converter.
        // N.B. Uses the default comma (i.e. ",") as the delimiter between individual numbers 
        ArrayConverter arrayConverter = new ArrayConverter(int[].class, integerConverter);

        // Construct a "Matrix" Converter which converts arrays of integer arrays using
        // the first (int[]) Converter as the element Converter.
        // N.B. Uses a semi-colon (i.e. ";") as the delimiter to separate the different sets of numbers.
        //      Also the delimiter for the above array Converter needs to be added to this
        //      array Converter's "allowed characters"
        ArrayConverter matrixConverter = new ArrayConverter(int[][].class, arrayConverter);
        matrixConverter.setDelimiter(';');
        matrixConverter.setAllowedChars(new char[]{','});

        try {
            // Do the Conversion
            Object result = matrixConverter.convert(int[][].class, matrixString);

            // Check it actually worked OK
            assertEquals("Check int[][].class", int[][].class, result.getClass());
            int[][] matrix = (int[][]) result;
            assertEquals("Check int[][] length", expected.length, matrix.length);
            for (int i = 0; i < expected.length; i++) {
                assertEquals("Check int[" + i + "] length", expected[i].length, matrix[i].length);
                for (int j = 0; j < expected[i].length; j++) {
                    String label = "Matrix int[" + i + "," + j + "] element";
                    assertEquals(label, expected[i][j], matrix[i][j]);
                    // System.out.println(label + " = " + matrix[i][j]);
                }
            }
        } catch (Exception e) {
            fail("Matrix Conversion threw " + e);
        }
    }

    /**
     * Test Converting using the IntegerConverter as the component Converter
     */
    public void testInvalidWithDefault() {
        int[] zeroArray = new int[0];
        int[] oneArray = new int[1];
        IntegerConverter intConverter = new IntegerConverter();

        assertNull("Null Default", new ArrayConverter(int[].class, intConverter, -1).convert(int[].class, null));
        checkArray("Zero Length", zeroArray, new ArrayConverter(int[].class, intConverter, 0).convert(int[].class, null));
        checkArray("One Length", oneArray, new ArrayConverter(Integer[].class, intConverter, 1).convert(int[].class, null));
    }

    /**
     * Test Empty String
     */
    public void testEmptyString() {
        int[] zeroArray = new int[0];
        IntegerConverter intConverter = new IntegerConverter();

        checkArray("Empty String", zeroArray, new ArrayConverter(int[].class, intConverter, -1).convert(int[].class, ""));
        assertNull("Default String", new ArrayConverter(int[].class, intConverter).convert(String.class, null));
    }

    /**
     * Test Errors creating the converter
     */
    public void testErrors() {
        try {
            new ArrayConverter(null, new DateConverter());
            fail("Default Type missing - expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected result
        }
        try {
            new ArrayConverter(Boolean.class, new DateConverter());
            fail("Default Type not an array - expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected result
        }
        try {
            new ArrayConverter(int[].class, null);
            fail("Component Converter missing - expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected result
        }
    }

    /**
     * Test for BEANUTILS-302 - throwing a NPE when underscore used
     */
    public void testUnderscore_BEANUTILS_302() {
        String value = "first_value,second_value";
        ArrayConverter converter = new ArrayConverter(String[].class, new StringConverter());

        // test underscore not allowed (the default)
        String[] result = (String[]) converter.convert(String[].class, value);
        assertNotNull("result.null", result);
        assertEquals("result.length", 4, result.length);
        assertEquals("result[0]", "first", result[0]);
        assertEquals("result[1]", "value", result[1]);
        assertEquals("result[2]", "second", result[2]);
        assertEquals("result[3]", "value", result[3]);

        // configure the converter to allow underscore
        converter.setAllowedChars(new char[]{'.', '-', '_'});

        // test underscore allowed
        result = (String[]) converter.convert(String[].class, value);
        assertNotNull("result.null", result);
        assertEquals("result.length", 2, result.length);
        assertEquals("result[0]", "first_value", result[0]);
        assertEquals("result[1]", "second_value", result[1]);
    }

    /**
     * Check that two arrays are the same.
     *
     * @param msg      Test prefix msg
     * @param expected Expected Array value
     * @param result   Result array value
     */
    private void checkArray(String msg, Object expected, Object result) {
        assertNotNull(msg + " Expected Null", expected);
        assertNotNull(msg + " Result   Null", result);
        assertTrue(msg + " Result   not array", result.getClass().isArray());
        assertTrue(msg + " Expected not array", expected.getClass().isArray());
        int resultLth = Array.getLength(result);
        assertEquals(msg + " Size", Array.getLength(expected), resultLth);
        assertEquals(msg + " Type", expected.getClass(), result.getClass());
        for (int i = 0; i < resultLth; i++) {
            Object expectElement = Array.get(expected, i);
            Object resultElement = Array.get(result, i);
            assertEquals(msg + " Element " + i, expectElement, resultElement);
        }
    }
}
