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
package org.evosuite.runtime.mock.java.time;

import org.evosuite.runtime.mock.StaticReplacementMock;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * Created by gordon on 24/01/2016.
 */
public class MockLocalTime implements StaticReplacementMock {
    @Override
    public String getMockedClassName() {
        return LocalTime.class.getName();
    }

    public static LocalTime now() {
        return now(MockClock.systemDefaultZone());
    }

    public static LocalTime now(ZoneId zone) {
        return now(MockClock.system(zone));
    }

    public static LocalTime now(Clock clock) {
        return LocalTime.now(clock);
    }

    public static LocalTime of(int hour, int minute) {
        return LocalTime.of(hour, minute);
    }

    public static LocalTime of(int hour, int minute, int second) {
        return LocalTime.of(hour, minute, second);
    }

    public static LocalTime of(int hour, int minute, int second, int nanoOfSecond) {
        return LocalTime.of(hour, minute, second, nanoOfSecond);
    }

    public static LocalTime ofSecondOfDay(long secondOfDay) {
        return LocalTime.ofSecondOfDay(secondOfDay);
    }

    public static LocalTime ofNanoOfDay(long nanoOfDay) {
        return LocalTime.ofNanoOfDay(nanoOfDay);
    }

    public static LocalTime from(TemporalAccessor temporal) {
        return LocalTime.from(temporal);
    }

    public static LocalTime parse(CharSequence text) {
        return LocalTime.parse(text);
    }

    public static LocalTime parse(CharSequence text, DateTimeFormatter formatter) {
        return LocalTime.parse(text, formatter);
    }

}
