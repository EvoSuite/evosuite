package com.examples.with.different.packagename;

public class Mask128 {
    public void testMask(char[] a) {
        char x = 0x55; // 1010101
        for (int i = 0; i < 10; i++) {
            x = (char) (x & a[i]); //bitwise and
        }
        if (x == 0x55) {
            // target
            System.out.println("target");
        }
    }
}
