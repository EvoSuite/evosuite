package com.examples.with.different.packagename;

public class TargetMethod {

  private int y = 0;

  public boolean boo(Integer x) {
    return foo(x);
  }

  public boolean foo(Integer x) throws NullPointerException, IllegalArgumentException {
    try {
      if (x == null) {
        throw new NullPointerException();
      }
    } catch (Exception e) {
      System.out.println(e.toString());
    }

    if (x > 0) {
      this.y = x;
      return bar(x);
    } else {
      throw new IllegalArgumentException();
    }
  }

  public boolean bar(Integer x) {
    if (x < 500) {
      return false;
    } else {
      return true;
    }
  }

  public int getY() {
    return this.y;
  }
}
