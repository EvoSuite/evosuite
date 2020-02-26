package org.evosuite.symbolic.DSE.algorithm.strategies.implementations.TestCaseBuildingStrategies;

import org.evosuite.symbolic.DSE.algorithm.strategies.TestCaseBuildingStrategy;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.utils.TestCaseUtils;

import java.lang.reflect.Method;

public class DefaultTestCaseBuildingStrategy implements TestCaseBuildingStrategy {
    @Override
    public TestCase buildInitialTestCase(Method method) {
        return TestCaseUtils.buildTestCaseWithDefaultValues(method);
    }
}
