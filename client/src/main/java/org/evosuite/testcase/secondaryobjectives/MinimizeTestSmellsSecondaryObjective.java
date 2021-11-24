package org.evosuite.testcase.secondaryobjectives;

import org.evosuite.ga.SecondaryObjective;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.utils.generic.GenericMethod;
import org.evosuite.testcase.statements.*;

import java.util.ArrayList;
import java.util.List;

public class MinimizeTestSmellsSecondaryObjective extends SecondaryObjective<TestChromosome> {

    @Override
    public int compareChromosomes(TestChromosome chromosome1, TestChromosome chromosome2) {
        // Reminder: it may be a good idea to define thresholds on a dedicated file
        return 0;
    }

    @Override
    public int compareGenerations(TestChromosome parent1, TestChromosome parent2, TestChromosome child1, TestChromosome child2) {
        return 0;
    }

    /**
     * Verify if a test case checks multiple methods of the class to be tested
     * @param chromosome - the test case
     * @return an int containing the number of different production methods checked by the test case
     */
    private int eagerTest(TestChromosome chromosome){
        int size = chromosome.size();
        int eagerCount = 0;

        Statement currentStatement;
        GenericMethod method;

        List<GenericMethod> listOfMethods = new ArrayList<>();

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);
            if(currentStatement instanceof MethodStatement){
                method = ((MethodStatement) currentStatement).getMethod();
                if(listOfMethods.contains(method)){
                    eagerCount++;
                }else {
                    listOfMethods.add(method);
                }
            }
        }
        return eagerCount;
    }


    private int verboseTest(TestChromosome chromosome){

        // This method is likely to be deleted in the future due to its simplicity
        // For the time being, it will remain in a separate method for organizational reasons

        return chromosome.size();
    }

    // Indirect Testing

    // Obscure In-line Setup

    // Slow tests

    // Test Code Duplication
}
