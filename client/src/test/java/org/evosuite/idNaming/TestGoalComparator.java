package org.evosuite.idNaming;

import org.evosuite.coverage.exception.ExceptionCoverageTestFitness;
import org.evosuite.coverage.method.MethodCoverageTestFitness;
import org.evosuite.coverage.output.OutputCoverageGoal;
import org.evosuite.coverage.output.OutputCoverageTestFitness;
import org.evosuite.runtime.mock.java.lang.MockArithmeticException;
import org.evosuite.testcase.TestFitnessFunction;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

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
        OutputCoverageGoal outputGoal = new OutputCoverageGoal("FooClass", "toString", "String", "NonNull");
        OutputCoverageTestFitness goal3 = new OutputCoverageTestFitness(outputGoal);

        List<TestFitnessFunction> goals = new ArrayList<>();
        goals.add(goal1);
        goals.add(goal2);
        goals.add(goal3);
        Collections.sort(goals, comparator);
        assertEquals(goal2, goals.get(0));
    }
}
