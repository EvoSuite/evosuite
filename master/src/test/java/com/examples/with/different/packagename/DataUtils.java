package com.examples.with.different.packagename;

import java.io.Serializable;
import java.util.List;

public class DataUtils {
    private int count=0;
    private Foo foo = new Foo();

    /*public static void listOperation(Foo foo) {
        Foo foo1 = foo;
        *//*List<Integer> list1 = list;*//*

        if(foo1.getSomething()){
            System.out.println("Equal");
        }else{
            System.out.println("Not Equal");
        }
    }*/
    /*public void listOperation1(int x, int z) {
        int count = x;
        listOperation2(2);
        if(x<z) {
            System.out.println("");
        }
        System.out.println(this.count);
    }*/

    public void listOperation2(int num) {
        int y = this.count;
        this.count = 4;
        this.count = 4;
        foo.j = 4;

        Foo foo1 = new Foo();
        foo1.j = 4;
        if(num*5 == 2)
            System.out.println("");
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
