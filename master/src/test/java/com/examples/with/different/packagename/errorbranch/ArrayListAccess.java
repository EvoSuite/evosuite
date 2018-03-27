package com.examples.with.different.packagename.errorbranch;

import java.util.ArrayList;
import java.util.List;

public class ArrayListAccess {

    public void testMe(List<Integer> integerList, int index) {
        int y = integerList.get(index);
    }
}
