package com.examples.with.different.packagename.test;

public class ArrayTest {
  public void test1(int[] values) {
     if(values.length == 5) {
       if(values[3] == 7) {
         System.out.println("Done.");
       } else if(values[3] == 100 && values[2] == values[1] && values[1] != 0) {
         System.out.println("Madness!");
       }
     }
  }
}
