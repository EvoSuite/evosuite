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
public class MockOffsetTime implements StaticReplacementMock {
    @Override
    public String getMockedClassName() {
        return OffsetTime.class.getName();
    }

    public static OffsetTime now() {
        return now(MockClock.systemDefaultZone());
    }

    public static OffsetTime now(ZoneId zone) {
        return now(MockClock.system(zone));
    }

    public static OffsetTime now(Clock clock) {
        return OffsetTime.now(clock);
    }

    public static OffsetTime of(LocalTime time, ZoneOffset offset) {
        return OffsetTime.of(time, offset);
    }

    public static OffsetTime of(int hour, int minute, int second, int nanoOfSecond, ZoneOffset offset) {
        return OffsetTime.of(hour, minute, second, nanoOfSecond, offset);
    }

    public static OffsetTime ofInstant(Instant instant, ZoneId zone) {
        return OffsetTime.ofInstant(instant, zone);
    }

    public static OffsetTime from(TemporalAccessor temporal) {
        return OffsetTime.from(temporal);
    }

    public static OffsetTime parse(CharSequence text) {
        return OffsetTime.parse(text);
    }

    public static OffsetTime parse(CharSequence text, DateTimeFormatter formatter) {
        return OffsetTime.parse(text, formatter);
    }
}
