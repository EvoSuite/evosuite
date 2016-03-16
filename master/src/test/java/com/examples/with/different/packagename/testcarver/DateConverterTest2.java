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
package com.examples.with.different.packagename.testcarver;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;

public class DateConverterTest2 {

    /**
     * Create the Converter with no default value.
     * @return A new Converter
     */
    protected DateTimeConverter makeConverter() {
        return new DateConverter();
    }
    
    /**
     * Create the Converter with a default value.
     * @param defaultValue The default value
     * @return A new Converter
     */
    protected DateTimeConverter makeConverter(Object defaultValue) {
        return new DateConverter(defaultValue);
    }

    /**
     * Return the expected type
     * @return The expected type
     */
    protected Class<?> getExpectedType() {
        return Date.class;
    }

    /**
     * Convert a Date or Calendar objects to the time in millisconds
     * @param date The date or calendar object
     * @return The time in milliseconds
     */
    long getTimeInMillis(Object date) {

        if (date instanceof java.sql.Timestamp) {
            // ---------------------- JDK 1.3 Fix ----------------------
            // N.B. Prior to JDK 1.4 the Timestamp's getTime() method
            //      didn't include the milliseconds. The following code
            //      ensures it works consistently accross JDK versions
            java.sql.Timestamp timestamp = (java.sql.Timestamp)date;
            long timeInMillis = ((timestamp.getTime() / 1000) * 1000);
            timeInMillis += timestamp.getNanos() / 1000000;
            return timeInMillis;
        }

        if (date instanceof Calendar) {
            return ((Calendar)date).getTime().getTime();
        } else {
            return ((Date)date).getTime();
        }
    }
    
    /**
     * Assumes convert() returns some non-null
     * instance of getExpectedType().
     */
    @Test
    public void testConvertDate() {
        String[] message= {
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
        ((GregorianCalendar)date[1]).setTime(new Date(now));

        for (int i = 0; i < date.length; i++) {
            Object val = makeConverter().convert(getExpectedType(), date[i]);
            assertNotNull("Convert " + message[i] + " should not be null", val);
            assertTrue("Convert " + message[i] + " should return a " + getExpectedType().getName(),
                       getExpectedType().isInstance(val));
            assertEquals("Convert " + message[i] + " should return a " + date[0],
                         now, getTimeInMillis(val));
        }
    }
}
