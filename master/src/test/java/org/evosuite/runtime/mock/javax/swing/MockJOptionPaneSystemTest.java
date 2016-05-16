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
package org.evosuite.runtime.mock.javax.swing;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.mock.javax.swing.AskUser;
import com.examples.with.different.packagename.mock.javax.swing.AskUserShowConfirmDialogs0;
import com.examples.with.different.packagename.mock.javax.swing.AskUserShowConfirmDialogs1;
import com.examples.with.different.packagename.mock.javax.swing.AskUserShowConfirmDialogs2;
import com.examples.with.different.packagename.mock.javax.swing.AskUserShowConfirmDialogs3;
import com.examples.with.different.packagename.mock.javax.swing.AskUserShowInputDailogs;
import com.examples.with.different.packagename.mock.javax.swing.AskUserShowInternalConfirmDialogs0;
import com.examples.with.different.packagename.mock.javax.swing.AskUserShowInternalConfirmDialogs1;
import com.examples.with.different.packagename.mock.javax.swing.AskUserShowInternalConfirmDialogs2;
import com.examples.with.different.packagename.mock.javax.swing.AskUserShowInternalConfirmDialogs3;
import com.examples.with.different.packagename.mock.javax.swing.AskUserShowOptionDialog;
import com.examples.with.different.packagename.mock.javax.swing.ShowMessageDialogExample;

/**
 * Created by galeotti on 11/05/2016.
 */
public class MockJOptionPaneSystemTest extends SystemTestBase {

	@Test
	public void testShowMessageDialogExample() throws Exception {
		String targetClass = ShowMessageDialogExample.class.getCanonicalName();

		Properties.CRITERION = new Properties.Criterion[] { Properties.Criterion.BRANCH };
		Properties.TARGET_CLASS = targetClass;
		Properties.REPLACE_GUI = true;
		Properties.MINIMIZE = true;
		// As mutation operators remove instrumentation. This needs fixing first
		Properties.ASSERTIONS = false;

		EvoSuite evosuite = new EvoSuite();
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		Assert.assertNotNull(best);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);

	}

	@Test
	public void testShowInputDialogExample() throws Exception {
		final String targetClass = AskUser.class.getCanonicalName();

		Properties.TEST_ARCHIVE = false;

		Properties.CRITERION = new Properties.Criterion[] { Properties.Criterion.BRANCH };
		Properties.TARGET_CLASS = targetClass;
		Properties.REPLACE_GUI = true;
		Properties.MINIMIZE = true;
		// As mutation operators remove instrumentation. This needs fixing first
		Properties.ASSERTIONS = false;

		EvoSuite evosuite = new EvoSuite();
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		Assert.assertNotNull(best);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
		Assert.assertEquals("Non-optimal fitness: ", 0d, best.getFitness(), 0.001);
	}

	@Test
	public void testShowInputMultipleDialogs() throws Exception {
		final String targetClass = AskUserShowInputDailogs.class.getCanonicalName();

		Properties.TEST_ARCHIVE = false;

		Properties.CRITERION = new Properties.Criterion[] { Properties.Criterion.BRANCH };
		Properties.TARGET_CLASS = targetClass;
		Properties.REPLACE_GUI = true;
		Properties.MINIMIZE = true;
		// As mutation operators remove instrumentation. This needs fixing first
		Properties.ASSERTIONS = false;

		EvoSuite evosuite = new EvoSuite();
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		Assert.assertNotNull(best);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
		Assert.assertEquals("Non-optimal fitness: ", 0d, best.getFitness(), 0.001);
	}

	@Test
	public void testShowConfirmDialogs0() throws Exception {
		final String targetClass = AskUserShowConfirmDialogs0.class.getCanonicalName();

		Properties.TEST_ARCHIVE = false;

		Properties.CRITERION = new Properties.Criterion[] { Properties.Criterion.BRANCH };
		Properties.TARGET_CLASS = targetClass;
		Properties.REPLACE_GUI = true;
		Properties.MINIMIZE = true;
		// As mutation operators remove instrumentation. This needs fixing first
		Properties.ASSERTIONS = false;

		EvoSuite evosuite = new EvoSuite();
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		Assert.assertNotNull(best);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
		Assert.assertEquals("Non-optimal fitness: ", 0d, best.getFitness(), 0.001);

	}

	@Test
	public void testShowConfirmDialogs1() throws Exception {
		final String targetClass = AskUserShowConfirmDialogs1.class.getCanonicalName();

		Properties.TEST_ARCHIVE = false;

		Properties.CRITERION = new Properties.Criterion[] { Properties.Criterion.BRANCH };
		Properties.TARGET_CLASS = targetClass;
		Properties.REPLACE_GUI = true;
		Properties.MINIMIZE = true;
		// As mutation operators remove instrumentation. This needs fixing first
		Properties.ASSERTIONS = false;

		EvoSuite evosuite = new EvoSuite();
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		Assert.assertNotNull(best);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
		Assert.assertEquals("Non-optimal fitness: ", 0d, best.getFitness(), 0.001);

	}

	@Test
	public void testShowConfirmDialogs2() throws Exception {
		final String targetClass = AskUserShowConfirmDialogs2.class.getCanonicalName();

		Properties.TEST_ARCHIVE = false;

		Properties.CRITERION = new Properties.Criterion[] { Properties.Criterion.BRANCH };
		Properties.TARGET_CLASS = targetClass;
		Properties.REPLACE_GUI = true;
		Properties.MINIMIZE = true;
		// As mutation operators remove instrumentation. This needs fixing first
		Properties.ASSERTIONS = false;

		EvoSuite evosuite = new EvoSuite();
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		Assert.assertNotNull(best);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
		Assert.assertEquals("Non-optimal fitness: ", 0d, best.getFitness(), 0.001);

	}

	@Test
	public void testShowConfirmDialogs3() throws Exception {
		final String targetClass = AskUserShowConfirmDialogs3.class.getCanonicalName();

		Properties.TEST_ARCHIVE = false;

		Properties.CRITERION = new Properties.Criterion[] { Properties.Criterion.BRANCH };
		Properties.TARGET_CLASS = targetClass;
		Properties.REPLACE_GUI = true;
		Properties.MINIMIZE = true;
		// As mutation operators remove instrumentation. This needs fixing first
		Properties.ASSERTIONS = false;

		EvoSuite evosuite = new EvoSuite();
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		Assert.assertNotNull(best);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
		Assert.assertEquals("Non-optimal fitness: ", 0d, best.getFitness(), 0.001);

	}

	@Test
	public void testShowInternalConfirmDialogs0() throws Exception {
		final String targetClass = AskUserShowInternalConfirmDialogs0.class.getCanonicalName();

		Properties.TEST_ARCHIVE = false;

		Properties.CRITERION = new Properties.Criterion[] { Properties.Criterion.BRANCH };
		Properties.TARGET_CLASS = targetClass;
		Properties.REPLACE_GUI = true;
		Properties.MINIMIZE = true;
		// As mutation operators remove instrumentation. This needs fixing first
		Properties.ASSERTIONS = false;

		EvoSuite evosuite = new EvoSuite();
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		Assert.assertNotNull(best);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
		Assert.assertEquals("Non-optimal fitness: ", 0d, best.getFitness(), 0.001);

	}

	@Test
	public void testShowInternalConfirmDialogs1() throws Exception {
		final String targetClass = AskUserShowInternalConfirmDialogs1.class.getCanonicalName();

		Properties.TEST_ARCHIVE = false;

		Properties.CRITERION = new Properties.Criterion[] { Properties.Criterion.BRANCH };
		Properties.TARGET_CLASS = targetClass;
		Properties.REPLACE_GUI = true;
		Properties.MINIMIZE = true;
		// As mutation operators remove instrumentation. This needs fixing first
		Properties.ASSERTIONS = false;

		EvoSuite evosuite = new EvoSuite();
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		Assert.assertNotNull(best);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
		Assert.assertEquals("Non-optimal fitness: ", 0d, best.getFitness(), 0.001);

	}

	@Test
	public void testShowInternalConfirmDialogs2() throws Exception {
		final String targetClass = AskUserShowInternalConfirmDialogs2.class.getCanonicalName();

		Properties.TEST_ARCHIVE = false;

		Properties.CRITERION = new Properties.Criterion[] { Properties.Criterion.BRANCH };
		Properties.TARGET_CLASS = targetClass;
		Properties.REPLACE_GUI = true;
		Properties.MINIMIZE = true;
		// As mutation operators remove instrumentation. This needs fixing first
		Properties.ASSERTIONS = false;

		EvoSuite evosuite = new EvoSuite();
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		Assert.assertNotNull(best);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
		Assert.assertEquals("Non-optimal fitness: ", 0d, best.getFitness(), 0.001);

	}

	@Test
	public void testShowInternalConfirmDialogs3() throws Exception {
		final String targetClass = AskUserShowInternalConfirmDialogs3.class.getCanonicalName();

		Properties.TEST_ARCHIVE = false;

		Properties.CRITERION = new Properties.Criterion[] { Properties.Criterion.BRANCH };
		Properties.TARGET_CLASS = targetClass;
		Properties.REPLACE_GUI = true;
		Properties.MINIMIZE = true;
		// As mutation operators remove instrumentation. This needs fixing first
		Properties.ASSERTIONS = false;

		EvoSuite evosuite = new EvoSuite();
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		Assert.assertNotNull(best);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
		Assert.assertEquals("Non-optimal fitness: ", 0d, best.getFitness(), 0.001);

	}

	@Test
	public void testShowOptionDialogExample() throws Exception {
		final String targetClass = AskUserShowOptionDialog.class.getCanonicalName();

		Properties.TEST_ARCHIVE = false;

		Properties.CRITERION = new Properties.Criterion[] { Properties.Criterion.BRANCH };
		Properties.TARGET_CLASS = targetClass;
		Properties.REPLACE_GUI = true;
		Properties.MINIMIZE = true;
		// As mutation operators remove instrumentation. This needs fixing first
		Properties.ASSERTIONS = false;

		EvoSuite evosuite = new EvoSuite();
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		Assert.assertNotNull(best);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
		Assert.assertEquals("Non-optimal fitness: ", 0d, best.getFitness(), 0.001);
	}

	
}
