package org.evosuite.testsmells.smells;

import org.evosuite.Properties;
import org.evosuite.assertion.Assertion;
import org.evosuite.assertion.InspectorAssertion;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsmells.AbstractTestCaseSmell;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

public class EagerTest extends AbstractTestCaseSmell {

    public EagerTest() {
        super("TestSmellEagerTest");
    }

    @Override
    public double computeNumberOfTestSmells(TestChromosome chromosome) {
        int size = chromosome.size();

        Statement currentStatement;
        Method method;

        Set<Method> setOfMethods = new LinkedHashSet<>();

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);
            if(currentStatement instanceof MethodStatement){
                method = ((MethodStatement) currentStatement).getMethod().getMethod();
                if(method.getDeclaringClass().getCanonicalName().equals(Properties.TARGET_CLASS)){
                    setOfMethods.add(method);
                }
            }

            if(currentStatement.hasAssertions()){

                Set<Assertion> assertions = currentStatement.getAssertions();

                for(Assertion assertion : assertions){
                    if(assertion instanceof InspectorAssertion){
                        method = ((InspectorAssertion) assertion).getInspector().getMethod();
                        if(method.getDeclaringClass().getCanonicalName().equals(Properties.TARGET_CLASS)){
                            setOfMethods.add(method);
                        }
                    }
                }
            }
        }

        return setOfMethods.size();
    }
}
