package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsmells.AbstractNormalizedTestCaseSmell;

/**
 * Definition:
 * Tests have assertions that are not executed, thus giving a false sense of security.
 *
 * Adaptation:
 * This smell occurs if a test case continues to exercise code after the statement in which the first exception was raised.
 *
 * Metric:
 * Number of statements that exist in a test case after the statement that raised the first exception.
 *
 * Computation:
 * 1 - Verify if the last execution result is not null
 * 2 (1 is True):
 *    2.1 - Get the position of the statement that raised the first exception
 *    2.2 - If the position of the first exception is not null, return the number of statements that exist after the position of the
 *          statement that raised the first exception; otherwise, return 0
 * 3 - Return 0
 */
public class RottenGreenTests extends AbstractNormalizedTestCaseSmell {

    private static final long serialVersionUID = 8172174304616644801L;

    public RottenGreenTests() {
        super("TestSmellRottenGreenTests");
    }

    @Override
    public long computeNumberOfTestSmells(TestChromosome chromosome) {
        int size = chromosome.size();

        ExecutionResult lastExecutionResult = chromosome.getLastExecutionResult();

        if(lastExecutionResult != null){

            Integer firstException = lastExecutionResult.getFirstPositionOfThrownExceptionSetOfExceptionPositions();

            if(firstException != null){
                return firstException < size ? size - firstException - 1 : 0;
            }
        }

        return 0;
    }

    /*
    @Override
    public long computeNumberOfTestSmells(TestChromosome chromosome) {
       int size = chromosome.size();

        ExecutionResult lastExecutionResult = chromosome.getLastExecutionResult();

        if(lastExecutionResult != null){

            Set<Integer> exceptionPositions = lastExecutionResult.getPositionsWhereExceptionsWereThrown();

            if(exceptionPositions.size() > 0){
                int firstException = Collections.min(exceptionPositions);
                return firstException < size ? size - firstException - 1 : 0;
            }
        }

        return 0;
    }
     */

    /*
    @Override
    public long computeNumberOfTestSmells(TestChromosome chromosome) {
        int size = chromosome.size();

        ExecutionResult lastExecutionResult = chromosome.getLastExecutionResult();

        if(lastExecutionResult != null){

            Set<Integer> exceptionPositions = lastExecutionResult.getPositionsWhereExceptionsWereThrown();

            if(exceptionPositions.size() > 0){

                LoggingUtils.getEvoLogger().info("--------------- Start ---------------");

                LoggingUtils.getEvoLogger().info("\n");

                TestCodeVisitor visitor = new TestCodeVisitor();

                TestCase testCase = chromosome.getTestCase();
                visitor.visitTestCase(testCase);

                for (int i = 0; i < size; i++){
                    visitor.visitStatement(testCase.getStatement(i));
                }

                LoggingUtils.getEvoLogger().info(visitor.getCode());

                LoggingUtils.getEvoLogger().info("\n");

                LoggingUtils.getEvoLogger().info("Size = " + size);

                LoggingUtils.getEvoLogger().info("\n");

                for(Integer position : exceptionPositions){
                    LoggingUtils.getEvoLogger().info("Exception -> Line: " + position);
                }

                LoggingUtils.getEvoLogger().info("\n");

                int firstException = Collections.min(exceptionPositions);
                LoggingUtils.getEvoLogger().info("The First Exception -> Line: " + firstException);
                LoggingUtils.getEvoLogger().info("Result: " + (firstException < size ? size - firstException - 1 : 0));
                LoggingUtils.getEvoLogger().info("--------------- End ---------------");
                return firstException < size ? size - firstException - 1 : 0;
            }

        }

        return 0;
    }

     */
}
