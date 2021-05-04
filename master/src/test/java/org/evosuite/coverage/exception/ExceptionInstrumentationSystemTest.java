/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.coverage.exception;

import com.examples.with.different.packagename.exception.*;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.statistics.OutputVariable;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.statistics.backend.DebugStatisticsBackend;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Created by gordon on 17/03/2016.
 */
public class ExceptionInstrumentationSystemTest extends SystemTestBase {

    private static final Properties.Criterion[] defaultCriterion = Properties.CRITERION;

    private static final boolean defaultArchive = Properties.TEST_ARCHIVE;

    @After
    public void resetProperties() {
        Properties.CRITERION = defaultCriterion;
    }

    public void checkCoverageGoals(Class<?> classUnderTest, int branchGoals, int exceptionGoals, int skippedBranches) {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = classUnderTest.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.BRANCH, Properties.Criterion.TRYCATCH};
        Properties.EXCEPTION_BRANCHES = true;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        StringBuilder s = new StringBuilder();
        s.append(RuntimeVariable.TryCatchCoverage);
        Properties.OUTPUT_VARIABLES = s.toString();

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        Assert.assertEquals(branchGoals, TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size());
        Assert.assertEquals(exceptionGoals, TestGenerationStrategy.getFitnessFactories().get(1).getCoverageGoals().size());

        Map<String, OutputVariable<?>> map = DebugStatisticsBackend.getLatestWritten();
        Assert.assertNotNull(map);
        OutputVariable<Double> tryCatchCoverage = (OutputVariable<Double>) map.get(RuntimeVariable.TryCatchCoverage.toString());

        double branchCoverageFitness = 1; // Since all the branches are always covered
        double tryCatchCoverageFitness = ((double) (exceptionGoals - skippedBranches) / (double) exceptionGoals);
        double coverage = (branchCoverageFitness + tryCatchCoverageFitness) * 0.5;

//        Assert.assertEquals("Non-optimal TryCatchCoverage: ", 1, tryCatchCoverage.getValue(), 0.001);
        Assert.assertEquals("Non-optimal coverage: ", coverage, best.getCoverage(), 0.001);
    }


    @Test
    public void testCheckedExceptionBranchesOneThrow() {
        // # branches == 0
        // # branchless methods == 1 (<init>)
        // # additional branches: 4 (FileNotFoundException true/false, RuntimeException true/false)
        // Passing assert
        // checkCoverageGoals(SimpleTryCatch.class, 2, 4, 1);
        // Failing assert
        checkCoverageGoals(SimpleTryCatch.class, 2, 3, 0);
    }

    @Test
    public void testCheckedExceptionBranchesTwoThrows() {
        // Passing assert
        // checkCoverageGoals(SimpleTry2Catches.class, 2, 6, 2);
        // Failing assert
        checkCoverageGoals(SimpleTry2Catches.class, 2, 5, 1);
    }

    @Test
    public void testReThrownCheckedExceptionBranchesTwoThrows() {
        // Passing assert
        // checkCoverageGoals(Rethrow2Exceptions.class, 2, 6, 2);
        // Failing assert
        checkCoverageGoals(Rethrow2Exceptions.class, 2, 5, 1);

    }


    @Test
    public void testReThrownCheckedAndUncheckedExceptionBranchesTwoThrows() {
        // Runtime Exception is thrown hence 5/6 TryCatchCoverage fitness value.
        // Passing assert
        // checkCoverageGoals(Rethrow2ExceptionsAndUncheckedException.class, 2, 6, 1);
        // Failing assert
        checkCoverageGoals(Rethrow2ExceptionsAndUncheckedException.class, 2, 5, 0);
    }

    @Test
    public void testReThrownCheckedAndErrorBranches() {
        Properties.ERROR_BRANCHES = true;
        // The NPE caused by "foo" being null is now caught outside the exception instrumentation
        // and thus represents a different coverage goal than a RuntimeException thrown _in_ foo.
        // Hence we now only cover 8/9 goals.
        // Failing assert - skipped branch = 1 since the runtime_exception's true branch would be false.
        checkCoverageGoals(Rethrow2ExceptionsAndUncheckedException.class, 2, 7, 1);
    }

    @Test
    public void testCatchWithUnknownThrow() {
        checkCoverageGoals(CatchWithUnknownThrow.class, 2, 3, 0);
    }

}
