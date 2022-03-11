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
package org.evosuite.testcarver.codegen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaptureLogAnalyzerException extends RuntimeException {

    private static final long serialVersionUID = 4585552843370187739L;

    private static final Logger LOGGER = LoggerFactory.getLogger(CaptureLogAnalyzerException.class);

    public CaptureLogAnalyzerException(final String msg) {
        super(msg);
    }

    public CaptureLogAnalyzerException(final Throwable e) {
        super(e);
    }

    public static void check(final boolean expr, final String msg, final Object... msgArgs)
            throws CaptureLogAnalyzerException {
        if (!expr) {
            final String finalMsg = String.format(msg, msgArgs);
            LOGGER.info(finalMsg);
            throw new CaptureLogAnalyzerException(finalMsg);
        }
    }


    public static void propagateError(final Throwable t, final String msg, final Object... msgArgs)
            throws CaptureLogAnalyzerException {
        final String finalMsg = String.format(msg, msgArgs);
        LOGGER.info(finalMsg, t);
        throw new CaptureLogAnalyzerException(finalMsg);
    }
}
