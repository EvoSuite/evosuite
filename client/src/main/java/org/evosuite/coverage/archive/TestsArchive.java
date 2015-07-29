package org.evosuite.coverage.archive;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.ga.Archive;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.setup.TestCluster;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.generic.GenericAccessibleObject;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericMethod;
import org.evosuite.utils.Randomness;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class incrementally builds a TestSuiteChromosome with passed test cases.
 * It means to be an archive of tests that covered goals during the evolution.
 * @author mattia
 */
public enum TestsArchive implements Archive<TestSuiteChromosome>, Serializable {

	/**
	 * singleton instance
	 */
	instance;
	
	private static final long serialVersionUID = 6665770735812413289L;

	private static final Logger logger = LoggerFactory.getLogger(TestsArchive.class);

	/*
		TODO: looks like here we keep track of only covered goals, and not their branch
		distance. In the future, if we want to get rid of the GA population, we ll
		need to keep track of branch distances as well
	 */

	/**
	 * necessary to avoid having a billion of redundant test cases
	 */
    private final Map<FitnessFunction<?>, Set<TestFitnessFunction>> coveredGoals;

    private final Map<FitnessFunction<?>, Integer> goalsCountMap;

    // This can probably be optimised, but to remove the testsuitechromosome
    // I'm just replicating the maps we used in here
    private final Map<FitnessFunction<?>, Integer> coveredGoalsCountMap;
    private final Map<FitnessFunction<?>, Double> coverageMap;
    
	private final Map<FitnessFunction<?>, Set<TestFitnessFunction>> goalMap;
    private final Map<String, Set<TestFitnessFunction>> methodMap;

	private final Map<TestFitnessFunction, ExecutionResult> testMap;

	private TestsArchive() {
		coveredGoals = new HashMap<>();
		goalsCountMap = new HashMap<>();
		coveredGoalsCountMap = new HashMap<>();
		coverageMap = new HashMap<>();
		goalMap = new HashMap<>();
		methodMap = new HashMap<>();
		testMap = new HashMap<>();
	}


	// ------- public methods ------------

	public void reset() {
		coveredGoals.clear();
		goalMap.clear();
		goalsCountMap.clear();
		methodMap.clear();
		testMap.clear();
		coveredGoalsCountMap.clear();
		coverageMap.clear();
	}


	public void addGoalToCover(FitnessFunction<?> ff, TestFitnessFunction goal) {
        String key = getGoalKey(goal);

		if(!methodMap.containsKey(key)) {
            methodMap.put(key, new HashSet<TestFitnessFunction>());
        }

		if(!goalMap.containsKey(ff)) {
			goalMap.put(ff, new HashSet<TestFitnessFunction>());
            goalsCountMap.put(ff, 0);
		}

		goalMap.get(ff).add(goal);
        methodMap.get(key).add(goal);
        goalsCountMap.put(ff, goalsCountMap.get(ff) + 1);

		logger.debug("Registering new goal: " + goal);
	}


    public void putTest(FitnessFunction<?> ff, TestFitnessFunction goal, ExecutionResult result) {

		if (! goalMap.containsKey(ff)) {
			return;
		}

		if (!coveredGoals.containsKey(ff)) {
			coveredGoals.put(ff,new HashSet<TestFitnessFunction>());
		}

		boolean isNewCoveredGoal = !coveredGoals.get(ff).contains(goal);

		if (isNewCoveredGoal) {
			coveredNewGoal(ff, goal);
		}

		boolean better = isBetterThanCurrent(goal, result);

		if(isNewCoveredGoal || better){
			ExecutionResult copy = result.clone();
			copy.test = copy.test.clone(); //result.clone() does not clone the test

			// Remove all statements after an exception
			if(!copy.noThrownExceptions()) {
				copy.test.chop(copy.getFirstPositionOfThrownException() + 1);
			}
			
			testMap.put(goal, copy);
			handleCollateralCoverage(copy); //check for collateral only when there is improvement over current goal
		}
	}

	/*
		TODO: does not seem it is really used for anything
	 */
	public TestSuiteChromosome getReducedChromosome() {
		TestSuiteChromosome suite = new TestSuiteChromosome();
		for(Entry<TestFitnessFunction, ExecutionResult> entry : testMap.entrySet()) {
			if(!entry.getKey().isCoveredBy(suite)) {
				suite.addTest(entry.getValue().test);
			}
		}
        for (FitnessFunction<?> ff : coverageMap.keySet()) {
        	suite.setCoverage(ff, coverageMap.get(ff));
        	suite.setNumOfCoveredGoals(ff, coveredGoalsCountMap.get(ff));
        }
		logger.info("Final test suite size from archive: " + suite.size());
		return suite;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public TestSuiteChromosome createMergedSolution(TestSuiteChromosome suite) {

		Properties.TEST_ARCHIVE = false; //TODO: why?
		TestSuiteChromosome best = null;
		try {
			best = suite.clone();

			for (Entry<TestFitnessFunction, ExecutionResult> entry : testMap.entrySet()) {
				if (!entry.getKey().isCoveredBy(best)) {
					TestChromosome chromosome = new TestChromosome();
					ExecutionResult copy = entry.getValue().clone();
					copy.test = copy.test.clone();
					chromosome.setTestCase(copy.test);
					chromosome.setLastExecutionResult(copy);
					best.addTest(chromosome); //should avoid re-execute the tests
				}
			}
			for (FitnessFunction ff : coveredGoals.keySet()) {
				ff.getFitness(best);
			}
		} finally {
			Properties.TEST_ARCHIVE = true;
		}

		logger.info("Final test suite size from archive: " + best.size());

		return best;
	}

	public boolean isArchiveEmpty(){
		return testMap.isEmpty();
	}
	
	public int getTotalNumberOfGoals() {
		int total = 0;
		for(Integer numGoals : goalsCountMap.values())
			total += numGoals;
		return total;
	}

	public int getNumberOfCoveredGoals() {
		int covered = 0;
		for(Integer numGoals : coveredGoalsCountMap.values())
			covered += numGoals;
		return covered;
	}

	public TestCase getCloneAtRandom(){
		/*
			Note: this gives higher probability to tests that cover more targets.
			Maybe it is not the best way, but likely the quickest to compute
		 */
		ExecutionResult res = Randomness.choice(testMap.values());
		if(!res.noThrownExceptions()) {
			// If the test ends with an exception, remove the statement 
			// that throws the exception
			TestCase copy = res.test.clone();
			copy.chop(res.getFirstPositionOfThrownException());
			return copy;
		}
		return res.test.clone();
	}
	
	@Override
	public String toString() {
        int sum = 0;
		for (FitnessFunction<?> ff : coveredGoals.keySet()) {
            sum += coveredGoals.get(ff).size();
        }
        return "Goals covered: " + sum;
	}



	// ---------  private/protected methods -------------------

	private void coveredNewGoal(FitnessFunction<?> ff, TestFitnessFunction goal) {
		if (!coveredGoals.containsKey(ff)) {
			coveredGoals.put(ff,new HashSet<TestFitnessFunction>());
		}

		logger.debug("Adding covered goal to archive: " + goal);
		coveredGoals.get(ff).add(goal);
		updateMaps(ff, goal);
		setCoverage(ff, goal);
		if (isMethodFullyCovered(getGoalKey(goal))) {
			removeTestCall(goal.getTargetClass(), goal.getTargetMethod());
		}
	}


	private void handleCollateralCoverage(ExecutionResult copy) {


		//check if this improves upon already covered targets
		for(Entry<FitnessFunction<?>, Set<TestFitnessFunction>> entry : coveredGoals.entrySet()){
			for(TestFitnessFunction goal : entry.getValue()){
				if(isBetterThanCurrent(goal,copy)){
					testMap.put(goal, copy);
				}
			}
		}


		Map<FitnessFunction<?>, Set<TestFitnessFunction>> toUpdate = new HashMap<>();

		//does it cover new targets?
		for(Entry<FitnessFunction<?>, Set<TestFitnessFunction>> entry : goalMap.entrySet()){
			Set<TestFitnessFunction> set = new HashSet<>();
			toUpdate.put(entry.getKey(),set);

			for(TestFitnessFunction goal : entry.getValue()){
				if(goal.isCovered(copy)){
					set.add(goal); //keep track, as cannot modify goalMap while looping over it
					testMap.put(goal, copy);
				}
			}
		}

		for(Entry<FitnessFunction<?>, Set<TestFitnessFunction>> entry : toUpdate.entrySet()) {
			for (TestFitnessFunction goal : entry.getValue()) {
				coveredNewGoal(entry.getKey(),goal);
			}
		}

	}

	private boolean isBetterThanCurrent(TestFitnessFunction goal, ExecutionResult result) {

		if(!goal.isCovered(result)){
			return false;
		}

		if(testMap.get(goal)==null){
			return true;
		}

		// If we try to add a test for a goal we've already covered
		// and the new test is shorter, keep the shorter one
		if(result.test.size() < testMap.get(goal).test.size()) {
			return true;
		}

		/*
			TODO: in the future, here we should handle also PrivateAccess
			and functional mocking
		 */
		return false;
	}

	private void setCoverage(FitnessFunction<?> ff, TestFitnessFunction goal) {
		assert(goalsCountMap != null);
		int covered = coveredGoals.get(ff).size();
		int total = goalsCountMap.containsKey(ff) ? goalsCountMap.get(ff) : 0;
		double coverage = total == 0 ? 1.0 : (double) covered / (double) total;
		coveredGoalsCountMap.put(ff, covered);
		coverageMap.put(ff, coverage);
	}


	private void writeObject(ObjectOutputStream oos) throws IOException {
		throw new RuntimeException("AAARGH"); //FIXME what the heck is this???
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


}
