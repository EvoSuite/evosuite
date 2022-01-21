package org.evosuite.testsmells.smells;

import org.evosuite.Properties;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractTestSmell;
import org.evosuite.testsuite.TestSuiteChromosome;

public class LackOfCohesionOfMethods extends AbstractTestSmell {

    public LackOfCohesionOfMethods() {
        super("LackOfCohesionOfMethods");
    }

    @Override
    public int computeNumberOfSmells(TestSuiteChromosome chromosome) {
        int count = 0;
        Class<?> targetClass = Properties.getTargetClassAndDontInitialise();

        for(TestChromosome testCase : chromosome.getTestChromosomes()){
            if(!testCase.getTestCase().getAccessedClasses().contains(targetClass)){
                count++;
            }
        }

        return count;
    }
}
