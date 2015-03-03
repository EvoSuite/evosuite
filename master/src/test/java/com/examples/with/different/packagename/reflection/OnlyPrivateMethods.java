package com.examples.with.different.packagename.reflection;

/**
 * Created by Andrea Arcuri on 02/03/15.
 */
public class OnlyPrivateMethods {

    private void param0(){
        System.out.println("param0");
    }

    private void param1(String s){
        System.out.println("param1: "+s);
    }

    /*
    private void param2(int x, String s){
        System.out.println("param2: "+s+" "+x);
    }

    private void param2(String s, int x){
        System.out.println("param2_b: "+s+" "+x);
    }
*/
}
