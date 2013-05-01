package com.examples.with.different.packagename.test;

public class FieldTest {


  public int x = 0;
  
  private int y = 661;
  
  public void test1(int z) {
    if(z == 2*x) {
      y -= x;
    }
  }
  
  public void setX(int x) {
    this.x = x;
  }
  
  public void test2(int z) {
    if(x == 0) {
      if(z == 2*y) {
        System.out.println("Target!");
      }
    }
  }
}