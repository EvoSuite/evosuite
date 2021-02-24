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
package org.evosuite.symbolic.vm.instructionlogger.implementations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instruction Logger that outputs through the standard logger.
 *
 * @author Ignacio Lebrero
 */
public final class StandardOutputInstructionLogger extends AbstractInstructionLogger {

    private static final Logger logger = LoggerFactory.getLogger(AbstractInstructionLogger.class);

    private final String prefix;

    public StandardOutputInstructionLogger(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Log parameter as p.
     */
    @Override
    public void log(String p) {
        /** ilebero: this could be done using MDC or some other pattern? this is the only place we actually use it */
        logger.info(prefix + " | " + p);
        buffer.append(p + " ");
    }

    /**
     * Log newline.
     */
    @Override
    public void logln() {
        logger.info("");
        instructionsExecuted.add(buffer.toString());
        buffer = new StringBuilder();
    }

    @Override
    public void cleanUp() {
        /** Nothing to do here */
    }

}
