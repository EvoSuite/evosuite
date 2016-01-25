package org.evosuite.runtime.mock.java.time;

import org.evosuite.runtime.mock.StaticReplacementMock;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * Created by gordon on 24/01/2016.
 */
public class MockOffsetTime implements StaticReplacementMock {
    @Override
    public String getMockedClassName() {
        return OffsetTime.class.getName();
    }

    public static OffsetTime now() {
        return now(MockClock.systemDefaultZone());
    }

    public static OffsetTime now(ZoneId zone) {
        return now(MockClock.system(zone));
    }

    public static OffsetTime now(Clock clock) {
        return OffsetTime.now(clock);
    }

    public static OffsetTime of(LocalTime time, ZoneOffset offset) {
        return OffsetTime.of(time, offset);
    }

    public static OffsetTime of(int hour, int minute, int second, int nanoOfSecond, ZoneOffset offset) {
        return OffsetTime.of(hour, minute, second, nanoOfSecond, offset);
    }

    public static OffsetTime ofInstant(Instant instant, ZoneId zone) {
        return OffsetTime.ofInstant(instant, zone);
    }

    public static OffsetTime from(TemporalAccessor temporal) {
        return OffsetTime.from(temporal);
    }

    public static OffsetTime parse(CharSequence text) {
        return OffsetTime.parse(text);
    }

    public static OffsetTime parse(CharSequence text, DateTimeFormatter formatter) {
        return OffsetTime.parse(text, formatter);
    }
}
