package org.evosuite.runtime.mock.java.util;

import java.util.TimeZone;

/**
 * TimeZone is used all over the places in the Java API. So not much of the point to try to mock it,
 * as anyway we just need to handle the default time zone that is machine dependent.
 *
 * Created by arcuri on 12/5/14.
 */
public abstract class MockTimeZone extends TimeZone{

    private static final TimeZone cloneGMT = (TimeZone) TimeZone.getTimeZone("GMT").clone();

    public static void reset(){
        TimeZone.setDefault((TimeZone) cloneGMT.clone());
    }
}
