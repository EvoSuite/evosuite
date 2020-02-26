package org.evosuite.symbolic.DSE.algorithm.strategies;

import org.evosuite.testcase.TestCase;
import java.util.List;

public interface KeepSearchingCriteriaStrategy {
    boolean ShouldKeepSearching(List<TestCase> generatedTests);
}
