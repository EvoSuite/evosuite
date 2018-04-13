package com.examples.with.different.packagename.errorbranch;

import java.util.ArrayList;

public class ArrayListAdd {
    public void testMe(ArrayList<Integer> integerList, Integer element, Integer index) {
        integerList.add(element, index);
    }
}
