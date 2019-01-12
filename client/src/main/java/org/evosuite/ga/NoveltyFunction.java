package org.evosuite.ga;

import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;

import java.util.Collection;
import java.util.List;

public abstract class NoveltyFunction<T extends Chromosome> {

    /*public abstract double getDistance(T individual1, T individual2);*/

    /*public double getNovelty(T individual, Collection<T> population) {
        double distance = 0.0;
        for(T other : population) {
            if(other == individual)
                continue;

            // this causes the distance vector to be stored in he 'other' individual
            // returns the euclidean distance from the distance vector
            double d = getDistance(individual, other);
            distance += d;
        }

        distance /= (population.size() - 1);

        return distance;
    }*/

    public abstract void calculateNovelty(Collection<T> population);
    public abstract void sortPopulation(List<T> population);

    private ExecutionResult runTest(TestCase test) {
        return TestCaseExecutor.runTest(test);
    }

    protected ExecutionResult getExecutionResult(TestChromosome individual) {
        ExecutionResult origResult = individual.getLastExecutionResult();
        if(origResult == null||individual.isChanged()) {
            origResult = runTest(individual.getTestCase());
            individual.setLastExecutionResult(origResult);
            individual.setChanged(false);
        }
        return individual.getLastExecutionResult();
    }
}
