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
public class MockLocalDateTime implements StaticReplacementMock {

    @Override
    public String getMockedClassName() {
        return LocalDateTime.class.getName();
    }

    public static LocalDateTime now() {
        return now(MockClock.systemDefaultZone());
    }

    public static LocalDateTime now(ZoneId zone) {
        return now(MockClock.system(zone));
    }

    public static LocalDateTime now(Clock clock) {
        return LocalDateTime.now(clock);
    }

    public static LocalDateTime of(int year, Month month, int dayOfMonth, int hour, int minute) {
        return LocalDateTime.of(year, month, dayOfMonth, hour, minute);
    }

    public static LocalDateTime of(int year, Month month, int dayOfMonth, int hour, int minute, int second) {
        return LocalDateTime.of(year, month, dayOfMonth, hour, minute, second);
    }

    public static LocalDateTime of(int year, Month month, int dayOfMonth, int hour, int minute, int second, int nanoOfSecond) {
        return LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond);
    }

    public static LocalDateTime of(int year, int month, int dayOfMonth, int hour, int minute) {
        return LocalDateTime.of(year, month, dayOfMonth, hour, minute);
    }


    public static LocalDateTime of(int year, int month, int dayOfMonth, int hour, int minute, int second) {
        return LocalDateTime.of(year, month, dayOfMonth, hour, minute, second);
    }


    public static LocalDateTime of(int year, int month, int dayOfMonth, int hour, int minute, int second, int nanoOfSecond) {
        return LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond);
    }


    public static LocalDateTime of(LocalDate date, LocalTime time) {
        return LocalDateTime.of(date, time);
    }

    public static LocalDateTime ofInstant(Instant instant, ZoneId zone) {
        return LocalDateTime.ofInstant(instant, zone);
    }

    public static LocalDateTime ofEpochSecond(long epochSecond, int nanoOfSecond, ZoneOffset offset) {
        return LocalDateTime.ofEpochSecond(epochSecond, nanoOfSecond, offset);
    }

    public static LocalDateTime from(TemporalAccessor temporal) {
        return LocalDateTime.from(temporal);
    }

    public static LocalDateTime parse(CharSequence text) {
        return LocalDateTime.parse(text);
    }

    public static LocalDateTime parse(CharSequence text, DateTimeFormatter formatter) {
        return LocalDateTime.parse(text, formatter);
    }
}
