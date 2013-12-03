package org.evosuite.instrumentation.error;

import java.util.List;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.errorbranch.VectorAccess;
import com.examples.with.different.packagename.errorbranch.VectorAccessIndex;

public class TestVectorInstrumentation extends SystemTest {

	@Test
	public void testVectorWithoutErrorBranches() {

		EvoSuite evosuite = new EvoSuite();

		String targetClass = VectorAccess.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.ERROR_BRANCHES = false;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		Assert.assertEquals("Wrong number of goals: ", 3, goals);
		// One infeasible error branch
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	@Test
	public void testVectorWithErrorBranches() {

		EvoSuite evosuite = new EvoSuite();

		String targetClass = VectorAccess.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.ERROR_BRANCHES = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		List<? extends TestFitnessFunction> goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals();
		Assert.assertEquals("Wrong number of goals: ", 13, goals.size());

		// Not sure why we have to do this:
		TestCaseExecutor.initExecutor();

		for (TestFitnessFunction goal : goals) {
			BranchCoverageTestFitness branchGoal = (BranchCoverageTestFitness) goal;
			if (branchGoal.getBranch() != null
			        && !branchGoal.getBranch().isInstrumented()) {
				Assert.assertTrue(branchGoal.isCoveredBy(best));
			}
		}
	}

	@Test
	public void testVectorIndexWithoutErrorBranches() {

		EvoSuite evosuite = new EvoSuite();

		String targetClass = VectorAccessIndex.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.ERROR_BRANCHES = false;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		Assert.assertEquals("Wrong number of goals: ", 3, goals);
		// One infeasible error branch
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	@Test
	public void testVectorIndexWithErrorBranches() {

		EvoSuite evosuite = new EvoSuite();

		String targetClass = VectorAccessIndex.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.ERROR_BRANCHES = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		List<? extends TestFitnessFunction> goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals();
		Assert.assertEquals("Wrong number of goals: ", 15, goals.size());

		// Not sure why we have to do this:
		TestCaseExecutor.initExecutor();

		for (TestFitnessFunction goal : goals) {
			BranchCoverageTestFitness branchGoal = (BranchCoverageTestFitness) goal;
			if (branchGoal.getBranch() != null
			        && !branchGoal.getBranch().isInstrumented()) {
				Assert.assertTrue(branchGoal.isCoveredBy(best));
			}
		}
	}
}
