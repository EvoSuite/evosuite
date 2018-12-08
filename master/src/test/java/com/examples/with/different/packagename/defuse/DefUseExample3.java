package com.examples.with.different.packagename.defuse;

public class DefUseExample3 {
    int count;

    public void initAndIncrement(){
        this.count=0;
        incrementCount();
    }
    public void incrementCount(){
        this.count++;
    }
}
