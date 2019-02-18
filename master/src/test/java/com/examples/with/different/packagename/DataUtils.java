package com.examples.with.different.packagename;

import java.io.Serializable;
import java.util.List;

public class DataUtils {
   /* private int count=0;
    private Foo foo = new Foo();*/

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

    /*public void listOperation2(int num, CharSequence sequence, char c, byte b, byte[] bArr) {
        *//*int y = this.count;
        this.count = 4;
        this.count = 4;
        foo.j = 4;


        Foo foo1 = new Foo();
        foo1.j = 4;*//*
        byte[] bArr2 = bArr;
        byte b1 = b;
        char f = c;
        CharSequence sequence1 = sequence;
        if(num*5 == 2)
            System.out.println("");
    }*/
    public boolean doSomething(int a){
        int c;
        int s = a;
        if(a == 5){
            c = 7;
            return true;
        }
        return true;

    }

    /*public static boolean isWellFormedSlowPath(byte[] bytes, int off, int end) {
        *//*int index = off;*//*
        Byte byte1 = bytes[4];
         if(true) {
                byte byte2;
                if (byte1 < -16) {// that's it
                    *//*if (index + 1 >= end) {
                        return false;
                    }*//*

                    byte2 = bytes[5];
                    *//*j = end;*//*
                    *//*if (byte2 > -65 || byte1 == -32 && byte2 < -96 || byte1 == -19 && -96 <= byte2 || bytes[index++] > -65) {
                        return false;
                    }*//*
                } *//*else {
                    if (index + 2 >= end) {
                        return false;
                    }

                    byte2 = bytes[index++];
                    if (byte2 > -65 || (byte1 << 28) + (byte2 - -112) >> 30 != 0 || bytes[index++] > -65 || bytes[index++] > -65) {
                        return false;
                    }
                }*//*
            }
            return true;
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
