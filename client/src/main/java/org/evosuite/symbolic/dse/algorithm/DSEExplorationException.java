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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exceptions for exploration related issues.
 *
 * @author Ignacio Lebrero
 */
public class DSEExplorationException extends RuntimeException {

    private static final long serialVersionUID = 7112951484696975475L;

    private static final Logger LOGGER = LoggerFactory.getLogger(DSEExplorationException.class);

    public DSEExplorationException(final String msg) {
        super(msg);
    }

    public DSEExplorationException(final Throwable e) {
        super(e);
    }

    public static void check(final boolean expr, final String msg, final Object... msgArgs)
            throws DSEExplorationException {
        if (!expr) {
            final String finalMsg = String.format(msg, msgArgs);
            LOGGER.info(finalMsg);
            throw new DSEExplorationException(finalMsg);
        }
    }


    public static void propagateError(final Throwable t, final String msg, final Object... msgArgs)
            throws DSEExplorationException {
        final String finalMsg = String.format(msg, msgArgs);

        if (t == null) {
            LOGGER.info(finalMsg);
        } else {
            LOGGER.error(finalMsg, t);
        }

        throw new DSEExplorationException(finalMsg);
    }

    public static void propagateError(final String msg, final Object... msgArgs)
            throws DSEExplorationException {
        propagateError(null, msg, msgArgs);
    }
}