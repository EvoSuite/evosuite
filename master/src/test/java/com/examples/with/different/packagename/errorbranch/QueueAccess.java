package com.examples.with.different.packagename.errorbranch;

import java.util.PriorityQueue;

public class QueueAccess {

    public void testMe(PriorityQueue<Integer> integerQueue) {
        integerQueue.element();
    }
}
