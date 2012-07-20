package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;

public class TestCase2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LinkedList linkedList0 = new LinkedList();
		int int0 = ConcolicMarker.mark(-756,"var1");
		Object object0 = linkedList0.get(int0);
		LinkedList linkedList1 = new LinkedList();
		int int1 = ConcolicMarker.mark(-542,"var2");
		Object object1 = linkedList1.get(int1);
		int int2 = linkedList1.size();
		linkedList1.add(int2);
		int int3 = ConcolicMarker.mark(1,"var3");
		int int4 = linkedList1.size();
		linkedList1.unreacheable();
		LinkedList linkedList2 = new LinkedList();
		linkedList1.unreacheable();
		int int5 = linkedList2.size();
		int int6 = linkedList1.size();
		linkedList1.add(linkedList2);
		linkedList2.unreacheable();
		int int7 = linkedList2.size();
		int int8 = ConcolicMarker.mark(-1480,"var4");
		LinkedList linkedList3 = (LinkedList)linkedList1.get(int5);
		linkedList2.unreacheable();
		int int9 = linkedList1.size();
		int int10 = linkedList1.size();
		int int11 = ConcolicMarker.mark(-1637,"var5");
		LinkedList linkedList4 = (LinkedList)linkedList1.get(int2);
		int int12 = linkedList1.size();
		linkedList1.add(int9);
		linkedList1.unreacheable();
		LinkedList linkedList5 = new LinkedList();
	}

}
