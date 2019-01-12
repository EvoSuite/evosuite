package com.examples.with.different.packagename;

import java.io.Serializable;

public class DataUtils implements Serializable {

    public static void listOperation(int a, int b) {

        int var1 = a;
        int var2 = b;


        if(a == b)
            System.out.println("Equal");
        if(a > b)
            System.out.println("One greater");
        else
            System.out.println("Two greater");

    }

}

/*class SomeClass{
    int j;
    private static int special = 12;
    long someL = 12l;
    int[] someArr = new int[2];
    public SomeClass(int num, int[] arr){
        j = num;
        this.someArr = arr;
    }
    public SomeClass(){

    }
    void doSomething(){
        System.out.println();
    }
}*/


