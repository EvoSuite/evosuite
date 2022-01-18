package org.evosuite.testsmells.smells;

import org.evosuite.assertion.Assertion;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractTestSmell;
import org.evosuite.testcase.statements.Statement;

import java.util.LinkedHashSet;
import java.util.Set;

public class DuplicateAssert extends AbstractTestSmell {

    public DuplicateAssert() {
        setSmellName("Duplicate Assert");
    }

    @Override
    public int obtainSmellCount(TestChromosome chromosome) {
        int size = chromosome.size();
        int count = 0;

        Statement currentStatement;

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);
            Set<Object> listOfValues = new LinkedHashSet<>();
            Set<Assertion> assertions = currentStatement.getAssertions();

            for(Assertion assertion : assertions){
                listOfValues.add(assertion.getValue());
            }

            count += assertions.size() - listOfValues.size();
        }
        return count;
    }
}
