package com.examples.with.different.packagename;

/**
 * Created by gordon on 10/05/2017.
 */
public class EnumUser {

    public boolean foo(EnumInInnerClass.AnEnum val) {
        if(val == EnumInInnerClass.AnEnum.FOO)
            return true;
        else
            return false;
    }
}
