/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;
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
import de.unisb.cs.st.evosuite.utils.Randomness;

/**
 * @author Gordon Fraser
 * 
 */
public class JUnitTestChromosomeFactory implements ChromosomeFactory<TestChromosome> {

	private static final long serialVersionUID = 2760642997019090252L;

	private static Logger logger = LoggerFactory.getLogger(JUnitTestChromosomeFactory.class);

	private final Set<TestCase> userTests = new LinkedHashSet<TestCase>();

	private final ChromosomeFactory<TestChromosome> defaultFactory;

	/**
	 * Attempt to read the test case
	 * 
	 * @param className
	 */
	public JUnitTestChromosomeFactory(ChromosomeFactory<TestChromosome> defaultFactory) {
		this.defaultFactory = defaultFactory;
		readTestCases();
	}

	private void readTestCases() {
		logger.info("Loading sequences from file");
		/*
		String SUTName = Properties.TARGET_CLASS_PREFIX.replace(".", "/");
		String className = Properties.getTargetClass().getSimpleName();
		Pattern pattern = Pattern.compile(SUTName + ".*" + className + ".*.java");
		for (String resource : ResourceList.getResources(pattern)) {
			logger.info("Trying to read test file " + resource);
			readTestCase(resource);
		}
		*/
		for (String testFile : getTestFiles(Properties.TARGET_CLASS)) {
			logger.info("Trying to read test file " + testFile);
			readTestCase(testFile);
		}
		logger.info("In total, we parsed " + userTests.size() + " test cases");
		getManualCoverage();
	}

	private void readTestCase(String fileName) {
		//JUnitTestReader reader = new ComplexJUnitTestReader(null,
		//        new String[] { fileName }); // TODO
		//reader.readJUnitTestCase(SimpleTestExample01.class.getName() + "#test");

		TestParser parser = new TestParser(null);
		List<TestCase> tests;
		try {
			tests = new ArrayList<TestCase>(); //parser.parseFile(fileName);
			logger.info("Parsed " + tests.size() + " test cases");
			for (TestCase test : tests) {
				if (test.getAccessedClasses().contains(Properties.getTargetClass())) {
					logger.info("Test accesses target class");
					userTests.add(test);
				} else {
					logger.info("Test does not access target class");
				}
			}
			// userTests.addAll(tests);
		} catch (Exception e) { //(IOException e) {
			logger.info("Error parsing file " + fileName + ": " + e);
		}

	}

	public Set<String> getTestFiles(String fullClassName) {
		String shortClassName = Properties.getTargetClass().getSimpleName();
		//Pattern pattern1 = Pattern.compile(Properties.CLASS_PREFIX + ".Test*"
		//        + shortClassName + ".*");
		//Pattern pattern2 = Pattern.compile(Properties.CLASS_PREFIX + "." + shortClassName
		//        + "Test");
		Pattern pattern1 = Pattern.compile(Properties.CLASS_PREFIX + ".Test*" + ".*");
		Pattern pattern2 = Pattern.compile(Properties.CLASS_PREFIX + ".*" + "Test");

		Set<File> files = initFiles();
		Set<String> testFiles = new HashSet<String>();
		logger.info("Looking for tests of class " + fullClassName + " in " + files.size()
		        + " files.");
		logger.debug("Files: " + files);
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
				logger.info("No match: " + f.getName());
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

	private String getClassName(String name) {
		if (name.contains("$")) {
			name = name.substring(0, name.indexOf('$'));
		}
		return name;
	}

	private void getManualCoverage() {
		BranchCoverageSuiteFitness fitness = new BranchCoverageSuiteFitness();
		TestSuiteChromosome chromosome = new TestSuiteChromosome(
		        new RandomLengthTestFactory());
		for (TestCase test : userTests) {
			chromosome.addTest(test);
		}
		fitness.getFitness(chromosome);
		System.out.println("* Parsed tests cover " + chromosome.getCoverage()
		        + " of branches");
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.ChromosomeFactory#getChromosome()
	 */
	@Override
	public TestChromosome getChromosome() {
		double P_delta = 0.2d;
		double P_clone = 1.0d;
		int MAX_CHANGES = 10;

		if (Randomness.nextDouble() >= P_clone || userTests.isEmpty()) {
			logger.info("Using random test");
			return defaultFactory.getChromosome();
		}

		// Cloning
		logger.info("Cloning user test");
		TestChromosome chromosome = new TestChromosome();
		chromosome.setTestCase(Randomness.choice(userTests).clone());

		// Delta
		if (Randomness.nextDouble() < P_delta) {
			logger.info("Mutating user test");
			// TODO: Use decreasing probability like during insertion?
			int numChanges = Randomness.nextInt(1, MAX_CHANGES);
			for (int i = 0; i < numChanges; i++) {
				chromosome.mutate();
			}
		}

		return chromosome;
	}

}
