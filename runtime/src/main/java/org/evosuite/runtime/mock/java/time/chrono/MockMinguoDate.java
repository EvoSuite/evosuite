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
package org.evosuite.runtime.mock.java.time.chrono;

import org.evosuite.runtime.mock.StaticReplacementMock;
import org.evosuite.runtime.mock.java.time.MockClock;

import java.time.Clock;
import java.time.ZoneId;
import java.time.chrono.MinguoDate;
import java.time.temporal.TemporalAccessor;

/**
 * Created by gordon on 24/01/2016.
 */
public class MockMinguoDate implements StaticReplacementMock {
    @Override
    public String getMockedClassName() {
        return MinguoDate.class.getName();
    }

    public static MinguoDate now() {
        return now(MockClock.systemDefaultZone());
    }

    public static MinguoDate now(ZoneId zone) {
        return now(MockClock.system(zone));
    }

    public static MinguoDate now(Clock clock) {
        return MinguoDate.now(clock);
    }

    public static MinguoDate of(int prolepticYear, int month, int dayOfMonth) {
        return MinguoDate.of(prolepticYear, month, dayOfMonth);
    }

    public static MinguoDate from(TemporalAccessor temporal) {
        return MinguoDate.from(temporal);
    }

}
