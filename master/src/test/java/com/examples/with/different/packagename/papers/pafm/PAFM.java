package com.examples.with.different.packagename.papers.pafm;

public class PAFM {

    public boolean checkIfOK(AnInterface x){
        if(x.isOK()){
            return true;
        } else {
            return false;
        }
    }

    private boolean aPrivateMethod(int y){
        return  y>0;
    }
}
