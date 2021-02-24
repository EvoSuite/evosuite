package com.examples.with.different.packagename.dse.invokedynamic;

import java.util.function.Function;

public class TestSAMConversions {

    public static int conversion(int val) {
        Function<Integer, Integer> fun = (
                (value) -> {
                    if (value == 50) {
                        return 1;
                    } else {
                        return 2;
                    }
                }
        );

        return fun.apply(val);
    }

}
