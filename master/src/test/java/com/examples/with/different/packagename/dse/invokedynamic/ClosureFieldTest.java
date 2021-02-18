package com.examples.with.different.packagename.dse.invokedynamic;

public class ClosureFieldTest {
    public static int test(int x) {
        ClosureField a = new ClosureField();

        if (a.test(x)) {
            return 0;
        } else {
            return 1;
        }
    }
}
