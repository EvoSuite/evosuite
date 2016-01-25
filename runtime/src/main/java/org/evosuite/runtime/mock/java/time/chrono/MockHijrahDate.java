package org.evosuite.runtime.mock.java.time.chrono;

import org.evosuite.runtime.mock.StaticReplacementMock;
import org.evosuite.runtime.mock.java.time.MockClock;

import java.time.Clock;
import java.time.ZoneId;
import java.time.chrono.HijrahDate;
import java.time.temporal.TemporalAccessor;

/**
 * Created by gordon on 24/01/2016.
 */
public class MockHijrahDate implements StaticReplacementMock {
    @Override
    public String getMockedClassName() {
        return HijrahDate.class.getName();
    }

    public static HijrahDate now() {
        return now(MockClock.systemDefaultZone());
    }

    public static HijrahDate now(ZoneId zone) {
        return now(MockClock.system(zone));
    }

    public static HijrahDate now(Clock clock) {
        return HijrahDate.now(clock);
    }

    public static HijrahDate of(int prolepticYear, int month, int dayOfMonth) {
        return HijrahDate.of(prolepticYear, month, dayOfMonth);
    }

    public static HijrahDate from(TemporalAccessor temporal) {
        return HijrahDate.from(temporal);
    }

}
