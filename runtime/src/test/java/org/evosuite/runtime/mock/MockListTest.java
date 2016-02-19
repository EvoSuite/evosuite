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
package org.evosuite.runtime.mock;

import java.util.List;

import org.junit.Assert;

import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.mock.java.lang.MockException;
import org.evosuite.runtime.mock.java.lang.MockRuntime;
import org.junit.After;
import org.junit.Test;

public class MockListTest {

	private static final boolean DEFAULT_JVM = RuntimeSettings.mockJVMNonDeterminism;
	
	@After
	public void tearDown(){
		RuntimeSettings.mockJVMNonDeterminism = DEFAULT_JVM;
	}
	
	@Test
	public void checkGetJVMMocks(){				
		
		RuntimeSettings.mockJVMNonDeterminism = false;		
		List<Class<? extends EvoSuiteMock>> list = MockList.getList();
		Assert.assertFalse(list.contains(MockRuntime.class));
		Assert.assertFalse(list.contains(MockException.class));
		
		RuntimeSettings.mockJVMNonDeterminism = true;
		list = MockList.getList();
		Assert.assertTrue(list.contains(MockRuntime.class));
		Assert.assertTrue(list.contains(MockException.class));
	}
	
	@Test
	public void testShouldBeMocked(){
		RuntimeSettings.mockJVMNonDeterminism = true;
		
		//first try with a override mock
		Assert.assertTrue(new MockException() instanceof OverrideMock);
		Assert.assertTrue(MockList.shouldBeMocked(Exception.class.getName()));
		
		//then try a static replacement one
		Assert.assertTrue(new MockRuntime() instanceof StaticReplacementMock);
		Assert.assertTrue(MockList.shouldBeMocked(java.lang.Runtime.class.getName()));
	}
}
