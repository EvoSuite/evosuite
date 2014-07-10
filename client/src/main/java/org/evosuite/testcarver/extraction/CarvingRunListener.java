package org.evosuite.testcarver.extraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.TimeController;
import org.evosuite.instrumentation.BytecodeInstrumentation;
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
	
	private final Map<Class<?>, List<TestCase>> carvedTests = new LinkedHashMap<Class<?>, List<TestCase>>();

	private final static Logger logger = LoggerFactory.getLogger(CarvingRunListener.class);

	public Map<Class<?>, List<TestCase>> getTestCases() {
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
	
	private List<Class<?>> getObservedClasses(final CaptureLog log) {
		List<Class<?>> targetClasses = new ArrayList<Class<?>>();
		targetClasses.add(Properties.getTargetClass());
		if(Properties.CARVE_OBJECT_POOL) {
			Set<String> uniqueClasses = new LinkedHashSet<String>(log.getObservedClasses());
			for(String className : uniqueClasses) {
				if(BytecodeInstrumentation.checkIfCanInstrument(className)) {
					logger.info("Instrumentable: "+className);
					try {
						Class<?> clazz = Class.forName(className);
						targetClasses.add(clazz);
					} catch(ClassNotFoundException e) {
						logger.info("Error loading class "+className+": "+e);
					}
				} else {
					logger.info("Not Instrumentable: "+className);
				}
			}

		}
		return targetClasses;
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

		for(Class<?> targetClass : getObservedClasses(log)) {
			logger.info("Carved tests for class "+targetClass);

			Class<?>[] targetClasses = new Class<?>[1];
			targetClasses[0] = targetClass;
			carvedTests.put(targetClass, new ArrayList<TestCase>());
		
			analyzer.analyze(log, codeGen, targetClasses);

			DefaultTestCase test = (DefaultTestCase) codeGen.getCode();
			if(test == null) {
				logger.warn("Failed to carve test for "+Arrays.asList(targetClasses));
				return;
			}
			logger.info("Carved test of length " + test.size());
			try {
				test.changeClassLoader(TestGenerationContext.getInstance().getClassLoaderForSUT());
				GenericTypeInference inference = new GenericTypeInference();
				//test.accept(inference);
				inference.inferTypes(test);

				carvedTests.get(targetClass).add(test);
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
}
