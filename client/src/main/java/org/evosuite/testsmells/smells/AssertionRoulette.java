package org.evosuite.testsmells.smells;

import org.evosuite.ga.FitnessFunction;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractTestCaseSmell;
import org.evosuite.testcase.statements.Statement;

public class AssertionRoulette extends AbstractTestCaseSmell {

    public AssertionRoulette() {
        super("TestSmellAssertionRoulette");
    }

    @Override
    public double computeNumberOfSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        int count = 0;

        Statement currentStatement;

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);
            count += currentStatement.getAssertions().size();
        }

        return FitnessFunction.normalize(count);
    }
}
