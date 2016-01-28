package org.evosuite.runtime.mock.java.time.chrono;

import org.evosuite.runtime.mock.StaticReplacementMock;
import org.evosuite.runtime.mock.java.time.MockClock;

import java.time.Clock;
import java.time.ZoneId;
import java.time.chrono.ThaiBuddhistDate;
import java.time.temporal.TemporalAccessor;

/**
 * Created by gordon on 24/01/2016.
 */
public class MockThaiBuddhistDate implements StaticReplacementMock {
    @Override
    public String getMockedClassName() {
        return ThaiBuddhistDate.class.getName();
    }

    public static ThaiBuddhistDate now() {
        return now(MockClock.systemDefaultZone());
    }

    public static ThaiBuddhistDate now(ZoneId zone) {
        return now(MockClock.system(zone));
    }

    public static ThaiBuddhistDate now(Clock clock) {
        return ThaiBuddhistDate.now(clock);
    }

    public static ThaiBuddhistDate of(int prolepticYear, int month, int dayOfMonth) {
        return ThaiBuddhistDate.of(prolepticYear, month, dayOfMonth);
    }

    public static ThaiBuddhistDate from(TemporalAccessor temporal) {
        return ThaiBuddhistDate.from(temporal);
    }

}
