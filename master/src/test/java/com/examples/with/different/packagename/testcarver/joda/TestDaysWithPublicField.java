package com.examples.with.different.packagename.testcarver.joda;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by gordon on 20/12/2015.
 */
public class TestDaysWithPublicField {
    public static final DateTimeZone PARIS = new DateTimeZone();

    @Test
    public void testFactory_daysIn_RInterval() {
        Days d = new Days(PARIS);
        assertEquals(0, d.getDays());
    }
}