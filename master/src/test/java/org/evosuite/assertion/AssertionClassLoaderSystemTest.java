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
package org.evosuite.assertion;

import org.evosuite.SystemTestBase;
import org.evosuite.TestGenerationContext;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.ExampleEnum;

public class AssertionClassLoaderSystemTest extends SystemTestBase {

	@Test
	public void testLoaderOfEnumsAreChanged() throws NoSuchMethodException, SecurityException {
		InspectorAssertion assertion = new InspectorAssertion();
		assertion.inspector = new Inspector(ExampleEnum.class, ExampleEnum.class.getMethod("testMe", new Class<?>[] {}));
		assertion.value = ExampleEnum.VALUE1;
		Assert.assertEquals(ExampleEnum.VALUE1, assertion.value);
		
		ClassLoader loader = TestGenerationContext.getInstance().getClassLoaderForSUT();
		assertion.changeClassLoader(loader);
		
		Assert.assertNotEquals(ExampleEnum.VALUE1, assertion.value);
	}
}
