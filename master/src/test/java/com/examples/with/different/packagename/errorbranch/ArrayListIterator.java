package com.examples.with.different.packagename.errorbranch;

import java.util.List;
import java.util.ListIterator;

public class ArrayListIterator {
    public void testMe(List<Integer> integerList) {
        ListIterator<Integer> litr = integerList.listIterator();
        int element = litr.next();
    }
}
