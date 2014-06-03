package com.examples.with.different.packagename.test;

public class ExceptionTest {
  public void foo(int x) {
    if(x < 0)
      throw new RuntimeException("Target");
  }
  
  public void bar(int x) {
    if(x == 5)
      throw new ArrayIndexOutOfBoundsException();
    Object[] y = new Object[10];
    y[x] = null;
  }
  
  public void zoo(int x) {
    Object foo = null;
    System.out.println(foo.toString());
  }
}
