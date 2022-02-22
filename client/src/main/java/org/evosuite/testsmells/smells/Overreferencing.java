package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsmells.AbstractTestCaseSmell;

public class Overreferencing extends AbstractTestCaseSmell {

    public Overreferencing() {
        super("TestSmellOverreferencing");
    }

    @Override
    public int computeNumberOfSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        int count = 0;

        Statement currentStatement;

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);

            //Is it possible to verify whether a class belongs to the application code?
            count += currentStatement instanceof ConstructorStatement ? 1 : 0;
        }

        return count;
    }
}
