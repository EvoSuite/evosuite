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

import org.evosuite.runtime.mock.OverrideMock;

import java.io.Serializable;
import java.time.*;
import java.util.Objects;

/**
 * Created by gordon on 24/01/2016.
 */
public abstract class MockClock extends java.time.Clock implements OverrideMock {

    /**
     * Seconds per minute.
     */
    static final int SECONDS_PER_MINUTE = 60;
    /**
     * Nanos per second.
     */
    static final long NANOS_PER_SECOND = 1000_000_000L;
    /**
     * Nanos per minute.
     */
    static final long NANOS_PER_MINUTE = NANOS_PER_SECOND * SECONDS_PER_MINUTE;


    public static Clock systemUTC() {
        return new MockSystemClock(ZoneOffset.UTC);
    }

    public static Clock systemDefaultZone() {
        return new MockSystemClock(ZoneId.systemDefault());
    }

    public static Clock system(ZoneId zone) {
        Objects.requireNonNull(zone, "zone");
        return new MockSystemClock(zone);
    }

    public static Clock tickSeconds(ZoneId zone) {
        return new MockTickClock(system(zone), NANOS_PER_SECOND);
    }


    public static Clock tickMinutes(ZoneId zone) {
        return new MockTickClock(system(zone), NANOS_PER_MINUTE);
    }


    public static Clock tick(Clock baseClock, Duration tickDuration) {
        return Clock.tick(baseClock, tickDuration);
    }


    public static Clock fixed(Instant fixedInstant, ZoneId zone) {
        return Clock.fixed(fixedInstant, zone);
    }


    public static Clock offset(Clock baseClock, Duration offsetDuration) {
        return Clock.offset(baseClock, offsetDuration);
    }


    protected MockClock() {
    }


    public abstract ZoneId getZone();


    public abstract Clock withZone(ZoneId zone);


    public long millis() {
        return instant().toEpochMilli();
    }


    public abstract Instant instant();


    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }


    @Override
    public  int hashCode() {
        return super.hashCode();
    }

    //-----------------------------------------------------------------------
    /**
     * Implementation of a clock that always returns the latest time from
     * {@link System#currentTimeMillis()}.
     */
    static final class MockSystemClock extends Clock implements Serializable {
        private static final long serialVersionUID = 6740630888130243051L;
        private final ZoneId zone;

        MockSystemClock(ZoneId zone) {
            this.zone = zone;
        }
        @Override
        public ZoneId getZone() {
            return zone;
        }
        @Override
        public Clock withZone(ZoneId zone) {
            if (zone.equals(this.zone)) {  // intentional NPE
                return this;
            }
            return new MockClock.MockSystemClock(zone);
        }
        @Override
        public long millis() {
            return org.evosuite.runtime.System.currentTimeMillis();
        }
        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(millis());
        }
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof MockClock.MockSystemClock) {
                return zone.equals(((MockClock.MockSystemClock) obj).zone);
            }
            return false;
        }
        @Override
        public int hashCode() {
            return zone.hashCode() + 1;
        }
        @Override
        public String toString() {
            return "SystemClock[" + zone + "]";
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Implementation of a clock that adds an offset to an underlying clock.
     */
    static final class MockTickClock extends Clock implements Serializable {
        private static final long serialVersionUID = 6504659149906368850L;
        private final Clock baseClock;
        private final long tickNanos;

        MockTickClock(Clock baseClock, long tickNanos) {
            this.baseClock = baseClock;
            this.tickNanos = tickNanos;
        }
        @Override
        public ZoneId getZone() {
            return baseClock.getZone();
        }
        @Override
        public Clock withZone(ZoneId zone) {
            if (zone.equals(baseClock.getZone())) {  // intentional NPE
                return this;
            }
            return new MockClock.MockTickClock(baseClock.withZone(zone), tickNanos);
        }
        @Override
        public long millis() {
            long millis = baseClock.millis();
            return millis - Math.floorMod(millis, tickNanos / 1000_000L);
        }
        @Override
        public Instant instant() {
            if ((tickNanos % 1000_000) == 0) {
                long millis = baseClock.millis();
                return Instant.ofEpochMilli(millis - Math.floorMod(millis, tickNanos / 1000_000L));
            }
            Instant instant = baseClock.instant();
            long nanos = instant.getNano();
            long adjust = Math.floorMod(nanos, tickNanos);
            return instant.minusNanos(adjust);
        }
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof MockClock.MockTickClock) {
                MockClock.MockTickClock other = (MockClock.MockTickClock) obj;
                return baseClock.equals(other.baseClock) && tickNanos == other.tickNanos;
            }
            return false;
        }
        @Override
        public int hashCode() {
            return baseClock.hashCode() ^ ((int) (tickNanos ^ (tickNanos >>> 32)));
        }
        @Override
        public String toString() {
            return "TickClock[" + baseClock + "," + Duration.ofNanos(tickNanos) + "]";
        }
    }
}
