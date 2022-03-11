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

import org.evosuite.utils.SystemPathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Instruction Logger that outputs through a text file.
 *
 * @author Ignacio Lebrero
 */
public final class FileDumpInstructionLogger extends AbstractInstructionLogger {

    private static final Logger logger = LoggerFactory.getLogger(FileDumpInstructionLogger.class);
    public static final String EXECUTED_BYTECODE_FILE_NAME = "executedBytecode";

    /**
     * For each time the same method gets called, so we don't overwrite the same file.
     * NOTE(ilebrero): A better solution for this? this is not an optimal way of doing this.
     */
    private static long internalCount = 0;

    private final String filePath;

    private FileWriter fstream;
    private BufferedWriter writer;
    private PrintWriter pw;

    public FileDumpInstructionLogger(String directory, String filename) {
        createDirectoryIfDoesntExistst(directory);

        this.filePath = SystemPathUtil.buildPath(directory, SystemPathUtil.joinWithDelimiter(
                SystemPathUtil.FILE_NAME_DELIMITER,
                String.valueOf(internalCount),
                filename));

        internalCount++;

        try {
            this.fstream = new FileWriter(filePath);
            this.writer = new BufferedWriter(this.fstream);
            this.pw = new PrintWriter(this.writer);
        } catch (IOException e) {
            logger.error("Error when opening file " + filename);
            logger.error(e.getMessage());
        }

    }

    @Override
    public void log(String p) {
        pw.print(p);
    }

    @Override
    public void logln() {
        pw.println();
    }

    @Override
    public void cleanUp() {
        try {
            pw.close();
            writer.close();
            fstream.close();
        } catch (IOException e) {
            logger.error("Error when trying to close file " + filePath);
            logger.error(e.getMessage());
        }
    }

    private void createDirectoryIfDoesntExistst(String directory) {
        File file = new File(directory);
        if (!file.exists()) file.mkdirs();
    }
}
