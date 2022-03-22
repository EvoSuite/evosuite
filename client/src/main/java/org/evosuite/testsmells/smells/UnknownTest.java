package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsmells.AbstractTestCaseSmell;

public class UnknownTest extends AbstractTestCaseSmell {

    public UnknownTest() {
        super("TestSmellUnknownTest");
    }

    @Override
    public int computeNumberOfSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        int count = 1;

        Statement currentStatement;

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);
            if(currentStatement.hasAssertions()){
                count = 0;
                break;
            }
        }

        // We will have to define a value greater than 1
        return count;
    }
}
