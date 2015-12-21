package com.examples.with.different.packagename.testcarver.joda;

import org.junit.Test;

import static junit.framework.TestCase.fail;

/**
 * Created by gordon on 20/12/2015.
 */
public class TestScaledDurationFieldWithException {
    @Test
    public void test_constructor() {
        try {
            new ScaledDurationField(null, 10);
            fail();
        } catch (IllegalArgumentException ex) {}
    }
}
