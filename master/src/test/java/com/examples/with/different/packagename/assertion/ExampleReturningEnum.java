package com.examples.with.different.packagename.assertion;


/**
 * Created by gordon on 28/03/2016.
 */
public class ExampleReturningEnum {

    public static enum Foo { FOO, BAR };

    public Foo foo(boolean x) {
        if(x)
            return Foo.FOO;
        else
            return Foo.BAR;
    }
}
