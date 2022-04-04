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
package org.evosuite.ga;

/**
 * Exception used when error occurs during mutation or crossover
 *
 * @author Gordon Fraser
 */
public class ConstructionFailedException extends Exception {

    private static final long serialVersionUID = -1326799751206971428L;

    /**
     * <p>Constructor for ConstructionFailedException.</p>
     *
     * @param reason a {@link java.lang.String} object.
     */
    public ConstructionFailedException(String reason) {
        super(reason);
    }

}
