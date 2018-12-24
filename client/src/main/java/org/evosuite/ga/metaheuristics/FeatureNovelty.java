package org.evosuite.ga.metaheuristics;

import org.evosuite.coverage.dataflow.Feature;
import org.evosuite.ga.Chromosome;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.ExecutionTrace;
import org.evosuite.testcase.execution.TestCaseExecutor;

import java.util.List;
import java.util.Map;

public class FeatureNovelty implements NoveltyMetric {

    private ExecutionResult runTest(TestCase test) {
        return TestCaseExecutor.runTest(test);
    }

    private ExecutionResult getExecutionResult(TestChromosome individual) {
        ExecutionResult origResult = individual.getLastExecutionResult();
        if(origResult == null||individual.isChanged()) {
            origResult = runTest(individual.getTestCase());
            individual.setLastExecutionResult(origResult);
            individual.setChanged(false);
        }
        return individual.getLastExecutionResult();
    }

    @Override
    public double calculateDistance(TestChromosome a, TestChromosome b) {
        ExecutionResult result1 = getExecutionResult((TestChromosome)a);
        ExecutionTrace trace1 = result1.getTrace();
        Map<Integer, Feature> featureMap1  = trace1.getVisitedFeaturesMap();
        trace1.updateFeatureObjectLink(a.getTestCase().getID(), featureMap1);


        ExecutionResult result2 = getExecutionResult((TestChromosome)b);
        ExecutionTrace trace2 = result2.getTrace();
        Map<Integer, Feature> featureMap2  = trace2.getVisitedFeaturesMap();
        trace2.updateFeatureObjectLink(b.getTestCase().getID(), featureMap2);
        double difference = 0.0;




        for (Map.Entry<Integer, Feature> entry : featureMap1.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
            difference  = getDistance(featureMap1.get(entry.getKey()), featureMap2.get(entry.getKey()));
        }

        return difference;
    }

    @Override
    public void sortPopulation(List<TestChromosome> population) {

    }
    private double getDistance(Feature feature1, Feature feature2){
        return Math.abs((Integer)feature1.getValue() - (Integer)feature2.getValue());
    }
}
