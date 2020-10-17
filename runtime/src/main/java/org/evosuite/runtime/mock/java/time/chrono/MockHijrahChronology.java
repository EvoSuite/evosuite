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
package org.evosuite.runtime.mock.java.time.chrono;

import org.evosuite.runtime.mock.StaticReplacementMock;
import org.evosuite.runtime.mock.java.time.MockClock;

import java.time.ZoneId;
import java.time.chrono.HijrahChronology;
import java.time.chrono.HijrahDate;

/**
 * Created by gordon on 24/01/2016.
 */
public class MockHijrahChronology implements StaticReplacementMock {
    @Override
    public String getMockedClassName() {
        return HijrahChronology.class.getName();
    }

    public static HijrahDate dateNow(HijrahChronology instance) {
        return instance.dateNow(MockClock.systemDefaultZone());
    }

    public static HijrahDate dateNow(HijrahChronology instance, ZoneId zone) {
        return instance.dateNow(MockClock.system(zone));
    }
}
