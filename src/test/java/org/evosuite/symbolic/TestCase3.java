package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;

public class TestCase3 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LinkedList linkedList0 = new LinkedList();
		int int0 = ConcolicMarker.mark(-1,"var1");
		Object object0 = linkedList0.get(int0);
	}

}
