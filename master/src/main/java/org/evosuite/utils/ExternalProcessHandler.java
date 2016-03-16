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

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import org.evosuite.ClientProcess;
import org.evosuite.ConsoleProgressBar;
import org.evosuite.Properties;
import org.evosuite.result.TestGenerationResult;
import org.evosuite.result.TestGenerationResultBuilder;
import org.evosuite.rmi.MasterServices;
import org.evosuite.rmi.service.ClientNodeRemote;
import org.evosuite.rmi.service.ClientState;
import org.evosuite.runtime.sandbox.Sandbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.Signal;
import sun.misc.SignalHandler;

/*
 * this code should be used by the main master process.
 * 
 * FIXME: once RMI is stable tested, we ll need to remove all the TCP stuff, and refactor
 */

public class ExternalProcessHandler {
	/** Constant <code>logger</code> */
	protected static final Logger logger = LoggerFactory.getLogger(ExternalProcessHandler.class);

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
	/** Constant <code>WAITING_FOR_DATA</code> */
	protected static final Object WAITING_FOR_DATA = "waiting_for_data_"
			+ System.currentTimeMillis();

	protected Thread processKillHook;
	protected Thread clientRunningOnThread;

	protected volatile CountDownLatch latch;

	protected String base_dir = System.getProperty("user.dir");

	private String hsErrFile;

	/**
	 * <p>
	 * Constructor for ExternalProcessHandler.
	 * </p>
	 */
	public ExternalProcessHandler() {

	}

	/**
	 * Only for debug reasons.
	 * @param ms
	 */
	public void stopAndWaitForClientOnThread(long ms){

		if(clientRunningOnThread != null && clientRunningOnThread.isAlive()){
			clientRunningOnThread.interrupt();
		}

		long start = System.currentTimeMillis();
		while( (System.currentTimeMillis() - start)  <  ms) { //to avoid miss it in case of interrupt
			if(clientRunningOnThread != null && clientRunningOnThread.isAlive()){
				try {
					clientRunningOnThread.join(ms - (System.currentTimeMillis() - start));
					break;
				} catch (InterruptedException e) {					
				}
			} else {
				break;
			}
		}

		if( clientRunningOnThread != null && clientRunningOnThread.isAlive()) {
			throw new AssertionError( "clientRunningOnThread is alive even after waiting "+ms+"ms");
		}
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

		if(! Properties.IS_RUNNING_A_SYSTEM_TEST) {
			logger.debug("Going to start process with command: " + Arrays.toString(command).replace(",", " "));
		}

		List<String> formatted = new LinkedList<>();
		for(String s : command){
			String token = s.trim();
			if(!token.isEmpty()){
				formatted.add(token);
			}
		}

		hsErrFile = "hs_err_EvoSuite_client_p"+getServerPort()+"_t"+System.currentTimeMillis();
		String option = "-XX:ErrorFile="+hsErrFile;
		formatted.add(1,option); // add it after the first "java" command

		return startProcess(formatted.toArray(new String[0]), null);
	}

	protected boolean didClientJVMCrash(){
		return new File(hsErrFile).exists();
	}

	protected String getAndDeleteHsErrFile(){
		if(!didClientJVMCrash()){
			return null;
		}

		StringBuffer buffer = new StringBuffer();

		File file = new File(hsErrFile);
		file.deleteOnExit();

		try(Scanner in = new Scanner(file);) {
			while(in.hasNextLine()){
				String row = in.nextLine();
				//do not read the full file, just the header
				if(row.startsWith("#")){
					buffer.append(row+"\n");
				} else {
					break; //end of the header
				}
			}
		} catch (FileNotFoundException e) {
			//shouldn't really happen
			logger.error("Error while reading "+file.getAbsolutePath() + ": "+e.getMessage());
			return null;
		}

		return buffer.toString();
	}

	public String getProcessState(){
		if(process == null){
			return "null";
		}
		try{
			return "Terminated with exit status "+process.exitValue();
		} catch(IllegalThreadStateException e){
			return "Still running"; 
		}
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
            Sandbox.addPriviligedThread(clientRunningOnThread);
		}

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


		/*
		 * TODO: use RMI to 'gracefully' stop the client
		 */

		if (process != null) {
			try {
				//be sure streamers are closed, otherwise process might hang on Windows
				process.getOutputStream().close();
				process.getInputStream().close();
				process.getErrorStream().close();
			} catch (Exception t){
				logger.error("Failed to close process stream: "+t.toString());
			}
			process.destroy();
		}
		process = null;

		if (clientRunningOnThread != null && clientRunningOnThread.isAlive()) {
			clientRunningOnThread.interrupt();
		}
		clientRunningOnThread = null;

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

	/**
	 * <p>
	 * getServerPort
	 * </p>
	 * 
	 * @return a int.
	 */
	public int getServerPort() {
		return MasterServices.getInstance().getRegistryPort();
	}

	/**
	 * <p>
	 * openServer
	 * </p>
	 * 
	 * @return a int.
	 */
	public int openServer() {
		boolean started = MasterServices.getInstance().startRegistry();
		if(!started){
			logger.error("Not possible to start RMI registry");
			return -1;
		}

		try {
			MasterServices.getInstance().registerServices();
		} catch (RemoteException e) {
			logger.error("Failed to start RMI services",e);
			return -1;
		}

		return MasterServices.getInstance().getRegistryPort();

		/*
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
		 */
	}

	/**
	 * <p>
	 * closeServer
	 * </p>
	 */
	public void closeServer() {
		MasterServices.getInstance().stopServices();
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
						if(MasterServices.getInstance().getMasterNode() == null)
							return;

						boolean finished = true;
						for(ClientState state : MasterServices.getInstance().getMasterNode().getCurrentState())  {
							if(state != ClientState.DONE) {
								finished = false;
								break;
							}
						}
						if(!finished)
							logger.error("Exception while reading output of client process. "
									+ e.getMessage());
						else
							logger.debug("Exception while reading output of client process. "
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
						String errorLine ="";
						while (data != -1 && !isInterrupted()) {
							data = proc_in.read();
							if (data != -1 && Properties.PRINT_TO_SYSTEM) {
								System.err.print((char) data);
								
								errorLine += (char) data;
								if((char)data == '\n'){
									logger.error(errorLine); 
									errorLine = "";
								}
							}
						}

					} catch (Exception e) {
						if(MasterServices.getInstance().getMasterNode() == null)
							return;

						boolean finished = true;
						for(ClientState state : MasterServices.getInstance().getMasterNode().getCurrentState())  {
							if(state != ClientState.DONE) {
								finished = false;
								break;
							}
						}
						if(!finished)
							logger.error("Exception while reading output of client process. "
									+ e.getMessage());
						else
							logger.debug("Exception while reading output of client process. "
									+ e.getMessage());
					}
				}
			};

			error_printer.start();
		}

		if (Properties.SHOW_PROGRESS  && 
				(Properties.LOG_LEVEL==null ||
				(!Properties.LOG_LEVEL.equals("info")
						&& !Properties.LOG_LEVEL.equals("debug")
						&& !Properties.LOG_LEVEL.equals("trace"))
						)	
				) {
			ConsoleProgressBar.startProgressBar();
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
								+ ". Error when reading message. Likely the client has crashed. Error message: "
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
					logger.warn("",e);
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
	public TestGenerationResult waitForResult(int timeout) {

		try {
			long start = System.currentTimeMillis();
			Set<ClientNodeRemote> clients = MasterServices.getInstance().getMasterNode().getClientsOnceAllConnected(timeout);
			if(clients==null){
				logger.error("Could not access client process");
				return TestGenerationResultBuilder.buildErrorResult("Could not access client process");
			}

			for(ClientNodeRemote client : clients){
				long passed = System.currentTimeMillis() - start;
				long remaining = timeout - passed;
				if(remaining <=0 ){ remaining = 1;}
				boolean finished = client.waitUntilFinished(remaining);

				if(!finished){
					/*
					 * TODO what to do here? Try to stop the the client through RMI?
					 * Or check in which state it is, and based on that decide if giving more time?
					 */
					logger.error("Class "+ Properties.TARGET_CLASS+". Clients have not finished yet, although a timeout occurred.\n"+MasterServices.getInstance().getMasterNode().getSummaryOfClientStatuses());
				}				
			}
		} catch (InterruptedException e) {		
		} catch(RemoteException e){

			String msg = "Class "+ Properties.TARGET_CLASS+". Lost connection with clients.\n"+MasterServices.getInstance().getMasterNode().getSummaryOfClientStatuses();

			if(didClientJVMCrash()){
				String err = getAndDeleteHsErrFile();
				msg += "The JVM of the client process crashed:\n"+err;
				logger.error(msg);
			} else {
				logger.error(msg, e);
			}
		}

		killProcess();
		LoggingUtils.getEvoLogger().info("* Computation finished");

		return null; //TODO refactoring
		/*
		try {
			latch.await(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			logger.warn("Class "
			                    + Properties.TARGET_CLASS
			                    + ". Thread interrupted while waiting for results from client process",
			            e);
		}

		return final_result;
		 */
	}

}
