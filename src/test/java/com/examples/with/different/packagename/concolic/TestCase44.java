package com.examples.with.different.packagename.concolic;

public class TestCase44 {

	private static class Foo {

		public boolean bar(int i) {

			if (i == Integer.MAX_VALUE) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * @param args
	 */

	public static void test(int int0, int int1, int int2, int int3, int int4,
			int int5, int int6, int int7, int int8, int int9, int int10) {
		Foo foo0 = new Foo();
		Foo foo1 = new Foo();
		boolean boolean0 = foo0.bar(int4);
		boolean boolean1 = foo0.bar(int6);
		boolean boolean2 = foo0.bar(int5);
		boolean boolean3 = foo0.bar(int8);
		boolean boolean4 = foo0.bar(int9);
		Foo foo2 = new Foo();
		boolean boolean5 = foo0.bar(int10);
		boolean boolean6 = foo0.bar(int0);
		boolean boolean7 = foo0.bar(int3);
		Foo foo3 = new Foo();
		boolean boolean8 = foo0.bar(int2);
		Foo foo4 = new Foo();
		Foo foo5 = new Foo();
		boolean boolean9 = foo0.bar(int1);
	}

}
