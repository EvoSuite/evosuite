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

import org.evosuite.testsuite.TestSuiteChromosome;

/**
 * Factory to create an output variable when given a test suite chromosome
 *
 * @param <T>
 * @author gordon
 */
public abstract class ChromosomeOutputVariableFactory<T> {

    private final RuntimeVariable variable;

    public ChromosomeOutputVariableFactory(RuntimeVariable variable) {
        this.variable = variable;
    }

    protected abstract T getData(TestSuiteChromosome individual);

    public OutputVariable<T> getVariable(TestSuiteChromosome chromosome) {
        return new OutputVariable<>(variable.name(), getData(chromosome));
    }

}
