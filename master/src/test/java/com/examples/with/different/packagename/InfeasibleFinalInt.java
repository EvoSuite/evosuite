package com.examples.with.different.packagename;

public class InfeasibleFinalInt {

    public static final int x = 42;

    public static boolean foo(int y){
        if(x == 11 || x * y == 41){
            //infeasible
            return true;
        } else {
            return false;
        }
    }
}
