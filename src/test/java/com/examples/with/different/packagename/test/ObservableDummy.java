package com.examples.with.different.packagename.test;

public class ObservableDummy {
  private int x;
  
  public ObservableDummy(int x) {
    this.x = x;
  }
  
  public void add(int y) {
    x = x + y;
  }
  
  public int getX() {
    return x;
  }
 
  public boolean nonZero() {
    return x != 0;
  }

}
