/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

	private final File testCache = new File(Properties.OUTPUT_DIR + "/UserTests.xml");

	/**
	 * Attempt to read the test case
	 * 
	 * @param className
	 */
	public JUnitTestChromosomeFactory(ChromosomeFactory<TestChromosome> defaultFactory) {
		this.defaultFactory = defaultFactory;
		userTests.addAll(filter(readTestCases()));
		System.out.println("* Found " + userTests.size() + " relevant tests");
	}

	private Set<TestCase> filter(Set<TestCase> tests) {
		Set<TestCase> relevantTests = new HashSet<TestCase>();
		for (TestCase test : tests) {
			for (Class<?> clazz : test.getAccessedClasses()) {
				if (clazz.getName().equals(Properties.TARGET_CLASS)) {
					relevantTests.add(test);
					logger.info("TestCase: " + test.toCode());
					break;
				}
			}
			/*
			if (test.getAccessedClasses().contains(Properties.getTargetClass())) {
				logger.info("Test accesses target class");
				relevantTests.add(test);
			}
			*/
		}
		return relevantTests;
	}

	private void writeTestCasesToXML(Set<TestCase> tests) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
			        testCache));
			out.writeObject(tests);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Set<TestCase> readTestCasesFromXML() {
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(testCache));
			Set<TestCase> tests = (Set<TestCase>) in.readObject();
			//logger.warn(tests.toString());
			return tests;
		} catch (FileNotFoundException e) {
			logger.warn(e.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.warn(e.toString());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			logger.warn(e.toString());
			e.printStackTrace();
		}
		return null;
	}

	private Set<TestCase> readTestCases() {
		logger.info("Loading sequences from file");

		Set<TestCase> tests = new HashSet<TestCase>();
		if (testCache.exists()) {
			tests.addAll(readTestCasesFromXML());
			System.out.println("* Deserialized " + tests.size() + " JUnit test cases");
		} else {

			for (String testFile : getTestFiles(Properties.TARGET_CLASS)) {
				logger.info("Trying to read test file " + testFile);
				tests.addAll(readTestCase(testFile));
			}

			System.out.println("* Parsed " + tests.size() + " JUnit test cases");
			//writeTestCasesToXML(tests);
		}
		//getManualCoverage();

		return tests;
	}

	private Set<TestCase> readTestCase(String fileName) {
		//JUnitTestReader reader = new ComplexJUnitTestReader(null,
		//        new String[] { fileName }); // TODO
		//reader.readJUnitTestCase(SimpleTestExample01.class.getName() + "#test");

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
		//Pattern pattern1 = Pattern.compile(Properties.CLASS_PREFIX + ".Test*"
		//        + shortClassName + ".*");
		//Pattern pattern2 = Pattern.compile(Properties.CLASS_PREFIX + "." + shortClassName
		//        + "Test");
		//Pattern pattern1 = Pattern.compile(Properties.CLASS_PREFIX + ".Test*" + ".*");
		//Pattern pattern2 = Pattern.compile(Properties.CLASS_PREFIX + ".*" + "Test");
		Pattern pattern1 = Pattern.compile(".*Test.*");
		Pattern pattern2 = Pattern.compile(".*Test.java");

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
		int N_mutations = Properties.SEED_MUTATIONS;
		double P_clone = Properties.SEED_CLONE;

		if (Randomness.nextDouble() >= P_clone || userTests.isEmpty()) {
			logger.info("Using random test");
			return defaultFactory.getChromosome();
		}

		// Cloning
		logger.info("Cloning user test");
		TestChromosome chromosome = new TestChromosome();
		chromosome.setTestCase(Randomness.choice(userTests).clone());
		if (N_mutations > 0) {
			int numMutations = Randomness.nextInt(N_mutations);
			logger.info("Mutations: " + numMutations);

			// Delta
			for (int i = 0; i < numMutations; i++) {
				chromosome.mutate();
			}
		}

		return chromosome;
	}

}
