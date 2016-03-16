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

import org.junit.Assert;
import org.junit.Test;

public class GenericObjectWrapperTwoParameterTest {

	@Test
	public void test01() {
		GenericObjectWrapperTwoParameter<String, String> wrapper = new GenericObjectWrapperTwoParameter<String, String>();
		Assert.assertNull(wrapper.getValue());

		wrapper.setValue("Test");
		Assert.assertEquals("Test", wrapper.getValue());
		Assert.assertTrue(wrapper.isEqual("Test"));
		Assert.assertFalse(wrapper.isEqual("Not"));		
	}
	
}
