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
public class MockZonedDateTime implements StaticReplacementMock {
    @Override
    public String getMockedClassName() {
        return ZonedDateTime.class.getName();
    }

    public static ZonedDateTime now() {
        return now(MockClock.systemDefaultZone());
    }

    public static ZonedDateTime now(ZoneId zone) {
        return now(MockClock.system(zone));
    }

    public static ZonedDateTime now(Clock clock) {
        return ZonedDateTime.now(clock);
    }

    public static ZonedDateTime of(LocalDate date, LocalTime time, ZoneId zone) {
        return ZonedDateTime.of(date, time, zone);
    }

    public static ZonedDateTime of(LocalDateTime localDateTime, ZoneId zone) {
        return ZonedDateTime.of(localDateTime, zone);
    }

    public static ZonedDateTime of(
            int year, int month, int dayOfMonth,
            int hour, int minute, int second, int nanoOfSecond, ZoneId zone) {
        return ZonedDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond, zone);
    }


    public static ZonedDateTime ofLocal(LocalDateTime localDateTime, ZoneId zone, ZoneOffset preferredOffset) {
        return ZonedDateTime.ofLocal(localDateTime, zone, preferredOffset);
    }

    public static ZonedDateTime ofInstant(Instant instant, ZoneId zone) {
        return ZonedDateTime.ofInstant(instant, zone);
    }

    public static ZonedDateTime ofInstant(LocalDateTime localDateTime, ZoneOffset offset, ZoneId zone) {
        return ZonedDateTime.ofInstant(localDateTime, offset, zone);
    }

    public static ZonedDateTime ofStrict(LocalDateTime localDateTime, ZoneOffset offset, ZoneId zone) {
        return ZonedDateTime.ofStrict(localDateTime, offset, zone);
    }

    public static ZonedDateTime from(TemporalAccessor temporal) {
        return ZonedDateTime.from(temporal);
    }

    public static ZonedDateTime parse(CharSequence text) {
        return ZonedDateTime.parse(text);
    }

    public static ZonedDateTime parse(CharSequence text, DateTimeFormatter formatter) {
        return ZonedDateTime.parse(text, formatter);
    }
}
