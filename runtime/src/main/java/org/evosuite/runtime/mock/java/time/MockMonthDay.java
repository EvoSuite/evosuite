package org.evosuite.runtime.mock.java.time;

import org.evosuite.runtime.mock.StaticReplacementMock;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * Created by gordon on 24/01/2016.
 */
public class MockMonthDay implements StaticReplacementMock {
    @Override
    public String getMockedClassName() {
        return MonthDay.class.getName();
    }

    public static MonthDay now() {
        return now(MockClock.systemDefaultZone());
    }

    public static MonthDay now(ZoneId zone) {
        return now(MockClock.system(zone));
    }

    public static MonthDay now(Clock clock) {
        return MonthDay.now(clock);
    }

    public static MonthDay of(Month month, int dayOfMonth) {
        return MonthDay.of(month, dayOfMonth);
    }

    public static MonthDay of(int month, int dayOfMonth) {
        return MonthDay.of(month, dayOfMonth);
    }

    public static MonthDay from(TemporalAccessor temporal) {
        return MonthDay.from(temporal);
    }

    public static MonthDay parse(CharSequence text) {
        return MonthDay.parse(text);
    }

    public static MonthDay parse(CharSequence text, DateTimeFormatter formatter) {
        return MonthDay.parse(text, formatter);
    }
}
