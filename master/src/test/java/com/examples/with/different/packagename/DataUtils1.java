package com.examples.with.different.packagename;

public class DataUtils1 {

    public boolean doSomething(boolean[] arr) throws RuntimeException{
        if(arr[100] == false){
            return true;
        }else{
            return false;
        }
    }
}
