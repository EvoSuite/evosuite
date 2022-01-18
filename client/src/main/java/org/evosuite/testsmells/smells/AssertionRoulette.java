package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractTestSmell;
import org.evosuite.testcase.statements.Statement;

public class AssertionRoulette extends AbstractTestSmell {

    public AssertionRoulette() {
        setSmellName("Assertion Roulette");
    }

    @Override
    public int obtainSmellCount(TestChromosome chromosome) {
        int size = chromosome.size();
        int count = 0;

        Statement currentStatement;

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);
            count += currentStatement.getAssertions().size();
        }
        return count;
    }
}
