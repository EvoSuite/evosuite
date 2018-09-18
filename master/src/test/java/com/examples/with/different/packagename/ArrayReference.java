package com.examples.with.different.packagename;

import java.awt.*;
import java.lang.reflect.Constructor;

public class ArrayReference {

    public void doSomething(Constructor<Insets>[] constructorArray0) {
        if (constructorArray0[0] != null) {
            System.out.println("Value is : " + constructorArray0[0]);
        } else {
            System.out.println("Value is : " + null);
        }
    }
}
