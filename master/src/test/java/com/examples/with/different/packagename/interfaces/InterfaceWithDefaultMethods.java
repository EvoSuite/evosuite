package com.examples.with.different.packagename.interfaces;

public interface InterfaceWithDefaultMethods {
    default int getFoo() {
        return 0;
    }
}
