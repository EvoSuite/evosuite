package com.examples.with.different.packagename.agent;

public class StartThreads {

    public void exe(int n){
        for(int i=0; i<n; i++){
            Thread t = new Thread(){
                @Override
                public void run(){
                    System.out.println("Started new thread");
                }
            };
            t.start();
        }
    }
}
