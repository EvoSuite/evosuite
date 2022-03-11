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
package org.evosuite.strategy;

import org.evosuite.Properties;
import org.evosuite.coverage.FitnessFunctions;
import org.evosuite.coverage.rho.RhoCoverageFactory;
import org.evosuite.coverage.rho.RhoCoverageTestFitness;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.result.TestGenerationResultBuilder;
import org.evosuite.rmi.ClientServices;
import org.evosuite.rmi.service.ClientState;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.ExecutionTrace;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author José Campos
 * @inproceedings{Campos:2013, author = {Campos, Jos{\'e} and Abreu, Rui and Fraser, Gordon and d'Amorim, Marcelo},
 * title = {{Entropy-Based Test Generation for Improved Fault Localization}},
 * booktitle = {Proceedings of the 28th IEEE/ACM International Conference on
 * Automated Software Engineering},
 * series = {ASE 2013},
 * year = {2013},
 * isbn = {978-1-4799-0215-6},
 * location = {Palo Alto, USA},
 * pages = {257--267},
 * numpages = {10},
 * url = {},
 * doi = {},
 * acmid = {},
 * publisher = {ACM},
 * address = {New York, NY, USA},
 * keywords = {Fault localization, test case generation},
 * }
 */
public class EntBugTestStrategy extends TestGenerationStrategy {

    private static final Logger logger = LoggerFactory.getLogger(IndividualTestStrategy.class);

    @Override
    public TestSuiteChromosome generateTests() {
        // In order to improve strategy's performance, in here we explicitly disable EvoSuite's
        // archive, as it is not used anyway by this strategy
        Properties.TEST_ARCHIVE = false;

        // Set up search algorithm
        LoggingUtils.getEvoLogger().info("* Setting up search algorithm for individual test generation (ASE'13)");
        ExecutionTracer.enableTraceCalls();

        // Set up genetic algorithm
        PropertiesTestGAFactory factory = new PropertiesTestGAFactory();
        GeneticAlgorithm<TestChromosome> ga = factory.getSearchAlgorithm();

        if (Properties.SERIALIZE_GA || Properties.CLIENT_ON_THREAD) {
            TestGenerationResultBuilder.getInstance().setGeneticAlgorithm(ga);
        }

        // What's the search target
        RhoCoverageFactory rhoFactory = (RhoCoverageFactory) FitnessFunctions.getFitnessFactory(Properties.Criterion.RHO);
        RhoCoverageTestFitness rhoTestFitnessFunction = new RhoCoverageTestFitness();
        ga.addFitnessFunction(rhoTestFitnessFunction);

        // Goals
        List<TestFitnessFunction> goals = new ArrayList<>(rhoFactory.getCoverageGoals());
        LoggingUtils.getEvoLogger().info("* Total number of test goals: ");
        LoggingUtils.getEvoLogger().info("  - Rho " + goals.size());
        ClientServices.getInstance().getClientNode().changeState(ClientState.SEARCH);

        double previous_fitness = RhoCoverageFactory.getRho();
        double best_fitness = 0.0;
        int number_of_generations = (int) (Properties.SEARCH_BUDGET / 10);
        //Properties.SEARCH_BUDGET = 10; // 10 seconds for each generation

        TestSuiteChromosome bests = new TestSuiteChromosome();
        while (number_of_generations > 0) {
            LoggingUtils.getEvoLogger().info("  * iteration(" + number_of_generations + ")");

            // 10 seconds for each generation
            ga.setStoppingConditionLimit(10); // FIXME: should be a parameter?
            ga.resetStoppingConditions();
            ga.clearPopulation();
            //ga.setChromosomeFactory(getDefaultChromosomeFactory()); // not in the original version

            ga.generateSolution();
            number_of_generations--;

            TestChromosome best = ga.getBestIndividual();
            if (best.getLastExecutionResult() == null) {
                // FIXME not sure yet how this can be null
                // some timeout?
                continue;
            }

            best_fitness = best.getFitness(rhoTestFitnessFunction);

            if ((best_fitness < previous_fitness) || // we've found a better test case
                    (best_fitness <= Properties.EPSON)) // or this new test case is not so bad (i.e., < Properties.EPSON)
            {
                // GOOD
                LoggingUtils.getEvoLogger().info("  * new best (previous fitness: " + previous_fitness + " | best_fitness: " + best_fitness + ")");

                ExecutionResult exec = best.getLastExecutionResult();
                ExecutionTrace trace = exec.getTrace();
                Set<Integer> testCoverage = trace.getCoveredLines();
                LoggingUtils.getEvoLogger().info("  * new test case added " + testCoverage.toString());

                rhoTestFitnessFunction.incrementNumber_of_Ones(testCoverage.size());
                rhoTestFitnessFunction.incrementNumber_of_Test_Cases();
                rhoTestFitnessFunction.addTestCoverage(testCoverage);

                bests.addTest(best);
                previous_fitness = best_fitness; // update global fitness
            } else {
                // BAD
                LoggingUtils.getEvoLogger().info("  * new test case ignored (previous fitness: " + previous_fitness + " | best_fitness: " + best_fitness + ")");
            }
        }

        LoggingUtils.getEvoLogger().info("* Search finished after, best individual has fitness "
                + best_fitness);
        LoggingUtils.getEvoLogger().info("Resulting test suite: " + bests.size() + " tests, length "
                + bests.totalLengthOfTestCases());

        return bests;
    }
}
