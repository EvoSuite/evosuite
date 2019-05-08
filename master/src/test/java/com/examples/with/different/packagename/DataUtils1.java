package com.examples.with.different.packagename;

public class DataUtils1 {

    public boolean doSomething(boolean[] arr) throws RuntimeException{
        if(arr[100] == false){
            return true;
        }else{
            System.out.println("in false");
            return false;
        }
    }
    public static void main(String args[]){
        DataUtils1 obj = new DataUtils1();

        obj.doSomething(new boolean[]{true});


    }
}

class Nested {
    public int findGreaterThan5or10(int x) {
        if (x > 5) {
            int a;
            if (x > 10) {
                int c = 10;
                return c;
            }
            a = 5;
            return a;
        }
        return 0;
    }
}
