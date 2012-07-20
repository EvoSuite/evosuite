package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;

public class TestCase1 {

	public static void main(String[] args) {
		LinkedList linkedList0 = new LinkedList();
		int int0 = ConcolicMarker.mark(179,"int0");
		int int1 = ConcolicMarker.mark(-374,"int1");
		Object object0 = linkedList0.get(int0);
		linkedList0.unreacheable();
		Integer integer0 = new Integer(int0);
		linkedList0.add(integer0);
	}
}
