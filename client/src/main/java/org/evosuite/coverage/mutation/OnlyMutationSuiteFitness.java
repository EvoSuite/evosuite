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
package org.evosuite.coverage.mutation;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.ga.archive.Archive;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.TestSuiteChromosome;

import java.util.*;
import java.util.Map.Entry;

/**
 * <p>
 * OnlyMutationSuiteFitness class.
 * </p>
 *
 * @author fraser
 */
public class OnlyMutationSuiteFitness extends MutationSuiteFitness {

    private static final long serialVersionUID = -8194940669364526758L;

    public OnlyMutationSuiteFitness() {
        super(Properties.Criterion.ONLYMUTATION);
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.FitnessFunction#getFitness(org.evosuite.ga.Chromosome)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public double getFitness(TestSuiteChromosome individual) {
        /*
         * e.g. classes with only static constructors
         */
        if (this.numMutants == 0) {
            updateIndividual(individual, 0.0);
            individual.setCoverage(this, 1.0);
            individual.setNumOfCoveredGoals(this, 0);
            return 0.0;
        }

        List<ExecutionResult> results = runTestSuite(individual);

        double fitness = 0.0;
        Map<Integer, Double> mutant_distance = new LinkedHashMap<>();
        Set<Integer> touchedMutants = new LinkedHashSet<>();

        for (ExecutionResult result : results) {
            // Using private reflection can lead to false positives
            // that represent unrealistic behaviour. Thus, we only
            // use reflection for basic criteria, not for mutation
            if (result.hasTimeout() || result.hasTestException() || result.calledReflection()) {
                continue;
            }

            touchedMutants.addAll(result.getTrace().getTouchedMutants());

            Map<Integer, Double> touchedMutantsDistances = result.getTrace().getMutationDistances();
            if (touchedMutantsDistances.isEmpty()) {
                // if 'result' does not touch any mutant, no need to continue
                continue;
            }

            TestChromosome test = new TestChromosome();
            test.setTestCase(result.test);
            test.setLastExecutionResult(result);
            test.setChanged(false);

            for (final Entry<Integer, MutationTestFitness> entry : this.mutantMap.entrySet()) {
                int mutantID = entry.getKey();
                TestFitnessFunction goal = entry.getValue();

                double fit = 0.0;
                if (touchedMutantsDistances.containsKey(mutantID)) {
                    fit = touchedMutantsDistances.get(mutantID);

                    if (!mutant_distance.containsKey(mutantID)) {
                        mutant_distance.put(mutantID, fit);
                    } else {
                        mutant_distance.put(mutantID, Math.min(mutant_distance.get(mutantID), fit));
                    }
                } else {
                    fit = goal.getFitness(test, result); // archive is updated by the TestFitnessFunction class
                }

                if (fit == 0.0) {
                    test.getTestCase().addCoveredGoal(goal); // update list of covered goals
                    this.toRemoveMutants.add(mutantID); // goal to not be considered by the next iteration of the evolutionary algorithm
                }

                if (Properties.TEST_ARCHIVE) {
                    Archive.getArchiveInstance().updateArchive(goal, test, fit);
                }
            }
        }

        // Second objective: touch all mutants?
        fitness += MutationPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getMutantCounter() - touchedMutants.size();
        int covered = this.removedMutants.size();

        for (Double distance : mutant_distance.values()) {
            if (distance < 0) {
                logger.warn("Distance is " + distance + " / " + Integer.MAX_VALUE + " / "
                        + Integer.MIN_VALUE);
                distance = 0.0; // FIXXME
            }

            fitness += normalize(distance);
            if (distance == 0.0)
                covered++;
        }

        updateIndividual(individual, fitness);
        individual.setCoverage(this, (double) covered / (double) this.numMutants);
        individual.setNumOfCoveredGoals(this, covered);

        return fitness;
    }
}
