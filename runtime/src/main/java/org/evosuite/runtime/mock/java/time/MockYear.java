package org.evosuite.runtime.mock.java.time;

import org.evosuite.runtime.mock.StaticReplacementMock;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * Created by gordon on 24/01/2016.
 */
public class MockYear implements StaticReplacementMock {
    @Override
    public String getMockedClassName() {
        return Year.class.getName();
    }

    public static Year now() {
        return now(MockClock.systemDefaultZone());
    }

    public static Year now(ZoneId zone) {
        return now(MockClock.system(zone));
    }

    public static Year now(Clock clock) {
        return Year.now(clock);
    }

    public static Year of(int isoYear) {
        return Year.of(isoYear);
    }

    public static Year from(TemporalAccessor temporal) {
        return Year.from(temporal);
    }

    public static Year parse(CharSequence text) {
        return Year.parse(text);
    }

    public static Year parse(CharSequence text, DateTimeFormatter formatter) {
        return Year.parse(text, formatter);
    }

    public static boolean isLeap(long year) {
        return Year.isLeap(year);
    }

}
