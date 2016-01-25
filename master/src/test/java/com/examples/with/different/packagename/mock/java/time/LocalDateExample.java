package com.examples.with.different.packagename.mock.java.time;

import java.time.LocalDate;

/**
 * Created by gordon on 25/01/2016.
 */
public class LocalDateExample {

    public boolean testMe(LocalDate date) {
        LocalDate now = LocalDate.now();
        LocalDate nowP = now.plusMonths(1L);
        if(nowP.isBefore(date))
            return true;
        else
            return false;
    }
}
