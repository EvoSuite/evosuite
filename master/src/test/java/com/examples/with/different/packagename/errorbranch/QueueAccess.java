package com.examples.with.different.packagename.errorbranch;

import java.util.Queue;

public class QueueAccess {

    public void testMe(Queue<Integer> integerQueue) {
        integerQueue.element();
    }
}
