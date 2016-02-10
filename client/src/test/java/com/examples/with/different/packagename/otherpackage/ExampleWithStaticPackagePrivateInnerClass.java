package com.examples.with.different.packagename.otherpackage;

/**
 * Created by gordon on 31/01/2016.
 */
public class ExampleWithStaticPackagePrivateInnerClass {

    static class Foo {
        public boolean foo() {
            return true;
        }
    }

    public Foo getFoo() {
        return null;
    }

    public void setFoo(Foo foo) {
        // ...
    }

    public void bar() {
        // ...
    }
}
