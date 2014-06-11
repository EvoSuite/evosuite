package com.examples.with.different.packagename.concolic;


public class TestCase1 {


	public static void test(int int0, int int1) {
		MyLinkedList linkedList0 = new MyLinkedList();
		Object object0 = linkedList0.get(int0);
		linkedList0.unreacheable();
		Integer integer0 = new Integer(int0);
		linkedList0.add(integer0);
	}
}
