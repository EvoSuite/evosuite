package com.examples.with.different.packagename.test;

public class PolyExample {
	public void testMe(Object o) {
		if (o instanceof Integer) {
			Integer i = (Integer) o;
			if (i.intValue() == 17) {
				System.out.println("test");
			}
		}
	}
}
