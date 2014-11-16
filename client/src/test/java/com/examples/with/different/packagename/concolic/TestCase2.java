package com.examples.with.different.packagename.concolic;


public class TestCase2 {

	/**
	 * @param args
	 */
	// int int0 = ConcolicMarker.mark(-756,"var1");
	// int int1 = ConcolicMarker.mark(-542,"var2");
	// int int3 = ConcolicMarker.mark(1,"var3");
	// int int8 = ConcolicMarker.mark(-1480,"var4");
	// int int11 = ConcolicMarker.mark(-1637,"var5");
	public static void test(int int0, int int1, int int3, int int8, int int11) {
		MyLinkedList linkedList0 = new MyLinkedList();
		Object object0 = linkedList0.get(int0);
		MyLinkedList linkedList1 = new MyLinkedList();
		Object object1 = linkedList1.get(int1);
		int int2 = linkedList1.size();
		linkedList1.add(int2);
		int int4 = linkedList1.size();
		linkedList1.unreacheable();
		MyLinkedList linkedList2 = new MyLinkedList();
		linkedList1.unreacheable();
		int int5 = linkedList2.size();
		int int6 = linkedList1.size();
		linkedList1.add(linkedList2);
		linkedList2.unreacheable();
		int int7 = linkedList2.size();
		MyLinkedList linkedList3 = (MyLinkedList) linkedList1.get(int5);
		linkedList2.unreacheable();
		int int9 = linkedList1.size();
		int int10 = linkedList1.size();
		MyLinkedList linkedList4 = (MyLinkedList) linkedList1.get(int2);
		int int12 = linkedList1.size();
		linkedList1.add(int9);
		linkedList1.unreacheable();
		MyLinkedList linkedList5 = new MyLinkedList();
	}

}
