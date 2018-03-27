package com.examples.with.different.packagename.errorbranch;

import java.util.Deque;

public class DequeNullInsertion {
    /*There is no instrumentation for Deque.add(ele) operation*/
    public void testMe(Deque<Integer> integerDeque, Integer element) {
        integerDeque.add(element);
    }
}
