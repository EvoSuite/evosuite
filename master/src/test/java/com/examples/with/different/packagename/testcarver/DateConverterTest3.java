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

import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.junit.Test;

public class DateConverterTest3 {


    /**
     * Create the Converter with no default value.
     *
     * @return A new Converter
     */
    protected DateTimeConverter makeConverter() {
        return new DateConverter();
    }

    /**
     * Create the Converter with a default value.
     *
     * @param defaultValue The default value
     * @return A new Converter
     */
    protected DateTimeConverter makeConverter(Object defaultValue) {
        return new DateConverter(defaultValue);
    }

    /**
     * Return the expected type
     *
     * @return The expected type
     */
    protected Class<?> getExpectedType() {
        return Date.class;
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
     * Convert from a Calendar to the appropriate Date type
     *
     * @param value The Calendar value to convert
     * @return The converted value
     */
    protected Object toType(Calendar value) {
        return value.getTime();
    }

    /**
     * Test Default Type conversion (i.e. don't specify target type)
     */
    @Test
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

}
