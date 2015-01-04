package com.examples.with.different.packagename.test;

public class CallTest {
  public void coverMe(int x) {
     Callee c = new Callee();
     int y = c.callme(x);
     if(y == 1240) {
       System.out.println("Target");
     }
  }
}

class Callee {
  public int callme(int x) {
    if(x == 115)
      return 1240;
    else
      return 0;
  }
}