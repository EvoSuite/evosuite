package org.evosuite.runtime.mock.java.time.chrono;

import org.evosuite.runtime.mock.StaticReplacementMock;
import org.evosuite.runtime.mock.java.time.MockClock;

import java.time.Clock;
import java.time.ZoneId;
import java.time.chrono.ThaiBuddhistChronology;
import java.time.chrono.ThaiBuddhistDate;

/**
 * Created by gordon on 24/01/2016.
 */
public class MockThaiBuddhistChronology implements StaticReplacementMock {
    @Override
    public String getMockedClassName() {
        return ThaiBuddhistChronology.class.getName();
    }

    public static ThaiBuddhistDate dateNow(ThaiBuddhistChronology instance) {
        return instance.dateNow(MockClock.systemDefaultZone());
    }

    public static ThaiBuddhistDate dateNow(ThaiBuddhistChronology instance, ZoneId zone) {
        return instance.dateNow(MockClock.system(zone));
    }

}
