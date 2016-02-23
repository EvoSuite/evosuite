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

import java.time.ZoneId;
import java.time.chrono.JapaneseChronology;
import java.time.chrono.JapaneseDate;

/**
 * Created by gordon on 24/01/2016.
 */
public class MockJapaneseChronology implements StaticReplacementMock {
    @Override
    public String getMockedClassName() {
        return JapaneseChronology.class.getName();
    }

    public static JapaneseDate dateNow(JapaneseChronology instance) {
        return instance.dateNow(MockClock.systemDefaultZone());
    }

    public static JapaneseDate dateNow(JapaneseChronology instance, ZoneId zone) {
        return instance.dateNow(MockClock.system(zone));
    }

}
