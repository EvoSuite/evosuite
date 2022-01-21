package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testsmells.AbstractTestCaseSmell;
import org.evosuite.testcase.statements.Statement;

public class BrittleAssertion extends AbstractTestCaseSmell {

    public BrittleAssertion() {
        super("BrittleAssertion");
    }

    @Override
    public int computeNumberOfSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        int count = 0;

        Statement currentStatement;

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);

            if(!(currentStatement instanceof MethodStatement)){
                count += currentStatement.hasAssertions() ? 1 : 0;
            }
        }
        return count;
    }
}
