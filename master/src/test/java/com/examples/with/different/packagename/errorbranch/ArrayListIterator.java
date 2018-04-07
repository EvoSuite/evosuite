package com.examples.with.different.packagename.errorbranch;

import java.util.ArrayList;
import java.util.ListIterator;

public class ArrayListIterator {
    public void testMe(ArrayList<Integer> integerList) {
        ListIterator<Integer> litr = integerList.listIterator();
        int element = litr.next();
    }
}
