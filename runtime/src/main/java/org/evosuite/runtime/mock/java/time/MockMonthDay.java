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
import java.time.temporal.TemporalAccessor;

/**
 * Created by gordon on 24/01/2016.
 */
public class MockMonthDay implements StaticReplacementMock {
    @Override
    public String getMockedClassName() {
        return MonthDay.class.getName();
    }

    public static MonthDay now() {
        return now(MockClock.systemDefaultZone());
    }

    public static MonthDay now(ZoneId zone) {
        return now(MockClock.system(zone));
    }

    public static MonthDay now(Clock clock) {
        return MonthDay.now(clock);
    }

    public static MonthDay of(Month month, int dayOfMonth) {
        return MonthDay.of(month, dayOfMonth);
    }

    public static MonthDay of(int month, int dayOfMonth) {
        return MonthDay.of(month, dayOfMonth);
    }

    public static MonthDay from(TemporalAccessor temporal) {
        return MonthDay.from(temporal);
    }

    public static MonthDay parse(CharSequence text) {
        return MonthDay.parse(text);
    }

    public static MonthDay parse(CharSequence text, DateTimeFormatter formatter) {
        return MonthDay.parse(text, formatter);
    }
}
