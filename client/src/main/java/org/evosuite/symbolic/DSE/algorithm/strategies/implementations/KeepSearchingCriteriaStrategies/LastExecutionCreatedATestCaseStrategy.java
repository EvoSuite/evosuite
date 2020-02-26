package org.evosuite.symbolic.DSE.algorithm.strategies.implementations.KeepSearchingCriteriaStrategies;

import org.evosuite.symbolic.DSE.algorithm.strategies.KeepSearchingCriteriaStrategy;
import org.evosuite.testcase.TestCase;

import java.util.List;

public class LastExecutionCreatedATestCaseStrategy implements KeepSearchingCriteriaStrategy {
    @Override
    public boolean ShouldKeepSearching(List<TestCase> generatedTests) {
        return false;
    }
}
