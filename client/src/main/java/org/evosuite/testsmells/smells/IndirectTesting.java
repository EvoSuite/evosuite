package org.evosuite.testsmells.smells;

import org.evosuite.Properties;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsmells.AbstractTestCaseSmell;
import org.evosuite.utils.generic.GenericConstructor;

public class IndirectTesting extends AbstractTestCaseSmell {

    public IndirectTesting() {
        super("TestSmellIndirectTesting");
    }

    @Override
    public int computeNumberOfSmells(TestChromosome chromosome) {
        int smellCount = 0;

        int size = chromosome.size();

        Statement currentStatement;
        GenericConstructor constructor;

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);
            if(currentStatement instanceof ConstructorStatement){
                constructor = ((ConstructorStatement) currentStatement).getConstructor();
                if(!constructor.getDeclaringClass().equals(Properties.getTargetClassAndDontInitialise())){
                    smellCount++;
                }
            }
        }
        return smellCount;
    }
}
