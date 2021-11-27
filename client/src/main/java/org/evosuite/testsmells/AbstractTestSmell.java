package org.evosuite.testsmells;

import org.evosuite.testcase.TestChromosome;

public abstract class AbstractTestSmell {

    private String smellName;

    public AbstractTestSmell (String smellName){
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
     * Calculate the smell count for a certain test
     * @param chromosome The test that will be analyzed
     * @return int with the total smell count
     */
    public abstract int obtainSmellCount (TestChromosome chromosome);
    
}
