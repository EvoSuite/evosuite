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
package org.evosuite.runtime.classhandling;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.junit.writer.TestSuiteWriter;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.Assert.assertFalse;

/**
 * @author Jose Rojas
 */
public class ClassWithProtectedConstructorSystemTest extends SystemTestBase {

	@Test
	public void shouldNotGenerateTryCatchForIllegalAccessException() throws IOException {

		// run EvoSuite
		EvoSuite evosuite = new EvoSuite();
		String targetClass = com.examples.with.different.packagename.listclasses.ClassWithProtectedMethods.class.getCanonicalName();
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		// write test suite
		TestCaseExecutor.initExecutor();
		TestSuiteWriter writer = new TestSuiteWriter();
		writer.insertAllTests(best.getTests());
		String name = targetClass.substring(targetClass.lastIndexOf(".") + 1) + Properties.JUNIT_SUFFIX;
		writer.writeTestSuite(name, Properties.TEST_DIR, Collections.EMPTY_LIST);

		// check that the test suite was created
		String junitFile = Properties.TEST_DIR + File.separatorChar +
				Properties.CLASS_PREFIX.replace('.', File.separatorChar) + File.separatorChar +
				name + ".java";
		Path path = Paths.get(junitFile);
		Assert.assertTrue("Test Suite does not exist", Files.exists(path));

		String testCode = new String(Files.readAllBytes(path));
		System.out.println(testCode);
		assertFalse("IllegalAccesException should not occur", testCode.contains("catch(IllegalAccessException e)"));
	}
}
