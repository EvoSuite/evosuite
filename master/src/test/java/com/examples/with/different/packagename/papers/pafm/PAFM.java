package com.examples.with.different.packagename.papers.pafm;

public class PAFM {

    private PAFM(){}

    public static boolean checkIfOK(AnInterface x){
        if(x.isOK()){
            return true;
        } else {
            return false;
        }
    }
}
