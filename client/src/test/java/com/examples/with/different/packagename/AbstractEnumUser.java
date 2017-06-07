package com.examples.with.different.packagename;

/**
 * Created by gordon on 10/05/2017.
 */
public class AbstractEnumUser {

    public boolean foo(AbstractEnumInInnerClass.AnEnum foo) {
        if(foo.foo(0))
            return true;
        else
            return false;
    }
}
