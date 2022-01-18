package org.evosuite.testsmells;

import org.evosuite.testsuite.TestSuiteChromosome;

public abstract class AbstractTestSuiteSmell {

    private String smellName;

    /**
     * Set the name of the smell
     * @param smellName The name of the test smell
     */
    protected void setSmellName(String smellName){
        this.smellName = smellName;
    }

    /**
     * Obtain the name of the smell
     * @return String that corresponds to the name of the test smell
     */
    public String getSmellName (){
        return smellName;
    }

    /**
     * Calculate the smell count for a given test suite
     * @param chromosome The test suite that will be analyzed
     * @return int with the total smell count
     */
    public abstract int obtainSmellCount (TestSuiteChromosome chromosome);
}
