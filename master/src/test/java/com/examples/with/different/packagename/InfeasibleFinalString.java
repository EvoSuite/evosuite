package com.examples.with.different.packagename;


public class InfeasibleFinalString {

    public static final String x = "infeasible";

    public static boolean foo(){
        if(x.equals("this branch is infeasible")){
            return true;
        } else {
            return false;
        }
    }
}
