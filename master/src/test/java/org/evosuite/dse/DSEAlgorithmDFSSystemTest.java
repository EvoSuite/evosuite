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
package org.evosuite.dse;

import com.examples.with.different.packagename.solver.MazeClientArrays;
import org.evosuite.Properties;
import org.evosuite.symbolic.dse.algorithm.DSEAlgorithms;
import org.junit.Before;
import org.junit.Test;

/**
 * DFS exploration algorithm related system tests.
 * Main DFS implementation can be found at
 * {@link org.evosuite.symbolic.dse.algorithm.explorationalgorithms.DFSExplorationAlgorithm}
 *
 * @author Ignacio Lebrero
 */
public class DSEAlgorithmDFSSystemTest extends DSESystemTestBase {

    @Before
    public void init() {
        super.init();

        Properties.CURRENT_DSE_MODULE_VERSION = Properties.DSE_MODULE_VERSION.NEW;
        Properties.DSE_EXPLORATION_ALGORITHM_TYPE = DSEAlgorithms.DFS;
        Properties.SELECTED_DSE_ARRAYS_MEMORY_MODEL_VERSION = Properties.DSE_ARRAYS_MEMORY_MODEL_VERSION.SELECT_STORE_EXPRESSIONS;
    }

    @Test
    public void testMazeClientInputWithDFSAlgorithm() {
        testDSEExecution(26, 0, MazeClientArrays.class);
    }
}
