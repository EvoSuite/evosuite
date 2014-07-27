package org.evosuite.testcarver.extraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.TimeController;
import org.evosuite.instrumentation.BytecodeInstrumentation;
import org.evosuite.setup.TestClusterGenerator;
import org.evosuite.testcarver.capture.CaptureLog;
import org.evosuite.testcarver.capture.Capturer;
import org.evosuite.testcarver.codegen.CaptureLogAnalyzer;
import org.evosuite.testcarver.testcase.EvoTestCaseCodeGenerator;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.utils.GenericTypeInference;
import org.evosuite.utils.LoggingUtils;
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
			LoggingUtils.getEvoLogger().info(" - Carving tests from {}.{}", description.getClassName(), description.getMethodName());
			this.processLog(log);
		}
		Capturer.clear();
	}
	
	private List<Class<?>> getObservedClasses(final CaptureLog log) {
		List<Class<?>> targetClasses = new ArrayList<Class<?>>();
		targetClasses.add(Properties.getTargetClass());
		String prop = Properties.SELECTED_JUNIT;
		if (prop == null || prop.trim().isEmpty()) {
			throw new IllegalStateException(
			        "Trying to use a test carver factory, but empty Properties.SELECTED_JUNIT");
		}

		String[] paths = prop.split(":");
		Collection<String> junitTestNames = new HashSet<String>();
		for (String s : paths) {
			junitTestNames.add(s.trim());
		}

		if(Properties.CARVE_OBJECT_POOL) {
			Set<String> uniqueClasses = new LinkedHashSet<String>(log.getObservedClasses());
			for(String className : uniqueClasses) {
				if(junitTestNames.contains(className)) {
					logger.info("Skipping JUnit test class: "+className);
					continue;
				}
				if(BytecodeInstrumentation.checkIfCanInstrument(className)) {
					logger.info("Instrumentable: "+className);
					try {
						Class<?> clazz = Class.forName(className, true, TestGenerationContext.getInstance().getClassLoaderForSUT());
						if(TestClusterGenerator.canUse(clazz) && !clazz.isArray()) {
							if(!targetClasses.contains(clazz))
								targetClasses.add(clazz);
						}
						else
							logger.info("Cannot access"+className);
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
		logger.debug("Current log: "+log);
		List<Class<?>> observedClasses = getObservedClasses(log);
		for(Class<?> targetClass : observedClasses) {
			logger.debug("Current observed class: {}", targetClass.getName());
			Class<?>[] targetClasses = new Class<?>[1];
			targetClasses[0] = targetClass;
			if(!carvedTests.containsKey(targetClass))
				carvedTests.put(targetClass, new ArrayList<TestCase>());
		
			analyzer.analyze(log, codeGen, targetClasses);

			DefaultTestCase test = (DefaultTestCase) codeGen.getCode();
			if(test == null) {
				logger.info("Failed to carve test for "+Arrays.asList(targetClasses));
				codeGen.clear();
				continue;
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
