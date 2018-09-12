package com.examples.with.different.packagename;

import com.examples.with.different.packagename.contracts.Foo;

public class MethodWithClassParameter {

    public void readFoos(Class<Foo> fooClass) {
        System.out.print("Test Class Name : " + fooClass.getName());
    }
}
