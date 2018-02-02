/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.testsuite;

import static org.evosuite.Properties.SEED_DIR;
import static org.evosuite.Properties.TARGET_CLASS;

import org.evosuite.Properties;
import org.evosuite.Properties.AssertionStrategy;
import org.evosuite.TestGenerationContext;
import org.evosuite.TestSuiteGeneratorHelper;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.ga.archive.Archive;
import org.evosuite.junit.JUnitAnalyzer;
import org.evosuite.junit.writer.TestSuiteWriter;
import org.evosuite.rmi.ClientServices;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.utils.LoggingUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RegressionSuiteSerializer {

  // base path for seeds directory
  private static final String BASE_PATH = SEED_DIR + File.separator + TARGET_CLASS;

  private static final String REGRESSION_FILE = BASE_PATH + ".regression";

  private static final String REGRESSION_ARCHIVE_FILE = BASE_PATH + ".regression_archive";

  private static final String JUNIT_ARCHIVE_SUFFIX = "_ESArchiveTest";

  public static void appendToRegressionTestSuite(TestSuiteChromosome testSuite) {

    // load previous regression test suite
    List<TestChromosome> previousSuite = TestSuiteSerialization.loadTests(REGRESSION_FILE);
    LoggingUtils.getEvoLogger().info("* previousSuite.size(): " + previousSuite.size());

    // execute previous regression test chromosome
    removeTestsThatDoNotcompile(previousSuite);

    // write some statistics, e.g., number of failing test cases
    ClientServices.getInstance().getClientNode()
        .trackOutputVariable(RuntimeVariable.NumRegressionTestCases, previousSuite.size());

    // join previous regression test suite with new test suite
    testSuite.addTests(previousSuite);

    // serialize
    TestSuiteSerialization.saveTests(testSuite, new File(REGRESSION_FILE));
  }

  private static void removeTestsThatDoNotcompile(List<TestChromosome> previousSuite) {
    // Store this value; if this option is true then the JUnit check
    // would not succeed, as the JUnit classloader wouldn't find the class
    boolean junitSeparateClassLoader = Properties.USE_SEPARATE_CLASSLOADER;
    Properties.USE_SEPARATE_CLASSLOADER = false;

    Iterator<TestChromosome> iterator = previousSuite.iterator();
    while (iterator.hasNext()) {
      TestCase tc = iterator.next().getTestCase();

      List<TestCase> testCases = new ArrayList<>();
      testCases.add(tc);

      JUnitAnalyzer.removeTestsThatDoNotCompile(testCases);
      if (testCases.isEmpty()) {
        // if TesCase 'tc' does not compile, just remove it
        iterator.remove();
      }
    }

    Properties.USE_SEPARATE_CLASSLOADER = junitSeparateClassLoader;
  }

  /**
   * Keep the coverage-based archive of the generated tests for disposable testing
   */
  public static void storeRegressionArchive() {
    TestSuiteChromosome testArchive = getAppendedRegressionSuiteArchive();

    // Save the actual unit test suite archive
    TestSuiteWriter suiteWriter = new TestSuiteWriter();
    suiteWriter.insertTests(testArchive.getTests());

    String name = TARGET_CLASS.substring(TARGET_CLASS.lastIndexOf(".") + 1);
    String testDir = Properties.TEST_DIR;

    LoggingUtils.getEvoLogger().info(
        "* Writing Archive JUnit test case '" + (name + JUNIT_ARCHIVE_SUFFIX) + "' to " + testDir);
    suiteWriter
        .writeTestSuite(name + JUNIT_ARCHIVE_SUFFIX, testDir,
            testArchive.getLastExecutionResults());

    // Serialise the test suite archive
    TestSuiteSerialization.saveTests(testArchive, new File(REGRESSION_ARCHIVE_FILE));
  }

  /**
   * Get (and append) a coverage-based test suite archive for regression testing
   */
  private static TestSuiteChromosome getAppendedRegressionSuiteArchive() {
    List<TestChromosome> previousArchive = TestSuiteSerialization
        .loadTests(REGRESSION_ARCHIVE_FILE);
    LoggingUtils.getEvoLogger().info("* previousArchive.size(): " + previousArchive.size());

    previousArchive.forEach(t -> t.getTestCase().removeAssertions());
    // execute previous regression test archive
    removeTestsThatDoNotcompile(previousArchive);

    Properties.TEST_ARCHIVE = false;

    TestSuiteChromosome archiveSuite = new TestSuiteChromosome();
    archiveSuite.addTests(previousArchive);

    BranchCoverageSuiteFitness branchCoverageSuiteFitness = new BranchCoverageSuiteFitness(
        TestGenerationContext.getInstance().getClassLoaderForSUT());

    // execute the test suite
    branchCoverageSuiteFitness.getFitness(archiveSuite);
    LoggingUtils.getEvoLogger()
        .info("* archive covered goals: " + archiveSuite.getCoveredGoals().size());

    Properties.TEST_ARCHIVE = true;

    TestSuiteChromosome testArchive = Archive.getArchiveInstance()
        .mergeArchiveAndSolution(archiveSuite);

    LoggingUtils.getEvoLogger().info("* newArchive.size(): " + testArchive.size());
    LoggingUtils.getEvoLogger()
        .info("* new covered goals: " + testArchive.getCoveredGoals().size());

    // add all assertions
    AssertionStrategy tmpStrategy = Properties.ASSERTION_STRATEGY;
    Properties.ASSERTION_STRATEGY = AssertionStrategy.ALL;
    TestSuiteGeneratorHelper.addAssertions(testArchive);
    Properties.ASSERTION_STRATEGY = tmpStrategy;

    return testArchive;
  }
}
