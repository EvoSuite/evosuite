package com.examples.with.different.packagename.test;

public class EnumTest {

    public enum MyOneEnum { ZERO, ONE; };
    
    public enum MyOtherEnum {};

    public void testMe(MyOneEnum e) {
	if(e == MyOneEnum.ZERO) {
	    System.out.println("ZERO");
	}

	if(e == MyOneEnum.ONE) {
	    System.out.println("ONE");
	}
    }
    
    public void testMeToo(MyOtherEnum e) {
    }

}
