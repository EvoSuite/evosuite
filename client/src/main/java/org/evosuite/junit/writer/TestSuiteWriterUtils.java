package org.evosuite.junit.writer;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.evosuite.Properties;
import org.evosuite.Properties.AssertionStrategy;
import org.evosuite.Properties.OutputFormat;
import org.evosuite.junit.JUnit3TestAdapter;
import org.evosuite.junit.JUnit4TestAdapter;
import org.evosuite.junit.UnitTestAdapter;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Set of utility functions for generation of JUnit files
 * @author arcuri
 *
 */
public class TestSuiteWriterUtils {

	public static final String METHOD_SPACE = "  ";
	public static final String BLOCK_SPACE = "    ";
	public static final String INNER_BLOCK_SPACE = "      ";
	public static final String INNER_INNER_BLOCK_SPACE = "        ";
	public static final String INNER_INNER_INNER_BLOCK_SPACE = "          ";
	
	protected final static Logger logger = LoggerFactory.getLogger(TestSuiteWriterUtils.class);

	
	public static boolean needToUseAgent(){
		return Properties.REPLACE_CALLS || Properties.VIRTUAL_FS
				|| Properties.RESET_STATIC_FIELDS;
	}
	
	public static boolean hasAnySecurityException(List<ExecutionResult> results) {
		for (ExecutionResult result : results) {
			if (result.hasSecurityException()) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean wasAnyWrittenProperty(List<ExecutionResult> results) {
		for (ExecutionResult res : results) {
			if (res.wasAnyPropertyWritten()) {
				return true;
			}
		}
		return false;
	}

	public static String getNameOfTest(List<TestCase> tests, int position) {

		if (Properties.ASSERTION_STRATEGY == AssertionStrategy.STRUCTURED) {
			throw new IllegalStateException(
			        "For the moment, structured tests are not supported");
		}

		int totalNumberOfTests = tests.size();
		String totalNumberOfTestsString = String.valueOf(totalNumberOfTests - 1);
		String testNumber = StringUtils.leftPad(String.valueOf(position),
		                                        totalNumberOfTestsString.length(), "0");
		String testName = "test" + testNumber;
		return testName;
	}
	
	public static Set<String> mergeProperties(List<ExecutionResult> results) {
		if (results == null) {
			return null;
		}
		Set<String> set = new LinkedHashSet<String>();
		for (ExecutionResult res : results) {
			Set<String> props = res.getReadProperties();
			if (props != null) {
				set.addAll(props);
			}
		}
		return set;
	}
	
	public static boolean shouldResetProperties(List<ExecutionResult> results) {
		/*
		 * Note: we need to reset the properties even if the SUT only read them. Reason is
		 * that we are modifying them in the test case in the @Before method
		 */
		Set<String> readProperties = null;
		if (Properties.REPLACE_CALLS) {
			readProperties = mergeProperties(results);
			if (readProperties.isEmpty()) {
				readProperties = null;
			}
		}

		boolean shouldResetProperties = Properties.REPLACE_CALLS
		        && (wasAnyWrittenProperty(results) || readProperties != null);

		return shouldResetProperties;
	}
	
	/**
	 * Create subdirectory for package in test directory
	 * 
	 * @param directory
	 *            a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String mainDirectory(String directory) {
		String dirname = directory + File.separator
		        + Properties.PROJECT_PREFIX.replace('.', File.separatorChar); // +"/GeneratedTests";
		File dir = new File(dirname);
		logger.debug("Target directory: " + dirname);
		dir.mkdirs();
		return dirname;
	}

	public static UnitTestAdapter getAdapter() {
		if (Properties.TEST_FORMAT == OutputFormat.JUNIT3)
			return new JUnit3TestAdapter();
		else if (Properties.TEST_FORMAT == OutputFormat.JUNIT4)
			return new JUnit4TestAdapter();
		else
			throw new RuntimeException("Unknown output format: " + Properties.TEST_FORMAT);
	}

	/**
	 * Create subdirectory for package in test directory
	 * 
	 * @param directory
	 *            a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String makeDirectory(String directory) {
		String dirname = directory + File.separator
		        + Properties.CLASS_PREFIX.replace('.', File.separatorChar); // +"/GeneratedTests";
		File dir = new File(dirname);
		logger.debug("Target directory: " + dirname);
		dir.mkdirs();
		return dirname;
	}


}
