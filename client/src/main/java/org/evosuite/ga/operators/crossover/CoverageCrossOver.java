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
package org.evosuite.ga.operators.crossover;

import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.Randomness;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * <p>CoverageCrossOver class.</p>
 *
 * @author Gordon Fraser
 */
public class CoverageCrossOver extends CrossOverFunction<TestSuiteChromosome> {

    private static final long serialVersionUID = -2203276450790663024L;

    /* (non-Javadoc)
     * @see org.evosuite.ga.CrossOverFunction#crossOver(org.evosuite.ga.Chromosome, org.evosuite.ga.Chromosome)
     */

    /**
     * {@inheritDoc}
     *
     * @param parent1
     * @param parent2
     */
    @Override
    public void crossOver(TestSuiteChromosome parent1, TestSuiteChromosome parent2)
            throws ConstructionFailedException {

        // Determine coverage information
        Map<TestFitnessFunction, Set<TestChromosome>> goalMap = new HashMap<>();
        populateCoverageMap(goalMap, parent1);
        populateCoverageMap(goalMap, parent2);

        // Extract set of tests that have unique coverage
        // We need all of these tests in both offspring
        Set<TestChromosome> unique = removeUniqueCoveringTests(goalMap);
        logger.debug("Uniquely covering tests: " + unique.size());

        Set<TestChromosome> offspring1 = new HashSet<>();
        Set<TestChromosome> offspring2 = new HashSet<>();
        Set<TestChromosome> workingSet = new HashSet<>();
        for (TestFitnessFunction goal : goalMap.keySet()) {
            workingSet.addAll(goalMap.get(goal));
        }

        int targetSize = workingSet.size() / 2;
        while (offspring2.size() < targetSize) {
            logger.debug("Sizes: " + workingSet.size() + ", " + offspring1.size() + ", "
                    + offspring2.size());

            // Move a randomly selected redundant test case t from workingset to offspring2
            TestChromosome choice = Randomness.choice(workingSet);
            workingSet.remove(choice);
            offspring2.add(choice);

            // Move all tests with unique coverage to offspring 1?
            offspring1.addAll(removeUniqueCoveringTests(goalMap));
        }

        offspring1.addAll(workingSet);

        parent1.clearTests();
        parent2.clearTests();

        // Add unique tests
        for (TestChromosome test : unique) {
            parent1.addTest(test.clone());
            parent2.addTest(test.clone());
        }

        // Add redundancy tests
        parent1.addTests(offspring1);
        parent2.addTests(offspring2);
        logger.debug("Final sizes: " + parent1.size() + ", " + parent2.size());

    }

    /**
     * Create a map from coverage goal to tests that cover this goal
     *
     * @param goalMap
     * @param suite
     */
    private void populateCoverageMap(
            Map<TestFitnessFunction, Set<TestChromosome>> goalMap,
            TestSuiteChromosome suite) {
        for (TestChromosome test : suite.getTestChromosomes()) {
            for (TestFitnessFunction goal : test.getTestCase().getCoveredGoals()) {
                if (!goalMap.containsKey(goal))
                    goalMap.put(goal, new HashSet<>());
                goalMap.get(goal).add(test);
            }

        }
    }

    private Set<TestChromosome> removeUniqueCoveringTests(
            Map<TestFitnessFunction, Set<TestChromosome>> goalMap) {
        Set<TestChromosome> tests = new HashSet<>();

        for (Entry<TestFitnessFunction, Set<TestChromosome>> entry : goalMap.entrySet()) {
            if (entry.getValue().size() == 1) {
                tests.add(entry.getValue().iterator().next());
            }
        }

        return tests;
    }

}
