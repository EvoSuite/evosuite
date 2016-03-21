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
package org.evosuite.regression;

import java.lang.reflect.Modifier;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.evosuite.PackageInfo;
import org.evosuite.Properties;
import org.evosuite.assertion.Assertion;
import org.evosuite.ga.Chromosome;
import org.evosuite.junit.JUnitAnalyzer;
import org.evosuite.runtime.mock.EvoSuiteMock;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.brics.automaton.RegExp;

/*
 * Assertion generator for regression testing.
 * 
 * [Experimental]
 */
public class RegressionAssertionCounter {
	protected static final Logger logger = LoggerFactory
			.getLogger(RegressionAssertionCounter.class);
	
	private static List<List<String>> assertionComments = new ArrayList<List<String>>();

	/*
	 * Gets and removes the number of assertions for the individual
	 */
	public static int getNumAssertions(Chromosome individual) {
		assertionComments.clear();
		int numAssertions = getNumAssertions(individual, true);
		int oldNumAssertions = numAssertions;

		if (numAssertions > 0) {
			logger.debug("num assertions bigger than 0");
			RegressionTestSuiteChromosome clone = new RegressionTestSuiteChromosome();

			List<TestCase> testCases = new ArrayList<TestCase>();

			if (individual instanceof RegressionTestChromosome) {
				// clone.addTest((RegressionTestChromosome)individual);
				testCases.add(((RegressionTestChromosome) individual)
						.getTheTest().getTestCase());

			} else {
				RegressionTestSuiteChromosome ind = (RegressionTestSuiteChromosome) individual;
				testCases.addAll(ind.getTests());
			}
			logger.debug("tests are copied");
			// List<TestCase> testCases = clone.getTests();
			numAssertions = 0;

			JUnitAnalyzer.removeTestsThatDoNotCompile(testCases);
			logger.debug("... removeTestsThatDoNotCompile()");
			
			int numUnstable = JUnitAnalyzer.handleTestsThatAreUnstable(testCases);				
			logger.debug("... handleTestsThatAreUnstable() = {}", numUnstable);
			
			if (testCases.size() > 0) {
				logger.debug("{} out of {} tests remaining!", testCases.size(), ((RegressionTestSuiteChromosome)individual).getTests().size());
				clone = new RegressionTestSuiteChromosome();

				for (TestCase t : testCases) {
					// logger.warn("adding cloned test ...");
					if(t.isUnstable()){
						logger.debug("skipping unstable test...");
						continue;
					}
					RegressionTestChromosome rtc = new RegressionTestChromosome();
					TestChromosome tc = new TestChromosome();
					tc.setTestCase(t);
					rtc.setTest(tc);
					clone.addTest(rtc);
				}

				logger.debug("getting new num assertions ...");
				List<List<String>> oldAssertionComments = new ArrayList<List<String>>(assertionComments);
				assertionComments.clear();
				numAssertions = getNumAssertions(clone, false);
				if(oldAssertionComments.size()!=assertionComments.size()){
					numAssertions=0;
					logger.error("Assertion test size mismatch: {} VS {}", oldAssertionComments.size(), assertionComments.size());
				}else
					for(int i=0; i<oldAssertionComments.size();i++){
						List<String> testAssertionCommentsOld = oldAssertionComments.get(i);
						List<String> testAssertionCommentsNew = assertionComments.get(i);
						
						if(testAssertionCommentsNew.size()!= testAssertionCommentsOld.size()){
							numAssertions=0;
							logger.error("Assertion comment size mismatch: {} VS {}", testAssertionCommentsNew.size(), testAssertionCommentsOld.size());
							break;
						}
						
						for(int j=0; j<testAssertionCommentsOld.size(); j++){
							if(!testAssertionCommentsOld.get(j).equals(testAssertionCommentsNew.get(j))){
								numAssertions=0;
								logger.error("Assertion comment mismatch: [{}] VS [{}]", testAssertionCommentsOld.get(j), testAssertionCommentsNew.get(j));
								break;
							}
						}
					}
				
				logger.debug("Keeping {} assertions.", numAssertions);

			} else {
				logger.debug("ignored assertions. tests were removed.");
			}
			
		}

		return numAssertions;

	}

	public static int getNumAssertions(Chromosome individual,
			Boolean removeAssertions) {
		return getNumAssertions(individual, removeAssertions, false);

	}

	public static int getNumAssertions(Chromosome individual,
			Boolean removeAssertions, Boolean noExecution) {
		long startTime = System.nanoTime();
		RegressionAssertionGenerator rgen = new RegressionAssertionGenerator();
		
		//(Hack) temporarily changing timeout to allow the assertions to run
		int oldTimeout = Properties.TIMEOUT;
		Properties.TIMEOUT *= 2;
		int totalCount = 0;
		RegressionSearchListener.exceptionDiff = 0;

		boolean timedOut = false;

		logger.debug("Running assertion generator...");

		// RegressionTestSuiteChromosome ind = null;

		RegressionSearchListener.previousTestSuite = new ArrayList<TestCase>();
		RegressionSearchListener.previousTestSuite
				.addAll(RegressionSearchListener.currentTestSuite);
		RegressionSearchListener.currentTestSuite.clear();
		if (individual instanceof RegressionTestChromosome) {
			totalCount += checkForAssertions(removeAssertions, noExecution,
					rgen, (RegressionTestChromosome) individual);
		} else {
			// assert false;
			RegressionTestSuiteChromosome ind = (RegressionTestSuiteChromosome) individual;
			for (TestChromosome regressionTest : ind
					.getTestChromosomes()) {
				
				RegressionTestChromosome rtc = (RegressionTestChromosome) regressionTest;

				totalCount += checkForAssertions(removeAssertions, noExecution, rgen, rtc);
			}
		}

		// if(totalCount>0)
		Properties.TIMEOUT = oldTimeout;
		if(totalCount>0)
			logger.warn("Assertions generated for the individual: " + totalCount);
		RegressionSearchListener.assertionTime += System.nanoTime() - startTime;
		return totalCount;
	}

	// public static boolean enable_a = false;

	private static int checkForAssertions(Boolean removeAssertions, Boolean noExecution,
			RegressionAssertionGenerator rgen, RegressionTestChromosome regressionTest) {
		long execStartTime = 0;
		long execEndTime = 0;
		int totalCount = 0;

		boolean timedOut;
		if (!noExecution) {
			execStartTime = System.currentTimeMillis();

			ExecutionResult result1 = rgen.runTest(regressionTest.getTheTest().getTestCase());
			// enable_a=true;
			// logger.warn("Fitness is: {}", regressionTest.getFitness());
			// rgen = new RegressionAssertionGenerator();
			ExecutionResult result2 = rgen.runTest(regressionTest.getTheSameTestForTheOtherClassLoader().getTestCase());
			// enable_a = false;
			execEndTime = System.currentTimeMillis();
			/*
			 * if((execEndTime-execStartTime)>1500) assert false;
			 */

			if (result1.test == null || result2.test == null || result1.hasTimeout() || result2.hasTimeout()) {

				logger.warn("================================== HAD TIMEOUT ==================================");
				timedOut = true;
				// assert false;
			} else {

				// logger.warn("getting exdiff..");
				double exDiff = compareExceptionDiffs(result1.getCopyOfExceptionMapping(),
						result2.getCopyOfExceptionMapping());

				if (exDiff > 0) {
					logger.debug("Had {} different exceptions! ({})", exDiff, totalCount);
					/*
					 * logger.warn("mapping1: {} | mapping 2: {}",
					 * result1.getCopyOfExceptionMapping(),
					 * result2.getCopyOfExceptionMapping());
					 */
				}

				totalCount += exDiff;
				RegressionSearchListener.exceptionDiff += exDiff;


				for (Class<?> observerClass : RegressionAssertionGenerator.observerClasses) {
					if (result1.getTrace(observerClass) != null) {
						result1.getTrace(observerClass).getAssertions(regressionTest.getTheTest().getTestCase(),
								result2.getTrace(observerClass));
					}

				}

			}

		}
		int assertionCount = regressionTest.getTheTest().getTestCase().getAssertions().size();
		totalCount += assertionCount;

		if (assertionCount > 0) {
			List<Assertion> asses = regressionTest.getTheTest().getTestCase().getAssertions();
			List<String> assComments = new ArrayList<String>();
			for (Assertion ass : asses) {
				logger.warn("+++++ Assertion: {} {}", ass.getCode(), ass.getComment());
				assComments.add(ass.getComment());
			}
			RegressionAssertionCounter.assertionComments.add(assComments);

			if (asses.size() == 0)
				logger.warn("=========> NO ASSERTIONS!!!");
			else
				logger.warn("Assertions ^^^^^^^^^");
		}

		RegressionSearchListener.currentTestSuite.add(regressionTest.getTheTest().getTestCase().clone());

		if (removeAssertions)
			regressionTest.getTheTest().getTestCase().removeAssertions();
		return totalCount;
	}

	/** Calculate the number of different exceptions.
	 * 
	 * @param originalExceptionMapping
	 * @param regressionExceptionMapping
	 * @return
	 */
	public static double compareExceptionDiffs(Map<Integer, Throwable> originalExceptionMapping,
			Map<Integer, Throwable> regressionExceptionMapping) {

		double exDiff = Math.abs((double) (originalExceptionMapping.size() - regressionExceptionMapping.size()));

		
		
		
		/*
		 * If the number of exceptions is different, clearly the executions are
		 * propagating to different results. Otherwise, we need to compare the
		 * same assertions to make sure they're actually the same.
		 */

		if (exDiff == 0) {
			// For all exceptions thrown original class
			for (Entry<Integer, Throwable> origException : originalExceptionMapping.entrySet()) {
				boolean skip = false;

				// Skip if the exception or the message are null
				// Sometimes the getMesage may call the CUT's exception handler which may crash
				try{
					if (origException.getValue() == null || origException.getValue().getMessage() == null) {
						originalExceptionMapping.remove(origException.getKey());
						skip = true;
					}
				} catch (Throwable t){
					continue;
				}
				
				// See if exception throwing classes differed
				try{
					Throwable x = origException.getValue();
					Class<?> ex = getExceptionClassToUse(x);
					String sourceClass = RegressionAssertionCounter.getSourceClassName(x);
					if (sourceClass != null && isValidSource(sourceClass) && isExceptionToAssertThrownBy(ex)) {
						// Get other exception throwing class and compare them
						Throwable otherX = regressionExceptionMapping.get(origException.getKey());
						String otherSourceClass = RegressionAssertionCounter.getSourceClassName(otherX);
						if (sourceClass != otherSourceClass) {
							exDiff++;
							//logger.warn("Exception throwing classes differed: {} {}", sourceClass, otherSourceClass);
						}

					}
				} catch (Throwable t){ 
					// ignore
				}
				
				// Skip if the exceptions are not comparable
				try{
					if (regressionExceptionMapping.containsKey(origException.getKey())
							&& (regressionExceptionMapping.get(origException.getKey()) == null
									|| regressionExceptionMapping.get(origException.getKey()).getMessage() == null)) {
						regressionExceptionMapping.remove(origException.getKey());
						skip = true;
					}
				} catch (Throwable t){
					continue;
				}

				// If they start differing from @objectID skip this one
				if (!skip && regressionExceptionMapping.get(origException.getKey()) != null) {
					String origExceptionMessage = origException.getValue().getMessage();
					String regExceptionMessage = regressionExceptionMapping.get(origException.getKey()).getMessage();
					int diffIndex = StringUtils.indexOfDifference(origExceptionMessage, regExceptionMessage);
					if (diffIndex > 0) {
						if (origExceptionMessage.charAt(diffIndex - 1) == '@') {
							originalExceptionMapping.remove(origException.getKey());
							regressionExceptionMapping.remove(origException.getKey());
							skip = true;
						} else {
							// If @ is in the last 10 characters, it's likely an object pointer comparison issue
							int howFarBack = 10;
							if (diffIndex > howFarBack) {
								String last10 = origExceptionMessage.substring(diffIndex - howFarBack, diffIndex);
								if (last10.contains("@")) {
									originalExceptionMapping.remove(origException.getKey());
									regressionExceptionMapping.remove(origException.getKey());
									skip = true;
								}
							}
						}
						
					}
				}

				// ignore security manager exceptions
				if (!skip && origException.getValue().getMessage().contains("Security manager blocks")) {
					originalExceptionMapping.remove(origException.getKey());
					regressionExceptionMapping.remove(origException.getKey());
					skip = true;
				}

				if (skip)
					continue;

				// do the comparison
				if (!regressionExceptionMapping.containsKey(origException.getKey()) || (!regressionExceptionMapping
						.get(origException.getKey()).getMessage().equals(origException.getValue().getMessage()))) {
					exDiff++;
				}
			}
			// For all exceptions in the regression class.
			// Any bad exceptions were removed from this object earlier
			for (Entry<Integer, Throwable> regException : regressionExceptionMapping.entrySet()) {
				if (!originalExceptionMapping.containsKey(regException.getKey()))
					exDiff++;
			}
		}

		return exDiff;
	}

	/*
	 * Add regression-diff comments for exception messages 
	 */
	public static void addExceptionAssertionComments(RegressionTestChromosome regressionTest,
			Map<Integer, Throwable> originalExceptionMapping, Map<Integer, Throwable> regressionExceptionMapping) {
		for (Entry<Integer, Throwable> origException : originalExceptionMapping.entrySet()) {
			if (!regressionExceptionMapping.containsKey(origException.getKey())) {

				if (regressionTest.getTheTest().getTestCase().hasStatement(origException.getKey())
						&& !regressionTest.getTheTest().getTestCase().getStatement(origException.getKey()).getComment()
								.contains("modified version")) {
					regressionTest.getTheTest().getTestCase().getStatement(origException.getKey())
							.addComment("EXCEPTION DIFF:\nThe modified version did not exhibit this exception:\n    "
									+ origException.getValue().getClass().getName() + " : "
									+ origException.getValue().getMessage() + "\n");
					// regressionTest.getTheSameTestForTheOtherClassLoader().getTestCase().getStatement(origException.getKey()).addComment("EXCEPTION
					// DIFF:\nThe modified version did not exhibit this
					// exception:\n "
					// + origException.getValue().getMessage() + "\n");
				}
			} else {
				if (origException != null && origException.getValue() != null
						&& origException.getValue().getMessage() != null){
					// compare the exception messages
					if (!origException.getValue().getMessage()
							.equals(regressionExceptionMapping.get(origException.getKey()).getMessage())) {
						if (regressionTest.getTheTest().getTestCase().hasStatement(origException.getKey())
								&& !regressionTest.getTheTest().getTestCase().getStatement(origException.getKey())
										.getComment().contains("EXCEPTION DIFF:"))
							regressionTest.getTheTest().getTestCase().getStatement(origException.getKey()).addComment(
									"EXCEPTION DIFF:\nDifferent Exceptions were thrown:\nOriginal Version:\n    "
											+ origException.getValue().getClass().getName() + " : "
											+ origException.getValue().getMessage() + "\nModified Version:\n    "
											+ regressionExceptionMapping.get(origException.getKey()).getClass()
													.getName()
											+ " : "
											+ regressionExceptionMapping.get(origException.getKey()).getMessage()
											+ "\n");
					} else {
						// Compare the classes throwing the exception
						Throwable x = origException.getValue();
						Class<?> ex = getExceptionClassToUse(x);
						String sourceClass = RegressionAssertionCounter.getSourceClassName(x);
						if (sourceClass != null && isValidSource(sourceClass) && isExceptionToAssertThrownBy(ex)
								&& regressionExceptionMapping.get(origException.getKey())!=null) {
							Throwable otherX = regressionExceptionMapping.get(origException.getKey());
							String otherSourceClass = RegressionAssertionCounter.getSourceClassName(otherX);
							if (sourceClass != otherSourceClass) {
								if (regressionTest.getTheTest().getTestCase().hasStatement(origException.getKey())
										&& !regressionTest.getTheTest().getTestCase()
												.getStatement(origException.getKey()).getComment()
												.contains("EXCEPTION DIFF:"))
									regressionTest.getTheTest().getTestCase().getStatement(origException.getKey())
											.addComment(
													"EXCEPTION DIFF:\nExceptions thrown by different classes:\nOriginal Version:\n    "
															+ sourceClass + "\nModified Version:\n    "
															+ otherSourceClass + "\n");

							}

						}
					}
				}

				// If both show the same error, pop the error from the
				// regression exception map, to get to a diff.
				regressionExceptionMapping.remove(origException.getKey());
			}
		}
		for (Entry<Integer, Throwable> regException : regressionExceptionMapping.entrySet()) {
			if (regressionTest.getTheTest().getTestCase().hasStatement(regException.getKey())
					&& !regressionTest.getTheTest().getTestCase().getStatement(regException.getKey()).getComment()
							.contains("original version")) {
				/*
				 * logger.warn(
				 * "Regression Test with exception \"{}\" was: \n{}\n---------\nException:\n{}"
				 * , regException.getValue().getMessage(),
				 * regressionTest.getTheTest().getTestCase(),
				 * regException.getValue().toString());
				 */
				regressionTest.getTheTest().getTestCase().getStatement(regException.getKey())
						.addComment("EXCEPTION DIFF:\nThe original version did not exhibit this exception:\n    "
								+ regException.getValue().getClass().getName() + " : "
								+ regException.getValue().getMessage() + "\n\n");
				regressionTest.getTheSameTestForTheOtherClassLoader().getTestCase().getStatement(regException.getKey())
						.addComment("EXCEPTION DIFF:\nThe original version did not exhibit this exception:\n    "
								+ regException.getValue().getClass().getName() + " : "
								+ regException.getValue().getMessage() + "\n\n");
			}
		}
	}
	
	/*
	 * This part is "temporarily" copied over from TestCodeVisitor. 
	 * Until they are made statically available to use in this class.
	 */
	private static String getSourceClassName(Throwable exception){
		if(exception.getStackTrace().length == 0){
			return null;
		}
		return exception.getStackTrace()[0].getClassName();
	}

	private static boolean isValidSource(String sourceClass){
		return (! sourceClass.startsWith(PackageInfo.getEvoSuitePackage()+".") ||
				sourceClass.startsWith(PackageInfo.getEvoSuitePackage()+".runtime.")) &&
				!sourceClass.equals(URLClassLoader.class.getName()) && // Classloaders may differ, e.g. when running with ant
                !sourceClass.startsWith(RegExp.class.getPackage().getName()) &&
                !sourceClass.startsWith("java.lang.System") &&
                !sourceClass.startsWith("java.lang.String") &&
                !sourceClass.startsWith("sun.") &&
                !sourceClass.startsWith("com.sun.") &&
                !sourceClass.startsWith("jdk.internal.");
	}

	private static List<Class<?>> invalidExceptions = Arrays.asList(new Class<?>[] {
			StackOverflowError.class, // Might be thrown at different places
			AssertionError.class}     // Depends whether assertions are enabled or not
	);

	private static boolean isExceptionToAssertThrownBy(Class<?> exceptionClass){
		return !invalidExceptions.contains(exceptionClass);
	}

	private static Class<?> getExceptionClassToUse(Throwable exception){
        /*
            we can only catch a public class.
            for "readability" of tests, it shouldn't be a mock one either
          */
        Class<?> ex = exception.getClass();
        while (!Modifier.isPublic(ex.getModifiers()) || EvoSuiteMock.class.isAssignableFrom(ex) ||
				ex.getCanonicalName().startsWith("com.sun.")) {
            ex = ex.getSuperclass();
        }
        return ex;
    }

}
