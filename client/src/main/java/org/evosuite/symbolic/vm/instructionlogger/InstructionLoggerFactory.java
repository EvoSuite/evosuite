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
package org.evosuite.symbolic.vm.instructionlogger;

import org.evosuite.Properties;
import org.evosuite.symbolic.vm.instructionlogger.implementations.FileDumpInstructionLogger;
import org.evosuite.symbolic.vm.instructionlogger.implementations.StandardOutputInstructionLogger;
import org.evosuite.utils.SystemPathUtil;

import static org.evosuite.symbolic.vm.instructionlogger.implementations.FileDumpInstructionLogger.EXECUTED_BYTECODE_FILE_NAME;

/**
 * Factory of possible bytecode loggers.
 *
 * @author Ignacio Lebrero
 */
public class InstructionLoggerFactory {
    public static final String LOGGING_MODE_NOT_PROVIDED = "A logging mode must be provided";
    public static final String LOGGING_MODE_NOT_YET_IMPLEMENTED = "logging mode not yet implemented: ";

    /**
     * Singleton instance
     */
    private static final InstructionLoggerFactory self = new InstructionLoggerFactory();

    public static InstructionLoggerFactory getInstance() {
        return self;
    }

    public IInstructionLogger getInstructionLogger(Properties.DSEBytecodeLoggingMode bytecodeLoggingMode) {
        if (bytecodeLoggingMode == null) {
            throw new IllegalArgumentException(LOGGING_MODE_NOT_PROVIDED);
        }

        switch (bytecodeLoggingMode) {
            case STD_OUT:
                return new StandardOutputInstructionLogger(SystemPathUtil.buildPath(
                        Properties.TARGET_CLASS,
                        Properties.CURRENT_TARGET_METHOD));
            case FILE_DUMP:
                return new FileDumpInstructionLogger(
                        SystemPathUtil.buildPath(
                                Properties.REPORT_DIR,
                                Properties.BYTECODE_LOGGING_REPORT_DIR),
                        SystemPathUtil.buildFileName(
                                SystemPathUtil.FileExtension.TXT,
                                EXECUTED_BYTECODE_FILE_NAME,
                                Properties.TARGET_CLASS,
                                Properties.CURRENT_TARGET_METHOD));
            default:
                throw new IllegalStateException(LOGGING_MODE_NOT_YET_IMPLEMENTED + bytecodeLoggingMode.name());
        }
    }
}