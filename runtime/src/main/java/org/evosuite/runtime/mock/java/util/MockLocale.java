package org.evosuite.runtime.mock.java.util;

import java.util.Locale;

/**
 * At least for the moment, no need to have a full mock
 *
 * Created by arcuri on 1/25/15.
 */
public class MockLocale { // extends Locale {


    public static void reset() {
        Locale.setDefault(Locale.ENGLISH);
    }
}