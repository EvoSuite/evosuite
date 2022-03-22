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

public class SensitiveEquality extends AbstractTestCaseSmell {

    public SensitiveEquality() {
        super("TestSmellSensitiveEquality");
    }

    @Override
    public int computeNumberOfSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        int count = 0;

        Statement currentStatement;

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);

            if(currentStatement instanceof MethodStatement){
                String curr = ((MethodStatement) currentStatement).getMethodName();
                String className = ((MethodStatement) currentStatement).getDeclaringClassName();

                if(curr.equals("toString") && !className.equals(Properties.TARGET_CLASS)){
                    count += currentStatement.hasAssertions() ? 1 : 0;
                }
            }

            if(currentStatement.hasAssertions()){

                Set<Assertion> assertions = currentStatement.getAssertions();

                for(Assertion assertion : assertions){
                    if(assertion instanceof InspectorAssertion){
                        Method method = ((InspectorAssertion) assertion).getInspector().getMethod();
                        String curr = method.getName();

                        if(curr.equals("toString") && !method.getDeclaringClass().getCanonicalName().equals(Properties.TARGET_CLASS)){
                            count ++;
                        }
                    }
                }
            }
        }

        return count;
    }
}
