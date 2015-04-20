package org.evosuite.testcarver.extraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.classpath.ResourceList;
import org.evosuite.rmi.ClientServices;
import org.evosuite.rmi.service.ClientState;
import org.evosuite.testcarver.capture.FieldRegistry;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.utils.LoggingUtils;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CarvingManager {

	private static Logger logger = LoggerFactory.getLogger(CarvingManager.class);
	
	private static CarvingManager instance = null;
	
	private CarvingManager() {
		
	}
	
	public static CarvingManager getInstance() {
		if(instance == null)
			instance = new CarvingManager();
		
		return instance;
	}
	
	private Map<Class<?>, List<TestCase>> carvedTests = new LinkedHashMap<Class<?>, List<TestCase>>();
	
	private boolean carvingDone = false;
	
	private Collection<String> getListOfJUnitClassNames() throws IllegalStateException {

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

		/* 
		Pattern pattern = Pattern.compile(Properties.JUNIT_PREFIX+".*.class");
		Collection<String> junitTestNames = ResourceList.getResources(pattern);		
		logger.info("Found "+junitTestNames.size()+" candidate junit classes for pattern "+pattern);
		*/
		return junitTestNames;
	}
	

	private void chopException(TestCase test, ExecutionResult result) {
		if (!result.noThrownExceptions()) {
			// No code including or after an exception should be in the pool
			Integer pos = result.getFirstPositionOfThrownException();
			if (pos != null) {
				test.chop(pos);
			} else {
				test.chop(test.size() - 1);
			}
		}
	}
	
	private void readTestCases() throws IllegalStateException {
		ClientServices.getInstance().getClientNode().changeState(ClientState.CARVING);
		Collection<String> junitTestNames = getListOfJUnitClassNames();
		LoggingUtils.getEvoLogger().info("* Executing tests from {} test classes for carving", junitTestNames.size());
		final JUnitCore runner = new JUnitCore();
		final CarvingRunListener listener = new CarvingRunListener();
		runner.addListener(listener);


		final List<Class<?>> junitTestClasses = new ArrayList<Class<?>>();
		final org.evosuite.testcarver.extraction.CarvingClassLoader classLoader = new org.evosuite.testcarver.extraction.CarvingClassLoader();
		// TODO: This really needs to be done in a nicer way!
		FieldRegistry.carvingClassLoader = classLoader;
		try {
			// instrument target class
			classLoader.loadClass(Properties.getTargetClass().getCanonicalName());
		} catch (final ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		for (String className : junitTestNames) {

			String classNameWithDots = ResourceList.getClassNameFromResourcePath(className);
			try {
				final Class<?> junitClass = classLoader.loadClass(classNameWithDots);
				junitTestClasses.add(junitClass);
			} catch (ClassNotFoundException e) {
				logger.warn("Error trying to load JUnit test class " + classNameWithDots
				        + ": " + e);
			}
		}

		final Class<?>[] classes = new Class<?>[junitTestClasses.size()];
		junitTestClasses.toArray(classes);
		Result result = runner.run(classes);
		logger.info("Result: "+result.getFailureCount() +"/"+result.getRunCount());
		for(Failure failure : result.getFailures()) {
			logger.info("Failure: "+failure.getMessage());
			logger.info("Exception: "+failure.getException());
		}
		
		Map<Class<?>, List<TestCase>> testMap = listener.getTestCases();
		for(Class<?> targetClass : testMap.keySet()) {

			List<TestCase> processedTests = new ArrayList<TestCase>();
			
			for (TestCase test : testMap.get(targetClass)) {
				if (test.isEmpty())
					continue;
				ExecutionResult executionResult = null;
				try {
					executionResult = TestCaseExecutor.runTest(test);
					
				} catch(Throwable t) {
					logger.info("Error while executing carved test: "+t);
					continue;
				}
				if (executionResult.noThrownExceptions()) {
					logger.info("Adding carved test without exception");
					logger.info(test.toCode());
					processedTests.add(test);
				} else {
					logger.info("Exception thrown in carved test: "
							+ executionResult.getExceptionThrownAtPosition(executionResult.getFirstPositionOfThrownException()));
					for (StackTraceElement elem : executionResult.getExceptionThrownAtPosition(executionResult.getFirstPositionOfThrownException()).getStackTrace()) {
						logger.info(elem.toString());
					}
					logger.info(test.toCode(executionResult.exposeExceptionMapping()));
					if (Properties.CHOP_CARVED_EXCEPTIONS) {
						logger.info("Chopping exception of carved test");
						chopException(test, executionResult);
						if (test.hasObject(Properties.getTargetClass(), test.size())) {
							processedTests.add(test);
						} else {
							logger.info("Chopped test is empty");
						}
					} else {
						logger.info("Not adding carved test with exception: ");
					}
				}
			}
			// junitTests.addAll(listener.getTestCases());

			if (processedTests.size() > 0) {
				LoggingUtils.getEvoLogger().info(" -> Carved {} tests for class {} from existing JUnit tests",
						processedTests.size(), targetClass);
				if (logger.isDebugEnabled()) {
					for (TestCase test : processedTests) {
						logger.debug("Carved Test: {}", test.toCode());
					}
				}
			} else {
				//String outcome = "";
				//for (Failure failure : result.getFailures()) {
				//	outcome += "(" + failure.getDescription() + ", " + failure.getTrace()
				//			+ ") ";
				//}
				logger.info("It was not possible to carve any test case for class {} from {}", targetClass.getName(),
						Arrays.toString(junitTestNames.toArray()));
				//		+ ". Test execution results: " + outcome);
			}
			carvedTests.put(targetClass, processedTests);
		}
		carvingDone = true;
		
		// TODO: Argh.
		FieldRegistry.carvingClassLoader = null;
		// TODO:
		// ClientNodeLocal client = ClientServices.getInstance().getClientNode();
		// client.trackOutputVariable(RuntimeVariable.CarvedTests, totalNumberOfTestsCarved);
		// client.trackOutputVariable(RuntimeVariable.CarvedCoverage,carvedCoverage);

	}
	
	public void clear() {
		carvingDone = false;
		carvedTests.clear();
	}
	
	public List<TestCase> getTestsForClass(Class<?> clazz) {
		if(!carvingDone)
			readTestCases();
		
		if(!carvedTests.containsKey(clazz))
			return new ArrayList<TestCase>();
		
		return carvedTests.get(clazz);
	}
	
	public Set<Class<?>> getClassesWithTests() {
		if(!carvingDone)
			readTestCases();

		return carvedTests.keySet();
	}
}
