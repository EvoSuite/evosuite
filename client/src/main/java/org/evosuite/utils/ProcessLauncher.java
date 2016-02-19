/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessLauncher {

	private final OutputStream outAndErr;

	private final InputStream input;

	public ProcessLauncher() {
		this(null,null);
	}
	
	public ProcessLauncher(OutputStream outAndErr) {
		this(outAndErr,null);
	}

	public ProcessLauncher(InputStream input) {
		this(null,input);
	}

	public ProcessLauncher(OutputStream outAndErr, InputStream input) {
		this.outAndErr = outAndErr;
		this.input = input;
	}

	private static Logger logger = LoggerFactory
			.getLogger(ProcessLauncher.class);

	private static String concatToString(String[] cmd) {
		String cmdLine = "";
		for (String cmdStr : cmd) {
			if (cmdLine.length() == 0) {
				cmdLine = cmdStr;
			} else {
				cmdLine += " " + cmdStr;
			}
		}
		return cmdLine;
	}

	public int launchNewProcess(String parsedCommand,
			int timeout) throws IOException, ProcessTimeoutException {
		int ret_code =launchNewProcess(null, parsedCommand,
				timeout);
		return ret_code;
	}

	public int launchNewProcess(File baseDir, String[] parsedCommand,
			int timeout) throws IOException, ProcessTimeoutException {
		String cmdString = concatToString(parsedCommand);
		int ret_code = launchNewProcess(baseDir, cmdString, timeout);
		return ret_code;
	}
	
	public int launchNewProcess(File baseDir, String cmdString,
			int timeout) throws IOException, ProcessTimeoutException {

		DefaultExecutor executor = new DefaultExecutor();
		ExecuteWatchdog timeoutWatchdog = new ExecuteWatchdog(timeout);
		executor.setWatchdog(timeoutWatchdog);

		PumpStreamHandler streamHandler =new PumpStreamHandler(this.outAndErr, this.outAndErr, this.input);
		executor.setStreamHandler(streamHandler);
		if (baseDir!=null) {
			executor.setWorkingDirectory(baseDir);
		}

		int exitValue;
		try {
			logger.debug("About to execute command " + cmdString);
			exitValue = executor.execute(CommandLine.parse(cmdString));
			if (executor.isFailure(exitValue)
					&& timeoutWatchdog.killedProcess()) {
				// it was killed on purpose by the watchdog
				logger.debug("A timeout occured while executing a process");
				logger.debug("The command is " + cmdString);
				throw new ProcessTimeoutException(
						"A timeout occurred while executing command "
								+ cmdString);
			}

			return exitValue;
		} catch (ExecuteException e) {
			if (timeoutWatchdog.killedProcess()) {
				logger.debug("A timeout occured while executing a process");
				logger.debug("The command is " + cmdString);
				throw new ProcessTimeoutException(
						"A timeout occurred while executing command "
								+ cmdString);
			} else {
				throw e;
			}

		}

	}

}
