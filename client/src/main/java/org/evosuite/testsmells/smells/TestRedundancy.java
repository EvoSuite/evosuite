package org.evosuite.testsmells.smells;

import org.evosuite.ga.FitnessFunction;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractTestSmell;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.LoggingUtils;

import java.util.List;
import java.util.Map;

public class TestRedundancy extends AbstractTestSmell {

    public TestRedundancy() {
        super("TestSmellTestRedundancy");
    }

    @Override
    public int computeNumberOfSmells(TestSuiteChromosome chromosome) {
        int count = 0;

        int size = chromosome.size();
        double initialFitness = chromosome.getFitness();
        Map<FitnessFunction<TestSuiteChromosome>, Double> fitnessValues = chromosome.getFitnessValues();

        TestSuiteChromosome copy;
        TestSuiteChromosome currentTestSuite;
        List<TestChromosome> currentTestSuiteList;

        for (int i = 0; i < size; i++){

            copy = chromosome.clone();
            currentTestSuite = new TestSuiteChromosome();
            currentTestSuiteList = copy.getTestChromosomes();
            currentTestSuiteList.remove(i);
            currentTestSuite.getTestChromosomes().addAll(currentTestSuiteList);

            for (FitnessFunction<TestSuiteChromosome> fitnessValue : fitnessValues.keySet()){
                currentTestSuite.getFitness(fitnessValue);
            }

            double newFitness = currentTestSuite.getFitness();

            if(newFitness < initialFitness){
                LoggingUtils.getEvoLogger().error("New fitness: " +  newFitness + " is better than the initial one: " + initialFitness + " this should never occur!");
            }

            if(Math.abs(initialFitness - newFitness) < 0.000001){
                count++;
            }

        }
        return count;
    }
}
