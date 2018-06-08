package com.examples.with.different.packagename.errorbranch;

import java.util.ArrayList;

public class ArrayListAccess {

    public void testMe(ArrayList<Integer> integerList, int index) {
        int y = integerList.get(index);
    }
}
