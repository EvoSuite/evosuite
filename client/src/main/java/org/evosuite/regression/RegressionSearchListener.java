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
/**
 * 
 */
package org.evosuite.regression;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.SearchListener;
import org.evosuite.rmi.ClientServices;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sina
 * 
 * This class is mainly here for generating regression statistics for
 * the http://evosuiter.sina.sh platform.
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
	public static long diversityCalculationTime = 0;

	public static boolean killTheSearch = false;

	public static long startTime;

	protected static final Logger logger = LoggerFactory
			.getLogger(RegressionSearchListener.class);

	public static String jdiffReport = "";

	public static String analysisReport = "";

	public static int exceptionDiff = 0;

	public static File statsFile;
	public static BufferedWriter statsFileWriter;
	public static double lastOD = 0.0;
	public static int lastAssertions = -1;
	public static boolean lastIterationSuccessful = false;
	
	public static boolean skipWritingStats = false;
	
	public double lastFitnessObserved = Double.MAX_VALUE;

	/*
	 * Create a stats directory and/or file when the search starts
	 * 
	 * @see
	 * org.evosuite.ga.metaheuristics.SearchListener#searchStarted(org.evosuite
	 * .ga.metaheuristics.GeneticAlgorithm)
	 */
	@Override
    public void searchStarted(GeneticAlgorithm<?> algorithm) {
      Class<?> targetClass = Properties.getTargetClassAndDontInitialise();
  
      // Set an initial unique statsID
      statsID = ((Properties.RANDOM_SEED == null) ? "0_" : Properties.RANDOM_SEED + "_")
          + (System.currentTimeMillis()) + "" + "_" + targetClass.getSimpleName();
  
      // Create the statistics directory and file
      if (Properties.REGRESSION_STATISTICS) {
        String statsDirName = "evosuiter-stats";
        File statsDir = new File(statsDirName);
        int filecount = 0;
        if (statsDir.exists() && statsDir.isDirectory()) {
          filecount = statsDir.list().length;
        } else {
          statsDir.mkdirs();
        }
  
  
        // Format: $rand_seed_$file-count-in-current-dir_$CUT-name.csv
        statsFile = new File(statsDirName + "/"
            + ((Properties.RANDOM_SEED == null) ? "0_" : Properties.RANDOM_SEED + "_") + (filecount)
            + "" + "_" + targetClass.getSimpleName() + ".csv");
  
        // if file exists, append time!
        if (statsFile.exists())
          statsFile = new File(
              statsFile.getName().replace(".csv", "_" + System.currentTimeMillis() + ".csv"));
  
        // remove extension
        statsID = statsFile.getName().replaceFirst("[.][^.]+$", "");
  
  
        try {
          String data =
              "fitness,test_count,test_size,branch_distance,object_distance,coverage,exception_diff,total_exceptions,coverage_old,coverage_new,executed_statements,age,time,assertions,state,exec_time,assert_time,cover_time,state_diff_time,branch_time,obj_time";
          statsFileWriter = new BufferedWriter(new FileWriter(statsFile));
          // FileUtils.writeStringToFile(statsFile, data, false);
          statsFileWriter.write(data);
          statsFileWriter.flush();
        } catch (IOException e) {
          skipWritingStats = true;
          e.printStackTrace();
        } catch (Throwable t) {
          // something happened, we don't care :-)
          t.printStackTrace();
        }
      } else {
        skipWritingStats = true;
      }
      
      startTime = System.currentTimeMillis();
    }

	@Override
	public void iteration(GeneticAlgorithm<?> algorithm) {
		//logger.warn("iterating ...");
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
		
		boolean fitnessNotChanged = false;
		if(ind.getFitness() >= lastFitnessObserved){
			fitnessNotChanged = true;
			//logger.warn("Fitness not changed {} vs {}", ind.getFitness(), lastFitnessObserved);
		}
		lastFitnessObserved = ind.getFitness();

		if(lastIterationSuccessful && lastAssertions>0){
			fitnessNotChanged = true;
		}
		
		int curAssertions = fitnessNotChanged ? lastAssertions : RegressionAssertionCounter.getNumAssertions(ind);
		// curAssertions += ind.diffExceptions;

		if (curAssertions > 0) {
			individual.setFitness(algorithm.getFitnessFunction(), 0);
			algorithm.setStoppingConditionLimit(0);
			lastIterationSuccessful = true;
			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Generated_Assertions, curAssertions);
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
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		RegressionSearchListener.lastOD = curOD;
		lastAssertions = curAssertions;
	}
	
	public static String lastLine = ""; 
	
	public static void flushLastLine(int assertions, int testCount, int testSize){
		if (lastLine == "")
			return;
		
		if(skipWritingStats)
			return;
		
		lastLine = lastLine.replace("ASSERTIONS", "" + assertions);
		lastLine = lastLine.replaceFirst("^\r\n([\\d\\.]*),\\d*,\\d*", "\r\n$1," + testCount + "," + testSize);
		try {
			statsFileWriter.write(lastLine);
			statsFileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(Throwable t){
			// something happened, we don't care :-)
			t.printStackTrace();
		}
	}

	private void writeIterationLog(GeneticAlgorithm<?> algorithm,
			char runState, RegressionTestSuiteChromosome ind,
			int curAssertions, double curOD) throws IOException {
		
		if(skipWritingStats)
			return;
		
		lastLine = "\r\n"
				+ (ind).fitnessData
				+ ","
				+ ((isLastRun) ? (algorithm.getAge() + 1)
						: algorithm.getAge())
				+ ","
				+ (System.currentTimeMillis() - startTime)
				+ ","
				+ ((isLastRun && Properties.MINIMIZE)?"ASSERTIONS":curAssertions)
				+ ","
				+ runState
				+ ((isLastRun) ? ("," + (testExecutionTime + 1)
						/ 1000000 + "," + (assertionTime + 1)
						/ 1000000 + "," + (coverageTime + 1)
						/ 1000000 + ","
						+ (ObjectDistanceTime + 1) / 1000000
						+ "," + (branchDistanceTime + 1)
						/ 1000000 + "," + (odCollectionTime + 1) / 1000000)
						: ",,,,,,");
		
		if(!Properties.MINIMIZE || !isLastRun){
			statsFileWriter.write(lastLine);
			lastLine = "";
		}

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
		statsFileWriter.flush();
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
			if(totalCount>lastAssertions)
				lastAssertions = totalCount;
			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Generated_Assertions, totalCount);
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
			String report = "Class: " + Properties.TARGET_CLASS
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
		
		try {
			if(!Properties.MINIMIZE && !skipWritingStats)
				statsFileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
