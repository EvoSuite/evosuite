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

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.*;

import com.examples.with.different.packagename.SingleMethod;


/**
 * @author Andrea Arcuri
 * 
 */
public class TestSUTWithSimpleSingleMethod_v2 extends SystemTest{

	@Test
	public void testSingleMethod(){
		EvoSuite evosuite = new EvoSuite();
		
		String targetClass = SingleMethod.class.getCanonicalName();
		
		Properties.TARGET_CLASS = targetClass;
		
		String[] command = new String[]{				
				"-generateSuite",
				"-class",
				targetClass
		};
		
		Object result = evosuite.parseCommandLine(command);
		
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		Assert.assertEquals("Wrong number of generations: ", 0, ga.getAge());
		TestSuiteChromosome best = (TestSuiteChromosome)ga.getBestIndividual();
		Assert.assertEquals("Wrong number of test cases: ",1 , best.size());
		Assert.assertEquals("Non-optimal coverage: ",1d, best.getCoverage(), 0.001);
		Assert.assertEquals("Wrong number of statements: ",2,best.getTestChromosome(0).getTestCase().size());
	}
}
