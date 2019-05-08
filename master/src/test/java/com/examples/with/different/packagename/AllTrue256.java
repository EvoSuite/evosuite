package com.examples.with.different.packagename;

public class AllTrue256 {
    public void testAllTrue(boolean[] a) {
        boolean alltrue = true;
        for (int i = 0; i < 256; i++) {
            alltrue = alltrue && a[i];
        }
        if (alltrue) {
            // target
            System.out.println("target");
        }
    }
}
