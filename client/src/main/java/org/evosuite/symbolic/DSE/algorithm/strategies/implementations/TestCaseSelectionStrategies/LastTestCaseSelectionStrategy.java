package org.evosuite.symbolic.DSE.algorithm.strategies.implementations.TestCaseSelectionStrategies;

import org.evosuite.symbolic.DSE.algorithm.strategies.TestCaseSelectionStrategy;
import org.evosuite.testcase.TestCase;

import java.util.List;

public class LastTestCaseSelectionStrategy implements TestCaseSelectionStrategy {
    @Override
    public TestCase getCurrentIterationBasedTestCase(List<TestCase> generatedTests) {
        return generatedTests.get(
                generatedTests.size() - 1
        );
    }
}
