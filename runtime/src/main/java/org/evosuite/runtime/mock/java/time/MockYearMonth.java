package org.evosuite.runtime.mock.java.time;

import org.evosuite.runtime.mock.StaticReplacementMock;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * Created by gordon on 24/01/2016.
 */
public class MockYearMonth implements StaticReplacementMock {
    @Override
    public String getMockedClassName() {
        return YearMonth.class.getName();
    }

    public static YearMonth now() {
        return now(MockClock.systemDefaultZone());
    }

    public static YearMonth now(ZoneId zone) {
        return now(MockClock.system(zone));
    }

    public static YearMonth now(Clock clock) {
        return YearMonth.now(clock);
    }

    public static YearMonth of(int year, Month month) {
        return YearMonth.of(year, month);
    }

    public static YearMonth of(int year, int month) {
        return YearMonth.of(year, month);
    }

    public static YearMonth from(TemporalAccessor temporal) {
        return YearMonth.from(temporal);
    }

    public static YearMonth parse(CharSequence text) {
        return YearMonth.parse(text);
    }

    public static YearMonth parse(CharSequence text, DateTimeFormatter formatter) {
        return YearMonth.parse(text, formatter);
    }
}
