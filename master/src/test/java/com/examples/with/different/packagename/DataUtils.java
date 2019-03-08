package com.examples.with.different.packagename;

import java.util.List;

public class DataUtils {

    public static void listOperation(Foo foo, int[] arr) throws Exception {
        Foo foo1 = foo;
        if(foo1.getSomething() && arr[2]==2){
            System.out.println("Equal");
        }else{
            System.out.println("Not Equal");
        }
    }


    /*public boolean doSomething(int a){
        int c;
        int s = a;
        if(a == 243432){
            c = 7;
            return true;
        }
        return true;

    }
    public static void sqrtFloor(int x) {
        int q = x;
        if(q ==100){
            int z1 = 1;
        }

    }*/

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
        if (boo.checkEquals() && someArr[4]==4) {
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
        if (input == 2) {
            return true;
        } else
            return false;
    }
}
