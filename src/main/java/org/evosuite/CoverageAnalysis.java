/**
 * 
 */
package org.evosuite;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;

import org.evosuite.Properties.Criterion;
import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.coverage.dataflow.DefUseCoverageFactory;
import org.evosuite.coverage.dataflow.DefUseCoverageTestFitness;
import org.evosuite.coverage.mutation.MutationSuiteFitness;
import org.evosuite.setup.TestClusterGenerator;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.ExecutionTracer;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.SearchStatistics;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.ReportGenerator.StatisticEntry;

/**
 * @author Gordon Fraser
 * 
 */
public class CoverageAnalysis {

	private static void reinstrument(TestSuiteChromosome testSuite,
	        Properties.Criterion criterion) {
		Properties.Criterion oldCriterion = Properties.CRITERION;
		if (oldCriterion == criterion)
			return;

		testSuite.setChanged(true);
		for (TestChromosome test : testSuite.getTestChromosomes()) {
			test.setChanged(true);
			test.clearCachedResults();
			test.clearCachedMutationResults();
		}


		/*
		List<Properties.Criterion> mutationCriteria = Arrays.asList(new Properties.Criterion[] {
		        Properties.Criterion.WEAKMUTATION, Properties.Criterion.STRONGMUTATION,
		        Properties.Criterion.MUTATION });
		if (mutationCriteria.contains(criterion)
		        && mutationCriteria.contains(oldCriterion))
			return;
			*/

		Properties.CRITERION = criterion;
		LoggingUtils.getEvoLogger().info("Re-instrumenting for criterion: "+Properties.CRITERION);
		TestGenerationContext.getInstance().resetContext();

		// TODO: Now all existing test cases have reflection objects pointing to the wrong classloader
		LoggingUtils.getEvoLogger().info("Changing classloader of test suite for criterion: "+Properties.CRITERION);
		for (TestChromosome test : testSuite.getTestChromosomes()) {
			DefaultTestCase dtest = (DefaultTestCase) test.getTestCase();
			dtest.changeClassLoader(TestGenerationContext.getClassLoader());
		}

	}

	public static void analyzeCriteria(TestSuiteChromosome testSuite, String criteria) {
		Criterion oldCriterion = Properties.CRITERION;
		for (String criterion : criteria.split(",")) {
			if(SearchStatistics.getInstance().hasCoverage(criterion)) {
				LoggingUtils.getEvoLogger().info("Skipping measuring coverage of criterion: "+criterion);
				continue;
			}
			
			analyzeCoverage(testSuite, criterion);
		}
		reinstrument(testSuite, oldCriterion);
		Properties.CRITERION = oldCriterion;
	}

	public static void analyzeCoverage(TestSuiteChromosome testSuite, String criterion) {
		try {
			LoggingUtils.getEvoLogger().info("Measuring coverage of criterion: "+criterion);

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
				if (Properties.CRITERION == Properties.Criterion.DEFUSE) {
					StatisticEntry entry = SearchStatistics.getInstance().getLastStatisticEntry();
							if (((DefUseCoverageTestFitness)goal).isInterMethodPair())
								entry.coveredInterMethodPairs++;
							else if (((DefUseCoverageTestFitness)goal).isIntraClassPair())
								entry.coveredIntraClassPairs++;
							else if (((DefUseCoverageTestFitness)goal).isParameterGoal())
								entry.coveredParameterPairs++;
							else
								entry.coveredIntraMethodPairs++;
					
				}
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
				
			}

			LoggingUtils.getEvoLogger().info("* Coverage of criterion "
			                                         + criterion
			                                         + ": "
			                                         + NumberFormat.getPercentInstance().format((double) covered
			                                                                                            / (double) goals.size()));

		}



	}
}
