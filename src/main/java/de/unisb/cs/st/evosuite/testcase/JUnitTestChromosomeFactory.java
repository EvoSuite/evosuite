/**
 * Copyright (C) 2012 Gordon Fraser, Andrea Arcuri
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import de.unisb.cs.st.evosuite.ga.ChromosomeFactory;
import de.unisb.cs.st.evosuite.ma.parser.TestParser;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;
import de.unisb.cs.st.evosuite.utils.LoggingUtils;
import de.unisb.cs.st.evosuite.utils.Randomness;

/**
 * @author Gordon Fraser
 * 
 */
public class JUnitTestChromosomeFactory implements ChromosomeFactory<TestChromosome> {

	private static final long serialVersionUID = 2760642997019090252L;

	private static Logger logger = LoggerFactory.getLogger(JUnitTestChromosomeFactory.class);

	private final static Set<TestCase> userTests = new LinkedHashSet<TestCase>();

	private final ChromosomeFactory<TestChromosome> defaultFactory;

	/**
	 * Attempt to read the test case
	 * 
	 * @param className
	 */
	public JUnitTestChromosomeFactory(ChromosomeFactory<TestChromosome> defaultFactory) {
		this.defaultFactory = defaultFactory;
		if (userTests.isEmpty())
			userTests.addAll(filter(readTestCases()));
		LoggingUtils.getEvoLogger().info("* Found " + userTests.size() + " relevant tests");
		// getManualCoverage();

	}

	public static int getNumTests() {
		return userTests.size();
	}

	private Set<TestCase> filter(Set<TestCase> tests) {
		Set<TestCase> relevantTests = new HashSet<TestCase>();
		for (TestCase test : tests) {
			for (Class<?> clazz : test.getAccessedClasses()) {
				if (StaticTestCluster.isTargetClassName(clazz.getName())) {
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
		TestParser parser = new TestParser(null);
		Set<TestCase> tests = new HashSet<TestCase>();
		try {
			System.out.print("* Parsing tests from " + fileName + ": ");
			tests.addAll(parser.parseFile(fileName));
			System.out.println(". Parsed " + tests.size() + " test cases");
		} catch (IOException e) {
			logger.info("Error parsing file " + fileName + ": " + e);
		}
		return tests;

	}

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
	 * @see de.unisb.cs.st.evosuite.ga.ChromosomeFactory#getChromosome()
	 */
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
