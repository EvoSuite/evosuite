package com.examples.with.different.packagename.test;

public class ObjectTest {
  private Object x = null;
  
  public void setX(Object x) {
    this.x = x;
  }
  
  public Object getX() {
    return x;
  }
  
  public boolean isEqual(Object y) {
    if(x.equals(y))
      return true;
    
    return false;
  }  
}
