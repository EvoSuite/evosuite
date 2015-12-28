package org.evosuite.idNaming;

import org.evosuite.coverage.exception.ExceptionCoverageTestFitness;
import org.evosuite.coverage.input.InputCoverageTestFitness;
import org.evosuite.coverage.method.MethodCoverageTestFitness;
import org.evosuite.coverage.method.MethodNoExceptionCoverageTestFitness;
import org.evosuite.coverage.output.OutputCoverageTestFitness;
import org.evosuite.testcase.TestFitnessFunction;

import java.util.Comparator;

/**
 * Created by gordon on 22/12/2015.
 */
public class GoalComparator implements Comparator<TestFitnessFunction> {

    // 1. MethodGoal
    // 2. Interface
    // 3. Exception
    // 4. Output
    // 5. Input
    // 6. Assertion

    @Override
    public int compare(TestFitnessFunction o1, TestFitnessFunction o2) {
        Class<?> c1 = o1.getClass();
        Class<?> c2 = o2.getClass();
        if(c1.equals(c2))
            return o1.compareTo(o2);

        if(c1.equals(ExceptionCoverageTestFitness.class))
            return -1;
        else if(c2.equals(ExceptionCoverageTestFitness.class))
            return 1;

        if(c1.equals(MethodCoverageTestFitness.class))
            return -1;
        else if(c2.equals(MethodCoverageTestFitness.class))
            return 1;

        if(c1.equals(MethodNoExceptionCoverageTestFitness.class))
            return -1;
        else if(c2.equals(MethodNoExceptionCoverageTestFitness.class))
            return 1;

        if(c1.equals(OutputCoverageTestFitness.class))
            return -1;
        else if(c2.equals(OutputCoverageTestFitness.class))
            return 1;

        if(c1.equals(InputCoverageTestFitness.class))
            return -1;
        else if(c2.equals(InputCoverageTestFitness.class))
            return 1;

        // TODO: Assertion

        return 0;
    }
}
