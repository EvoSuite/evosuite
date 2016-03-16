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
package org.evosuite.basic;


import static org.junit.Assert.*;

import java.util.List;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.result.TestGenerationResult;
import org.junit.Test;



public class TestShouldNotWorkSystemTest extends SystemTestBase {

	@Test(expected=IllegalArgumentException.class)
	public void testShouldNotWorkOnEvoSuitePackage(){
		EvoSuite evosuite = new EvoSuite();
		
		String targetClass = SystemTestBase.class.getCanonicalName();
		
		Properties.TARGET_CLASS = targetClass;
		
		String[] command = new String[]{				
				"-generateSuite",
				"-class",
				targetClass
		};

		
		evosuite.parseCommandLine(command);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testJavaPackageNotOnProjectCP(){
		EvoSuite evosuite = new EvoSuite();
		
		String targetClass = java.util.TreeMap.class.getCanonicalName();
		
		Properties.TARGET_CLASS = targetClass;
		
		String[] command = new String[]{				
				"-generateSuite",
				"-class",
				targetClass
		};

		
		Object result = evosuite.parseCommandLine(command);
		//List<TestGenerationResult> results = (List<TestGenerationResult>)result;
		List<List<TestGenerationResult>> results = (List<List<TestGenerationResult>>)result;
		assertEquals(1, results.size());
		//TestGenerationResult testResult = results.iterator().next();
		TestGenerationResult testResult = results.get(0).get(0);
		System.out.println(testResult.getErrorMessage());
		assertFalse(testResult.getErrorMessage().isEmpty());
		assertEquals(TestGenerationResult.Status.ERROR, testResult.getTestGenerationStatus());
		
	}
	
}
