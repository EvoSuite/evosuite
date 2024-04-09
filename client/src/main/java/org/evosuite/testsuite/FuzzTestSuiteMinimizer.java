package org.evosuite.testsuite;

import org.evosuite.Properties;
import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.junit.writer.TestSuiteWriter;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;

import java.util.*;
import java.util.stream.Collectors;

public class FuzzTestSuiteMinimizer extends TestSuiteMinimizer {

    private boolean minimizeSuiteOnly = false;
    private int timeBudget = Properties.FUZZ_MINIMIZATION_TIMEOUT;

    public FuzzTestSuiteMinimizer(TestFitnessFactory<?> factory) {
        super(factory);
    }

    public FuzzTestSuiteMinimizer(List<TestFitnessFactory<? extends TestFitnessFunction>> factories) {
        super(factories);
    }

    public FuzzTestSuiteMinimizer(List<TestFitnessFactory<? extends TestFitnessFunction>> factories,
                                  boolean minimizeSuiteOnly) {
        super(factories);
        this.minimizeSuiteOnly = minimizeSuiteOnly;
    }

    public FuzzTestSuiteMinimizer(List<TestFitnessFactory<? extends TestFitnessFunction>> factories,
                                  int timeBudget) {
        super(factories);
        this.timeBudget = timeBudget;
    }


    @Override
    protected void updateClientStatus(int progress) {
        return;
    }

    @Override
    protected void updateCoverageProgressStatus(double suiteCoverage) {
        return;
    }

    @Override
    protected boolean isTimeoutReached() {
        return (System.currentTimeMillis() - startTime) / 1000 > timeBudget;
    }

    @Override
    protected TestChromosome minimizeTestCase(TestChromosome test, TestFitnessFunction goal) {
        if(minimizeSuiteOnly) {
            return test.clone();
        }else {
            Properties.MINIMIZE = true;
            TestChromosome copy = super.minimizeTestCase(test, goal);
            Properties.MINIMIZE = false;
            return copy;
        }
    }


    //Note: this is incomplete implementation
    public List<Integer> getMinimumTestIndices(TestSuiteChromosome suite) {
        List<TestFitnessFunction> goals = new ArrayList<>();
        for (TestFitnessFactory<?> ff : testFitnessFactories) {
            goals.addAll(ff.getCoverageGoals());
        }

        if (Properties.MINIMIZE_SORT)
            Collections.sort(goals);

        Set<TestFitnessFunction> covered = new LinkedHashSet<>();
        List<TestChromosome> minimizedTests = new ArrayList<>();
        TestSuiteWriter minimizedSuite = new TestSuiteWriter();

        for (TestFitnessFunction goal : goals) {
            if (isTimeoutReached()) {
                return null;
            }
            logger.info("Considering goal: " + goal);
            if (Properties.MINIMIZE_SKIP_COINCIDENTAL) {
                for(TestChromosome test : minimizedTests) {
                    if (isTimeoutReached()) {
                        return null;
                    }
                    if (goal.isCovered(test)) {
                        covered.add(goal);
                        break;
                    }
                }
            }
            if (covered.contains(goal)) {
                continue;
            }

            List<TestChromosome> coveringTests = new ArrayList<>();
            for (TestChromosome test : suite.getTestChromosomes()) {
                if (goal.isCovered(test)) {
                    coveringTests.add(test);
                }
            }

            if (!coveringTests.isEmpty()) {
                Collections.sort(coveringTests);
                TestChromosome test = coveringTests.get(0);
                for (TestFitnessFunction g : goals) {
                    if (g.isCovered(test)) { // isCovered(copy) adds the goal
                        covered.add(g);
                    }
                }
                minimizedTests.add(test);
            }
        }

//        TestSuiteChromosome minimizedSuite = new TestSuiteChromosome();
//        minimizedSuite.addTests(minimizedTests);
//        if(Properties.MINIMIZE_SECOND_PASS) {
//            removeRedundantTestCases(minimizedSuite, goals);
//        }

        if(minimizedTests.size() > 0) {
            Set<Integer> minimizedTestIDs = new HashSet<>(minimizedTests
                    .stream()
                    .map(t -> t.getTestCase().getID())
                    .collect(Collectors.toSet()));
            List<Integer> minimizedTestIndices = new ArrayList<>();
            for(int i=0; i<suite.size(); ++i) {
                TestChromosome test = suite.getTestChromosome(i);
                if(minimizedTestIDs.contains(test.getTestCase().getID())) {
                    minimizedTestIndices.add(i);
                }
            }
            return minimizedTestIndices;
        }else{
            return null;
        }
    }
}
