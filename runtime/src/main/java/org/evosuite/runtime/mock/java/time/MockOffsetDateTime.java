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
import java.util.Comparator;

/**
 * Created by gordon on 24/01/2016.
 */
public class MockOffsetDateTime implements StaticReplacementMock {
    @Override
    public String getMockedClassName() {
        return OffsetDateTime.class.getName();
    }

    public static Comparator<OffsetDateTime> timeLineOrder() {
        return OffsetDateTime.timeLineOrder();
    }

    public static OffsetDateTime now() {
        return now(MockClock.systemDefaultZone());
    }

    public static OffsetDateTime now(ZoneId zone) {
        return now(MockClock.system(zone));
    }

    public static OffsetDateTime now(Clock clock) {
        return OffsetDateTime.now(clock);
    }

    public static OffsetDateTime of(LocalDate date, LocalTime time, ZoneOffset offset) {
        return OffsetDateTime.of(date, time, offset);
    }

    public static OffsetDateTime of(LocalDateTime dateTime, ZoneOffset offset) {
        return OffsetDateTime.of(dateTime, offset);
    }

    public static OffsetDateTime of(
            int year, int month, int dayOfMonth,
            int hour, int minute, int second, int nanoOfSecond, ZoneOffset offset) {
        return OffsetDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond, offset);
    }

    public static OffsetDateTime ofInstant(Instant instant, ZoneId zone) {
        return OffsetDateTime.ofInstant(instant, zone);
    }

    public static OffsetDateTime from(TemporalAccessor temporal) {
        return OffsetDateTime.from(temporal);
    }

    public static OffsetDateTime parse(CharSequence text) {
        return OffsetDateTime.parse(text);
    }

    public static OffsetDateTime parse(CharSequence text, DateTimeFormatter formatter) {
        return OffsetDateTime.parse(text, formatter);
    }
}
