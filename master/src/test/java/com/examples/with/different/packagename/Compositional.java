package com.examples.with.different.packagename;

/**
 * @author Jose Miguel Rojas
 */
public class Compositional {
    public void foo(int x) {
        if (x > 0)
            bar(x);
        else
            baz(x);
    }

    public void bar(int x) {
        if (x < 500)
            baz(x * 2);
        else
            baz(x);
    }

    public void baz(int x) {
        if (x > 0 && x < 100)
            throw new IllegalArgumentException();
    }
}
