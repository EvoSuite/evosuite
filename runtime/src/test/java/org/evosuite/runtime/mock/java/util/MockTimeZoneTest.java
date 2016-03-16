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
package org.evosuite.runtime.mock.java.util;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import java.sql.Time;
import java.util.TimeZone;

/**
 * Created by arcuri on 12/5/14.
 */
public class MockTimeZoneTest {

    @Test
    public void testGettingGMT(){
        TimeZone defaultTZ = TimeZone.getDefault();

        TimeZone gb = TimeZone.getTimeZone("GB"); //just need any non-GMT ones
        Assume.assumeNotNull(gb); //No point if for some reason GB is null

        try {
            TimeZone.setDefault(gb);
            MockTimeZone.reset();
            TimeZone res = TimeZone.getDefault();
            Assert.assertEquals("GMT",res.getID());
        } finally {
            TimeZone.setDefault(defaultTZ);
        }
    }
}
