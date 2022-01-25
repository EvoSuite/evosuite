package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsmells.AbstractTestSmell;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.generic.GenericMethod;

import java.util.LinkedHashMap;

public class LazyTest extends AbstractTestSmell {

    public LazyTest() {
        super("TestSmellLazyTest");
    }

    @Override
    public int computeNumberOfSmells(TestSuiteChromosome chromosome) {
        Statement currentStatement;
        int count = 0;

        LinkedHashMap<GenericMethod, TestChromosome> methodsCalledByTestCases = new LinkedHashMap<>();

        for(TestChromosome testCase : chromosome.getTestChromosomes()){

            int size = testCase.size();

            for (int i = 0; i < size; i++){
                currentStatement = testCase.getTestCase().getStatement(i);

                if(currentStatement instanceof MethodStatement){

                    GenericMethod method = ((MethodStatement) currentStatement).getMethod();

                    //Verify if a different test case tests the same method
                    if(methodsCalledByTestCases.containsKey(method) && !methodsCalledByTestCases.get(method).equals(testCase)){
                        count++;
                    }else{
                        methodsCalledByTestCases.put(method, testCase);
                    }
                }
            }
        }

        return count;
    }
}
