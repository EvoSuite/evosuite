package com.examples.with.different.packagename;

import java.util.*;

public class DataUtils {

        //int i = 0;// putfield
        //int f; // ... nothing
        //SomeClass someClass = new SomeClass(2); // putfield
    public void verify2DArray() {
        //int a = 1; // istore
        //Integer a_1 = 1; // astore
        //float b = 2.4f; // fstore
        //Float b_1 = 2.4f; // astore
        //SomeClass someClass = new SomeClass(2); // astore. Note that for the ctor assignment there is not corresponding istore instr.
        //String s = "asjadkjasj"; // astore

        List<Integer> list = new ArrayList<>(); // astore_1
        //list.add(2); // pop
        //list.add(3); // pop . So this means in the earlier instr it pops the obj, for this curr instr loads it again and then pops it again.
        list.addAll(Arrays.asList(1,2,3)); // pop . Only once

        //Map<Integer, String> integerStringMap = new HashMap<>(); // astore_1
        //integerStringMap.put(1, "One"); // pop
    }

}
class SomeClass{
    int j;
    public SomeClass(int num){
        j = num;
    }
}

