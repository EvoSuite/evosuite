package org.evosuite.runtime.mock.java.time.chrono;

import org.evosuite.runtime.mock.StaticReplacementMock;
import org.evosuite.runtime.mock.java.time.MockClock;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.chrono.HijrahChronology;
import java.time.chrono.HijrahDate;

/**
 * Created by gordon on 24/01/2016.
 */
public class MockHijrahChronology implements StaticReplacementMock {
    @Override
    public String getMockedClassName() {
        return HijrahChronology.class.getName();
    }

    public static HijrahDate dateNow(HijrahChronology instance) {
        return instance.dateNow(MockClock.systemDefaultZone());
    }

    public static HijrahDate dateNow(HijrahChronology instance, ZoneId zone) {
        return instance.dateNow(MockClock.system(zone));
    }
}
