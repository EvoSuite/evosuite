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
package org.evosuite.runtime.mock.java.time;

import org.evosuite.runtime.mock.StaticReplacementMock;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;


/**
 * Created by gordon on 24/01/2016.
 */
public class MockLocalDate implements StaticReplacementMock {

    @Override
    public String getMockedClassName() {
        return LocalDate.class.getName();
    }

    // ---- static methods -------

    public static LocalDate now() {
        return now(MockClock.systemDefaultZone());
    }

    public static LocalDate now(ZoneId zone) {
        return now(MockClock.system(zone));
    }

    public static LocalDate now(Clock clock) {
        return LocalDate.now(clock);
    }

    public static LocalDate of(int year, Month month, int dayOfMonth) {
        return LocalDate.of(year, month, dayOfMonth);
    }

    public static LocalDate of(int year, int month, int dayOfMonth) {
        return LocalDate.of(year, month, dayOfMonth);
    }

    public static LocalDate ofYearDay(int year, int dayOfYear) {
        return LocalDate.ofYearDay(year, dayOfYear);
    }

    public static LocalDate ofEpochDay(long epochDay) {
        return LocalDate.ofEpochDay(epochDay);
    }

    public static LocalDate from(TemporalAccessor temporal) {
        return LocalDate.from(temporal);
    }

    public static LocalDate parse(CharSequence text) {
        return LocalDate.parse(text);
    }

    public static LocalDate parse(CharSequence text, DateTimeFormatter formatter) {
        return LocalDate.parse(text, formatter);
    }

}
