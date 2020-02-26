package org.evosuite.symbolic.DSE.algorithm.strategies;

import java.lang.reflect.Method;
import org.evosuite.testcase.TestCase;

public interface TestCaseBuildingStrategy {
    TestCase buildInitialTestCase(Method method);
}
