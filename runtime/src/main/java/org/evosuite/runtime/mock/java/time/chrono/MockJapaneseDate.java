package org.evosuite.runtime.mock.java.time.chrono;

import org.evosuite.runtime.mock.StaticReplacementMock;
import org.evosuite.runtime.mock.java.time.MockClock;

import java.time.Clock;
import java.time.ZoneId;
import java.time.chrono.JapaneseDate;
import java.time.chrono.JapaneseEra;
import java.time.temporal.TemporalAccessor;

/**
 * Created by gordon on 24/01/2016.
 */
public class MockJapaneseDate implements StaticReplacementMock {

    @Override
    public String getMockedClassName() {
        return JapaneseDate.class.getName();
    }

    public static JapaneseDate now() {
        return now(MockClock.systemDefaultZone());
    }

    public static JapaneseDate now(ZoneId zone) {
        return now(MockClock.system(zone));
    }

    public static JapaneseDate now(Clock clock) {
        return JapaneseDate.now(clock);
    }

    public static JapaneseDate of(JapaneseEra era, int yearOfEra, int month, int dayOfMonth) {
        return JapaneseDate.of(era, yearOfEra, month, dayOfMonth);
    }

    public static JapaneseDate of(int prolepticYear, int month, int dayOfMonth) {
        return JapaneseDate.of(prolepticYear, month, dayOfMonth);
    }

    public static JapaneseDate from(TemporalAccessor temporal) {
        return JapaneseDate.from(temporal);
    }

}
