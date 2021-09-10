/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.instrumentation.error;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.statistics.OutputVariable;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.statistics.backend.DebugStatisticsBackend;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;

import java.util.Map;

public class AbstractErrorBranchTest extends SystemTestBase {
    private TestSuiteChromosome runEvoSuite(Class<?> targetClass) {
        EvoSuite evosuite = new EvoSuite();

        String targetClassName = targetClass.getCanonicalName();

        Properties.TARGET_CLASS = targetClassName;
        Properties.ASSERTIONS = false;
        Properties.JUNIT_TESTS = false;
        Properties.JUNIT_CHECK = Properties.JUnitCheckValues.FALSE;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.BRANCH, Properties.Criterion.TRYCATCH};

        String[] command = new String[]{"-generateSuite", "-class", targetClassName};

        StringBuilder s = new StringBuilder();
        s.append(RuntimeVariable.Coverage);
        s.append(",");
        s.append(RuntimeVariable.BranchCoverage);
        s.append(",");
        s.append(RuntimeVariable.Covered_Goals);
        s.append(",");
        s.append(RuntimeVariable.Total_Goals);
        s.append(",");
        s.append(RuntimeVariable.Covered_Branches);
        s.append(",");
        s.append(RuntimeVariable.Covered_Branchless_Methods);
        s.append(",");
        s.append(RuntimeVariable.Covered_Branches_Real);
        s.append(",");
        s.append(RuntimeVariable.Covered_Branches_Instrumented);
        s.append(",");
        Properties.OUTPUT_VARIABLES = s.toString();

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);

        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println(best.toString());
        return best;
    }

    private void assertBranchStats(int realBranches, int instrumentedBranches, int coveredRealBranches, int coveredInstrumentedBranches) {
        Map<String, OutputVariable<?>> map = DebugStatisticsBackend.getLatestWritten();
        Assert.assertNotNull(map);

        OutputVariable<Double> coverage = (OutputVariable<Double>) map.get(RuntimeVariable.Coverage.toString());
        OutputVariable<Double> branchCoverage = (OutputVariable<Double>) map.get(RuntimeVariable.BranchCoverage.toString());

        OutputVariable<Integer> totalGoals = (OutputVariable<Integer>) map.get(RuntimeVariable.Total_Goals.toString());
        OutputVariable<Integer> coveredGoals = (OutputVariable<Integer>) map.get(RuntimeVariable.Covered_Goals.toString());

        OutputVariable<Integer> coveredBranches = (OutputVariable<Integer>) map.get(RuntimeVariable.Covered_Branches.toString());
        OutputVariable<Integer> coveredBranchlessMethods = (OutputVariable<Integer>) map.get(RuntimeVariable.Covered_Branchless_Methods.toString());
        OutputVariable<Integer> coveredBranchesReal = (OutputVariable<Integer>) map.get(RuntimeVariable.Covered_Branches_Real.toString());
        OutputVariable<Integer> coveredBranchesInstrumented = (OutputVariable<Integer>) map.get(RuntimeVariable.Covered_Branches_Instrumented.toString());

        double meanCoverage = 0.0;
        if (instrumentedBranches > 0) {
            meanCoverage += (double) coveredInstrumentedBranches / instrumentedBranches;
        } else {
            meanCoverage += 1.0;
        }
        if (realBranches > 0) {
            meanCoverage += (double) coveredRealBranches / realBranches;
        } else {
            meanCoverage += 1.0;
        }
        meanCoverage /= 2.0;

        Assert.assertEquals("Incorrect value for " + coverage, meanCoverage, coverage.getValue(), 0.0);
        Assert.assertEquals("Incorrect value for " + branchCoverage, (double) (coveredRealBranches) / (double) (realBranches), branchCoverage.getValue(), 0.0);
        Assert.assertEquals("Incorrect value for " + totalGoals, realBranches + instrumentedBranches, (int) totalGoals.getValue());
        Assert.assertEquals("Incorrect value for " + coveredGoals, coveredRealBranches + coveredInstrumentedBranches, (int) coveredGoals.getValue());
        Assert.assertEquals("Incorrect value for " + coveredBranches, coveredRealBranches + coveredInstrumentedBranches, coveredBranches.getValue() + coveredBranchlessMethods.getValue());
        Assert.assertEquals("Incorrect value for " + coveredBranchesInstrumented, coveredInstrumentedBranches, (int) coveredBranchesInstrumented.getValue());
        Assert.assertEquals("Incorrect value for " + coveredBranchesReal, coveredRealBranches, coveredBranchesReal.getValue() + coveredBranchlessMethods.getValue());
    }

    protected void checkErrorBranches(Class<?> targetClass, int realBranches, int instrumentedBranches, int coveredRealBranches, int coveredInstrumentedBranches) {
        TestSuiteChromosome best = runEvoSuite(targetClass);
        assertBranchStats(realBranches, instrumentedBranches, coveredRealBranches, coveredInstrumentedBranches);
        Assert.assertEquals(realBranches, TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size());
        Assert.assertEquals(instrumentedBranches, TestGenerationStrategy.getFitnessFactories().get(1).getCoverageGoals().size());
        double meanCoverage = 0.0;
        if (instrumentedBranches > 0) {
            meanCoverage += (double) coveredInstrumentedBranches / instrumentedBranches;
        } else {
            meanCoverage += 1.0;
        }
        if (realBranches > 0) {
            meanCoverage += (double) coveredRealBranches / realBranches;
        } else {
            meanCoverage += 1.0;
        }
        meanCoverage /= 2.0;
        Assert.assertEquals("Non-optimal coverage: ", meanCoverage, best.getCoverage(), 0.001);
    }

}
