package com.examples.with.different.packagename.reflection;

/**
 * Created by Andrea Arcuri on 03/03/15.
 */
public class PrivateFieldInPrivateMethod {

    private boolean flag = false;

    private void flag(){
        if(flag){
            System.out.println("Flag is true");
        } else {
            System.out.println("Flag is false");
        }
    }
}
