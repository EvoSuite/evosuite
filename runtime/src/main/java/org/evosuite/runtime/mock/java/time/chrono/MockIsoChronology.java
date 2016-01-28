package org.evosuite.runtime.mock.java.time.chrono;

import org.evosuite.runtime.mock.StaticReplacementMock;
import org.evosuite.runtime.mock.java.time.MockClock;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.chrono.IsoChronology;

/**
 * Created by gordon on 24/01/2016.
 */
public class MockIsoChronology implements StaticReplacementMock {
    @Override
    public String getMockedClassName() {
        return IsoChronology.class.getName();
    }

    public static LocalDate dateNow(IsoChronology instance) {
        return instance.dateNow(MockClock.systemDefaultZone());
    }

    public static LocalDate dateNow(IsoChronology instance, ZoneId zone) {
        return instance.dateNow(MockClock.system(zone));
    }

}
