package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsmells.AbstractTestCaseSmell;

public class UnknownTest extends AbstractTestCaseSmell {

    public UnknownTest() {
        super("UnknownTest");
    }

    @Override
    public int computeNumberOfSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        int count = 0;

        Statement currentStatement;

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);
            count += currentStatement.getAssertions().size();
        }

        // We will have to define a value greater than 1
        return count == 0 ? 1 : 0;
    }
}
