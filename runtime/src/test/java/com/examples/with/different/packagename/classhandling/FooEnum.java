package com.examples.with.different.packagename.classhandling;

public enum FooEnum {

    X, Y, Z;

    public static boolean check() {
        return X == FooEnum.valueOf("X");
    }
}
