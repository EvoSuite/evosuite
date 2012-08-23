/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Gordon Fraser
 */
package org.evosuite.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.evosuite.ClientProcess;
import org.evosuite.ConsoleProgressBar;
import org.evosuite.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.Signal;
import sun.misc.SignalHandler;

/*
 * this code should be used by the main process
 */

@SuppressWarnings("restriction")
public class ExternalProcessHandler {
	/** Constant <code>logger</code> */
	protected static final Logger logger = LoggerFactory.getLogger(ExternalProcessHandler.class);

	protected ServerSocket server;
	protected Process process;
	protected String[] last_command;

	protected Thread output_printer;
	protected Thread error_printer;
	protected Thread message_handler;
	protected Thread progress_printer;

	protected Socket connection;
	protected ObjectOutputStream out;
	protected ObjectInputStream in;

	protected Object final_result;
	/** Constant <code>WAITING_FOR_DATA</code> */
	protected static final Object WAITING_FOR_DATA = "waiting_for_data_"
	        + System.currentTimeMillis();

	protected Thread processKillHook;
	protected Thread clientRunningOnThread;

	protected volatile CountDownLatch latch;

	protected String base_dir = System.getProperty("user.dir");

	/**
	 * <p>
	 * Constructor for ExternalProcessHandler.
	 * </p>
	 */
	public ExternalProcessHandler() {

	}

	/**
	 * <p>
	 * setBaseDir
	 * </p>
	 * 
	 * @param base_dir
	 *            a {@link java.lang.String} object.
	 */
	public void setBaseDir(String base_dir) {
		this.base_dir = base_dir;
	}

	/**
	 * <p>
	 * startProcess
	 * </p>
	 * 
	 * @param command
	 *            an array of {@link java.lang.String} objects.
	 * @return a boolean.
	 */
	public boolean startProcess(String[] command) {
		return startProcess(command, null);
	}

	/**
	 * <p>
	 * startProcess
	 * </p>
	 * 
	 * @param command
	 *            an array of {@link java.lang.String} objects.
	 * @param population_data
	 *            a {@link java.lang.Object} object.
	 * @return a boolean.
	 */
	protected boolean startProcess(String[] command, Object population_data) {
		if (process != null) {
			logger.warn("Already running an external process");
			return false;
		}

		latch = new CountDownLatch(1);
		final_result = WAITING_FOR_DATA;

		//the following thread is important to make sure that the external process is killed
		//when current process ends

		processKillHook = new Thread() {
			@Override
			public void run() {
				killProcess();
				closeServer();
			}
		};

		Runtime.getRuntime().addShutdownHook(processKillHook);
		// now start the process

		if (!Properties.CLIENT_ON_THREAD) {
			File dir = new File(base_dir);
			ProcessBuilder builder = new ProcessBuilder(command);
			builder.directory(dir);
			builder.redirectErrorStream(false);

			try {
				process = builder.start();
			} catch (IOException e) {
				logger.error("Failed to start external process", e);
				return false;
			}

			//FIXME: shouldn't it be deprecated???
			startExternalProcessPrinter();
		} else {
			/*
			 * Here we run client on a thread instead of process.
			 * NOTE: this should only be done for debugging, ie in
			 * JUnit files created for testing EvoSuite. 
			 */
			clientRunningOnThread = new Thread() {
				@Override
				public void run() {
					/*
					 * NOTE: the handling of the parameters "-D" should be handled
					 * directly in JUnit by setting the different values in Properties
					 */
					ClientProcess.main(new String[0]);
				}
			};
			clientRunningOnThread.setName("client");
			clientRunningOnThread.start();
		}
		//wait for connection from external process

		try {
			connection = server.accept();
			out = new ObjectOutputStream(connection.getOutputStream());
			in = new ObjectInputStream(connection.getInputStream());

			if (population_data == null) {
				//tell the external process to start search from scratch
				out.writeObject(Messages.NEW_SEARCH);
				out.flush();
			} else {
				out.writeObject(Messages.CONTINUE_SEARCH);
				out.flush();
				out.writeObject(population_data);
				out.flush();
			}
		} catch (Exception e) {
			logger.error("Class " + Properties.TARGET_CLASS
			        + ". Error while waiting for connection from external process ");
			return false;
		}

		startExternalProcessMessageHandler();
		startSignalHandler();
		last_command = command;

		return true;
	}

	/**
	 * <p>
	 * killProcess
	 * </p>
	 */
	public void killProcess() {
		try {
			Runtime.getRuntime().removeShutdownHook(processKillHook);
		} catch (Exception e) { /* do nothing. this can happen if shutdown is in progress */
		}

		if (process != null)
			process.destroy();
		process = null;

		if (clientRunningOnThread != null && clientRunningOnThread.isAlive()) {
			clientRunningOnThread.interrupt();
		}
		clientRunningOnThread = null;

		if (output_printer != null && output_printer.isAlive())
			output_printer.interrupt();
		output_printer = null;

		if (progress_printer != null && progress_printer.isAlive())
			progress_printer.interrupt();
		progress_printer = null;

		if (error_printer != null && error_printer.isAlive())
			error_printer.interrupt();
		error_printer = null;

		if (message_handler != null && message_handler.isAlive()) {
			message_handler.interrupt();
		}
		message_handler = null;
	}

	/**
	 * <p>
	 * getServerPort
	 * </p>
	 * 
	 * @return a int.
	 */
	public int getServerPort() {
		if (server != null)
			return server.getLocalPort();
		else
			return -1;
	}

	/**
	 * <p>
	 * openServer
	 * </p>
	 * 
	 * @return a int.
	 */
	public int openServer() {
		if (server == null) {
			try {
				server = new ServerSocket();
				server.setSoTimeout(10000);
				server.bind(null);
				return server.getLocalPort();
			} catch (Exception e) {
				logger.error("Not possible to start TCP server", e);
			}
		}
		return -1;
	}

	/**
	 * <p>
	 * closeServer
	 * </p>
	 */
	public void closeServer() {
		if (server != null) {
			try {
				server.close();
			} catch (IOException e) {
				logger.error("Error in closing the TCP server", e);
			}

			server = null;
		}
	}

	/**
	 * <p>
	 * startExternalProcessPrinter
	 * </p>
	 */
	protected void startExternalProcessPrinter() {

		if (output_printer == null || !output_printer.isAlive()) {
			output_printer = new Thread() {
				@Override
				public void run() {
					try {
						BufferedReader proc_in = new BufferedReader(
						        new InputStreamReader(process.getInputStream()));

						int data = 0;
						while (data != -1 && !isInterrupted()) {
							data = proc_in.read();
							if (data != -1 && Properties.PRINT_TO_SYSTEM) {
								System.out.print((char) data);
							}
						}

					} catch (Exception e) {
						logger.error("Exception while reading output of client process. "
						        + e.getMessage());
					}
				}
			};

			output_printer.start();
		}

		if (error_printer == null || !error_printer.isAlive()) {
			error_printer = new Thread() {
				@Override
				public void run() {
					try {
						BufferedReader proc_in = new BufferedReader(
						        new InputStreamReader(process.getErrorStream()));

						int data = 0;
						while (data != -1 && !isInterrupted()) {
							data = proc_in.read();
							if (data != -1 && Properties.PRINT_TO_SYSTEM) {
								System.err.print((char) data);
							}
						}

					} catch (Exception e) {
						logger.error("Exception while reading output of client process. "
						        + e.getMessage());
					}
				}
			};

			error_printer.start();
		}

		if (Properties.SHOW_PROGRESS
		        && (progress_printer == null || !progress_printer.isAlive())
		        && !Properties.LOG_LEVEL.equals("info")
		        && !Properties.LOG_LEVEL.equals("debug")
		        && !Properties.LOG_LEVEL.equals("trace")) {
			logger.warn("STARTING PROGRESS BAR " + Properties.SHOW_PROGRESS);
			progress_printer = ConsoleProgressBar.startProgressBar();
		}

	}

	/**
	 * <p>
	 * startExternalProcessMessageHandler
	 * </p>
	 */
	protected void startExternalProcessMessageHandler() {
		if (message_handler != null && message_handler.isAlive())
			return;

		message_handler = new Thread() {
			@Override
			public void run() {
				boolean read = true;
				while (read && !isInterrupted()) {
					String message = null;
					Object data = null;

					try {
						message = (String) in.readObject();
						data = in.readObject();
						logger.debug("Received msg: " + message);
						logger.debug("Received data: " + data);
					} catch (Exception e) {
						/*
						 * TODO: this parts need to be improved.
						 * An exception here is most likely due to the client crashing.
						 * If there is still enough budget (this might not be trivial to check,
						 * eg it could be fine for time, but not number of fitness evaluations), then
						 * we should try to re-start based on the partial info received so far, eg
						 * the best solutions found so far which was sent to master
						 */
						logger.error("Class "
						        + Properties.TARGET_CLASS
						        + ". Error in reading message. Likely the client has crashed. Error message: "
						        + e.getMessage());
						message = Messages.FINISHED_COMPUTATION;
						data = null;
					}

					if (message.equals(Messages.FINISHED_COMPUTATION)) {
						LoggingUtils.getEvoLogger().info("* Computation finished");
						read = false;
						killProcess();
						final_result = data;
						latch.countDown();
					} else if (message.equals(Messages.NEED_RESTART)) {
						//now data represent the current generation
						LoggingUtils.getEvoLogger().info("* Restarting client process");
						killProcess();
						/*
						 * TODO: this will need to be changed, to take into account
						 * a possible reduced budget
						 */
						startProcess(last_command, data);
					} else {
						killProcess();
						logger.error("Class " + Properties.TARGET_CLASS
						        + ". Error, received invalid message: ", message);
						return;
					}
				}
			}
		};
		message_handler.start();
	}

	/**
	 * <p>
	 * startSignalHandler
	 * </p>
	 */
	protected void startSignalHandler() {
		Signal.handle(new Signal("INT"), new SignalHandler() {

			private boolean interrupted = false;

			@Override
			public void handle(Signal arg0) {
				if (interrupted)
					System.exit(0);
				try {
					interrupted = true;
					if (process != null)
						process.waitFor();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		});
	}

	/**
	 * <p>
	 * waitForResult
	 * </p>
	 * 
	 * @param timeout
	 *            a int.
	 * @return a {@link java.lang.Object} object.
	 */
	public Object waitForResult(int timeout) {
		try {
			latch.await(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			logger.warn("Class "
			                    + Properties.TARGET_CLASS
			                    + ". Thread interrupted while waiting for results from client process",
			            e);
		}

		return final_result;
	}

}
