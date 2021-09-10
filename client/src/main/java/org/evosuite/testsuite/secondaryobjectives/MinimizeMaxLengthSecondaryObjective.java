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
package org.evosuite.testsuite.secondaryobjectives;

import org.evosuite.ga.SecondaryObjective;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;


/**
 * <p>MinimizeMaxLengthSecondaryObjective class.</p>
 *
 * @author Gordon Fraser
 */
public class MinimizeMaxLengthSecondaryObjective extends SecondaryObjective<TestSuiteChromosome> {

    private static final long serialVersionUID = 2270058273932360617L;

    private int getMaxLength(TestSuiteChromosome chromosome) {
        int max = 0;
        for (TestChromosome test : chromosome.getTestChromosomes()) {
            max = Math.max(max, test.size());
        }
        return max;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.evosuite.testcase.secondaryobjectives.SecondaryObjective#compareChromosomes(org.evosuite.ga.Chromosome,
     * org.evosuite.ga.Chromosome)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareChromosomes(TestSuiteChromosome chromosome1, TestSuiteChromosome chromosome2) {
        return getMaxLength(chromosome1) - getMaxLength(chromosome2);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.evosuite.testcase.secondaryobjectives.SecondaryObjective#compareGenerations(org.evosuite.ga.Chromosome,
     * org.evosuite.ga.Chromosome, org.evosuite.ga.Chromosome, org.evosuite.ga.Chromosome)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareGenerations(TestSuiteChromosome parent1, TestSuiteChromosome parent2,
                                  TestSuiteChromosome child1, TestSuiteChromosome child2) {
        return Math.min(getMaxLength(parent1), getMaxLength(parent2))
                - Math.min(getMaxLength(child1), getMaxLength(child2));
    }

}
