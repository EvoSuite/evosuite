/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.coverage.epa;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Set;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.StoppingCondition;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.ExecutionObserver;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.epa.ListItr;

public class EPACoverageWithMinimizationSystemTest extends SystemTestBase {

	@Before
	public void prepare() {
		Properties.ASSERTIONS = false;
		Properties.P_FUNCTIONAL_MOCKING = 0.0;
		Properties.P_REFLECTION_ON_PRIVATE = 0.0;
	}

	
	@Test
	public void testEPACoverageWithMinimization() {
		Properties.STOPPING_CONDITION = StoppingCondition.MAXTIME;
		Properties.SEARCH_BUDGET = 60;
		Properties.MINIMIZE = true;
		Properties.CRITERION = new Properties.Criterion[] { Properties.Criterion.EPATRANSITION };

		// check test case
		String xmlFilename = String.join(File.separator, System.getProperty("user.dir"), "..", "client", "src", "test",
				"resources", "epas", "ListItr.xml");
		File epaXMLFile = new File(xmlFilename);
		Assume.assumeTrue(epaXMLFile.exists());

		final String targetClass = ListItr.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		Properties.EPA_XML_PATH = xmlFilename;

		final EvoSuite evoSuite = new EvoSuite();
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		final Object results = evoSuite.parseCommandLine(command);
		Assert.assertNotNull(results);
		GeneticAlgorithm<?> ga = getGAFromResult(results);

		TestSuiteChromosome bestIndividual = (TestSuiteChromosome) ga.getBestIndividual();
		assertTrue(!bestIndividual.getTests().isEmpty());

		TestCase test = bestIndividual.getTests().get(0);
		assertTrue(!test.isEmpty());

		String individual = bestIndividual.toString();
		System.out.println("===========================");
		System.out.println("Best Individual:");
		System.out.println(individual);
		System.out.println("===========================");
	}

}
