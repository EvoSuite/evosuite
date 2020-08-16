/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.runtime.mock.java.time;

import org.evosuite.runtime.mock.StaticReplacementMock;

import java.time.*;
import java.time.temporal.*;
import java.util.Objects;


/**
 * Created by gordon on 23/01/2016.
 */
public class MockInstant implements StaticReplacementMock {
    @Override
    public String getMockedClassName() {
        return Instant.class.getName();
    }


    // ---- static methods -------

    public static Instant now() {
        return MockClock.systemUTC().instant();
    }

    public static Instant now(Clock clock) {
        Objects.requireNonNull(clock, "clock");
        return clock.instant();
    }

    public static Instant ofEpochSecond(long epochSecond) {
        return Instant.ofEpochSecond(epochSecond);
    }

    public static Instant ofEpochSecond(long epochSecond, long nanoAdjustment) {
        return Instant.ofEpochSecond(epochSecond, nanoAdjustment);
    }


    public static Instant ofEpochMilli(long epochMilli) {
        return Instant.ofEpochMilli(epochMilli);
    }

    public static Instant from(TemporalAccessor temporal) {
        return Instant.from(temporal);
    }


    public static Instant parse(final CharSequence text) {
        return Instant.parse(text);
    }

    // ----- instance replacement methods -------------

    public static boolean isSupported(Instant instant, TemporalField field) {
        return instant.isSupported(field);
    }

    public static boolean isSupported(Instant instant, TemporalUnit unit) {
        return instant.isSupported(unit);
    }

    public static ValueRange range(Instant instant, TemporalField field) {
        return instant.range(field);
    }

    public static int get(Instant instant, TemporalField field) {
        return instant.get(field);
    }

    public static long getLong(Instant instant, TemporalField field) {
        return instant.getLong(field);
    }

    public static long getEpochSecond(Instant instant) {
        return instant.getEpochSecond();
    }

    public static int getNano(Instant instant) {
        return instant.getNano();
    }

    public static Instant with(Instant instant, TemporalAdjuster adjuster) {
        return instant.with(adjuster);
    }

    public static Instant with(Instant instant, TemporalField field, long newValue) {
        return instant.with(field, newValue);
    }

    public static Instant truncatedTo(Instant instant, TemporalUnit unit) {
        return instant.truncatedTo(unit);
    }

    public static Instant plus(Instant instant, TemporalAmount amountToAdd) {
        return instant.plus(amountToAdd);
    }

    public static Instant plus(Instant instant, long amountToAdd, TemporalUnit unit) {
        return instant.plus(amountToAdd, unit);
    }

    public static Instant plusSeconds(Instant instant, long secondsToAdd) {
        return instant.plusSeconds(secondsToAdd);
    }

    public static Instant plusMillis(Instant instant, long millisToAdd) {
        return instant.plusMillis(millisToAdd);
    }

    public static Instant plusNanos(Instant instant, long nanosToAdd) {
        return instant.plusNanos(nanosToAdd);
    }

    public static Instant minus(Instant instant, TemporalAmount amountToSubtract) {
        return instant.minus(amountToSubtract);
    }

    public static Instant minus(Instant instant, long amountToSubtract, TemporalUnit unit) {
        return instant.minus(amountToSubtract, unit);
    }

    public static Instant minusSeconds(Instant instant, long secondsToSubtract) {
        return instant.minusSeconds(secondsToSubtract);
    }

    public static Instant minusMillis(Instant instant, long millisToSubtract) {
        return instant.minusMillis(millisToSubtract);
    }

    public static Instant minusNanos(Instant instant, long nanosToSubtract) {
        return instant.minusNanos(nanosToSubtract);
    }

    @SuppressWarnings("unchecked")
    public static <R> R query(Instant instant, TemporalQuery<R> query) {
        return instant.query(query);
    }

    public static Temporal adjustInto(Instant instant, Temporal temporal) {
        return instant.adjustInto(temporal);
    }

    public static long until(Instant instant, Temporal endExclusive, TemporalUnit unit) {
        return instant.until(endExclusive, unit);
    }

    public static OffsetDateTime atOffset(Instant instant, ZoneOffset offset) {
        return instant.atOffset(offset);
    }

    public static ZonedDateTime atZone(Instant instant, ZoneId zone) {
        return instant.atZone(zone);
    }

    public static long toEpochMilli(Instant instant) {
        return instant.toEpochMilli();
    }

    public static int compareTo(Instant instant, Instant otherInstant) {
        return instant.compareTo(otherInstant);
    }

    public static boolean isAfter(Instant instant, Instant otherInstant) {
        return instant.isAfter(otherInstant);
    }

    public static boolean isBefore(Instant instant, Instant otherInstant) {
        return instant.isBefore(otherInstant);
    }

    public static boolean equals(Instant instant, Object otherInstant) {
        return instant.equals(otherInstant);
    }

    public static int hashCode(Instant instant) {
        return instant.hashCode();
    }

    public static String toString(Instant instant) {
        return instant.toString();
    }



}
