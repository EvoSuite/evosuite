package org.evosuite.testsmells.smells;

import org.evosuite.Properties;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractTestSmell;
import org.evosuite.testsuite.TestSuiteChromosome;

import java.util.Set;

public class LackOfCohesionOfMethods extends AbstractTestSmell {

    public LackOfCohesionOfMethods() {
        super("TestSmellLackOfCohesionOfMethods");
    }

    @Override
    public int computeNumberOfSmells(TestSuiteChromosome chromosome) {
        int count = 0;
        String targetClass = Properties.TARGET_CLASS;

        for(TestChromosome testCase : chromosome.getTestChromosomes()){

            boolean contains = false;
            Set<Class<?>> accessedClasses = testCase.getTestCase().getAccessedClasses();

            for(Class<?> accessedClass : accessedClasses) {
                if(accessedClass.getCanonicalName().equals(targetClass)){
                    contains = true;
                    break;
                }
            }

            count += contains ? 0 : 1;
        }

        return count;
    }
}
