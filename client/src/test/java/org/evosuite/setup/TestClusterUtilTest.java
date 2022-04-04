/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.setup;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestClusterUtilTest {

    @Test
    public void testIsAnonymousWithValidName() {
        assertFalse(TestClusterUtils.isAnonymousClass(TestClusterUtilTest.class.getName()));
    }

    @Test
    public void testIsAnonymousWithVAnonymousClassName() {
        Object o = new Object() {
            // ...
        };
        assertTrue(TestClusterUtils.isAnonymousClass(o.getClass().getName()));
    }

    @Test
    public void testIsAnonymousWithNameEndingWithDollar() {
        assertFalse(TestClusterUtils.isAnonymousClass("Option$None$"));
    }
}
