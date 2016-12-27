package com.examples.with.different.packagename.test;

/**
 * Created by gordon on 27/12/2016.
 */
public class ConcreteSubclass extends AbstractSuperclass {

    @Override
    public boolean getFoo() {
        return true;
    }

    public boolean getBar() {
        return true;
    }

    public boolean fieldInConcreteClass  = true;
}
