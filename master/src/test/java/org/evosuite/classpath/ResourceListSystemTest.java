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
package org.evosuite.classpath;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.evosuite.SystemTestBase;
import org.evosuite.TestGenerationContext;
import org.junit.Test;

import com.examples.with.different.packagename.listclasses.AbstractClass;
import com.examples.with.different.packagename.listclasses.ClassWithDefaultMethods;
import com.examples.with.different.packagename.listclasses.ClassWithProtectedMethods;
import com.examples.with.different.packagename.listclasses.ClassWithoutPublicMethods;

public class ResourceListSystemTest extends SystemTestBase {

	@Test
	public void testCanAccessAbstractClass() throws IOException {
		assertTrue(ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).isClassTestable(AbstractClass.class.getCanonicalName()));
	}
	
	@Test
	public void testCanAccessAClassWithDefaultMethods() throws IOException {
		assertTrue(ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).isClassTestable(ClassWithDefaultMethods.class.getCanonicalName()));
	}

	@Test
	public void testCanAccessClassWithoutPublicMethods() throws IOException {
		assertFalse(ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).isClassTestable(ClassWithoutPublicMethods.class.getCanonicalName()));
	}

	@Test
	public void testCanAccessClassWithProtectedMethods() throws IOException {
		assertTrue(ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).isClassTestable(ClassWithProtectedMethods.class.getCanonicalName()));
	}

	@Test
	public void testCanAccessNonPublicClass() throws IOException {
		assertTrue(ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).isClassTestable("com.examples.with.different.packagename.listclasses.NonPublicClass"));
	}

}
