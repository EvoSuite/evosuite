package com.examples.with.different.packagename.errorbranch;

import java.util.ArrayList;

public class ArrayListSet {
    public void testMe(ArrayList<Integer> integerList, int index, Integer element) {
        integerList.set(index, element);
    }
}
