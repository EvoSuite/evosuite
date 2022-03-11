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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import junit.framework.TestCase;
import com.examples.with.different.packagename.testcarver.Converter;
import com.examples.with.different.packagename.testcarver.ConversionException;

/**
 * Abstract base for &lt;Date&gt;Converter classes.
 *
 * @version $Revision: 471689 $ $Date: 2006-11-06 10:52:49 +0000 (Mon, 06 Nov 2006) $
 */

public abstract class DateConverterTestBase extends TestCase {

    // ------------------------------------------------------------------------

    /**
     * Construtc a new test case.
     *
     * @param name Name of the test
     */
    public DateConverterTestBase(String name) {
        super(name);
    }

    // ------------------------------------------------------------------------

    /**
     * Create the Converter with no default value.
     *
     * @return A new Converter
     */
    protected abstract DateTimeConverter makeConverter();

    /**
     * Create the Converter with a default value.
     *
     * @param defaultValue The default value
     * @return A new Converter
     */
    protected abstract DateTimeConverter makeConverter(Object defaultValue);

    /**
     * Return the expected type
     *
     * @return The expected type
     */
    protected abstract Class getExpectedType();

    /**
     * Convert from a Calendar to the appropriate Date type
     *
     * @param value The Calendar value to convert
     * @return The converted value
     */
    protected abstract Object toType(Calendar value);

    // ------------------------------------------------------------------------

    /**
     * Assumes ConversionException in response to covert(getExpectedType(), null).
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
     * Assumes convert() returns some non-null
     * instance of getExpectedType().
     */
    public void testConvertDate() {
        String[] message = {
                "from Date",
                "from Calendar",
                "from SQL Date",
                "from SQL Time",
                "from SQL Timestamp"
        };

        long now = System.currentTimeMillis();

        Object[] date = {
                new Date(now),
                new java.util.GregorianCalendar(),
                new java.sql.Date(now),
                new java.sql.Time(now),
                new java.sql.Timestamp(now)
        };

        // Initialize calendar also with same ms to avoid a failing test in a new time slice
        ((GregorianCalendar) date[1]).setTime(new Date(now));

        for (int i = 0; i < date.length; i++) {
            Object val = makeConverter().convert(getExpectedType(), date[i]);
            assertNotNull("Convert " + message[i] + " should not be null", val);
            assertTrue("Convert " + message[i] + " should return a " + getExpectedType().getName(),
                    getExpectedType().isInstance(val));
            assertEquals("Convert " + message[i] + " should return a " + date[0],
                    now, getTimeInMillis(val));
        }
    }

    /**
     * Test Default Type conversion (i.e. don't specify target type)
     */
    public void testDefaultType() {
        String pattern = "yyyy-MM-dd";

        // Create & Configure the Converter
        DateTimeConverter converter = makeConverter();
        converter.setPattern(pattern);

        // Valid String --> Type Conversion
        String testString = "2006-10-29";
        Calendar calendar = toCalendar(testString, pattern, null);
        Object expected = toType(calendar);

        Object result = converter.convert(null, testString);
        if (getExpectedType().equals(Calendar.class)) {
            assertTrue("TYPE ", getExpectedType().isAssignableFrom(result.getClass()));
        } else {
            assertEquals("TYPE ", getExpectedType(), result.getClass());
        }
        assertEquals("VALUE ", expected, result);
    }

    /**
     * Test default String to type conversion
     * <p>
     * N.B. This method is overriden by test case
     * implementations for java.sql.Date/Time/Timestamp
     */
    public void testDefaultStringToTypeConvert() {

        // Create & Configure the Converter
        DateTimeConverter converter = makeConverter();
        converter.setUseLocaleFormat(false);
        try {
            converter.convert(getExpectedType(), "2006-10-23");
            fail("Expected Conversion exception");
        } catch (ConversionException e) {
            // expected result
        }

    }

    /**
     * Test Conversion to String
     */
    public void testStringConversion() {

        String pattern = "yyyy-MM-dd";

        // Create & Configure the Converter
        DateTimeConverter converter = makeConverter();
        converter.setPattern(pattern);

        // Create Values
        String expected = "2006-10-29";
        Calendar calendar = toCalendar(expected, pattern, null);

        // Type --> String Conversion
        stringConversion(converter, expected, toType(calendar));

        // Calendar --> String Conversion
        stringConversion(converter, expected, calendar);

        // java.util.Date --> String Conversion
        stringConversion(converter, expected, toDate(calendar));

        // java.sql.Date --> String Conversion
        stringConversion(converter, expected, toSqlDate(calendar));

        // java.sql.Timestamp --> String Conversion
        stringConversion(converter, expected, toSqlTimestamp(calendar));

        // java.sql.Time --> String Conversion
        stringConversion(converter, expected, toSqlTime(calendar));

        stringConversion(converter, null, null);
        stringConversion(converter, "", "");

    }

    /**
     * Test Converter with no default value
     */
    public void testPatternNoDefault() {

        String pattern = "yyyy-MM-dd";

        // Create & Configure the Converter
        DateTimeConverter converter = makeConverter();
        converter.setPattern(pattern);

        // Valid String --> Type Conversion
        String testString = "2006-10-29";
        Calendar calendar = toCalendar(testString, pattern, null);
        Object expected = toType(calendar);
        validConversion(converter, expected, testString);

        // Valid java.util.Date --> Type Conversion
        validConversion(converter, expected, calendar);

        // Valid Calendar --> Type Conversion
        validConversion(converter, expected, toDate(calendar));

        // Test java.sql.Date --> Type Conversion
        validConversion(converter, expected, toSqlDate(calendar));

        // java.sql.Timestamp --> String Conversion
        validConversion(converter, expected, toSqlTimestamp(calendar));

        // java.sql.Time --> String Conversion
        validConversion(converter, expected, toSqlTime(calendar));

        // Invalid Conversions
        invalidConversion(converter, null);
        invalidConversion(converter, "");
        invalidConversion(converter, "2006-10-2X");
        invalidConversion(converter, "2006/10/01");
        invalidConversion(converter, "02/10/2006");
        invalidConversion(converter, "02/10/06");
        invalidConversion(converter, 2);

    }

    /**
     * Test Converter with no default value
     */
    public void testPatternDefault() {

        String pattern = "yyyy-MM-dd";

        // Create & Configure the Converter
        Object defaultValue = toType("2000-01-01", pattern, null);
        assertNotNull("Check default date", defaultValue);
        DateTimeConverter converter = makeConverter(defaultValue);
        converter.setPattern(pattern);

        // Valid String --> Type Conversion
        String testString = "2006-10-29";
        Object expected = toType(testString, pattern, null);
        validConversion(converter, expected, testString);

        // Invalid Values, expect default value
        validConversion(converter, defaultValue, null);
        validConversion(converter, defaultValue, "");
        validConversion(converter, defaultValue, "2006-10-2X");
        validConversion(converter, defaultValue, "2006/10/01");
        validConversion(converter, defaultValue, "02/10/06");
        validConversion(converter, defaultValue, 2);

    }

    /**
     * Test Converter with no default value
     */
    public void testPatternNullDefault() {

        String pattern = "yyyy-MM-dd";

        // Create & Configure the Converter
        Object defaultValue = null;
        DateTimeConverter converter = makeConverter(defaultValue);
        converter.setPattern(pattern);

        // Valid String --> Type Conversion
        String testString = "2006-10-29";
        Object expected = toType(testString, pattern, null);
        validConversion(converter, expected, testString);

        // Invalid Values, expect default --> null
        validConversion(converter, defaultValue, null);
        validConversion(converter, defaultValue, "");
        validConversion(converter, defaultValue, "2006-10-2X");
        validConversion(converter, defaultValue, "2006/10/01");
        validConversion(converter, defaultValue, "02/10/06");
        validConversion(converter, defaultValue, 2);

    }

    /**
     * Test Converter with multiple patterns
     */
    public void testMultiplePatterns() {
        String testString = null;
        Object expected = null;

        // Create & Configure the Converter
        String[] patterns = new String[]{"yyyy-MM-dd", "yyyy/MM/dd"};
        DateTimeConverter converter = makeConverter();
        converter.setPatterns(patterns);

        // First Pattern
        testString = "2006-10-28";
        expected = toType(testString, patterns[0], null);
        validConversion(converter, expected, testString);

        // Second pattern
        testString = "2006/10/18";
        expected = toType(testString, patterns[1], null);
        validConversion(converter, expected, testString);

        // Invalid Conversion
        invalidConversion(converter, "17/03/2006");
        invalidConversion(converter, "17.03.2006");

    }

    /**
     * Test Date Converter with no default value
     */
    public void testLocale() {

        // Re-set the default Locale to Locale.US
        Locale defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);

        String pattern = "M/d/yy"; // SHORT style date format for US Locale

        // Create & Configure the Converter
        DateTimeConverter converter = makeConverter();
        converter.setUseLocaleFormat(true);

        // Valid String --> Type Conversion
        String testString = "10/28/06";
        Object expected = toType(testString, pattern, null);
        validConversion(converter, expected, testString);

        // Invalid Conversions
        invalidConversion(converter, null);
        invalidConversion(converter, "");
        invalidConversion(converter, "2006-10-2X");
        invalidConversion(converter, "10.28.06");
        invalidConversion(converter, "10-28-06");
        invalidConversion(converter, 2);

        // Restore the default Locale
        Locale.setDefault(defaultLocale);

    }

    /**
     * Test Converter with types it can't handle
     */
    public void testInvalidType() {

        // Create & Configure the Converter
        DateTimeConverter converter = makeConverter();

        // Invalid Class Type
        try {
            converter.convert(Character.class, new Date());
            fail("Requested Character.class conversion, expected ConversionException");
        } catch (ConversionException e) {
            // Expected result
        }
    }

    /**
     * Test Conversion to the required type
     *
     * @param converter The converter to use
     * @param expected  The expected result
     * @param value     The value to convert
     */
    void validConversion(Converter converter, Object expected, Object value) {
        String valueType = (value == null ? "null" : value.getClass().getName());
        String msg = "Converting '" + valueType + "' value '" + value + "'";
        try {
            Object result = converter.convert(getExpectedType(), value);
            Class resultType = (result == null ? null : result.getClass());
            Class expectType = (expected == null ? null : expected.getClass());
            assertEquals("TYPE " + msg, expectType, resultType);
            assertEquals("VALUE " + msg, expected, result);
        } catch (Exception ex) {
            fail(msg + " threw " + ex.toString());
        }
    }

    /**
     * Test Conversion to String
     *
     * @param converter The converter to use
     * @param expected  The expected result
     * @param value     The value to convert
     */
    void stringConversion(Converter converter, String expected, Object value) {
        String valueType = (value == null ? "null" : value.getClass().getName());
        String msg = "Converting '" + valueType + "' value '" + value + "' to String";
        try {
            Object result = converter.convert(String.class, value);
            Class resultType = (result == null ? null : result.getClass());
            Class expectType = (expected == null ? null : expected.getClass());
            assertEquals("TYPE " + msg, expectType, resultType);
            assertEquals("VALUE " + msg, expected, result);
        } catch (Exception ex) {
            fail(msg + " threw " + ex.toString());
        }
    }

    /**
     * Test Conversion Error
     *
     * @param converter The converter to use
     * @param value     The value to convert
     */
    void invalidConversion(Converter converter, Object value) {
        String valueType = (value == null ? "null" : value.getClass().getName());
        String msg = "Converting '" + valueType + "' value '" + value + "'";
        try {
            Object result = converter.convert(getExpectedType(), value);
            fail(msg + ", expected ConversionException, but result = '" + result + "'");
        } catch (ConversionException ex) {
            // Expected Result
        }
    }

    /**
     * Parse a String value to the required type
     *
     * @param value   The String value to parse
     * @param pattern The date pattern
     * @param locale  The locale to use (or null)
     * @return parsed Calendar value
     */
    Object toType(String value, String pattern, Locale locale) {
        Calendar calendar = toCalendar(value, pattern, locale);
        return toType(calendar);
    }

    /**
     * Parse a String value to a Calendar
     *
     * @param value   The String value to parse
     * @param pattern The date pattern
     * @param locale  The locale to use (or null)
     * @return parsed Calendar value
     */
    Calendar toCalendar(String value, String pattern, Locale locale) {
        Calendar calendar = null;
        try {
            DateFormat format = (locale == null)
                    ? new SimpleDateFormat(pattern)
                    : new SimpleDateFormat(pattern, locale);
            format.setLenient(false);
            format.parse(value);
            calendar = format.getCalendar();
        } catch (Exception e) {
            fail("Error creating Calendar value ='"
                    + value + ", pattern='" + pattern + "' " + e.toString());
        }
        return calendar;
    }

    /**
     * Convert a Calendar to a java.util.Date
     *
     * @param calendar The calendar object to convert
     * @return The converted java.util.Date
     */
    Date toDate(Calendar calendar) {
        return calendar.getTime();
    }

    /**
     * Convert a Calendar to a java.sql.Date
     *
     * @param calendar The calendar object to convert
     * @return The converted java.sql.Date
     */
    java.sql.Date toSqlDate(Calendar calendar) {
        return new java.sql.Date(getTimeInMillis(calendar));
    }

    /**
     * Convert a Calendar to a java.sql.Time
     *
     * @param calendar The calendar object to convert
     * @return The converted java.sql.Time
     */
    java.sql.Time toSqlTime(Calendar calendar) {
        return new java.sql.Time(getTimeInMillis(calendar));
    }

    /**
     * Convert a Calendar to a java.sql.Timestamp
     *
     * @param calendar The calendar object to convert
     * @return The converted java.sql.Timestamp
     */
    java.sql.Timestamp toSqlTimestamp(Calendar calendar) {
        return new java.sql.Timestamp(getTimeInMillis(calendar));
    }

    /**
     * Convert a Date or Calendar objects to the time in millisconds
     *
     * @param date The date or calendar object
     * @return The time in milliseconds
     */
    long getTimeInMillis(Object date) {

        if (date instanceof java.sql.Timestamp) {
            // ---------------------- JDK 1.3 Fix ----------------------
            // N.B. Prior to JDK 1.4 the Timestamp's getTime() method
            //      didn't include the milliseconds. The following code
            //      ensures it works consistently accross JDK versions
            java.sql.Timestamp timestamp = (java.sql.Timestamp) date;
            long timeInMillis = ((timestamp.getTime() / 1000) * 1000);
            timeInMillis += timestamp.getNanos() / 1000000;
            return timeInMillis;
        }

        if (date instanceof Calendar) {
            return ((Calendar) date).getTime().getTime();
        } else {
            return ((Date) date).getTime();
        }
    }
}
