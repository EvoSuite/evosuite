package org.evosuite.testsmells.smells;

import org.evosuite.Properties;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsmells.AbstractTestSmell;

public class SensitiveEquality extends AbstractTestSmell {

    public SensitiveEquality(String smellName) {
        super(smellName);
    }

    @Override
    public int obtainSmellCount(TestChromosome chromosome) {
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
        }

        return count;
    }
}
