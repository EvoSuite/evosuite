package com.examples.with.different.packagename.stable;

import java.util.Date;

/**
 * Created by gordon on 14/09/2016.
 */
public class DateInConstructor {

    private long millis;

    public DateInConstructor() {
        this(new Date());
    }

    public DateInConstructor(Date date) {
        millis = date.getTime();
    }

    public long getMillis() {
        return millis;
    }
}
