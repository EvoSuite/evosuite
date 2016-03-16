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

import org.evosuite.EvoSuite;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.*;

import com.examples.with.different.packagename.SingleMethod;


/**
 * @author Andrea Arcuri
 * 
 */
public class SUTWithSimpleSingleMethodSystemTest extends SystemTestBase {

	/*
	 * for now ignore it, as long as we don't fix the issue with serialization
	 */
	@Ignore
	@Test
	public void testSingleMethod(){
		EvoSuite evosuite = new EvoSuite();
		int generations = 1;
		
		String targetClass = SingleMethod.class.getCanonicalName();
		
		String[] command = new String[]{				
				//EvoSuite.JAVA_CMD,
				"-generateTests",
				"-class",
				targetClass,
				"-Dplot=false",
				"-Djunit_tests=false",
				"-Dshow_progress=false",
				"-Dgenerations="+generations,
				"-Dserialize_result=true"
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
