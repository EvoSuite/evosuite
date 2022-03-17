package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsmells.AbstractTestSmell;
import org.evosuite.testsuite.TestSuiteChromosome;

import java.util.List;
import java.util.Set;

public class TestRedundancy extends AbstractTestSmell {

    public TestRedundancy() {
        super("TestSmellTestRedundancy");
    }

    @Override
    public int computeNumberOfSmells(TestSuiteChromosome chromosome) {
        int count = 0;

        int size = chromosome.size();

        TestSuiteChromosome currentTestSuite;
        List<TestChromosome> currentTestSuiteList;

        Set<TestFitnessFunction> goals = chromosome.getCoveredGoals();

        for (int i = 0; i < size; i++){

            currentTestSuite = chromosome.clone();
            currentTestSuiteList = currentTestSuite.getTestChromosomes();
            currentTestSuite.deleteTest(currentTestSuiteList.get(i));

            if(currentTestSuite.getCoveredGoals().size() == goals.size()){
                count++;
            }
        }
        return count;
    }
}
