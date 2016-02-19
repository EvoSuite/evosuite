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

import java.io.ByteArrayOutputStream;


public class TestCase41 {

	/**
	 * @param args
	 */
	public static void test(int int0, int int1, int int3, int int4) {
		MyLinkedList linkedList0 = new MyLinkedList();
		ByteArrayOutputStream byteArrayOutputStream0 = new ByteArrayOutputStream();
		linkedList0.add(byteArrayOutputStream0);
		linkedList0.add(int0);
		Integer integer0 = (Integer) linkedList0.get(int1);
		int int2 = linkedList0.size();
		ByteArrayOutputStream byteArrayOutputStream1 = (ByteArrayOutputStream) linkedList0
				.get(int3);
		int int5 = linkedList0.size();
		ByteArrayOutputStream byteArrayOutputStream2 = (ByteArrayOutputStream) linkedList0
				.get(int4);
		Double double0 = new Double((double) integer0);
		ByteArrayOutputStream byteArrayOutputStream3 = new ByteArrayOutputStream();
		linkedList0.unreacheable();
		linkedList0.add(double0);
		int int6 = linkedList0.size();
		int int7 = linkedList0.size();
		Object object0 = linkedList0.get(int7);
	}

}
