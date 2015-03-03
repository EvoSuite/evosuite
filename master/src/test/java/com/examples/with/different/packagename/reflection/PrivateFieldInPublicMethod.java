package com.examples.with.different.packagename.reflection;

/**
 * Created by Andrea Arcuri on 03/03/15.
 */
public class PrivateFieldInPublicMethod {

    private boolean flag = false;

    private String s = "foo";

    public void flag(){
        if(flag){
            System.out.println("Flag is true");
        } else {
            System.out.println("Flag is false");
        }
    }

    public void checkString(){
        if(s.equals("42")){
            System.out.println("String is 42");
        } else {
            System.out.println("false");
        }
    }
}
