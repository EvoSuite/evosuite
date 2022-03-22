package org.evosuite.testsmells.smells;

import org.evosuite.Properties;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testsmells.AbstractTestCaseSmell;
import org.evosuite.testcase.statements.Statement;

import java.lang.reflect.Method;

public class UnusedInputs extends AbstractTestCaseSmell {

    public UnusedInputs() {
        super("TestSmellUnusedInputs");
    }

    @Override
    public int computeNumberOfSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        int count = 0;

        Statement currentStatement;

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);

            if(currentStatement instanceof MethodStatement){

                Method method = ((MethodStatement) currentStatement).getMethod().getMethod();
                String typeName = method.getGenericReturnType().getTypeName();

                if(method.getDeclaringClass().getCanonicalName().equals(Properties.TARGET_CLASS) && !typeName.equals("void")){
                    count += currentStatement.hasAssertions() ? 0 : 1;
                }
            }
        }
        return count;
    }
}
