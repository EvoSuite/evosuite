package org.evosuite.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessLauncher {

	private OutputStream sinkStdOut = null;

	private OutputStream sinkStdErr = null;

	public void setSinkStdOut(OutputStream sinkStdOut) {
		this.sinkStdOut = sinkStdOut;
	}

	public void setSinkStdErr(OutputStream sinkStdErr) {
		this.sinkStdErr = sinkStdErr;
	}

	private static Logger logger = LoggerFactory
			.getLogger(ProcessLauncher.class);

	public int launchNewProcess(File baseDir, String[] parsedCommand,
			int timeout) throws IOException {

		ProcessBuilder builder = new ProcessBuilder(parsedCommand);
		builder.directory(baseDir);
		builder.redirectErrorStream(false);
		final Process process = builder.start();

		InputStream stdout = process.getInputStream();
		InputStream stderr = process.getErrorStream();
		logger.debug("Process output:");

		Timer t = new Timer();
		t.schedule(new TimerTask() {

			@Override
			public void run() {
				process.destroy();
			}
		}, timeout);

		do {
			readInputStream(stdout, sinkStdOut);
			readInputStream(stderr, sinkStdErr);
		} while (!isFinished(process));

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

	private static void readInputStream(InputStream in, OutputStream out)
			throws IOException {
		InputStreamReader is = new InputStreamReader(in);
		BufferedReader br = new BufferedReader(is);
		String read = br.readLine();
		while (read != null) {
			logger.debug(read);
			if (out != null) {
				byte[] bytes = (read + "\n").getBytes();
				out.write(bytes);
			}
			read = br.readLine();
		}
	}

}
