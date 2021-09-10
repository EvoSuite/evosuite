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
package org.evosuite.symbolic.dse.algorithm;

import org.evosuite.symbolic.PathCondition;

/**
 * Wrapper object of Path conditions aimed at storing the bound info in generational search.
 * See "Automated whitebox fuzzing testing" for more info.
 *
 * @author Ignacio Lebrero
 */
public class GenerationalSearchPathCondition {
    private final PathCondition pathCondition;

    /**
     * Index from which this path condition was generated.
     * Useful to avoid recreating the same path conditions
     */
    private final int generatedFromIndex;

    public GenerationalSearchPathCondition(PathCondition pathCondition, int generatedFromIndex) {
        this.pathCondition = pathCondition;
        this.generatedFromIndex = generatedFromIndex;
    }

    public int getGeneratedFromIndex() {
        return generatedFromIndex;
    }

    public PathCondition getPathCondition() {
        return pathCondition;
    }
}
