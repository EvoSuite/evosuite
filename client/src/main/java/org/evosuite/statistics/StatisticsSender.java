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
package org.evosuite.statistics;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.coverage.exception.ExceptionCoverageSuiteFitness;
import org.evosuite.ga.Chromosome;
import org.evosuite.rmi.ClientServices;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.ExecutionTrace;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testsuite.TestSuiteChromosome;

import java.util.*;

/**
 * Class responsible to send "individuals" from Client to Master process.
 * All sending of individuals should go through this class, and not
 * calling ClientServices directly
 *
 * <p>
 * TODO: still to clarify what type of extra information we want to send with each individual,
 * eg the state in which it was computed (Search vs Minimization)
 *
 * @author arcuri
 */
public class StatisticsSender {

    /**
     * Send the given individual to the Client, plus any other needed info
     *
     * @param individual
     */
    public static <T extends Chromosome<T>> void sendIndividualToMaster(T individual) throws IllegalArgumentException {
        if (individual == null) {
            throw new IllegalArgumentException("No defined individual to send");
        }
        if (!Properties.NEW_STATISTICS)
            return;

        ClientServices.<T>getInstance().getClientNode().updateStatistics(individual);
    }


    /**
     * First execute (if needed) the test cases to be sure to have latest correct data,
     * and then send it to Master
     */
    public static void executedAndThenSendIndividualToMaster(TestSuiteChromosome testSuite) throws IllegalArgumentException {
        if (testSuite == null) {
            throw new IllegalArgumentException("No defined test suite to send");
        }
        if (!Properties.NEW_STATISTICS)
            return;

        /*
         * TODO: shouldn't a test that was never executed always be executed before sending?
         * ie, do we really need a separated public sendIndividualToMaster???
         */

        for (TestChromosome test : testSuite.getTestChromosomes()) {
            if (test.getLastExecutionResult() == null) {
                ExecutionResult result = TestCaseExecutor.runTest(test.getTestCase());
                test.setLastExecutionResult(result);
            }
        }

        sendCoveredInfo(testSuite);
        sendExceptionInfo(testSuite);
        sendIndividualToMaster(testSuite);
    }

    // -------- private methods ------------------------

    private static void sendExceptionInfo(TestSuiteChromosome testSuite) {

        List<ExecutionResult> results = new ArrayList<>();

        for (TestChromosome testChromosome : testSuite.getTestChromosomes()) {
            results.add(testChromosome.getLastExecutionResult());
        }

        /*
         * for each method name, check the class of thrown exceptions in those methods
         */
        Map<String, Set<Class<?>>> implicitTypesOfExceptions = new HashMap<>();
        Map<String, Set<Class<?>>> explicitTypesOfExceptions = new HashMap<>();
        Map<String, Set<Class<?>>> declaredTypesOfExceptions = new HashMap<>();

        ExceptionCoverageSuiteFitness.calculateExceptionInfo(results, implicitTypesOfExceptions, explicitTypesOfExceptions, declaredTypesOfExceptions, null);

        ClientServices.getInstance().getClientNode().trackOutputVariable(
                RuntimeVariable.Explicit_MethodExceptions, ExceptionCoverageSuiteFitness.getNumExceptions(explicitTypesOfExceptions));
        ClientServices.getInstance().getClientNode().trackOutputVariable(
                RuntimeVariable.Explicit_TypeExceptions, ExceptionCoverageSuiteFitness.getNumClassExceptions(explicitTypesOfExceptions));
        ClientServices.getInstance().getClientNode().trackOutputVariable(
                RuntimeVariable.Implicit_MethodExceptions, ExceptionCoverageSuiteFitness.getNumExceptions(implicitTypesOfExceptions));
        ClientServices.getInstance().getClientNode().trackOutputVariable(
                RuntimeVariable.Implicit_TypeExceptions, ExceptionCoverageSuiteFitness.getNumClassExceptions(implicitTypesOfExceptions));

        /*
         * NOTE: in old report generator, we were using Properties.SAVE_ALL_DATA
         * to check if writing the full explicitTypesOfExceptions and implicitTypesOfExceptions
         */
    }


    private static void sendCoveredInfo(TestSuiteChromosome testSuite) {

        Set<String> coveredMethods = new HashSet<>();
        Set<Integer> coveredTrueBranches = new HashSet<>();
        Set<Integer> coveredFalseBranches = new HashSet<>();
        Set<String> coveredBranchlessMethods = new HashSet<>();
        Set<Integer> coveredLines = new HashSet<>();

        for (TestChromosome test : testSuite.getTestChromosomes()) {
            ExecutionTrace trace = test.getLastExecutionResult().getTrace();
            coveredMethods.addAll(trace.getCoveredMethods());
            coveredTrueBranches.addAll(trace.getCoveredTrueBranches());
            coveredFalseBranches.addAll(trace.getCoveredFalseBranches());
            coveredBranchlessMethods.addAll(trace.getCoveredBranchlessMethods());
            coveredLines.addAll(trace.getCoveredLines());
        }

        int coveredBranchesInstrumented = 0;
        int coveredBranchesReal = 0;
        if (Properties.ERROR_BRANCHES || Properties.EXCEPTION_BRANCHES) {
            BranchPool branchPool = BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT());
            for (Integer branchId : coveredTrueBranches) {
                Branch b = branchPool.getBranch(branchId);
                if (b.isInstrumented())
                    coveredBranchesInstrumented++;
                else {
                    coveredBranchesReal++;
                }
            }
            for (Integer branchId : coveredFalseBranches) {
                Branch b = branchPool.getBranch(branchId);
                if (b.isInstrumented())
                    coveredBranchesInstrumented++;
                else {
                    coveredBranchesReal++;
                }
            }
        } else {
            coveredBranchesReal = coveredTrueBranches.size() + coveredFalseBranches.size();
        }

        ClientServices.getInstance().getClientNode().trackOutputVariable(
                RuntimeVariable.Covered_Goals, testSuite.getCoveredGoals().size());
        ClientServices.getInstance().getClientNode().trackOutputVariable(
                RuntimeVariable.Covered_Methods, coveredMethods.size());
        ClientServices.getInstance().getClientNode().trackOutputVariable(
                RuntimeVariable.Covered_Branches, coveredTrueBranches.size() + coveredFalseBranches.size());
        ClientServices.getInstance().getClientNode().trackOutputVariable(
                RuntimeVariable.Covered_Branchless_Methods, coveredBranchlessMethods.size());
        ClientServices.getInstance().getClientNode().trackOutputVariable(
                RuntimeVariable.Covered_Branches_Real, coveredBranchesReal);
        ClientServices.getInstance().getClientNode().trackOutputVariable(
                RuntimeVariable.Covered_Branches_Instrumented, coveredBranchesInstrumented);
        ClientServices.getInstance().getClientNode().trackOutputVariable(
                RuntimeVariable.Covered_Lines, coveredLines.size());
    }
}
