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
package com.examples.with.different.packagename;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Test;

/**
 * Snippet from Lang project
 * (org.apache.commons.lang3.StringUtilsEqualsIndexOfTest)
 */
public class StringUtilsEqualsIndexOfTest {

    @Test
    public void testContainsIgnoreCase_LocaleIndependence() {
        final Locale[] locales = { Locale.ENGLISH, new Locale("tr"), Locale.getDefault() };

        final String[][] tdata = {
            { "i", "I" },
            { "I", "i" },
            { "\u03C2", "\u03C3" },
            { "\u03A3", "\u03C2" },
            { "\u03A3", "\u03C3" },
        };

        final String[][] fdata = {
            { "\u00DF", "SS" },
        };

        for (final Locale testLocale : locales) {
            Locale.setDefault(testLocale);
            for (int j = 0; j < tdata.length; j++) {
                assertTrue(Locale.getDefault() + ": " + j + " " + tdata[j][0] + " " + tdata[j][1], StringUtils
                        .containsIgnoreCase(tdata[j][0], tdata[j][1]));
            }
            for (int j = 0; j < fdata.length; j++) {
                assertFalse(Locale.getDefault() + ": " + j + " " + fdata[j][0] + " " + fdata[j][1], StringUtils
                        .containsIgnoreCase(fdata[j][0], fdata[j][1]));
            }
        }
    }
}
