package org.evosuite.ga;

import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;

import java.util.Collection;
import java.util.List;

public abstract class NoveltyFunction<T extends Chromosome> {


    public abstract void calculateNovelty(Collection<T> population, Collection<T> noveltyArchive, List<String> uncoveredMethodList);

    public abstract void sortPopulation(List<T> population);

    private ExecutionResult runTest(TestCase test) {
        return TestCaseExecutor.runTest(test);
    }

    protected ExecutionResult getExecutionResult(TestChromosome individual) {
        ExecutionResult origResult = individual.getLastExecutionResult();
        if (origResult == null || individual.isChanged()) {
            origResult = runTest(individual.getTestCase());
            individual.setLastExecutionResult(origResult);
            individual.setChanged(false);
        }
        return individual.getLastExecutionResult();
    }
}
