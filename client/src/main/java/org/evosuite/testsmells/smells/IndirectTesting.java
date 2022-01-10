package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsmells.AbstractTestSmell;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.generic.GenericConstructor;

import java.util.Set;
import java.util.LinkedHashSet;

public class IndirectTesting extends AbstractTestSmell {

    public IndirectTesting(String smellName) {
        super(smellName);
    }

    @Override
    public int obtainSmellCount(TestChromosome chromosome) {
        int size = chromosome.size();

        Statement currentStatement;
        GenericConstructor constructor;

        Set<GenericConstructor> setOfConstructors = new LinkedHashSet<>();

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);
            if(currentStatement instanceof ConstructorStatement){
                constructor = ((ConstructorStatement) currentStatement).getConstructor();
                setOfConstructors.add(constructor);
            }
        }
        return setOfConstructors.size();
    }

    @Override
    public int obtainSmellCount(TestSuiteChromosome chromosome) {
        int smellCount = 0;

        for(TestChromosome testcase : chromosome.getTestChromosomes()){
            smellCount += obtainSmellCount(testcase);
        }

        return smellCount;
    }
}
