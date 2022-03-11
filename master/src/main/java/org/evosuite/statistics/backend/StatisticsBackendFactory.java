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
package org.evosuite.statistics.backend;

import org.evosuite.Properties;

/**
 * Factory of Statistics Backend
 *
 * @author Ignacio Lebrero
 */
public class StatisticsBackendFactory {

    /**
     * Builds the type of Statistics backend required
     *
     * @param backendType
     * @return
     */
    public static StatisticsBackend getStatisticsBackend(Properties.StatisticsBackend backendType) {
        switch (backendType) {
            case CONSOLE:
                return new ConsoleStatisticsBackend();
            case CSV:
                return new CSVStatisticsBackend();
            case HTML:
                return new HTMLStatisticsBackend();
            case DEBUG:
                return new DebugStatisticsBackend();
            case NONE:
            default:
                // If no backend is specified, there is no output
                return null;
        }
    }

}
