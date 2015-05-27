/**
 * 
 */
package org.evosuite.regression;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.SearchListener;
import org.evosuite.testcase.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sina
 * 
 */
public class RegressionSearchListener implements SearchListener {

	public boolean isFirstRun = true;
	public boolean isFirst = true;
	public boolean isLastRun = false;
	public static List<TestCase> previousTestSuite = new ArrayList<TestCase>();
	public static List<TestCase> currentTestSuite = new ArrayList<TestCase>();
	public static int firstAssertionCount = 0;
	public static String statsID = "";

	public static long ObjectDistanceTime = 0;
	public static long branchDistanceTime = 0;
	public static long odCollectionTime = 0;
	public static long coverageTime = 0;
	public static long assertionTime = 0;
	public static long testExecutionTime = 0;

	public static boolean killTheSearch = false;

	public static long startTime;

	protected static final Logger logger = LoggerFactory
			.getLogger(RegressionSearchListener.class);

	public static String jdiffReport = "";

	public static String analysisReport = "";

	public static int exceptionDiff = 0;

	public static File statsFile;
	public static double lastOD = 0.0;
	public static int lastAssertions = 0;

	/*
	 * Create a stats directory and/or file when the search starts
	 * 
	 * @see
	 * org.evosuite.ga.metaheuristics.SearchListener#searchStarted(org.evosuite
	 * .ga.metaheuristics.GeneticAlgorithm)
	 */
	@Override
	public void searchStarted(GeneticAlgorithm<?> algorithm) {
		String statsDirName = "evosuiter-stats";
		File statsDir = new File(statsDirName);
		int filecount = 0;
		if (statsDir.exists() && statsDir.isDirectory()) {
			filecount = statsDir.list().length;
		} else {
			statsDir.mkdirs();
		}

		statsFile = new File(statsDirName + "/" + (filecount + 1) + ""
				+ ((int) (Math.random() * 1000)) + "_"
				+ Properties.getTargetClass().getSimpleName() + ".csv");

		if (statsFile.exists())
			statsFile = new File(statsDirName + "/" + (filecount + 1) + ""
					+ ((int) (Math.random() * 1000)) + "_"
					+ Properties.getTargetClass().getSimpleName() + "_"
					+ System.currentTimeMillis() + ".csv");

		statsID = statsFile.getName().replaceFirst("[.][^.]+$", "");

		try {
			String data = "fitness,test_count,test_size,branch_distance,object_distance,coverage,exception_diff,total_exceptions,coverage_old,coverage_new,executed_statements,age,time,assertions,state,exec_time,assert_time,cover_time,state_diff_time,branch_time,obj_time";
			FileUtils.writeStringToFile(statsFile, data, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		startTime = System.currentTimeMillis();
	}

	@Override
	public void iteration(GeneticAlgorithm<?> algorithm) {
		// runState: First, Processing, Last
		char runState = (isFirst) ? 'F' : ((isLastRun) ? 'L' : 'P');
		if (isFirst)
			isFirst = false;
		RegressionTestSuiteChromosome ind = null;
		Chromosome individual = algorithm.getBestIndividual();
		if (individual instanceof RegressionTestChromosome) {
			ind = new RegressionTestSuiteChromosome();
			ind.addTest((RegressionTestChromosome) individual);
			ind.fitnessData = ((RegressionTestChromosome) individual).fitnessData;
			ind.objDistance = ((RegressionTestChromosome) individual).objDistance;
			ind.diffExceptions = ((RegressionTestChromosome) individual).diffExceptions;
		} else { // should be instanceof RegressionTestSuiteChromosome
			ind = (RegressionTestSuiteChromosome) individual;
			ind.fitnessData = ((RegressionTestSuiteChromosome) individual).fitnessData;
			ind.objDistance = ((RegressionTestSuiteChromosome) individual).objDistance;
			ind.diffExceptions = ((RegressionTestSuiteChromosome) individual).diffExceptions;
		}
		// hack for getting the correct number of different exceptions (of the
		// last diff)
		ind.fitnessData = ind.fitnessData.replace("numDifferentExceptions", ""
				+ exceptionDiff);

		int curAssertions = RegressionAssertionCounter.getNumAssertions(ind);
		// curAssertions += ind.diffExceptions;

		if (curAssertions > 0) {
			individual.setFitness(algorithm.getFitnessFunction(), 0);
			algorithm.setStoppingConditionLimit(0);
			// RegressionSearchListener.killTheSearch = false;
		}

		double curOD = ind.objDistance;

		try {

			writeIterationLog(algorithm, runState, ind, curAssertions, curOD);

			// previousTestSuite.clear();
			if (curAssertions > 0) {
				/*
				 * previousTestSuite .addAll( ((RegressionTestSuiteChromosome)
				 * algorithm .getBestIndividual()).getTests() );
				 */
				/*
				 * previousTestSuite = new
				 * ArrayList<TestCase>(((RegressionTestSuiteChromosome
				 * )((RegressionTestSuiteChromosome) algorithm
				 * .getBestIndividual()).clone()).getTests() );
				 */
			}

			RegressionSearchListener.lastOD = curOD;
			lastAssertions = curAssertions;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeIterationLog(GeneticAlgorithm<?> algorithm,
			char runState, RegressionTestSuiteChromosome ind,
			int curAssertions, double curOD) throws IOException {
		FileUtils
				.writeStringToFile(
						statsFile,
						"\r\n"
								+ (ind).fitnessData
								+ ","
								+ ((isLastRun) ? (algorithm.getAge() + 1)
										: algorithm.getAge())
								+ ","
								+ (System.currentTimeMillis() - startTime)
								+ ","
								+ curAssertions
								+ ","
								+ runState
								+ ((isLastRun) ? ("," + (testExecutionTime + 1)
										/ 1000000 + "," + (assertionTime + 1)
										/ 1000000 + "," + (coverageTime + 1)
										/ 1000000 + ","
										+ (ObjectDistanceTime + 1) / 1000000
										+ "," + (branchDistanceTime + 1)
										/ 1000000 + "," + (odCollectionTime + 1) / 1000000)
										: ",,,,,,"), true);

		if (!isFirstRun) {
			if (lastAssertions < curAssertions && lastAssertions == 0
					&& lastOD <= curOD && (lastOD != 0 && curOD != 0)
					&& (algorithm.getAge() != 0)) {
				/*
				 * int aCount = getNumAssertions( algorithm.getBestIndividual(),
				 * false);
				 */
				String comments = "// Assertions count: " + curAssertions
						+ "\n" + "// Last assertions count: " + lastAssertions
						+ "\n" + "// Current Object Distance: " + curOD + "\n"
						+ "// Last Object Distance: " + RegressionSearchListener.lastOD + "\n"
						+ "// StatsID: " + statsID + "\n" + "// Age: "
						+ algorithm.getAge() + "\n"
						+ "//--------------------------------------------"
						+ "\n" + "//--- CURRENT VERSION" + "\n"
						+ "//--------------------------------------------"
						+ "\n" + "\n";
				/*
				 * TestSuiteGenerator.keepJUnitTests(
				 * ((RegressionTestSuiteChromosome) algorithm
				 * .getBestIndividual()).getTests(), comments);
				 */
				/*
				 * TestSuiteGenerator.keepJUnitTests(previousTestSuite,
				 * comments);
				 * TestSuiteGenerator.keepJUnitTests(currentTestSuite,
				 * comments);
				 */
				// getNumAssertions(algorithm.getBestIndividual());
			} else if (curAssertions == 0 && lastAssertions > 0
					&& (algorithm.getAge() != 0)
					&& previousTestSuite.size() > 0) {
				/*
				 * int aCount = getNumAssertions( algorithm.getBestIndividual(),
				 * false);
				 */
				String comments = "// Assertions count: " + curAssertions
						+ "\n" + "// Last assertions count: " + lastAssertions
						+ "\n" + "// Current Object Distance: " + curOD + "\n"
						+ "// Last Object Distance: " + RegressionSearchListener.lastOD + "\n"
						+ "// StatsID: " + statsID + "\n" + "// Age: "
						+ algorithm.getAge() + "\n"
						+ "//--------------------------------------------"
						+ "\n" + "//--- OLD VERSION" + "\n"
						+ "//--------------------------------------------"
						+ "\n" + "\n";
				/*
				 * TestSuiteGenerator.keepJUnitTests(previousTestSuite,
				 * comments);
				 * TestSuiteGenerator.keepJUnitTests(currentTestSuite,
				 * comments);
				 */
				// getNumAssertions(algorithm.getBestIndividual());
			}

		}
	}

	@Override
	public void searchFinished(GeneticAlgorithm<?> algorithm) {
		isLastRun = true;
		this.iteration(algorithm);
		int iteration = algorithm.getAge();
		logger.warn("total number of generations: " + iteration);
		RegressionTestSuiteChromosome ind = null;
		Chromosome individual = algorithm.getBestIndividual();
		if (individual instanceof RegressionTestChromosome) {
			ind = new RegressionTestSuiteChromosome();
			ind.addTest((RegressionTestChromosome) individual);
			ind.fitnessData = ((RegressionTestChromosome) individual).fitnessData;
			ind.objDistance = ((RegressionTestChromosome) individual).objDistance;
			ind.diffExceptions = ((RegressionTestChromosome) individual).diffExceptions;
		} else {
			ind = (RegressionTestSuiteChromosome) individual;
			ind.fitnessData = ((RegressionTestSuiteChromosome) individual).fitnessData;
			ind.objDistance = ((RegressionTestSuiteChromosome) individual).objDistance;
			ind.diffExceptions = ((RegressionTestSuiteChromosome) individual).diffExceptions;
			// assert false;
		}
		// hack for getting the correct number of different exceptions
		ind.fitnessData = ind.fitnessData.replace("numDifferentExceptions", ""
				+ exceptionDiff);

		int totalCount = 0;
		if (iteration > 0) {
			totalCount = RegressionAssertionCounter.getNumAssertions(ind);
			// totalCount += ind.diffExceptions;
			if (firstAssertionCount == 0 && totalCount > 0) {
				logger.warn("Successful GA! First Gen: " + firstAssertionCount
						+ " | Last Gen: " + totalCount);
			} else {
				logger.warn("Failed GA! First Gen: " + firstAssertionCount
						+ " | Last Gen: " + totalCount);
			}
		}

		if (Properties.REGRESSION_ANALYZE) {
			String analyzeDirName = "evosuiter-analyze";
			File analyzeDir = new File(analyzeDirName);
			int filecount = 0;
			if (analyzeDir.exists() && analyzeDir.isDirectory()) {
				filecount = analyzeDir.list().length;
			} else {
				analyzeDir.mkdirs();
			}
			File analysisFile = new File(analyzeDirName + "/analysis.txt");
			String report = "Class: " + Properties.getTargetClass().getName()
					+ " | " + "gens: " + iteration + " | assertions: "
					+ totalCount + " | " + analysisReport + " | " + jdiffReport;
			logger.warn("Analysis report: " + report);
			try {
				FileUtils.writeStringToFile(analysisFile, report + "\n", true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	public void fitnessEvaluation(Chromosome individual) {
		if (isFirstRun) {
			isFirstRun = false;
			int totalCount = RegressionAssertionCounter
					.getNumAssertions(individual);
			RegressionSearchListener.firstAssertionCount = totalCount;
		}
	}

	@Override
	public void modification(Chromosome individual) {

	}
}
