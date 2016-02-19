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
public class MockYearMonth implements StaticReplacementMock {
    @Override
    public String getMockedClassName() {
        return YearMonth.class.getName();
    }

    public static YearMonth now() {
        return now(MockClock.systemDefaultZone());
    }

    public static YearMonth now(ZoneId zone) {
        return now(MockClock.system(zone));
    }

    public static YearMonth now(Clock clock) {
        return YearMonth.now(clock);
    }

    public static YearMonth of(int year, Month month) {
        return YearMonth.of(year, month);
    }

    public static YearMonth of(int year, int month) {
        return YearMonth.of(year, month);
    }

    public static YearMonth from(TemporalAccessor temporal) {
        return YearMonth.from(temporal);
    }

    public static YearMonth parse(CharSequence text) {
        return YearMonth.parse(text);
    }

    public static YearMonth parse(CharSequence text, DateTimeFormatter formatter) {
        return YearMonth.parse(text, formatter);
    }
}
