package org.evosuite.runtime;

import org.junit.Assert;

/**
 * Created by Andrea Arcuri on 19/08/15.
 */
public class EvoAssertions {

    public static void assertThrownBy(String sourceClass, Throwable t) throws AssertionError{
        StackTraceElement el = t.getStackTrace()[0];
        Assert.assertEquals(sourceClass , el.getClassName());
    }
}
