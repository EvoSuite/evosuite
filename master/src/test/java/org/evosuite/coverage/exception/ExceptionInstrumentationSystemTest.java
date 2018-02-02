/**
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
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by gordon on 17/03/2016.
 */
public class ExceptionInstrumentationSystemTest extends SystemTestBase {

    private static final Properties.Criterion[] defaultCriterion = Properties.CRITERION;

    private static boolean defaultArchive = Properties.TEST_ARCHIVE;

    @After
    public void resetProperties() {
        Properties.CRITERION = defaultCriterion;
    }

    public void checkCoverageGoals(Class<?> classUnderTest, int branchGoals, int exceptionGoals, int expectedCoveredGoals) {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = classUnderTest.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.BRANCH, Properties.Criterion.TRYCATCH};
        Properties.EXCEPTION_BRANCHES = true;

        String[] command = new String[] { "-generateSuite", "-class", targetClass };

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        Assert.assertEquals(branchGoals, TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size());
        Assert.assertEquals(exceptionGoals, TestGenerationStrategy.getFitnessFactories().get(1).getCoverageGoals().size());
        Assert.assertEquals("Non-optimal coverage: ", (double)expectedCoveredGoals/(double)(branchGoals + exceptionGoals), best.getCoverage(), 0.001);
    }


    @Test
    public void testCheckedExceptionBranchesOneThrow() {
        // # branches == 0
        // # branchless methods == 1 (<init>)
        // # additional branches: 4 (FileNotFoundException true/false, RuntimeException true/false)
        checkCoverageGoals(SimpleTryCatch.class, 2, 2, 4);
    }

    @Test
    public void testCheckedExceptionBranchesTwoThrows() {
        checkCoverageGoals(SimpleTry2Catches.class, 2, 3, 5);
    }

    @Test
    public void testReThrownCheckedExceptionBranchesTwoThrows() {
        checkCoverageGoals(Rethrow2Exceptions.class, 2, 3, 5);
    }

    @Test
    public void testReThrownCheckedAndUncheckedExceptionBranchesTwoThrows() {
        checkCoverageGoals(Rethrow2ExceptionsAndUncheckedException.class, 2, 3, 5);
    }

    @Test
    public void testReThrownCheckedAndErrorBranches() {
        Properties.ERROR_BRANCHES = true;
        // The NPE caused by "foo" being null is now caught outside the exception instrumentation
        // and thus represents a different coverage goal than a RuntimeException thrown _in_ foo.
        // Hence we now only cover 8/9 goals.
        checkCoverageGoals(Rethrow2ExceptionsAndUncheckedException.class, 2, 4, 6);
    }

    @Test
    public void testCatchWithUnknownThrow() {
        checkCoverageGoals(CatchWithUnknownThrow.class, 2, 2, 4);
    }

}
