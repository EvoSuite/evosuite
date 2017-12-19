package org.evosuite.novelty;

import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.ga.NoveltyFunction;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.ExecutionTrace;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Set;

public class BranchNoveltyFunction extends NoveltyFunction<TestChromosome> {

    private static final Logger logger = LoggerFactory.getLogger(BranchNoveltyFunction.class);

    private Set<Integer> branches = new LinkedHashSet<>();

    private Set<String> branchlessMethods = new LinkedHashSet<>();

    public BranchNoveltyFunction() {
        for (Branch branch : BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getAllBranches()) {
            if(!branch.isInstrumented()) {
                branches.add(branch.getActualBranchId());
            }
        }
        branchlessMethods.addAll(BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getBranchlessMethods());
        logger.warn("Number of branches: "+branches.size()+" branches and "+branchlessMethods.size() +" branchless methods");
    }

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
    public double getDistance(TestChromosome individual1, TestChromosome individual2) {
        ExecutionResult result1 = getExecutionResult(individual1);
        ExecutionResult result2 = getExecutionResult(individual2);

        ExecutionTrace trace1 = result1.getTrace();
        ExecutionTrace trace2 = result2.getTrace();

        double difference = 0.0;

        for(Integer branch : branches) {
            if(trace1.hasTrueDistance(branch) && trace2.hasTrueDistance(branch)) {
                double distance1 = trace1.getTrueDistance(branch);
                double distance2 = trace2.getTrueDistance(branch);

                difference += Math.abs(distance1 - distance2);

            } else if(trace1.hasTrueDistance(branch) || trace2.hasTrueDistance(branch)) {
                difference += 1.0;
            }
        }

        Set<String> methods1 = trace1.getCoveredBranchlessMethods();
        Set<String> methods2 = trace2.getCoveredBranchlessMethods();
        for(String branchlessMethod : branchlessMethods) {
            if(methods1.contains(branchlessMethod) != methods2.contains(branchlessMethod)) {
                difference += 1.0;
            }
        }

        difference /= (branches.size() + branchlessMethods.size());

        return difference;
    }

}
