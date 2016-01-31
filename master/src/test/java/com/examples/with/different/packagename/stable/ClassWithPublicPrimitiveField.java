package com.examples.with.different.packagename.stable;

/**
 * Created by gordon on 31/01/2016.
 */
public class ClassWithPublicPrimitiveField {

    public int x = 0;

    public float y = 0.0F;

    public boolean foo() {
        return x > 0 && (int)y == x;
    }
}
