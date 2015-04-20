package com.examples.with.different.packagename.test;

public class ErrorTest {

  public boolean useless = true;

  public int testMe(int x, int y) {
    return x / y;
  }

  public void arrayTest(char[] arr) {
    char x = arr[5];
  }  
  
  public void testField(ErrorTest test1, ErrorTest test2) {
    test1.useless = test2.useless;
  }
  
  public void testCast(Object o) {
    ErrorTest test = (ErrorTest)o;
  }
  
  public void testMethod(ErrorTest test) {
    test.testMe(1, 1);
  }
  
  public void testOverflow(int x, int y) {
    int z = x + y;
  }
  
  public void testAssertion(int x) {
    assert(x != 1024);
  }
/*
  public void testOverflow(float x, float y) {
    float z = x + y;
  }
  public void testOverflow(double x, double y) {
    double z = x + y;
  }
  public void testOverflow(long x, long y) {
    long z = x + y;
  }
  */
}