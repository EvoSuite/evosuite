package com.examples.with.different.packagename.concolic;

import org.evosuite.symbolic.Assertions;

public class TestCase89 {

	public class InnerClass {

		private int innerField;

		public InnerClass(int val) {
			innerField = TestCase89.this.outerField;
			outerField = val;
		}
		
	}

	protected int outerField = 4;

	public static void test(int int0) {
		TestCase89 outerClassInstance = new TestCase89();
		InnerClass innerClass = outerClassInstance.new InnerClass(
				int0);
		int int1 = outerClassInstance.outerField;
		Assertions.checkEquals(int0, int1);
	}
}
