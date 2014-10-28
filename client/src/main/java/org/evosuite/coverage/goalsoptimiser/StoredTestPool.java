package org.evosuite.coverage.goalsoptimiser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFitnessFunction;

public class StoredTestPool implements Serializable {
	private static final long serialVersionUID = 6665770735812413289L;
	
	private final Map<TestFitnessFunction, TestCase> goalsToTestsMap;
	
	public StoredTestPool() {
		goalsToTestsMap = new HashMap<>();
	}

	public void putTest(TestFitnessFunction goal, TestCase test) {
		goalsToTestsMap.put(goal, test);
	}

	public boolean contains(TestFitnessFunction goal) {
		return goalsToTestsMap.get(goal) != null;
	}

	public void registerAllTests(Map<TestFitnessFunction, TestCase> goalsToTestsMap) {
		this.goalsToTestsMap.putAll(goalsToTestsMap);
	}

	public List<TestCase> getTestSuite() {
		return new ArrayList<>(goalsToTestsMap.values());
	}

	public TestCase getTest(TestFitnessFunction goal) {
		if (!goalsToTestsMap.containsKey(goal))
			throw new IllegalArgumentException();
		return goalsToTestsMap.get(goal);
	}
}
