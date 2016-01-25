package org.evosuite.runtime.mock.java.time;

import org.evosuite.runtime.mock.StaticReplacementMock;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * Created by gordon on 24/01/2016.
 */
public class MockZonedDateTime implements StaticReplacementMock {
    @Override
    public String getMockedClassName() {
        return ZonedDateTime.class.getName();
    }

    public static ZonedDateTime now() {
        return now(MockClock.systemDefaultZone());
    }

    public static ZonedDateTime now(ZoneId zone) {
        return now(MockClock.system(zone));
    }

    public static ZonedDateTime now(Clock clock) {
        return ZonedDateTime.now(clock);
    }

    public static ZonedDateTime of(LocalDate date, LocalTime time, ZoneId zone) {
        return ZonedDateTime.of(date, time, zone);
    }

    public static ZonedDateTime of(LocalDateTime localDateTime, ZoneId zone) {
        return ZonedDateTime.of(localDateTime, zone);
    }

    public static ZonedDateTime of(
            int year, int month, int dayOfMonth,
            int hour, int minute, int second, int nanoOfSecond, ZoneId zone) {
        return ZonedDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond, zone);
    }


    public static ZonedDateTime ofLocal(LocalDateTime localDateTime, ZoneId zone, ZoneOffset preferredOffset) {
        return ZonedDateTime.ofLocal(localDateTime, zone, preferredOffset);
    }

    public static ZonedDateTime ofInstant(Instant instant, ZoneId zone) {
        return ZonedDateTime.ofInstant(instant, zone);
    }

    public static ZonedDateTime ofInstant(LocalDateTime localDateTime, ZoneOffset offset, ZoneId zone) {
        return ZonedDateTime.ofInstant(localDateTime, offset, zone);
    }

    public static ZonedDateTime ofStrict(LocalDateTime localDateTime, ZoneOffset offset, ZoneId zone) {
        return ZonedDateTime.ofStrict(localDateTime, offset, zone);
    }

    public static ZonedDateTime from(TemporalAccessor temporal) {
        return ZonedDateTime.from(temporal);
    }

    public static ZonedDateTime parse(CharSequence text) {
        return ZonedDateTime.parse(text);
    }

    public static ZonedDateTime parse(CharSequence text, DateTimeFormatter formatter) {
        return ZonedDateTime.parse(text, formatter);
    }
}
