/**
 * Copyright (C) 2010-2020 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.symbolic.DSE.algorithm.strategies.implementations.TestCaseBuildingStrategies;

import org.evosuite.symbolic.DSE.algorithm.strategies.TestCaseBuildingStrategy;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.utils.TestCaseUtils;

import java.lang.reflect.Method;

/**
 * Strategy for building the initial test case with default values
 *
 * @author ignacio lebrero
 */
public class DefaultTestCaseBuildingStrategy implements TestCaseBuildingStrategy {
    @Override
    public TestCase buildInitialTestCase(Method method) {
        return TestCaseUtils.buildTestCaseWithDefaultValues(method);
    }
}
