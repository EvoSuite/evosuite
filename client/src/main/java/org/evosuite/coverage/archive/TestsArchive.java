package org.evosuite.coverage.archive;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.ga.Archive;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.setup.TestCluster;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
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
public enum TestsArchive implements Archive<TestSuiteChromosome>, Serializable {

	instance;
	
	private static final long serialVersionUID = 6665770735812413289L;

	private static final Logger logger = LoggerFactory.getLogger(TestsArchive.class);
	
	//necessary to avoid having a billion of redundant test cases
    private Map<FitnessFunction<?>, Set<TestFitnessFunction>> coveredGoals = new HashMap<>();

    private Map<FitnessFunction<?>, Integer> goalsCountMap = new HashMap<>();

    // This can probably be optimised, but to remove the testsuitechromosome
    // I'm just replicating the maps we used in here
    private Map<FitnessFunction<?>, Integer> coveredGoalsCountMap = new HashMap<>();
    private Map<FitnessFunction<?>, Double> coverageMap = new HashMap<>();
    
	private Map<FitnessFunction<?>, Set<TestFitnessFunction>> goalMap = new HashMap<>();
    private Map<String, Set<TestFitnessFunction>> methodMap = new HashMap<>();
	private Map<TestFitnessFunction, TestCase> testMap = new HashMap<>();
	
	// To avoid duplicate tests there's a set of all tests
	// but is this redundant wrt testMap.values()?
	private Set<TestCase> testCases = new LinkedHashSet<TestCase>();

	private TestsArchive() {
		coveredGoals = new HashMap<>();
	}
	
	public void addGoalToCover(FitnessFunction<?> ff, TestFitnessFunction goal) {
        String key = getGoalKey(goal);
        if(!methodMap.containsKey(key)) {
            methodMap.put(key,new HashSet<TestFitnessFunction>());
        }
		if(!goalMap.containsKey(ff)) {
			goalMap.put(ff, new HashSet<TestFitnessFunction>());
            goalsCountMap.put(ff, 0);
		}
		goalMap.get(ff).add(goal);
        methodMap.get(key).add(goal);
        goalsCountMap.put(ff, goalsCountMap.get(ff) + 1);
        logger.info("Registering new goal: "+goal);
	}
	
	protected boolean isMethodFullyCovered(String methodKey) {
		if(!methodMap.containsKey(methodKey))
			return true;
		return methodMap.get(methodKey).isEmpty();
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

	private void updateMaps(FitnessFunction<?> ff, TestFitnessFunction goal) {
		String key = getGoalKey(goal);
		if (! goalMap.containsKey(ff))
			return;
        goalMap.get(ff).remove(goal);
		methodMap.get(key).remove(goal);
	}

    private String getGoalKey(TestFitnessFunction goal) {
        return goal.getTargetClass() + goal.getTargetMethod();
    }

    public void putTest(FitnessFunction<?> ff, TestFitnessFunction goal, ExecutionResult result) {
    	TestCase testClone = result.test.clone();
    	if(!result.noThrownExceptions()) {
    		testClone.chop(result.getFirstPositionOfThrownException());
    	}
    	putTest(ff, goal, result.test);
    }
    
    // This method will keep the test, so it needs to be a clone if it is used again outside
    public void putTest(FitnessFunction<?> ff, TestFitnessFunction goal, TestCase test) {
		if (! goalMap.containsKey(ff)) {
			return;
		}

        if (!coveredGoals.containsKey(ff)) {
            coveredGoals.put(ff,new HashSet<TestFitnessFunction>());
        }
		if (!coveredGoals.get(ff).contains(goal)) {
			logger.debug("Adding covered goal to archive: "+goal);
			coveredGoals.get(ff).add(goal);
			// TestSuiteChromosome contains a list, but we don't need duplicate tests
			testCases.add(test);
			testMap.put(goal, test);
			updateMaps(ff, goal);
            setCoverage(ff, goal);
            if (isMethodFullyCovered(getGoalKey(goal))) {
				removeTestCall(goal.getTargetClass(), goal.getTargetMethod());
			}
		} else {
			// If we try to add a test for a goal we've already covered
			// and the new test is shorter, keep the shorter one
			if(test.size() < testMap.get(goal).size()) {
				testCases.remove(testMap.get(goal));
				testCases.add(test);
				testMap.put(goal, test);
			}
		}
	}

    private void setCoverage(FitnessFunction<?> ff, TestFitnessFunction goal) {
        assert(goalsCountMap != null);
        int covered = coveredGoals.get(ff).size();
        int total = goalsCountMap.containsKey(ff) ? goalsCountMap.get(ff) : 0;
        double coverage = total == 0 ? 1.0 : (double) covered / (double) total;
        coveredGoalsCountMap.put(ff, covered);
        coverageMap.put(ff, coverage);
    }

	public TestSuiteChromosome getReducedChromosome() {
		TestSuiteChromosome suite = new TestSuiteChromosome();
		for(Entry<TestFitnessFunction, TestCase> entry : testMap.entrySet()) {
			if(!entry.getKey().isCoveredBy(suite)) {
				suite.addTest(entry.getValue());
			}
		}
        for (FitnessFunction<?> ff : coverageMap.keySet()) {
        	suite.setCoverage(ff, coverageMap.get(ff));
        	suite.setNumOfCoveredGoals(ff, coveredGoalsCountMap.get(ff));
        }
		logger.info("Reduced test suite from archive: "+suite.size() +" from "+testCases.size());
		return suite;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public TestSuiteChromosome createMergedSolution(TestSuiteChromosome suite) {
		
		Properties.TEST_ARCHIVE = false;
		TestSuiteChromosome best = suite.clone();
		for(Entry<TestFitnessFunction, TestCase> entry : testMap.entrySet()) {
			if(!entry.getKey().isCoveredBy(best)) {
				best.addTest(entry.getValue().clone());
			}
		}
		for(FitnessFunction ff : coveredGoals.keySet()) {
			ff.getFitness(best);
		}
//        for (FitnessFunction<?> ff : bestChromo.getCoverages().keySet()) {
//            suite.setCoverage(ff, bestChromo.getCoverage(ff));
//            suite.setNumOfCoveredGoals(ff, bestChromo.getNumOfCoveredGoals(ff));
//        }
		Properties.TEST_ARCHIVE = true;
		logger.info("Reduced test suite from archive: "+suite.size() +" from "+testCases.size());

		return best;
	}
	
	public int getNumberOfTestsInArchive() {
		return testCases.size();
	}
	
	public Set<TestCase> getTests() {
		return testCases;
	}
	
	@Override
	public String toString() {
        int sum = 0;
		for (FitnessFunction<?> ff : coveredGoals.keySet()) {
            sum += coveredGoals.get(ff).size();
        }
        return "Goals covered: " + sum + ", tests: " + testCases.size();
	}
	
	public void reset() {
		testCases.clear();
		coveredGoals.clear();
		goalMap.clear();
        goalsCountMap.clear();
        methodMap.clear();
		testMap.clear();
		coveredGoalsCountMap.clear();
		coverageMap.clear();
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		throw new RuntimeException("AAARGH");
	}
}
