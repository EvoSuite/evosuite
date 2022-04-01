package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsmells.AbstractNormalizedTestCaseSmell;

import java.util.Collections;
import java.util.Set;

public class RottenGreenTests extends AbstractNormalizedTestCaseSmell {

    public RottenGreenTests() {
        super("TestSmellRottenGreenTests");
    }

    @Override
    public long computeNumberOfTestSmells(TestChromosome chromosome) {
       int size = chromosome.size();

        ExecutionResult lastExecutionResult = chromosome.getLastExecutionResult();

        if(lastExecutionResult != null){

            Set<Integer> exceptionPositions = lastExecutionResult.getPositionsWhereExceptionsWereThrown();

            if(exceptionPositions.size() > 0){
                int firstException = Collections.min(exceptionPositions);
                return size - firstException - 1;
            }
        }

        return 0;
    }
}
