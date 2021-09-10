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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import junit.framework.TestCase;

import com.examples.with.different.packagename.testcarver.ConversionException;


/**
 * Abstract base for &lt;Number&gt;Converter classes.
 *
 * @author Rodney Waldhoff
 * @version $Revision: 541692 $ $Date: 2007-05-25 16:34:19 +0100 (Fri, 25 May 2007) $
 */

public abstract class NumberConverterTestBase extends TestCase {

    /**
     * Test Number values
     */
    protected Number[] numbers = new Number[4];

    // ------------------------------------------------------------------------

    public NumberConverterTestBase(String name) {
        super(name);
    }

    // ------------------------------------------------------------------------

    protected abstract NumberConverter makeConverter();

    protected abstract NumberConverter makeConverter(Object defaultValue);

    protected abstract Class getExpectedType();

    // ------------------------------------------------------------------------

    /**
     * Assumes ConversionException in response to covert(getExpectedType(),null).
     */
    public void testConvertNull() {
        try {
            makeConverter().convert(getExpectedType(), null);
            fail("Expected ConversionException");
        } catch (ConversionException e) {
            // expected
        }
    }

    /**
     * Assumes convert(getExpectedType(),Number) returns some non-null
     * instance of getExpectedType().
     */
    public void testConvertNumber() {
        String[] message = {
                "from Byte",
                "from Short",
                "from Integer",
                "from Long",
                "from Float",
                "from Double",
                "from BigDecimal",
                "from BigInteger",
                "from Integer array",
        };

        Object[] number = {
                (byte) 7,
                (short) 8,
                9,
                10L,
                11.1f,
                12.2,
                new BigDecimal("17.2"),
                new BigInteger("33"),
                new Integer[]{3, 2, 1}
        };

        for (int i = 0; i < number.length; i++) {
            Object val = makeConverter().convert(getExpectedType(), number[i]);
            assertNotNull("Convert " + message[i] + " should not be null", val);
            assertTrue(
                    "Convert " + message[i] + " should return a " + getExpectedType().getName(),
                    getExpectedType().isInstance(val));
        }
    }

    /**
     * Convert Number --> String (using a Pattern, with default and specified Locales)
     */
    public void testNumberToStringPattern() {

        // Re-set the default Locale to Locale.US
        Locale defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);

        NumberConverter converter = makeConverter();
        converter.setPattern("[0,0.0];(0,0.0)");

        // Default Locale
        assertEquals("Default Locale " + numbers[0], "(12.0)", converter.convert(String.class, numbers[0]));
        assertEquals("Default Locale " + numbers[1], "[13.0]", converter.convert(String.class, numbers[1]));

        // Locale.GERMAN
        converter.setLocale(Locale.GERMAN);
        assertEquals("Locale.GERMAN " + numbers[2], "(22,0)", converter.convert(String.class, numbers[2]));
        assertEquals("Locale.GERMAN " + numbers[3], "[23,0]", converter.convert(String.class, numbers[3]));

        // Restore the default Locale
        Locale.setDefault(defaultLocale);
    }

    /**
     * Convert Number --> String (using default and specified Locales)
     */
    public void testNumberToStringLocale() {

        // Re-set the default Locale to Locale.US
        Locale defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);

        NumberConverter converter = makeConverter();
        converter.setUseLocaleFormat(true);

        // Default Locale
        assertEquals("Default Locale " + numbers[0], "-12", converter.convert(String.class, numbers[0]));
        assertEquals("Default Locale " + numbers[1], "13", converter.convert(String.class, numbers[1]));

        // Locale.GERMAN
        converter.setLocale(Locale.GERMAN);
        assertEquals("Locale.GERMAN " + numbers[2], "-22", converter.convert(String.class, numbers[2]));
        assertEquals("Locale.GERMAN " + numbers[3], "23", converter.convert(String.class, numbers[3]));

        // Restore the default Locale
        Locale.setDefault(defaultLocale);
    }

    /**
     * Convert Array --> Number
     */
    public void testStringArrayToInteger() {

        Integer defaultValue = -1;
        NumberConverter converter = makeConverter(defaultValue);

        // Default Locale
        assertEquals("Valid First", 5, converter.convert(Integer.class, new String[]{"5", "4", "3"}));
        assertEquals("Invalid First", defaultValue, converter.convert(Integer.class, new String[]{"FOO", "1", "2"}));
        assertEquals("Null First", defaultValue, converter.convert(Integer.class, new String[]{null, "1", "2"}));
        assertEquals("Long Array", 9, converter.convert(Integer.class, new long[]{9, 2, 6}));
    }

    /**
     * Convert Number --> String (default conversion)
     */
    public void testNumberToStringDefault() {

        NumberConverter converter = makeConverter();

        // Default Number --> String conversion
        assertEquals("Default Convert " + numbers[0], numbers[0].toString(), converter.convert(String.class, numbers[0]));
        assertEquals("Default Convert " + numbers[1], numbers[1].toString(), converter.convert(String.class, numbers[1]));

    }

    /**
     * Convert String --> Number (using a Pattern, with default and specified Locales)
     */
    public void testStringToNumberPattern() {

        // Re-set the default Locale to Locale.US
        Locale defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);

        NumberConverter converter = makeConverter();
        converter.setPattern("[0,0];(0,0)");

        // Default Locale
        assertEquals("Default Locale " + numbers[0], numbers[0], converter.convert(getExpectedType(), "(1,2)"));
        assertEquals("Default Locale " + numbers[1], numbers[1], converter.convert(getExpectedType(), "[1,3]"));

        // Locale.GERMAN
        converter.setLocale(Locale.GERMAN);
        assertEquals("Locale.GERMAN " + numbers[2], numbers[2], converter.convert(getExpectedType(), "(2.2)"));
        assertEquals("Locale.GERMAN " + numbers[3], numbers[3], converter.convert(getExpectedType(), "[2.3]"));

        // Invalid Value
        try {
            converter.convert(getExpectedType(), "1,2");
            fail("Expected invalid value to cause ConversionException");
        } catch (Exception e) {
            // expected result
        }

        // Invalid Type (will try via String)
        Object obj = new Object() {
            public String toString() {
                return "dsdgsdsdg";
            }
        };
        try {
            converter.convert(getExpectedType(), obj);
            fail("Expected invalid value to cause ConversionException");
        } catch (Exception e) {
            // expected result
        }

        // Restore the default Locale
        Locale.setDefault(defaultLocale);
    }

    /**
     * Convert String --> Number (using default and specified Locales)
     */
    public void testStringToNumberLocale() {

        // Re-set the default Locale to Locale.US
        Locale defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);

        NumberConverter converter = makeConverter();
        converter.setUseLocaleFormat(true);

        // Default Locale
        assertEquals("Default Locale " + numbers[0], numbers[0], converter.convert(getExpectedType(), "-0,012"));
        assertEquals("Default Locale " + numbers[1], numbers[1], converter.convert(getExpectedType(), "0,013"));

        // Invalid Value
        try {
            converter.convert(getExpectedType(), "0,02x");
            fail("Expected invalid value to cause ConversionException");
        } catch (Exception e) {
            // expected result
        }

        // Locale.GERMAN
        converter.setLocale(Locale.GERMAN);
        assertEquals("Locale.GERMAN " + numbers[2], numbers[2], converter.convert(getExpectedType(), "-0.022"));
        assertEquals("Locale.GERMAN " + numbers[3], numbers[3], converter.convert(getExpectedType(), "0.023"));

        // Invalid Value
        try {
            converter.convert(getExpectedType(), "0.02x");
            fail("Expected invalid value to cause ConversionException");
        } catch (Exception e) {
            // expected result
        }

        // Restore the default Locale
        Locale.setDefault(defaultLocale);
    }

    /**
     * Convert String --> Number (default conversion)
     */
    public void testStringToNumberDefault() {

        NumberConverter converter = makeConverter();
        converter.setUseLocaleFormat(false);

        // Default String --> Number conversion
        assertEquals("Default Convert " + numbers[0], numbers[0], converter.convert(getExpectedType(), numbers[0].toString()));

        // Invalid
        try {
            converter.convert(getExpectedType(), "12x");
            fail("Expected invalid value to cause ConversionException");
        } catch (Exception e) {
            // expected result
        }
    }

    /**
     * Convert Boolean --> Number (default conversion)
     */
    public void testBooleanToNumberDefault() {

        NumberConverter converter = makeConverter();

        // Other type --> String conversion
        assertEquals("Boolean.FALSE to Number ", 0, ((Number) converter.convert(getExpectedType(), Boolean.FALSE)).intValue());
        assertEquals("Boolean.TRUE to Number ", 1, ((Number) converter.convert(getExpectedType(), Boolean.TRUE)).intValue());

    }

    /**
     * Convert Date --> Long
     */
    public void testDateToNumber() {

        NumberConverter converter = makeConverter();

        Date dateValue = new Date();
        long longValue = dateValue.getTime();

        // Date --> Long conversion
        assertEquals("Date to Long", longValue, converter.convert(Long.class, dateValue));

        // Date --> Integer
        try {
            converter.convert(Integer.class, dateValue);
            fail("Date to Integer - expected a ConversionException");
        } catch (ConversionException e) {
            // expected result - too large for Integer
        }

    }

    /**
     * Convert Calendar --> Long
     */
    public void testCalendarToNumber() {

        NumberConverter converter = makeConverter();

        Calendar calendarValue = Calendar.getInstance();
        long longValue = calendarValue.getTime().getTime();

        // Calendar --> Long conversion
        assertEquals("Calendar to Long", longValue, converter.convert(Long.class, calendarValue));

        // Calendar --> Integer
        try {
            converter.convert(Integer.class, calendarValue);
            fail("Calendar to Integer - expected a ConversionException");
        } catch (ConversionException e) {
            // expected result - too large for Integer
        }

    }

    /**
     * Convert Other --> String (default conversion)
     */
    public void testOtherToStringDefault() {

        NumberConverter converter = makeConverter();

        // Other type --> String conversion
        assertEquals("Default Convert ", "ABC", converter.convert(String.class, new StringBuffer("ABC")));

    }

    /**
     * Convert Number --> String (using default and specified Locales)
     */
    public void testInvalidDefault() {

        Object defaultvalue = numbers[0];
        NumberConverter converter = makeConverter(defaultvalue);

        // Default String --> Number conversion
        assertEquals("Invalid null ", defaultvalue, converter.convert(getExpectedType(), null));
        assertEquals("Default XXXX ", defaultvalue, converter.convert(getExpectedType(), "XXXX"));
    }

    /**
     * Convert Number --> String (using default and specified Locales)
     */
    public void testInvalidException() {

        NumberConverter converter = makeConverter();

        try {
            converter.convert(getExpectedType(), null);
            fail("Null test, expected ConversionException");
        } catch (ConversionException e) {
            // expected result
        }
        try {
            converter.convert(getExpectedType(), "XXXX");
            fail("Invalid test, expected ConversionException");
        } catch (ConversionException e) {
            // expected result
        }
    }

    /**
     * Test specifying an invalid type.
     */
    public void testInvalidType() {

        NumberConverter converter = makeConverter();

        try {
            converter.convert(Object.class, numbers[0]);
            fail("Invalid type test, expected ConversionException");
        } catch (ConversionException e) {
            // expected result
        }
    }
}

