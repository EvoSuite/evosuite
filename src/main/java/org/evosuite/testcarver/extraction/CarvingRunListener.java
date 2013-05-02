package org.evosuite.testcarver.extraction;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.testcarver.capture.CaptureLog;
import org.evosuite.testcarver.capture.Capturer;
import org.evosuite.testcarver.codegen.CaptureLogAnalyzer;
import org.evosuite.testcarver.testcase.EvoTestCaseCodeGenerator;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.utils.GenericTypeInference;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

public class CarvingRunListener extends RunListener {

	private final List<TestCase> carvedTests = new ArrayList<TestCase>();

	private final Class<?>[] targetClasses;

	public CarvingRunListener() {
		targetClasses = new Class<?>[1];
		targetClasses[0] = Properties.getTargetClass();
	}

	public List<TestCase> getTestCases() {
		return carvedTests;
	}

	@Override
	public void testStarted(Description description) throws Exception {
		Capturer.startCapture();
	}

	@Override
	public void testFinished(Description description) throws Exception {
		final CaptureLog log = Capturer.stopCapture();
		this.processLog(log);
		Capturer.clear();
	}

	/**
	 * Creates TestCase out of the captured log
	 * 
	 * @param log
	 *            log captured from test execution
	 */
	private void processLog(final CaptureLog log) {
		final CaptureLogAnalyzer analyzer = new CaptureLogAnalyzer();
		final EvoTestCaseCodeGenerator codeGen = new EvoTestCaseCodeGenerator();
		analyzer.analyze(log, codeGen, this.targetClasses);

		DefaultTestCase test = (DefaultTestCase) codeGen.getCode();
		test.changeClassLoader(TestGenerationContext.getClassLoader());
		GenericTypeInference inference = new GenericTypeInference();
		//test.accept(inference);
		inference.inferTypes(test);

		carvedTests.add(test);
		codeGen.clear();
	}
}
