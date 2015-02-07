package org.evosuite.coverage.archive;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.evosuite.setup.TestCluster;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.GenericAccessibleObject;
import org.evosuite.utils.GenericConstructor;
import org.evosuite.utils.GenericMethod;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class incrementally builds a TestSuiteChromosome with passed test cases.
 * It means to be an archive of tests that covered goals during the evolution.
 * @author mattia
 */
public enum TestsArchive implements Serializable {

	instance;
	
	private static final long serialVersionUID = 6665770735812413289L;

	private static final Logger logger = LoggerFactory.getLogger(TestsArchive.class);
	
	private TestSuiteChromosome bestChromo;
	//necessary to avoid having a billion of redundant test cases
	private final Set<Integer> coveredGoals;
	
	private Map<String, Set<TestFitnessFunction>> goalMap = new HashMap<>();

	private Map<TestFitnessFunction, TestCase> testMap = new HashMap<>();

	private TestsArchive() {
		bestChromo = new TestSuiteChromosome();
		coveredGoals = new HashSet<>();
	}
	
	public void addGoalToCover(TestFitnessFunction goal) {
		String key = goal.getTargetClass()+goal.getTargetMethod();
		if(!goalMap.containsKey(key)) {
			goalMap.put(key, new HashSet<TestFitnessFunction>());
		}
		goalMap.get(key).add(goal);
	}
	
	protected boolean isMethodFullyCovered(String className, String methodName) {
		if(!goalMap.containsKey(className+methodName))
			return true;
		return goalMap.get(className+methodName).isEmpty();
	}	
	
	protected void removeTestCall(String className, String methodName) {
		TestCluster cluster = TestCluster.getInstance();
		List<GenericAccessibleObject<?>> calls = cluster.getTestCalls();
		for(GenericAccessibleObject<?> call : calls) {
			if(!call.getDeclaringClass().getName().equals(className)) {
				continue;
			}
			if(call instanceof GenericMethod) {
				GenericMethod genericMethod = (GenericMethod)call;
				if(!methodName.startsWith(genericMethod.getName())) {
					continue;
				}
				String desc = Type.getMethodDescriptor(genericMethod.getMethod());
				if((genericMethod.getName()+desc).equals(methodName)) {
					logger.info("Removing method "+methodName+" from cluster");
					cluster.removeTestCall(call);
					logger.info("Testcalls left: "+cluster.getNumTestCalls());
				}
			} else if(call instanceof GenericConstructor) {
				GenericConstructor genericConstructor = (GenericConstructor)call;
				if(!methodName.startsWith("<init>")) {
					continue;
				}
				String desc = Type.getConstructorDescriptor(genericConstructor.getConstructor());
				if(("<init>" + desc).equals(methodName)) {
					logger.info("Removing constructor "+methodName+" from cluster");
					cluster.removeTestCall(call);
					logger.info("Testcalls left: "+cluster.getNumTestCalls());
				}
			}
		}
	}

	private void updateGoalMap(TestFitnessFunction goal) {
		String key = goal.getTargetClass()+goal.getTargetMethod();
		if(!goalMap.containsKey(key))
			return;
		goalMap.get(key).remove(goal);
	}
	
	public void putTest(TestFitnessFunction goal, TestCase test) {
		if (!coveredGoals.contains(goal.hashCode())) {
			logger.info("Adding covered goal to archive: "+goal);
			coveredGoals.add(goal.hashCode());
			bestChromo.addTest(test);
			testMap.put(goal, test);
			updateGoalMap(goal);
			if(isMethodFullyCovered(goal.getTargetClass(), goal.getTargetMethod())) {
				removeTestCall(goal.getTargetClass(), goal.getTargetMethod());
			}
		}
	}

	public void registerAllTests(Collection<TestChromosome> tests) {
		bestChromo.addTests(tests);
	} 
	
	/**
	 * return the chromosome with the tests of the archive
	 * @return
	 */
	public  TestSuiteChromosome getBestChromosome() {
		return bestChromo;
	}
	
	public TestSuiteChromosome getReducedChromosome() {
		TestSuiteChromosome suite = new TestSuiteChromosome();
		for(Entry<TestFitnessFunction, TestCase> entry : testMap.entrySet()) {
			if(!entry.getKey().isCoveredBy(suite)) {
				suite.addTest(entry.getValue());
			}
		}
		return suite;
	}
	
	public int getNumberOfTestsInArchive() {
		return bestChromo.size();
	}
	
	@Override
	public String toString() {		
		return "Goals covered: "+coveredGoals.size()+", tests: "+bestChromo.size();
	}
	
	public void reset() {
		bestChromo = new TestSuiteChromosome();
		coveredGoals.clear();
		goalMap.clear();
		testMap.clear();
	}

}
