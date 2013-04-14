package com.examples.with.different.packagename.concolic;


public class TestCase0 {

	public static void test() {
		MyLinkedList linkedList0 = new MyLinkedList();
		linkedList0.unreacheable();
		int int0 = linkedList0.size();
		Object object0 = linkedList0.get(int0);
		int int1 = linkedList0.size();

	}
}
