package org.evosuite.runtime.mock.java.time;

import org.evosuite.runtime.mock.StaticReplacementMock;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * Created by gordon on 24/01/2016.
 */
public class MockLocalTime implements StaticReplacementMock {
    @Override
    public String getMockedClassName() {
        return LocalTime.class.getName();
    }

    public static LocalTime now() {
        return now(MockClock.systemDefaultZone());
    }

    public static LocalTime now(ZoneId zone) {
        return now(MockClock.system(zone));
    }

    public static LocalTime now(Clock clock) {
        return LocalTime.now(clock);
    }

    public static LocalTime of(int hour, int minute) {
        return LocalTime.of(hour, minute);
    }

    public static LocalTime of(int hour, int minute, int second) {
        return LocalTime.of(hour, minute, second);
    }

    public static LocalTime of(int hour, int minute, int second, int nanoOfSecond) {
        return LocalTime.of(hour, minute, second, nanoOfSecond);
    }

    public static LocalTime ofSecondOfDay(long secondOfDay) {
        return LocalTime.ofSecondOfDay(secondOfDay);
    }

    public static LocalTime ofNanoOfDay(long nanoOfDay) {
        return LocalTime.ofNanoOfDay(nanoOfDay);
    }

    public static LocalTime from(TemporalAccessor temporal) {
        return LocalTime.from(temporal);
    }

    public static LocalTime parse(CharSequence text) {
        return LocalTime.parse(text);
    }

    public static LocalTime parse(CharSequence text, DateTimeFormatter formatter) {
        return LocalTime.parse(text, formatter);
    }

}
