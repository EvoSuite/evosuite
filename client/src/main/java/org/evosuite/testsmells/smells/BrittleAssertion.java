package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testsmells.AbstractTestSmell;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsuite.TestSuiteChromosome;

public class BrittleAssertion extends AbstractTestSmell {

    public BrittleAssertion(String smellName) {
        super(smellName);
    }

    @Override
    public int obtainSmellCount(TestChromosome chromosome) {
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

    @Override
    public int obtainSmellCount(TestSuiteChromosome chromosome) {
        int smellCount = 0;

        for(TestChromosome testcase : chromosome.getTestChromosomes()){
            smellCount += obtainSmellCount(testcase);
        }

        return smellCount;
    }
}
