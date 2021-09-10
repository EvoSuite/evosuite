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
package org.evosuite.coverage.rho;

import org.evosuite.ga.SecondaryObjective;
import org.evosuite.testsuite.TestSuiteChromosome;

/**
 * RhoTestSuiteSecondaryObjective class.
 *
 * @author José Campos
 */
public class RhoTestSuiteSecondaryObjective extends SecondaryObjective<TestSuiteChromosome> {

    private static final long serialVersionUID = 3483170260455441964L;

    private double getRhoFitnessValue(TestSuiteChromosome suite) {
        RhoCoverageSuiteFitness fitness = new RhoCoverageSuiteFitness();
        return fitness.getFitness(suite, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareChromosomes(TestSuiteChromosome suite1, TestSuiteChromosome suite2) {
        double c1 = this.getRhoFitnessValue(suite1);
        double c2 = this.getRhoFitnessValue(suite2);

        if (c1 == c2) {
            return 0;
        } else if (c1 < c2) {
            // the Chromosome with the lowest rho value should be ranked first
            return -1;
        } else {
            return 1;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareGenerations(TestSuiteChromosome parent1, TestSuiteChromosome parent2,
                                  TestSuiteChromosome child1, TestSuiteChromosome child2) {
        // this function is not used
        throw new RuntimeException(
                "compareGenerations function of " + RhoTestSuiteSecondaryObjective.class.getCanonicalName()
                        + " has not been implemented yet");
    }
}
