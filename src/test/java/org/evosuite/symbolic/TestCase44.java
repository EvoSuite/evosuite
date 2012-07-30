package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;

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
	public static void main(String[] args) {
		Foo foo0 = new Foo();
		int int0 = ConcolicMarker.mark(1111, "var0");
		int int1 = ConcolicMarker.mark(1111, "var1");
		int int2 = ConcolicMarker.mark(1111, "var2");
		int int3 = ConcolicMarker.mark(1111, "var3");
		int int4 = ConcolicMarker.mark(-285, "var4");
		int int5 = ConcolicMarker.mark(-285, "var5");
		Foo foo1 = new Foo();
		boolean boolean0 = foo0.bar(int4);
		int int6 = ConcolicMarker.mark(6, "var6");
		int int7 = ConcolicMarker.mark(302, "var7");
		boolean boolean1 = foo0.bar(int6);
		boolean boolean2 = foo0.bar(int5);
		int int8 = ConcolicMarker.mark(1565, "var8");
		boolean boolean3 = foo0.bar(int8);
		int int9 = ConcolicMarker.mark(1893, "var9");
		boolean boolean4 = foo0.bar(int9);
		int int10 = ConcolicMarker.mark(-1956, "var10");
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
