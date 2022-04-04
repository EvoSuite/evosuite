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
package org.evosuite.seeding.factories;

import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.testsuite.TestSuiteChromosome;

public class ChromosomeSampleFactory implements ChromosomeFactory<TestSuiteChromosome> {
    public static final TestSuiteChromosome CHROMOSOME;
    private static final TestSampleFactory FACTORY;
    private static final long serialVersionUID = -5227032406625911394L;

    static {
        FACTORY = new TestSampleFactory();
        CHROMOSOME = new TestSuiteChromosome();
        for (int i = 0; i < 10; i++) {
            CHROMOSOME.addTest(FACTORY.getChromosome());
        }
    }

    @Override
    public TestSuiteChromosome getChromosome() {
        // TODO Auto-generated method stub
        return CHROMOSOME.clone();
    }


}
