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
package org.evosuite.symbolic.solver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.evosuite.utils.ProcessLauncher;
import org.evosuite.utils.ProcessTimeoutException;

public abstract class SubProcessSolver extends Solver {

	public SubProcessSolver(boolean addMissingVariables) {
		super(addMissingVariables);
	}

	public SubProcessSolver() {
		super();
	}

	protected static int launchNewProcess(String solverCmd, String smtQuery, int hard_timeout, OutputStream outputStream) throws IOException {

		ByteArrayInputStream input = new ByteArrayInputStream(smtQuery.getBytes());

		ProcessLauncher launcher = new ProcessLauncher(outputStream, input);

		long solver_start_time_millis = System.currentTimeMillis();
		try {
			int exit_code = launcher.launchNewProcess(solverCmd, hard_timeout);
			if (exit_code == 0) {
				logger.debug("Solver execution finished normally");
			} else {
				logger.debug("Solver execution finished abnormally with exit code {}", exit_code);
			}
			return exit_code;
		} catch (IOException ex) {
			logger.debug("An IO Exception occurred while executing Solver");
			return -1;
		} catch (ProcessTimeoutException ex) {
			logger.debug("Solver execution stopped due to solver timeout");
			return -1;
		} finally {
			long solver_end_time_millis = System.currentTimeMillis();
			long solver_duration_secs = (solver_end_time_millis - solver_start_time_millis) / 1000;
			logger.debug("Solver execution time was {}s", solver_duration_secs);
		}

	}


}
