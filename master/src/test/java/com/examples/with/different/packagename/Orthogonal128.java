package com.examples.with.different.packagename;

public class Orthogonal128 {
    public void testOrthogonal(int[] a, int[] b) {
        // for all i, a[i] and b[i] in [0, 1]
        int product = 0;
        for (int i = 0; product == 0 && i < 128; i++) {
            product = a[i] * b[i];
        }
        if (product == 0) {
            // target
            System.out.println("target");
        }
    }
}
