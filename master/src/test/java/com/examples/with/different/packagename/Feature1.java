package com.examples.with.different.packagename;

/**
 * This class is used to test FeatureInstrumentation
 */
public class Feature1 {
    private int count=0;
    public void listOperation2(int num) {
        //doSomething
        if(num==2)
            System.out.println("Equals");
    }
}
