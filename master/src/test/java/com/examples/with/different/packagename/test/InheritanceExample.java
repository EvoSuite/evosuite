package com.examples.with.different.packagename.test;

public class InheritanceExample extends InheritanceSuperClass {

  public void subMethod(int x, int y) {
    if(x == 125) {
       InheritanceSubClass test = new InheritanceSubClass();
       test.testMe(y);
    }
  }


}