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
package org.evosuite.assertion.purity;

import static org.junit.Assert.assertFalse;

import java.util.Map;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.assertion.CheapPurityAnalyzer;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.statistics.OutputVariable;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.statistics.backend.DebugStatisticsBackend;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Type;

import com.examples.with.different.packagename.purity.ImpureRandomness;

public class ImpureRandomnessSystemTest extends SystemTestBase {
	private final boolean DEFAULT_PURE_INSPECTORS = Properties.PURE_INSPECTORS;
	private final boolean DEFAULT_ASSERTIONS = Properties.ASSERTIONS;
	private final boolean DEFAULT_SANDBOX = Properties.SANDBOX;
	private final boolean DEFAULT_RESET_STATIC_FIELDS = Properties.RESET_STATIC_FIELDS;
	private final boolean DEFAULT_REPLACE_CALLS = Properties.REPLACE_CALLS;
	private final boolean DEFAULT_JUNIT_CHECK = Properties.JUNIT_CHECK;
	private final boolean DEFAULT_JUNIT_TESTS = Properties.JUNIT_TESTS;
	private final boolean DEFAULT_JUNIT_CHECK_ON_SEPARATE_PROCESS = Properties.JUNIT_CHECK_ON_SEPARATE_PROCESS;

	@Before
	public void saveProperties() {
		Properties.SANDBOX = true;
		Properties.ASSERTIONS = false;
		Properties.JUNIT_TESTS = true;
		Properties.PURE_INSPECTORS = true;
		Properties.RESET_STATIC_FIELDS = true;
		Properties.REPLACE_CALLS = true;
		Properties.JUNIT_CHECK = true;
		Properties.JUNIT_CHECK_ON_SEPARATE_PROCESS = false;
	}

	@After
	public void restoreProperties() {
		Properties.SANDBOX = DEFAULT_SANDBOX;
		Properties.ASSERTIONS = DEFAULT_ASSERTIONS;
		Properties.JUNIT_TESTS = DEFAULT_JUNIT_TESTS;
		Properties.PURE_INSPECTORS = DEFAULT_PURE_INSPECTORS;
		Properties.RESET_STATIC_FIELDS = DEFAULT_RESET_STATIC_FIELDS;
		Properties.REPLACE_CALLS = DEFAULT_REPLACE_CALLS;
		Properties.JUNIT_CHECK = DEFAULT_JUNIT_CHECK;
		Properties.JUNIT_CHECK_ON_SEPARATE_PROCESS = DEFAULT_JUNIT_CHECK_ON_SEPARATE_PROCESS;

	}

	@Test
	public void test() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = ImpureRandomness.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		Properties.OUTPUT_VARIABLES=""+RuntimeVariable.HadUnstableTests;
		String[] command = new String[] { "-generateSuite", "-class",
				targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);
		double best_fitness = best.getFitness();
		Assert.assertTrue("Optimal coverage was not achieved ",
				best_fitness == 0.0);

		CheapPurityAnalyzer purityAnalyzer = CheapPurityAnalyzer.getInstance();

		String intTypeDescriptor = Type.getMethodDescriptor(Type.INT_TYPE);
		boolean randomNextInt = purityAnalyzer.isPure(targetClass,
				"randomNextInt", intTypeDescriptor);
		assertFalse(randomNextInt);

		boolean secureRandomNextInt = purityAnalyzer.isPure(targetClass,
				"secureRandomNextInt", intTypeDescriptor);
		assertFalse(secureRandomNextInt);

		String stringTypeDescriptor = Type.getMethodDescriptor(Type
				.getType(String.class));
		boolean randomUUIDToString = purityAnalyzer.isPure(targetClass,
				"randomUUIDToString", stringTypeDescriptor);
		assertFalse(randomUUIDToString);

		String doubleTypeDescriptor = Type
				.getMethodDescriptor(Type.DOUBLE_TYPE);
		boolean randomMath = purityAnalyzer.isPure(targetClass, "randomMath",
				doubleTypeDescriptor);
		assertFalse(randomMath);

		Map<String, OutputVariable<?>> map = DebugStatisticsBackend.getLatestWritten();
		Assert.assertNotNull(map);
		OutputVariable unstable = map.get(RuntimeVariable.HadUnstableTests.toString());
		Assert.assertNotNull(unstable);
		Assert.assertEquals(Boolean.FALSE, unstable.getValue());
	}

}
