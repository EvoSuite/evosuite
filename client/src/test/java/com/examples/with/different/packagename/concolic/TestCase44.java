/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
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
