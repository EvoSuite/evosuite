package de.unisb.cs.st.evosuite.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.Signal;
import sun.misc.SignalHandler;

/*
 * this code should be used by the main process
 */

@SuppressWarnings("restriction")
public class ExternalProcessHandler {
	protected static Logger logger = LoggerFactory.getLogger(ExternalProcessHandler.class);

	protected ServerSocket server;
	protected Process process;
	protected String[] last_command;

	protected Thread output_printer;
	protected Thread error_printer;
	protected Thread message_handler;

	protected Socket connection;
	protected ObjectOutputStream out;
	protected ObjectInputStream in;

	protected Object final_result;
	protected static final Object WAITING_FOR_DATA = "waiting_for_data_"
	        + System.currentTimeMillis();
	protected final Object MONITOR = new Object();

	protected Thread processKillHook;

	public ExternalProcessHandler() {

	}

	public boolean startProcess(String[] command) {
		return startProcess(command, null);
	}

	protected boolean startProcess(String[] command, Object population_data) {
		if (process != null) {
			logger.warn("Already running an external process");
			return false;
		}

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

		File dir = new File(System.getProperty("user.dir"));
		ProcessBuilder builder = new ProcessBuilder(command);
		builder.directory(dir);
		builder.redirectErrorStream(false);

		try {
			process = builder.start();
		} catch (IOException e) {
			logger.error("Failed to start external process", e);
			return false;
		}

		startExternalProcessPrinter();

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
			logger.error("Error while waiting for connection from external process ");
			return false;
		}

		startExternalProcessMessageHandler();
		startSignalHandler();
		last_command = command;

		return true;
	}

	public void killProcess() {
		try {
			Runtime.getRuntime().removeShutdownHook(processKillHook);
		} catch (Exception e) { /* do nothing. this can happen if shutdown is in progress */
		}

		if (process != null)
			process.destroy();
		process = null;

		if (output_printer != null && output_printer.isAlive())
			output_printer.interrupt();
		output_printer = null;

		if (error_printer != null && error_printer.isAlive())
			error_printer.interrupt();
		error_printer = null;

		if (message_handler != null && message_handler.isAlive()) {
			message_handler.interrupt();
		}
		message_handler = null;
	}

	public int getServerPort() {
		if (server != null)
			return server.getLocalPort();
		else
			return -1;
	}

	public void openServer() {
		if (server == null) {
			try {
				server = new ServerSocket();
				server.setSoTimeout(10000);
				server.bind(null);
			} catch (Exception e) {
				logger.error("Not possible to start TCP server", e);
			}
		}

	}

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
							if (data != -1)
								System.out.print((char) data);
						}

					} catch (Exception e) {
						logger.error("Exception while reading output of client process",
						             e);
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
							if (data != -1)
								System.err.print((char) data);
						}

					} catch (Exception e) {
						logger.error("Exception while reading output of client process",
						             e);
					}
				}
			};

			error_printer.start();
		}

	}

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
					} catch (Exception e) {
						/*
						 * TODO: this parts need to be improved.
						 * An exception here is most likely due to the client crashing.
						 * If there is still enough budget (this might not be trivial to check,
						 * eg it could be fine for time, but not number of fitness evaluations), then
						 * we should try to re-start based on the partial info received so far, eg
						 * the best solutions found so far which was sent to master
						 */
						logger.error("Error in reading message ", e);
						message = Messages.FINISHED_COMPUTATION;
						data = null;
					}

					if (message.equals(Messages.FINISHED_COMPUTATION)) {
						System.out.println("* Computation finished");
						read = false;
						killProcess();
						final_result = data;
						synchronized (MONITOR) {
							MONITOR.notifyAll();
						}
					} else if (message.equals(Messages.NEED_RESTART)) {
						//now data represent the current generation
						System.out.println("* Restarting client process");
						killProcess();
						/*
						 * TODO: this will need to be changed, to take into account
						 * a possible reduced budget
						 */
						startProcess(last_command, data);
					} else {
						killProcess();
						logger.error("Error, received invalid message: ", message);
						return;
					}
				}
			}
		};
		message_handler.start();
	}

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

	public Object waitForResult(int timeout) {
		try {
			synchronized (MONITOR) {
				if (WAITING_FOR_DATA.equals(final_result)) {
					MONITOR.wait(timeout);
				}
			}
		} catch (InterruptedException e) {
			logger.warn("Thread interrupted while waiting for results from client process",
			            e);
		}

		return final_result;
	}

}
