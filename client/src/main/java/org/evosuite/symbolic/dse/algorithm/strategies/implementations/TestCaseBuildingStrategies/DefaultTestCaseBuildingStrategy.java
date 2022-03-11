/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.symbolic.dse.algorithm.strategies.implementations.TestCaseBuildingStrategies;

import org.evosuite.symbolic.PathCondition;
import org.evosuite.symbolic.dse.DSETestCase;
import org.evosuite.symbolic.dse.algorithm.GenerationalSearchPathCondition;
import org.evosuite.symbolic.dse.algorithm.strategies.TestCaseBuildingStrategy;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCaseUpdater;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Strategy for building the initial test case with default values
 *
 * @author ignacio lebrero
 */
public class DefaultTestCaseBuildingStrategy implements TestCaseBuildingStrategy {
    @Override
    public DSETestCase buildInitialTestCase(Method method) {
        TestCase testCase = TestCaseUpdater.buildTestCaseWithDefaultValues(method);
        PathCondition emptyPathCondition = new PathCondition(new ArrayList());

        GenerationalSearchPathCondition emptyGenerationalSearchPathCondition = new GenerationalSearchPathCondition(
                emptyPathCondition,
                0// Initial index is 0 to negate all branch conditions
        );

        return new DSETestCase(
                testCase,
                emptyGenerationalSearchPathCondition,
                0.0 // Initial test case will always be seen the first time.
        );
    }
}
