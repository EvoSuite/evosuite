package com.examples.with.different.packagename.performance;

public class Looping {

    public static void forLoop(int i) {
        for (int j = 0; j < i; j++) {
        }
    }

    public static void whileDo(int i) {
        int j = 0;
        while (j < i)
            j++;
    }
}