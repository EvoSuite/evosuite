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
package org.evosuite.utils;

import org.junit.Assert;
import org.junit.Test;

import static org.evosuite.utils.SystemPathUtil.DELIMITER_MUST_NOT_BE_NULL_EXCEPTION_MESSAGE;
import static org.evosuite.utils.SystemPathUtil.ELEMENTS_MUST_NOT_BE_NULL_EXCEPTION_MESSAGE;
import static org.junit.Assert.assertTrue;

/**
 * @author Ignacio Lebrero
 */
public class SystemPathUtilTest {

    @Test
    public void testJoinWithDelimiter() {
        String a = "element1";
        String b = "element2";
        String c = "element3";

        Assert.assertEquals("element1_element2_element3", SystemPathUtil.joinWithDelimiter("_", a, b, c));
    }

    @Test
    public void testJoinWithDelimiterEmptyDelimiter() {
        String a = "element1";
        String b = "element2";
        String c = "element3";

        Assert.assertEquals("element1element2element3", SystemPathUtil.joinWithDelimiter("", a, b, c));
    }

    @Test
    public void testJoinWithDelimiterNullDelimiter() {
        String a = "element1";
        String b = "element2";
        String c = "element3";
        boolean exceptionThrown = false;

        try {
            SystemPathUtil.joinWithDelimiter(null, a, b, c);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().equals(DELIMITER_MUST_NOT_BE_NULL_EXCEPTION_MESSAGE)) {
                exceptionThrown = true;
            }
        }

        assertTrue(exceptionThrown);
    }

    @Test
    public void testJoinWithDelimiterNullElements() {
        boolean exceptionThrown = false;

        try {
            SystemPathUtil.joinWithDelimiter("_", null);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().equals(ELEMENTS_MUST_NOT_BE_NULL_EXCEPTION_MESSAGE)) {
                exceptionThrown = true;
            }
        }

        assertTrue(exceptionThrown);
    }

    @Test
    public void testJoinWithDelimiterEmptyString() {
        Assert.assertEquals("", SystemPathUtil.joinWithDelimiter("_"));
    }
}