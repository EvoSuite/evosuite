/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite;


import static org.junit.Assert.*;

import java.util.List;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.result.TestGenerationResult;
import org.junit.Test;



public class TestShouldNotWork {

	@Test(expected=IllegalArgumentException.class)
	public void testShouldNotWorkOnEvoSuitePackage(){
		EvoSuite evosuite = new EvoSuite();
		
		String targetClass = org.evosuite.SystemTest.class.getCanonicalName();
		
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
		List<TestGenerationResult> results = (List<TestGenerationResult>)result;
		assertEquals(1, results.size());
		TestGenerationResult testResult = results.iterator().next();
		System.out.println(testResult.getErrorMessage());
		assertFalse(testResult.getErrorMessage().isEmpty());
		assertEquals(TestGenerationResult.Status.ERROR, testResult.getTestGenerationStatus());
		
	}
	
}
