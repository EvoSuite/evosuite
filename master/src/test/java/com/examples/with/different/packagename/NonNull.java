package com.examples.with.different.packagename;

import javax.annotation.Nonnull;

public class NonNull {


    public NonNull(@Nonnull Object a) {
        // do nothing
        System.out.println(a.toString());
    }

    public void call1(@Nonnull Object o) {
        // call2(a);
        System.out.println(o.toString());
    }


}


