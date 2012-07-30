package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase42 {

	private int[] intArrayField = new int[10];
	private TestCase42[] objectArrayField = new TestCase42[10];

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		TestCase42 instance = new TestCase42();
		int int0 = ConcolicMarker.mark(Integer.MAX_VALUE, "var0");
		instance.intArrayField[0] = int0;
		instance.objectArrayField[0] = instance;
		int int1 = instance.intArrayField[0];
		int int2 = Integer.MAX_VALUE;
		checkEquals(int1,int2);
	}

}
