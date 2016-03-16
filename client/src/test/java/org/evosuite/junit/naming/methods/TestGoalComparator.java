/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.junit.naming.methods;

import static org.junit.Assert.assertEquals;

import org.evosuite.coverage.exception.ExceptionCoverageTestFitness;
import org.evosuite.coverage.io.IOCoverageConstants;
import org.evosuite.coverage.io.output.OutputCoverageGoal;
import org.evosuite.coverage.io.output.OutputCoverageTestFitness;
import org.evosuite.coverage.method.MethodCoverageTestFitness;
import org.evosuite.junit.naming.methods.GoalComparator;
import org.evosuite.runtime.mock.java.lang.MockArithmeticException;
import org.evosuite.testcase.TestFitnessFunction;
import org.junit.Test;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by gordon on 28/12/2015.
 */
public class TestGoalComparator {

    @Test
    public void testCompareEqual() {
        GoalComparator comparator = new GoalComparator();
        ExceptionCoverageTestFitness goal1 = new ExceptionCoverageTestFitness("FooClass", "toString()", MockArithmeticException.class, ExceptionCoverageTestFitness.ExceptionType.EXPLICIT);
        ExceptionCoverageTestFitness goal2 = new ExceptionCoverageTestFitness("FooClass", "toString()", MockArithmeticException.class, ExceptionCoverageTestFitness.ExceptionType.EXPLICIT);
        assertEquals(0, comparator.compare(goal1, goal2));
    }

    @Test
    public void testCompareExceptionMethod() {
        GoalComparator comparator = new GoalComparator();
        MethodCoverageTestFitness goal1 = new MethodCoverageTestFitness("FooClass", "toString()");
        ExceptionCoverageTestFitness goal2 = new ExceptionCoverageTestFitness("FooClass", "toString()", MockArithmeticException.class, ExceptionCoverageTestFitness.ExceptionType.EXPLICIT);

        assertEquals(1, comparator.compare(goal1, goal2));
    }

    @Test
    public void testComparatorList() {
        GoalComparator comparator = new GoalComparator();
        MethodCoverageTestFitness goal1 = new MethodCoverageTestFitness("FooClass", "toString()");
        ExceptionCoverageTestFitness goal2 = new ExceptionCoverageTestFitness("FooClass", "toString()", MockArithmeticException.class, ExceptionCoverageTestFitness.ExceptionType.EXPLICIT);
        OutputCoverageGoal outputGoal = new OutputCoverageGoal("FooClass", "toString", Type.getType("Ljava.lang.String;"), IOCoverageConstants.REF_NONNULL);
        OutputCoverageTestFitness goal3 = new OutputCoverageTestFitness(outputGoal);

        List<TestFitnessFunction> goals = new ArrayList<>();
        goals.add(goal1);
        goals.add(goal2);
        goals.add(goal3);
        Collections.sort(goals, comparator);
        assertEquals(goal2, goals.get(0));
    }
}
