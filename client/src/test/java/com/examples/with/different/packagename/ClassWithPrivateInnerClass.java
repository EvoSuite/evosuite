package com.examples.with.different.packagename;

import java.util.Collections;
import java.util.List;

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

    public List<Property> getPropertyList() {
        return Collections.EMPTY_LIST;
    }
}
