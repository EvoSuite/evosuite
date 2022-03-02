package org.evosuite.testsmells.smells;

import org.evosuite.assertion.Assertion;
import org.evosuite.assertion.InspectorAssertion;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testsmells.AbstractTestCaseSmell;
import org.evosuite.testcase.statements.Statement;

import java.lang.reflect.Method;
import java.util.*;

public class DuplicateAssert extends AbstractTestCaseSmell {

    public DuplicateAssert() {
        super("TestSmellDuplicateAssert");
    }

    @Override
    public int computeNumberOfSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        int count = 0;

        List<Method> methods = new ArrayList<>();
        List< Class<? extends Assertion>> assertionTypes = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        Method method;
        Class<? extends Assertion> assertionType;
        Object value;

        for (int i = 0; i < size; i++){

            Statement currentStatement = chromosome.getTestCase().getStatement(i);
            Set<Assertion> assertions = currentStatement.getAssertions();

            if(currentStatement instanceof MethodStatement){

                for(Assertion assertion : assertions){

                    if(assertion instanceof InspectorAssertion){
                        method = ((InspectorAssertion) assertion).getInspector().getMethod();
                    } else {
                        method = ((MethodStatement) currentStatement).getMethod().getMethod();
                    }

                    assertionType = assertion.getClass();
                    value = assertion.getValue();

                    boolean found = false;

                    for(int j = 0; j < methods.size(); j++){
                        if(methods.get(j).equals(method)){
                            if(assertionTypes.get(j).equals(assertionType) && values.get(j).equals(value)){
                                count++;
                                found = true;
                                break;
                            }
                        }
                    }

                    if(!found){
                        methods.add(method);
                        assertionTypes.add(assertionType);
                        values.add(value);
                    }
                }
            } else {

                for(Assertion assertion : assertions){

                    if(assertion instanceof InspectorAssertion){

                        method = ((InspectorAssertion) assertion).getInspector().getMethod();
                        assertionType = assertion.getClass();
                        value = assertion.getValue();

                        boolean found = false;

                        for(int j = 0; j < methods.size(); j++){
                            if(methods.get(j).equals(method)){
                                if(assertionTypes.get(j).equals(assertionType) && values.get(j).equals(value)){
                                    count++;
                                    found = true;
                                    break;
                                }
                            }
                        }

                        if(!found){
                            methods.add(method);
                            assertionTypes.add(assertionType);
                            values.add(value);
                        }
                    }
                }
            }
        }
        return count;
    }
}
