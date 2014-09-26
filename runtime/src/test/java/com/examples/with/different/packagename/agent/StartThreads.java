package com.examples.with.different.packagename.agent;

public class StartThreads {

    public void exe(int n){
        for(int i=0; i<n; i++){
            Thread t = new Thread();
            t.start();
        }
    }
}
