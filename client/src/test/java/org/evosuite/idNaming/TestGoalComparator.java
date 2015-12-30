package org.evosuite.idNaming;

import static org.junit.Assert.assertEquals;

import org.evosuite.coverage.exception.ExceptionCoverageTestFitness;
import org.evosuite.coverage.io.IOCoverageConstants;
import org.evosuite.coverage.io.output.OutputCoverageGoal;
import org.evosuite.coverage.io.output.OutputCoverageTestFitness;
import org.evosuite.coverage.method.MethodCoverageTestFitness;
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
