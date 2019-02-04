package com.examples.with.different.packagename;

import java.io.Serializable;
import java.util.List;

public class DataUtils {
    int count = 0;

    /*public static void listOperation(Foo foo) {
        Foo foo1 = foo;
        *//*List<Integer> list1 = list;*//*

        if(foo1.getSomething()){
            System.out.println("Equal");
        }else{
            System.out.println("Not Equal");
        }
    }*/
    public void listOperation1(int x, int z) {
        int a = x;
        if(x<z) {

        }
    }

    public static void listOperation2(int x) {
        int y = x;
    }

}

class Foo {
    int j;
    private static int special = 12;
    long someL = 12l;
    private Boo boo;
    int[] someArr;
    private boolean someFlag;

    public Foo(int num, int[] arr, Boo boo) {
        j = num;
        this.someArr = arr;
        this.boo = boo;
    }

    public Foo() {

    }

    boolean getSomething() {

        if (boo.checkEquals()) {
            System.out.println("Equals");
            return true;
        } else {
            System.out.println("Not Equals");
            return false;
        }
    }
}

class Boo {
    private boolean result;
    private int input;
    private int input2;

    Boo(boolean res, int i, int j) {
        this.result = res;
        input = i;
        input2 = j;
    }

    public boolean checkEquals() {
        if (input == 2 && result && input2 == 12) {
            return true;
        } else
            return false;
    }
}
