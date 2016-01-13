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

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.StoppingCondition;
import org.evosuite.SystemTest;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testcase.TestCase;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.epa.ListItr;

public class EPACoverageSystemTest extends SystemTest {

	@Before
	public void prepare() {
		Properties.TIMEOUT = 15 * 60 * 1000; // test execution timeout 15 min
		// Properties.STRATEGY = Strategy.ENTBUG;
		// Properties.STOPPING_CONDITION = StoppingCondition.MAXTIME;
		// Properties.SEARCH_BUDGET = 60;
		//
		// Properties.TEST_ARCHIVE = false;
		// Properties.TEST_FACTORY = TestFactory.RANDOM;
		Properties.MINIMIZE = false;
		// Properties.MINIMIZE_VALUES = false;
		// Properties.INLINE = false;
		Properties.ASSERTIONS = false;
		// Properties.USE_EXISTING_COVERAGE = false;
	}

	@Test
	public void testOnlyEPACoverage() {
		Properties.STOPPING_CONDITION = StoppingCondition.MAXTIME;
		Properties.SEARCH_BUDGET = 60;
		
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

	@Test
	public void testBranchAndEPACoverage() {
		Properties.CRITERION = new Properties.Criterion[] { Properties.Criterion.BRANCH,
				Properties.Criterion.EPATRANSITION };

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

		for (FitnessFunction<?> ff : ga.getFitnessFunctions()) {
			int coveredGoals = bestIndividual.getNumOfCoveredGoals(ff);
			int notCoveredGoals = bestIndividual.getNumOfNotCoveredGoals(ff);
			System.out.println(ff.toString());
			System.out.println("Covered_Goals " + coveredGoals);
			System.out.println("Not_Covered_Goals " +  notCoveredGoals);
		}
		
		

		TestCase test = bestIndividual.getTests().get(0);
		assertTrue(!test.isEmpty());

		String individual = bestIndividual.toString();
		System.out.println("===========================");
		System.out.println("Best Individual:");
		System.out.println(individual);
		System.out.println("===========================");
	}

	@Test
	public void testBranchOnlyCoverage() {
		Properties.CRITERION = new Properties.Criterion[] { Properties.Criterion.BRANCH };
	
		// check test case
		final String targetClass = ListItr.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
	
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

	//
	// @Test
	// public void testZeroRhoScoreWithoutPreviousCoverage() throws IOException
	// {
	//
	// EvoSuite evosuite = new EvoSuite();
	//
	// String targetClass = Compositional.class.getCanonicalName();
	// Properties.TARGET_CLASS = targetClass;
	//
	// Properties.OUTPUT_VARIABLES = RuntimeVariable.RhoScore.name();
	// Properties.STATISTICS_BACKEND = StatisticsBackend.CSV;
	//
	// String[] command = new String[] {
	// "-class", targetClass,
	// "-generateTests"
	// };
	//
	// Object result = evosuite.parseCommandLine(command);
	// Assert.assertNotNull(result);
	//
	// List<?> goals = RhoCoverageFactory.getGoals();
	// assertEquals(11, goals.size());
	//
	// String statistics_file = System.getProperty("user.dir") + File.separator
	// + Properties.REPORT_DIR + File.separator + "statistics.csv";
	//
	// CSVReader reader = new CSVReader(new FileReader(statistics_file));
	// List<String[]> rows = reader.readAll();
	// assertTrue(rows.size() == 2);
	// reader.close();
	//
	// assertEquals("0.5", rows.get(1)[0]);
	// }
	//
	// @Test
	// public void testZeroRhoScoreWithPreviousCoverage() throws IOException {
	//
	// EvoSuite evosuite = new EvoSuite();
	//
	// String targetClass = Compositional.class.getCanonicalName();
	// Properties.TARGET_CLASS = targetClass;
	//
	// String previous_tmp_coverage =
	// "1 1 1 1 1 1 1 1 1 1 1 +\n" +
	// "1 1 1 1 0 0 0 0 0 0 0 +\n";
	// this.writeMatrix(previous_tmp_coverage);
	// Properties.USE_EXISTING_COVERAGE = true;
	//
	// Properties.OUTPUT_VARIABLES = RuntimeVariable.RhoScore.name();
	// Properties.STATISTICS_BACKEND = StatisticsBackend.CSV;
	//
	// String[] command = new String[] {
	// "-class", targetClass,
	// "-generateTests"
	// };
	//
	// Object result = evosuite.parseCommandLine(command);
	// Assert.assertNotNull(result);
	//
	// List<?> goals = RhoCoverageFactory.getGoals();
	// assertEquals(11, goals.size());
	// assertEquals(15, RhoCoverageFactory.getNumber_of_Ones());
	// assertEquals(2, RhoCoverageFactory.getNumber_of_Test_Cases());
	// assertEquals((15.0 / 11.0 / 2.0) - 0.5, RhoCoverageFactory.getRho(),
	// 0.0001);
	//
	// String statistics_file = System.getProperty("user.dir") + File.separator
	// + Properties.REPORT_DIR + File.separator + "statistics.csv";
	//
	// CSVReader reader = new CSVReader(new FileReader(statistics_file));
	// List<String[]> rows = reader.readAll();
	// assertTrue(rows.size() == 2);
	// reader.close();
	//
	// assertEquals("0.5", rows.get(1)[0]);
	// }
}
