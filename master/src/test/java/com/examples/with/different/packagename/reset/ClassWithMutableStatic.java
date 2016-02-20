package com.examples.with.different.packagename.reset;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gordon on 20/02/2016.
 */
public class ClassWithMutableStatic {

    private static final List<Integer> theList = new ArrayList<>();

    public void foo() {
        theList.add(0);
    }

    public int getSize() {
        return theList.size();
    }
}
