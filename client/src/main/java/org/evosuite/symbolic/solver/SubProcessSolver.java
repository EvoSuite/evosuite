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
