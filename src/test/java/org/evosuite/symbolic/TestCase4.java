package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;

public class TestCase4 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LinkedList linkedList0 = new LinkedList();
		long long0 = ConcolicMarker.mark(1554151784714561687L,"var1");
		linkedList0.add(long0);
		int int0 = ConcolicMarker.mark(0, "var2");
		Object object0 = linkedList0.get(int0);
		
	}

}
