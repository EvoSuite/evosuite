package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsmells.AbstractTestCaseSmell;

import java.util.Collections;
import java.util.Set;

public class RottenGreenTests extends AbstractTestCaseSmell {

    public RottenGreenTests() {
        super("TestSmellRottenGreenTests");
    }

    @Override
    public int computeNumberOfSmells(TestChromosome chromosome) {
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
