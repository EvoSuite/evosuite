package com.examples.with.different.packagename.mock.java.time;

import java.time.Clock;

/**
 * Created by gordon on 25/01/2016.
 */
public class ClockExample {

    public long testMe(Clock clock) {
        return clock.millis();
    }

    public boolean testMe2(long millis) {
        Clock clock = Clock.systemDefaultZone();
        if(clock.millis() == millis)
            return true;
        else
            return false;
    }

    public boolean testMe3(long millis) {
        Clock clock = Clock.systemUTC();
        if(clock.millis() == millis)
            return true;
        else
            return false;
    }

}
