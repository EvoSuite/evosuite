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
package org.evosuite.coverage.exception;

import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.AbstractFitnessFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Coverage factory for observed exceptions
 *
 * @author Gordon Fraser, Jose Miguel Rojas
 */
public class ExceptionCoverageFactory extends AbstractFitnessFactory<TestFitnessFunction> {

    private static final Map<String, ExceptionCoverageTestFitness> goals = new LinkedHashMap<>();

    public static Map<String, ExceptionCoverageTestFitness> getGoals() {
        return goals;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TestFitnessFunction> getCoverageGoals() {
        return new ArrayList<>(goals.values());
    }
}
