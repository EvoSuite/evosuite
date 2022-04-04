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
package org.evosuite.utils;

import org.apache.commons.exec.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ProcessLauncher {

    private final OutputStream outAndErr;

    private final InputStream input;

    public ProcessLauncher(OutputStream outAndErr, InputStream input) {
        this.outAndErr = outAndErr;
        this.input = input;
    }

    private static final Logger logger = LoggerFactory.getLogger(ProcessLauncher.class);

    public int launchNewProcess(String parsedCommand, int timeout) throws IOException, ProcessTimeoutException {
        int ret_code = launchNewProcess(null, parsedCommand, timeout);
        return ret_code;
    }

    private int launchNewProcess(File baseDir, String cmdString, int timeout)
            throws IOException, ProcessTimeoutException {

        DefaultExecutor executor = new DefaultExecutor();
        ExecuteWatchdog timeoutWatchdog = new ExecuteWatchdog(timeout);
        executor.setWatchdog(timeoutWatchdog);

        PumpStreamHandler streamHandler = new PumpStreamHandler(this.outAndErr, this.outAndErr, this.input);
        executor.setStreamHandler(streamHandler);
        if (baseDir != null) {
            executor.setWorkingDirectory(baseDir);
        }

        try {
            logger.debug("About to execute command " + cmdString);
            CommandLine cmdLine = CommandLine.parse(cmdString);
            int exitValue = executor.execute(cmdLine);

            if (executor.isFailure(exitValue) && timeoutWatchdog.killedProcess()) {
                // it was killed on purpose by the watchdog
                logger.debug("A timeout occured while executing a process");
                logger.debug("The command is " + cmdString);
                throw new ProcessTimeoutException("A timeout occurred while executing command " + cmdString);
            }

            return exitValue;
        } catch (ExecuteException ex) {
            if (timeoutWatchdog.killedProcess()) {
                logger.debug("A timeout occured while executing a process");
                logger.debug("The command is " + cmdString);
                throw new ProcessTimeoutException("A timeout occurred while executing command " + cmdString);
            } else {
                throw ex;
            }

        }

    }

}
