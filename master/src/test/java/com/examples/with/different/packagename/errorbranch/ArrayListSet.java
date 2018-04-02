package com.examples.with.different.packagename.errorbranch;

import java.util.List;

public class ArrayListSet {
    public void testMe(List<Integer> integerList, int index, Integer element) {
        integerList.set(index, element);
    }
}
