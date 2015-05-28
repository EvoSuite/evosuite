package com.examples.with.different.packagename.mock.java.lang;

/**
 * Created by Andrea Arcuri on 08/05/15.
 */
public class LongSleep {

    public boolean foo(int x) throws InterruptedException {

        Thread.sleep(24 * 60 * 60 * 1000); //if not mocked, following will never be covered

        if(x > 0){
            return true;
        } else {
            return false;
        }
    }
}
