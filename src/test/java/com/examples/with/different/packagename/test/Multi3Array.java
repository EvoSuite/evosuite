package com.examples.with.different.packagename.test;

public class Multi3Array {
  public void testMe(int[][][] x) {
    if(x.length == 3)
      if(x[2].length == 2)
        System.out.println("Juhu");
  }
  
}
