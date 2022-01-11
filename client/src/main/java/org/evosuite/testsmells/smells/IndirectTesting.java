package org.evosuite.testsmells.smells;

import org.evosuite.Properties;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsmells.AbstractTestSmell;
import org.evosuite.utils.generic.GenericConstructor;

public class IndirectTesting extends AbstractTestSmell {

    public IndirectTesting(String smellName) {
        super(smellName);
    }

    @Override
    public int obtainSmellCount(TestChromosome chromosome) {
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
