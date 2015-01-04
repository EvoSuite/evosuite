package com.examples.with.different.packagename.test;

public class AbsTest {
  public void testMe(double x) {
    if(Math.abs(x) == 1252.43) {
      System.out.println("Covered");
    }
  }
}