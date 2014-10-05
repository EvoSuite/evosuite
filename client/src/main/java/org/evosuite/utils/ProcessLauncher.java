package org.evosuite.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ProcessLauncher {

	private static Logger logger = LoggerFactory
			.getLogger(ProcessLauncher.class);

	public static int launchNewProcess(File baseDir, String[] parsedCommand,
			List<String> bufferStdOut, int timeout) throws IOException {

		ProcessBuilder builder = new ProcessBuilder(parsedCommand);
		builder.directory(baseDir);
		builder.redirectErrorStream(true);
		final Process process = builder.start();

		InputStream stdout = process.getInputStream();
		logger.debug("Process output:");

		Timer t = new Timer();
		t.schedule(new TimerTask() {

			@Override
			public void run() {
				process.destroy();
			}
		}, timeout);

		do {
			ProcessLauncher.readInputStream("Finished process output - ",
					stdout, bufferStdOut);
		} while (!ProcessLauncher.isFinished(process));

		int exitValue = process.exitValue();
		return exitValue;
	}

	private static boolean isFinished(Process process) {
		try {
			process.exitValue();
			return true;
		} catch (IllegalThreadStateException ex) {
			return false;
		}
	}

	private static void readInputStream(String prefix, InputStream in,
			List<String> buffer) throws IOException {
		InputStreamReader is = new InputStreamReader(in);
		BufferedReader br = new BufferedReader(is);
		String read = br.readLine();
		while (read != null) {
			logger.debug(prefix + read);
			buffer.add(prefix + read);
			read = br.readLine();
		}
	}

}
