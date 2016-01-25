package org.evosuite.runtime.mock.java.time.chrono;

import org.evosuite.runtime.mock.StaticReplacementMock;
import org.evosuite.runtime.mock.java.time.MockClock;

import java.time.ZoneId;
import java.time.chrono.MinguoChronology;
import java.time.chrono.MinguoDate;

/**
 * Created by gordon on 25/01/2016.
 */
public class MockMinguoChronology implements StaticReplacementMock {
    @Override
    public String getMockedClassName() {
        return MinguoChronology.class.getName();
    }

    public static MinguoDate dateNow(MinguoChronology instance) {
        return instance.dateNow(MockClock.systemDefaultZone());
    }

    public static MinguoDate dateNow(MinguoChronology instance, ZoneId zone) {
        return instance.dateNow(MockClock.system(zone));
    }

}
