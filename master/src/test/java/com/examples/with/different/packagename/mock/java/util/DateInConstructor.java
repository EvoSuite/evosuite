package com.examples.with.different.packagename.mock.java.util;

import java.util.Date;

/**
 * Created by gordon on 25/01/2016.
 */
public class DateInConstructor {

    private Date date;

    public DateInConstructor() {
        this(new Date());
    }

    public DateInConstructor(Date date) {
        this.date = date;
    }

    public long foo() {
        return date.getTime();
    }

    public boolean testMe(Date other) {
        if(date.after(other))
            return true;
        else
            return false;
    }
}
