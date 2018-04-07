package com.examples.with.different.packagename.errorbranch;

import java.util.ArrayList;
import java.util.List;

public class ArrayListAddAll {
    public void testMe(ArrayList<Integer> integerList, Integer index, List<Integer> integerList1) {
        integerList.addAll(index, integerList1);
    }
}
