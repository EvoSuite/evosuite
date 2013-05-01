package com.examples.with.different.packagename.test;

public class MultiArray {
  public void testMe(int[][] x) {
    if(x.length == 3)
      if(x[0].length == 2)
        System.out.println("Juhu");
  }
  
}
