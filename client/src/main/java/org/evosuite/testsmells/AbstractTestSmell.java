package org.evosuite.testsmells;

import org.evosuite.testsuite.TestSuiteChromosome;

public abstract class AbstractTestSmell {

    private final String name;

    public AbstractTestSmell(String name){
        this.name = name;
    }

    /**
     * Obtain the name of the smell
     * @return String that corresponds to the name of the test smell
     */
    public String getName(){
        return name;
    }

    /**
     * Calculate the smell count for a given test suite
     * @param chromosome The test suite that will be analyzed
     * @return int with the total smell count
     */
    public abstract int computeNumberOfSmells(TestSuiteChromosome chromosome);
}
