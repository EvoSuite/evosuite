/*
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

package org.evosuite.testcase;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.evosuite.Properties;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.junit.JUnitTestReader;
import org.evosuite.setup.TestCluster;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * JUnitTestChromosomeFactory class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class JUnitTestParsedChromosomeFactory implements ChromosomeFactory<TestChromosome> {

	private static final long serialVersionUID = 2760642997019090252L;

	private static final Logger logger = LoggerFactory.getLogger(JUnitTestParsedChromosomeFactory.class);

	private final static Set<TestCase> userTests = new LinkedHashSet<TestCase>();

	private final ChromosomeFactory<TestChromosome> defaultFactory;

	/**
	 * Attempt to read the test case
	 * 
	 * @param defaultFactory
	 *            a {@link org.evosuite.ga.ChromosomeFactory} object.
	 */
	public JUnitTestParsedChromosomeFactory(ChromosomeFactory<TestChromosome> defaultFactory) {
		this.defaultFactory = defaultFactory;
		if (userTests.isEmpty())
			userTests.addAll(filter(readTestCases()));
		LoggingUtils.getEvoLogger().info("* Found " + userTests.size()
		                                         + " relevant tests");
		// getManualCoverage();

	}

	/**
	 * <p>
	 * getNumTests
	 * </p>
	 * 
	 * @return a int.
	 */
	public static int getNumTests() {
		return userTests.size();
	}

	private Set<TestCase> filter(Set<TestCase> tests) {
		Set<TestCase> relevantTests = new HashSet<TestCase>();
		for (TestCase test : tests) {
			for (Class<?> clazz : test.getAccessedClasses()) {
				if (TestCluster.isTargetClassName(clazz.getName())) {
					relevantTests.add(test);
					logger.info("TestCase: " + test.toCode());
					break;
				}
			}
		}
		return relevantTests;
	}

	private Set<TestCase> readTestCases() {
		logger.info("Loading sequences from file");

		Set<TestCase> tests = new HashSet<TestCase>();
		for (String testFile : getTestFiles(Properties.TARGET_CLASS)) {
			logger.info("Trying to read test file " + testFile);
			tests.addAll(readTestCase(testFile));
		}

		LoggingUtils.getEvoLogger().info("* Parsed " + tests.size() + " JUnit test cases");
		return tests;
	}

	private Set<TestCase> readTestCase(String fileName) {
		JUnitTestReader parser = new JUnitTestReader();
		Set<TestCase> tests = new HashSet<TestCase>();
		System.out.print("* Parsing tests from " + fileName + ": ");
		tests.addAll(parser.readTests(fileName).values());
		System.out.println(". Parsed " + tests.size() + " test cases");
		return tests;

	}

	/**
	 * <p>
	 * getTestFiles
	 * </p>
	 * 
	 * @param fullClassName
	 *            a {@link java.lang.String} object.
	 * @return a {@link java.util.Set} object.
	 */
	public Set<String> getTestFiles(String fullClassName) {
		String shortClassName = Properties.getTargetClass().getSimpleName();
		Pattern pattern1 = Pattern.compile(".*Test.*");
		Pattern pattern2 = Pattern.compile(".*Test.java");
		if (Properties.JUNIT_STRICT) {
			pattern1 = Pattern.compile(".*Test" + shortClassName + ".*");
			pattern2 = Pattern.compile(".*" + shortClassName + "Test.java");
		}
		//Pattern pattern1 = Pattern.compile(Properties.CLASS_PREFIX + ".Test*"
		//        + shortClassName + ".*");
		//Pattern pattern2 = Pattern.compile(Properties.CLASS_PREFIX + "." + shortClassName
		//        + "Test");
		//Pattern pattern1 = Pattern.compile(Properties.CLASS_PREFIX + ".Test*" + ".*");
		//Pattern pattern2 = Pattern.compile(Properties.CLASS_PREFIX + ".*" + "Test");

		Set<File> files = initFiles();
		Set<String> testFiles = new HashSet<String>();
		logger.info("Looking for tests of class " + fullClassName + " in " + files.size()
		        + " files.");
		//System.out.println("Files: " + files);
		for (File f : files) {
			String name = getContainingClassName(f);
			//if (name.endsWith(className) && !name.endsWith("Test" + className)) {
			//	testFiles.add(f.getAbsolutePath());
			//}
			if (pattern1.matcher(name).matches()) {
				logger.info("Found match: " + f.getName());
				testFiles.add(f.getAbsolutePath());
			} else if (pattern2.matcher(name).matches()) {
				logger.info("Found match: " + f.getName());
				testFiles.add(f.getAbsolutePath());
			} else {
				logger.debug("No match: " + f.getName());
			}
		}
		return testFiles;
	}

	/**
	 * <p>
	 * getContainingClassName
	 * </p>
	 * 
	 * @param f
	 *            a {@link java.io.File} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getContainingClassName(File f) {
		String name = f.getAbsolutePath();
		String sep = System.getProperty("file.separator");
		name = name.replace(sep, ".");
		if (name.endsWith(".java")) {
			name = name.substring(0, name.length() - 5);
		}
		int i = name.lastIndexOf(Properties.PROJECT_PREFIX);
		if (i < 0) {
			name = "";
		} else {
			name = name.substring(i);
		}
		return name;
	}

	private Set<File> initFiles() {
		File startDirectory = new File(".");
		String[] extensions = { "java" };
		Collection<File> javaFiles = FileUtils.listFiles(startDirectory, extensions, true);
		return new HashSet<File>(javaFiles);
	}

	@SuppressWarnings("unused")
	private void getManualCoverage() {
		BranchCoverageSuiteFitness fitness = new BranchCoverageSuiteFitness();
		TestSuiteChromosome chromosome = new TestSuiteChromosome(
		        new RandomLengthTestFactory());
		int totalLength = 0;
		for (TestCase test : userTests) {
			chromosome.addTest(test);
			totalLength += test.size();
		}
		fitness.getFitness(chromosome);
		System.out.println("PARSED," + Properties.TARGET_CLASS + "," + userTests.size()
		        + "," + totalLength + "," + chromosome.getCoverage());
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.ChromosomeFactory#getChromosome()
	 */
	/** {@inheritDoc} */
	@Override
	public TestChromosome getChromosome() {
		int N_mutations = Properties.SEED_MUTATIONS;
		double P_clone = Properties.SEED_CLONE;

		if (Randomness.nextDouble() >= P_clone || userTests.isEmpty()) {
			logger.info("Using random test");
			return defaultFactory.getChromosome();
		}

		// Cloning
		logger.debug("Cloning user test");
		TestCase test = Randomness.choice(userTests);
		TestChromosome chromosome = new TestChromosome();
		chromosome.setTestCase(test.clone());
		if (N_mutations > 0) {
			int numMutations = Randomness.nextInt(N_mutations);
			logger.debug("Mutations: " + numMutations);

			// Delta
			for (int i = 0; i < numMutations; i++) {
				chromosome.mutate();
			}
		}

		return chromosome;
	}

}
