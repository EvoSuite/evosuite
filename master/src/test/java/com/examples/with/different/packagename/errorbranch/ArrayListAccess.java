package com.examples.with.different.packagename.errorbranch;

import java.util.ArrayList;
import java.util.List;

public class ArrayListAccess {

    public void testMe(int index) {
        List<Integer> integerList = new ArrayList<Integer>();
        int y = integerList.get(index);
    }
}
