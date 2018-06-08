package com.examples.with.different.packagename.errorbranch;

import java.util.concurrent.LinkedBlockingDeque;

public class DequeGetFirst {
    public void testMe(LinkedBlockingDeque<Integer> integerDeque) {
        int x = integerDeque.getFirst();
    }
}
