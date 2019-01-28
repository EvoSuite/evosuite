package com.examples.with.different.packagename;

import java.io.Serializable;
import java.util.List;

public class DataUtils {

    public static void listOperation(Foo foo) {
        Foo foo1 = foo;
        /*List<Integer> list1 = list;*/

        if(foo1.getSomething()){
            System.out.println("Equal");
        }else{
            System.out.println("Not Equal");
        }
    }

}

class Foo{
    int j;
    private static int special = 12;
    long someL = 12l;
    private Boo boo;
    int[] someArr ;
    private boolean someFlag;
    public Foo(int num, int[] arr, Boo boo){
        j = num;
        this.someArr = arr;
        this.boo = boo;
    }

    public Foo(){

    }
    boolean getSomething(){

        if(boo.checkEquals() && this.someArr[4]==4){
            System.out.println("Equals");
            return true;
        }else{
            System.out.println("Not Equals");
            return false;
        }
    }
}

class Boo{
    private boolean result;
    private int input;
    private int input2;

    Boo(boolean res, int i, int j){
        this.result = res;
        input = i;
        input2 = j;
    }

    public boolean checkEquals(){
        if(input==2 && result && input2 == 12){
            return true;
        }else
            return false;
    }
}
