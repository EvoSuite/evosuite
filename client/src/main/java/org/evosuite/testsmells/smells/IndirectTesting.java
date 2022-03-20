package org.evosuite.testsmells.smells;

import org.evosuite.Properties;
import org.evosuite.assertion.Assertion;
import org.evosuite.assertion.InspectorAssertion;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsmells.AbstractTestCaseSmell;

import java.lang.reflect.Method;
import java.util.Set;

public class IndirectTesting extends AbstractTestCaseSmell {

    public IndirectTesting() {
        super("TestSmellIndirectTesting");
    }

    @Override
    public int computeNumberOfSmells(TestChromosome chromosome) {
        int smellCount = 0;

        int size = chromosome.size();

        Statement currentStatement;
        Method method;

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);
            if(currentStatement instanceof MethodStatement){
                method = ((MethodStatement) currentStatement).getMethod().getMethod();
                if(!method.getDeclaringClass().getCanonicalName().equals(Properties.TARGET_CLASS)){
                    smellCount++;
                }
            }

            if(currentStatement.hasAssertions()){

                Set<Assertion> assertions = currentStatement.getAssertions();

                for(Assertion assertion : assertions) {
                    if (assertion instanceof InspectorAssertion) {
                        method = ((InspectorAssertion) assertion).getInspector().getMethod();
                        if(!method.getDeclaringClass().getCanonicalName().equals(Properties.TARGET_CLASS)){
                            smellCount++;
                        }
                    }
                }
            }
        }
        return smellCount;
    }
}
