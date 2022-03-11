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
package org.evosuite.symbolic.dse;

import org.evosuite.Properties.DSE_MODULE_VERSION;
import org.evosuite.strategy.TestGenerationStrategy;

/**
 * DSE strategies factory.
 * Probably this is only going to be used for creating the legacy strategy for benchmarking due to it's search algorithm
 *
 * @author Ignacio Lebrero
 */
public class DSEStrategyFactory {

    public static final String DSE_MODULE_NOT_PROVIDED = "A DSE Module must be provided: ";
    public static final String DSE_MODULE_NOT_YET_IMPLEMENTED = "DSE Module not yet implemented: ";

    /**
     * Builds a new DSE strategy
     *
     * @param selectedModule
     * @return
     */
    public static TestGenerationStrategy getDSEStrategy(DSE_MODULE_VERSION selectedModule) {
        if (selectedModule == null) {
            throw new IllegalArgumentException(DSE_MODULE_NOT_PROVIDED);
        }

        switch (selectedModule) {
            case LEGACY:
                return new DSEStrategyLegacy();
            case NEW:
                return new DSEStrategy();
            default:
                throw new IllegalStateException(DSE_MODULE_NOT_YET_IMPLEMENTED + selectedModule.name());
        }
    }
}
