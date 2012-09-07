/**
 * 
 */
package org.evosuite;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;

import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.coverage.mutation.MutationSuiteFitness;
import org.evosuite.setup.TestCluster;
import org.evosuite.setup.TestClusterGenerator;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.ExecutionTracer;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.SearchStatistics;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.LoggingUtils;

/**
 * @author Gordon Fraser
 * 
 */
public class CoverageAnalysis {

	private static void reinstrument(TestSuiteChromosome testSuite,
	        Properties.Criterion criterion) {
		Properties.Criterion oldCriterion = Properties.CRITERION;

		testSuite.setChanged(true);
		for (TestChromosome test : testSuite.getTestChromosomes()) {
			test.setChanged(true);
			test.clearCachedResults();
			test.clearCachedMutationResults();
		}

		if (oldCriterion == criterion)
			return;

		List<Properties.Criterion> mutationCriteria = Arrays.asList(new Properties.Criterion[] {
		        Properties.Criterion.WEAKMUTATION, Properties.Criterion.STRONGMUTATION,
		        Properties.Criterion.MUTATION });
		if (mutationCriteria.contains(criterion)
		        && mutationCriteria.contains(oldCriterion))
			return;

		Properties.CRITERION = criterion;

		try {
			TestClusterGenerator.resetCluster();
		} catch (Exception e) {
			LoggingUtils.getEvoLogger().info("Error while instrumenting for assertion generation: "
			                                         + e.getMessage());
			return;
		}

		// TODO: Now all existing test cases have reflection objects pointing to the wrong classloader
		for (TestChromosome test : testSuite.getTestChromosomes()) {
			DefaultTestCase dtest = (DefaultTestCase) test.getTestCase();
			dtest.changeClassLoader(TestCluster.classLoader);
		}

	}

	public static void analyzeCriteria(TestSuiteChromosome testSuite, String criteria) {
		for (String criterion : criteria.split(",")) {
			analyzeCoverage(testSuite, criterion);
		}
	}

	public static void analyzeCoverage(TestSuiteChromosome testSuite, String criterion) {
		try {
			Properties.Criterion crit = Properties.Criterion.valueOf(criterion.toUpperCase());
			analyzeCoverage(testSuite, crit);
		} catch (IllegalArgumentException e) {
			LoggingUtils.getEvoLogger().info("* Unknown coverage criterion: " + criterion);
		}
	}

	public static void analyzeCoverage(TestSuiteChromosome testSuite,
	        Properties.Criterion criterion) {

		Properties.Criterion oldCriterion = Properties.CRITERION;
		ExecutionTracer.enableTraceCalls();

		reinstrument(testSuite, criterion);
		TestFitnessFactory factory = TestSuiteGenerator.getFitnessFactory(criterion);

		int covered = 0;
		List<TestFitnessFunction> goals = factory.getCoverageGoals();
		for (TestFitnessFunction goal : goals) {
			if (goal.isCoveredBy(testSuite)) {
				covered++;
			}
		}

		if (goals.isEmpty()) {
			SearchStatistics.getInstance().addCoverage(criterion.toString(), 1.0);
			if (criterion == Properties.Criterion.MUTATION
			        || criterion == Properties.Criterion.STRONGMUTATION)
				SearchStatistics.getInstance().mutationScore(1.0);
			LoggingUtils.getEvoLogger().info("* Coverage of criterion " + criterion
			                                         + ": 100% (no goals)");
		} else {
			SearchStatistics.getInstance().addCoverage(criterion.toString(),
			                                           (double) covered
			                                                   / (double) goals.size());
			if (criterion == Properties.Criterion.MUTATION
			        || criterion == Properties.Criterion.STRONGMUTATION) {
				SearchStatistics.getInstance().mutationScore((double) covered
				                                                     / (double) goals.size());
				if (oldCriterion == criterion)
					SearchStatistics.getInstance().setCoveredGoals(covered);
				MutationSuiteFitness.mostCoveredGoals = covered;
			}

			LoggingUtils.getEvoLogger().info("* Coverage of criterion "
			                                         + criterion
			                                         + ": "
			                                         + NumberFormat.getPercentInstance().format((double) covered
			                                                                                            / (double) goals.size()));

		}
		Properties.CRITERION = oldCriterion;

	}
}
