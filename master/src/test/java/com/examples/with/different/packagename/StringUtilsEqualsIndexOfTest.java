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
