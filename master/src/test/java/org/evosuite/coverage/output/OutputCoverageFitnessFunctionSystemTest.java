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
package org.evosuite.coverage.output;

import com.examples.with.different.packagename.coverage.ClassWithHashCode;
import com.examples.with.different.packagename.coverage.MethodReturnsArray;
import com.examples.with.different.packagename.coverage.MethodReturnsObject;
import com.examples.with.different.packagename.coverage.MethodReturnsPrimitive;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.SystemTestBase;
import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.*;

/**
 * @author Jose Miguel Rojas
 *
 */
public class OutputCoverageFitnessFunctionSystemTest extends SystemTestBase {

    private static final Criterion[] defaultCriterion = Properties.CRITERION;
    
    private static boolean defaultArchive = Properties.TEST_ARCHIVE;

	@After
	public void resetProperties() {
		Properties.CRITERION = defaultCriterion;
		Properties.TEST_ARCHIVE = defaultArchive;
	}

	@Before
	public void beforeTest() {
        Properties.CRITERION = new Properties.Criterion[] { Criterion.BRANCH, Criterion.OUTPUT };
	}

	@Test
	public void testOutputCoveragePrimitiveTypesWithArchive() {
		Properties.TEST_ARCHIVE = true;
		testOutputCoveragePrimitiveTypes();
	}
	
	@Test
	public void testOutputCoveragePrimitiveTypesWithoutArchive() {
		Properties.TEST_ARCHIVE = false;
		testOutputCoveragePrimitiveTypes();
	}
		
	public void testOutputCoveragePrimitiveTypes() {
		EvoSuite evosuite = new EvoSuite();
		
		String targetClass = MethodReturnsPrimitive.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		int goals = 0;
		for (TestFitnessFactory ff : TestGenerationStrategy.getFitnessFactories())
			goals += ff.getCoverageGoals().size();
		Assert.assertEquals("Unexpected number of goals", 24, goals);
		Assert.assertEquals("Non-optimal fitness: ", 0.0, best.getFitness(), 0.001);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	@Test @Ignore("Changed output coverage for Objects")
	public void testOutputCoverageObjectTypeWithArchive() {
		Properties.TEST_ARCHIVE = true;
		testOutputCoverageObjectType();
	}
	
	@Test @Ignore("Changed output coverage for Objects")
	public void testOutputCoverageObjectTypeWithoutArchive() {
		Properties.TEST_ARCHIVE = false;
		testOutputCoverageObjectType();
	}

	public void testOutputCoverageObjectType() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = MethodReturnsObject.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		int goals = 0;
		for (TestFitnessFactory ff : TestGenerationStrategy.getFitnessFactories())
			goals += ff.getCoverageGoals().size();
		Assert.assertEquals("Unexpected number of goals", 14, goals);
		Assert.assertEquals("Unexpected coverage: ", 0.888d, best.getCoverage(), 0.001); // sub-optimal due to hashcode observer
	}

	@Test
	public void testOutputCoverageArray() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = MethodReturnsArray.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		int goals = 0;
		for (TestFitnessFactory ff : TestGenerationStrategy.getFitnessFactories())
			goals += ff.getCoverageGoals().size();
		Assert.assertEquals("Unexpected number of goals", 15, goals);
		Assert.assertEquals("Non-optimal fitness: ", 0.0, best.getFitness(), 0.001);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	@Test
	public void testOutputCoverageIgnoreHashCode() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = ClassWithHashCode.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		int goals = 0;
		for (TestFitnessFactory ff : TestGenerationStrategy.getFitnessFactories())
			goals += ff.getCoverageGoals().size();
		Assert.assertEquals("Unexpected number of goals", 2, goals);
		Assert.assertEquals("Non-optimal fitness: ", 0.0, best.getFitness(), 0.001);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
}
