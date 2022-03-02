package org.evosuite.testsmells.smells;

import org.evosuite.assertion.Assertion;
import org.evosuite.assertion.InspectorAssertion;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testsmells.AbstractTestCaseSmell;
import org.evosuite.testcase.statements.Statement;

import java.util.Set;

public class BrittleAssertion extends AbstractTestCaseSmell {

    public BrittleAssertion() {
        super("TestSmellBrittleAssertion");
    }

    @Override
    public int computeNumberOfSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        int count = 0;

        Statement currentStatement;

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);

            Set<Assertion> assertions = currentStatement.getAssertions();

            for(Assertion assertion : assertions){
                if(assertion instanceof InspectorAssertion){
                    count++;
                } else {
                    count += assertion.getSource().getStPosition() == i ? 0 : 1;
                }
            }
        }

        return count;
    }
}
