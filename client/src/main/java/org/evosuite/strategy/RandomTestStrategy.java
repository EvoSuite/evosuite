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
import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.archive.ArchiveTestChromosomeFactory;
import org.evosuite.ga.stoppingconditions.MaxTestsStoppingCondition;
import org.evosuite.ga.stoppingconditions.StoppingCondition;
import org.evosuite.rmi.ClientServices;
import org.evosuite.rmi.service.ClientState;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.statistics.StatisticsSender;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.factories.AllMethodsTestChromosomeFactory;
import org.evosuite.testcase.factories.JUnitTestCarvedChromosomeFactory;
import org.evosuite.testcase.factories.RandomLengthTestFactory;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Iteratively generate random tests. If adding the random test
 * leads to improved fitness, keep it, otherwise drop it again.
 *
 * @author gordon
 */
public class RandomTestStrategy extends TestGenerationStrategy {

    private static final Logger logger = LoggerFactory.getLogger(RandomTestStrategy.class);

    @Override
    public TestSuiteChromosome generateTests() {
        LoggingUtils.getEvoLogger().info("* Using random test generation");

        List<TestSuiteFitnessFunction> fitnessFunctions = getFitnessFunctions();

        TestSuiteChromosome suite = new TestSuiteChromosome();
        for (TestSuiteFitnessFunction fitnessFunction : fitnessFunctions)
            suite.addFitness(fitnessFunction);

        List<TestFitnessFactory<? extends TestFitnessFunction>> goalFactories = getFitnessFactories();
        List<TestFitnessFunction> goals = new ArrayList<>();
        LoggingUtils.getEvoLogger().info("* Total number of test goals: ");
        for (TestFitnessFactory<? extends TestFitnessFunction> goalFactory : goalFactories) {
            goals.addAll(goalFactory.getCoverageGoals());
            LoggingUtils.getEvoLogger().info("  - " + goalFactory.getClass().getSimpleName().replace("CoverageFactory", "")
                    + " " + goalFactory.getCoverageGoals().size());
        }
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Goals,
                goals.size());

        if (!canGenerateTestsForSUT()) {
            LoggingUtils.getEvoLogger().info("* Found no testable methods in the target class "
                    + Properties.TARGET_CLASS);
            return new TestSuiteChromosome();
        }
        ChromosomeFactory<TestChromosome> factory = getChromosomeFactory();

        StoppingCondition<TestSuiteChromosome> stoppingCondition = getStoppingCondition();
        for (FitnessFunction<TestSuiteChromosome> fitness_function : fitnessFunctions)
            fitness_function.getFitness(suite);
        ClientServices.getInstance().getClientNode().changeState(ClientState.SEARCH);

        int number_generations = 0;
        while (!isFinished(suite, stoppingCondition)) {
            number_generations++;
            TestChromosome test = factory.getChromosome();
            TestSuiteChromosome clone = suite.clone();
            clone.addTest(test);
            for (FitnessFunction<TestSuiteChromosome> fitness_function : fitnessFunctions) {
                fitness_function.getFitness(clone);
                logger.debug("Old fitness: {}, new fitness: {}", suite.getFitness(),
                        clone.getFitness());
            }
            if (clone.compareTo(suite) < 0) {
                suite = clone;
                StatisticsSender.executedAndThenSendIndividualToMaster(clone);
            }
        }
        //statistics.searchFinished(suiteGA);
        LoggingUtils.getEvoLogger().info("* Search Budget:");
        LoggingUtils.getEvoLogger().info("\t- " + stoppingCondition);

        // In the GA, these statistics are sent via the SearchListener when notified about the GA completing
        // Search is finished, send statistics
        sendExecutionStatistics();

        // TODO: Check this: Fitness_Evaluations = getNumExecutedTests?
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Fitness_Evaluations, MaxTestsStoppingCondition.getNumExecutedTests());
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Generations, number_generations);

        return suite;
    }

    protected ChromosomeFactory<TestChromosome> getChromosomeFactory() {
        switch (Properties.TEST_FACTORY) {
            case ALLMETHODS:
                return new AllMethodsTestChromosomeFactory();
            case RANDOM:
                return new RandomLengthTestFactory();
            case ARCHIVE:
                return new ArchiveTestChromosomeFactory();
            case JUNIT:
                return new JUnitTestCarvedChromosomeFactory(
                        new RandomLengthTestFactory());
            default:
                throw new RuntimeException("Unsupported test factory: "
                        + Properties.TEST_FACTORY);
        }

    }


}
