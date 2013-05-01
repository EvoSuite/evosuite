package com.examples.with.different.packagename.test;

public class EnumTest2 {
  public MyEnum e;
  
  public EnumTest2(MyEnum e) {
    this.e = e;
  }
  
  public void setEnum(MyEnum e) {
    if(e == MyEnum.GEQ)
      this.e = e;
  }
  
  public MyEnum getEnum() {
    return e;
  }
}