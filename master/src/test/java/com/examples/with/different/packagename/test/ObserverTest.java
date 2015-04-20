package com.examples.with.different.packagename.test;

public class ObserverTest {
  private ObservableDummy dummy;;

  public ObserverTest(int x) {
     this.dummy = new ObservableDummy(x);
  }  
  
  public void add(int x) {
    dummy.add(x);
  }
 
  public ObservableDummy getDummy() {
    return dummy;
  }

}
