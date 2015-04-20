package com.examples.with.different.packagename.mock.java.lang;

/**
 * Created by foo on 16/02/15.
 */
public class ExtendingThread extends Thread{

    @Override
    public void run(){
        System.out.println("Called run()");
    }
}
