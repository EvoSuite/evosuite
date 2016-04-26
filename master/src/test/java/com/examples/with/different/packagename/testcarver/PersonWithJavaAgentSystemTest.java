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

import org.evosuite.runtime.agent.InstrumentingAgent;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PersonWithJavaAgentSystemTest {

	
	@BeforeClass 
	public static void initEvoSuiteFramework(){ 
		org.evosuite.Properties.REPLACE_CALLS = true;
		InstrumentingAgent.initialize();
	} 

	@Before
	public void init() {
		InstrumentingAgent.activate();
	}

	@After
	public void tearDown() {
		InstrumentingAgent.deactivate();
	}

	
	@Test
	public void test0_1() throws Throwable {
		Person person0 = new Person("", "");
		String string0 = person0.getFirstName();
		assertEquals("", string0);
	}



	@Test
	public void test0() throws Throwable {
		Person person0 = new Person("", "");
		String string0 = person0.getLastName();
		assertEquals("", string0);
	}

}
