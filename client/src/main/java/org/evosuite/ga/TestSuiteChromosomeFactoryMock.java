/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.ga;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;

/**
 * Mocks factories for {@code TestChromosome}s as factories for
 * {@code TestSuiteChromosome}s.
 */
public class TestSuiteChromosomeFactoryMock
        extends ChromosomeFactoryMock<TestChromosome, TestSuiteChromosome> {

    private static final long serialVersionUID = 8395282399919895283L;

    /**
     * {@inheritDoc}
     */
    public TestSuiteChromosomeFactoryMock(final ChromosomeFactory<TestChromosome> wrapped) {
        super(wrapped);
    }
}
