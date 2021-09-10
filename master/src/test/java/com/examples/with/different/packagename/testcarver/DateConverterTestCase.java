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

import java.util.Calendar;
import java.util.Date;

import junit.framework.TestSuite;

/**
 * Test Case for the DateConverter class.
 *
 * @version $Revision: 471352 $
 */
public class DateConverterTestCase extends DateConverterTestBase {

    /**
     * Construct a new Date test case.
     *
     * @param name Test Name
     */
    public DateConverterTestCase(String name) {
        super(name);
    }

    // ------------------------------------------------------------------------

    /**
     * Create Test Suite
     *
     * @return test suite
     */
    public static TestSuite suite() {
        return new TestSuite(DateConverterTestCase.class);
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
    protected Class getExpectedType() {
        return Date.class;
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
}