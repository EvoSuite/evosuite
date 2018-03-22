package com.examples.with.different.packagename.errorbranch;

import java.util.Deque;

public class DequeNullInsertion {

    public void testMe(Deque<Integer> integerDeque, Integer element) {
        integerDeque.add(element);
    }
}
