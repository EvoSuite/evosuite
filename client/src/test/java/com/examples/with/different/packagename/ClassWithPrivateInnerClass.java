package com.examples.with.different.packagename;

/**
 * Created by gordon on 19/02/2016.
 */
class ClassWithPrivateInnerClass {

    private class Property {
        final String foo = "";
    }

    protected Property getProperty() {
        return new Property();
    }
}
