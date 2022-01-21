package org.evosuite.testsmells.smells;

import org.evosuite.Properties;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsmells.AbstractTestCaseSmell;
import org.evosuite.utils.generic.GenericMethod;

import java.util.LinkedHashSet;
import java.util.Set;

public class EagerTest extends AbstractTestCaseSmell {

    public EagerTest() {
        super("EagerTest");
    }

    @Override
    public int computeNumberOfSmells(TestChromosome chromosome) {
        int size = chromosome.size();

        Statement currentStatement;
        GenericMethod method;

        Set<GenericMethod> listOfMethods = new LinkedHashSet<>();

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);
            if(currentStatement instanceof MethodStatement){
                method = ((MethodStatement) currentStatement).getMethod();
                if(method.getDeclaringClass().equals(Properties.getTargetClassAndDontInitialise())){
                    listOfMethods.add(method);
                }
            }
        }
        return listOfMethods.size();
    }
}
