package org.evosuite.runtime.mock.java.time.chrono;

import org.evosuite.runtime.mock.StaticReplacementMock;
import org.evosuite.runtime.mock.java.time.MockClock;

import java.time.Clock;
import java.time.ZoneId;
import java.time.chrono.MinguoDate;
import java.time.temporal.TemporalAccessor;

/**
 * Created by gordon on 24/01/2016.
 */
public class MockMinguoDate implements StaticReplacementMock {
    @Override
    public String getMockedClassName() {
        return MinguoDate.class.getName();
    }

    public static MinguoDate now() {
        return now(MockClock.systemDefaultZone());
    }

    public static MinguoDate now(ZoneId zone) {
        return now(MockClock.system(zone));
    }

    public static MinguoDate now(Clock clock) {
        return MinguoDate.now(clock);
    }

    public static MinguoDate of(int prolepticYear, int month, int dayOfMonth) {
        return MinguoDate.of(prolepticYear, month, dayOfMonth);
    }

    public static MinguoDate from(TemporalAccessor temporal) {
        return MinguoDate.from(temporal);
    }

}
