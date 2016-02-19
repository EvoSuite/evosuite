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
/**
 * 
 */
package com.examples.with.different.packagename.testcarver;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Gordon Fraser
 * 
 */
public class GenericObjectWrapperSetTest {
	@Test
	public void test01() {
		GenericObjectWrapper<Set<Long>> wrapper = new GenericObjectWrapper<Set<Long>>();
		Assert.assertNull(wrapper.get());

		Set<Long> someSet = new HashSet<Long>();
		someSet.add(42l);
		someSet.add(47l);
		someSet.remove(42l);
		someSet.add(48l);

		wrapper.set(someSet);
		Assert.assertNotNull(wrapper.get());

		GenericObjectWrapper<Long> fortySeven = new GenericObjectWrapper<Long>();
		fortySeven.set(47l);

		Set<Long> foo = wrapper.get();

		Assert.assertTrue(foo.contains(fortySeven.get()));
	}
}
