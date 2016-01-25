package org.evosuite.runtime.mock.java.time.chrono;

import org.evosuite.runtime.mock.StaticReplacementMock;
import org.evosuite.runtime.mock.java.time.MockClock;

import java.time.ZoneId;
import java.time.chrono.JapaneseChronology;
import java.time.chrono.JapaneseDate;

/**
 * Created by gordon on 24/01/2016.
 */
public class MockJapaneseChronology implements StaticReplacementMock {
    @Override
    public String getMockedClassName() {
        return JapaneseChronology.class.getName();
    }

    public static JapaneseDate dateNow(JapaneseChronology instance) {
        return instance.dateNow(MockClock.systemDefaultZone());
    }

    public static JapaneseDate dateNow(JapaneseChronology instance, ZoneId zone) {
        return instance.dateNow(MockClock.system(zone));
    }

}
