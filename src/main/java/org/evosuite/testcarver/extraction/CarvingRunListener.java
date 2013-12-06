package org.evosuite.testcarver.extraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.TimeController;
import org.evosuite.testcarver.capture.CaptureLog;
import org.evosuite.testcarver.capture.Capturer;
import org.evosuite.testcarver.codegen.CaptureLogAnalyzer;
import org.evosuite.testcarver.testcase.EvoTestCaseCodeGenerator;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.utils.GenericTypeInference;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CarvingRunListener extends RunListener {

	private final List<TestCase> carvedTests = new ArrayList<TestCase>();

	private final static Logger logger = LoggerFactory.getLogger(CarvingRunListener.class);

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
		if (TimeController.getInstance().isThereStillTimeInThisPhase()) {
			logger.info("Not yet reached maximum time to carve unit tests - executing test with carver");
			Capturer.startCapture();
		} else {
			logger.info("Reached maximum time to carve unit tests - executing test without carver");
		}
	}

	@Override
	public void testFinished(Description description) throws Exception {
		final CaptureLog log = Capturer.stopCapture();
		if (TimeController.getInstance().isThereStillTimeInThisPhase()) {
			this.processLog(log);
		}
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
		if(test == null) {
			logger.warn("Failed to carve test for "+Arrays.asList(this.targetClasses));
			return;
		}
		logger.info("Carved test of length " + test.size());
		try {
			test.changeClassLoader(TestGenerationContext.getClassLoader());
			GenericTypeInference inference = new GenericTypeInference();
			//test.accept(inference);
			inference.inferTypes(test);

			carvedTests.add(test);
		} catch (Throwable t) {
			logger.info("Exception during carving: " + t);
			for(StackTraceElement elem : t.getStackTrace()) {
				logger.info(elem.toString());
			}
			logger.info(test.toCode());

		}
		codeGen.clear();
	}
}
