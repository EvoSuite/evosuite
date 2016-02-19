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
package com.examples.with.different.packagename.testcarver;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class GenericObjectWrapperWithListTest {
	public static class Foo {
		private int x = 0;

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}
	}

	@Test
	public void test() {
		int x = 42;
		Foo foo = new Foo();
		foo.setX(x);

		GenericObjectWrapperWithList<Foo> wrapper = new GenericObjectWrapperWithList<Foo>();
		wrapper.add(foo);
		List<Foo> list = wrapper.getList();
		// list.clear();
		wrapper.setList(list);
		Assert.assertFalse(wrapper.getList().isEmpty());
	}
}
