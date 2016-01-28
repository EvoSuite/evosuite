package com.examples.with.different.packagename.mock.java.time;

import java.time.Clock;
import java.time.Instant;

/**
 * Created by gordon on 25/01/2016.
 */
public class InstantExample {

    public long testMe() {
        Instant instant = Instant.now();
        return instant.getEpochSecond();
    }

    public boolean testMe2(Instant otherInstant) {
        Instant instant = Instant.now(Clock.systemDefaultZone());
        if(instant.isBefore(otherInstant))
            return true;
        else
            return false;
    }
}
