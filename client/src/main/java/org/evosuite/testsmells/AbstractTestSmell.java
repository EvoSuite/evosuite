package org.evosuite.testsmells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;

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
     * Calculate the smell count for a given test case
     * @param chromosome The test case that will be analyzed
     * @return int with the total smell count
     */
    public abstract int obtainSmellCount (TestChromosome chromosome);

    /**
     * Calculate the smell count for a given test suite
     * @param chromosome The test suite that will be analyzed
     * @return int with the total smell count
     */
    public int obtainSmellCount (TestSuiteChromosome chromosome){
        int smellCount = 0;

        for(TestChromosome testcase : chromosome.getTestChromosomes()){
            smellCount += obtainSmellCount(testcase);
        }

        return smellCount;
    }

}
