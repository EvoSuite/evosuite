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
