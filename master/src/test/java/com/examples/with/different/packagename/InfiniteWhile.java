package com.examples.with.different.packagename;

/**
 * Created by Andrea Arcuri on 29/03/15.
 */
public class InfiniteWhile {

    public void infiniteLoop(){
        int counter = 0;
        while(true){
            System.out.println("Iteration "+ counter);
            counter++;
        }
    }
}
