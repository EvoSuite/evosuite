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
 */
package org.evosuite.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.evosuite.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * this class is used to get help on some customization of logging facility
 * 
 * @author arcuri
 */
public class LoggingUtils {

	private static final Logger log = LoggerFactory.getLogger(LoggingUtils.class);

	/** Constant <code>DEFAULT_OUT</code> */
	public static final PrintStream DEFAULT_OUT = System.out;
	/** Constant <code>DEFAULT_ERR</code> */
	public static final PrintStream DEFAULT_ERR = System.err;

	private static final String EVO_LOGGER = "evo_logger";

	/** Constant <code>latestOut</code> */
	protected static PrintStream latestOut = null;
	/** Constant <code>latestErr</code> */
	protected static PrintStream latestErr = null;

	/** Constant <code>LOG_TARGET="log.target"</code> */
	public static final String LOG_TARGET = "log.target";
	/** Constant <code>LOG_LEVEL="log.level"</code> */
	public static final String LOG_LEVEL = "log.level";

	private static volatile boolean alreadyMuted = false;

	private ServerSocket serverSocket;

	private final ExecutorService logConnections = Executors.newSingleThreadExecutor();
	private final ExecutorService logHandler = Executors.newCachedThreadPool();

	/**
	 * <p>
	 * Constructor for LoggingUtils.
	 * </p>
	 */
	public LoggingUtils() {

	}

	/**
	 * <p>
	 * getEvoLogger
	 * </p>
	 * 
	 * @return a {@link org.slf4j.Logger} object.
	 */
	public static Logger getEvoLogger() {
		return LoggerFactory.getLogger(EVO_LOGGER);
	}

	/**
	 * <p>
	 * startLogServer
	 * </p>
	 * 
	 * @return a boolean.
	 */
	public boolean startLogServer() {
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(null);

			logConnections.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					while (!isServerClosed()) {
						final Socket socket = serverSocket.accept();

						logHandler.submit(new Callable<Void>() {
							@Override
							public Void call() {
								try {
									ObjectInputStream ois = new ObjectInputStream(
									        new BufferedInputStream(
									                socket.getInputStream()));
									while (socket != null && socket.isConnected()
									        && !isServerClosed()) {
										ILoggingEvent event = (ILoggingEvent) ois.readObject();
										/*
										 * We call the appender regardless of level in the master (ie, if the level was
										 * set in the client and we receive a log message, then we just print it). 
										 * Note: we use 
										 * the local logger with same name just for formatting reasons 
										 */
										ch.qos.logback.classic.Logger remoteLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(event.getLoggerName());
										remoteLogger.callAppenders(event);
									}
								} catch (java.net.SocketException se) {
									/*
									 * FIXME: this can happen if client dies or is stopped by master with "destroy" in Windows. It is not a big problem,
									 * but anyway how we stop clients will need to be refactored.
									 * It can also happen if client just crash. As we already report such info in ExternalProcessHandler, we do not need to
									 * log it here
									 */
								} catch(java.io.UTFDataFormatException utfe){
									//as above
								}
								catch (java.io.EOFException eof) {
									//this is normal, do nothing
								} catch(java.io.InvalidClassException ice){
									/*
									 * TODO: unclear why it happens... need more investigation 
									 */
									log.error("Error in de-serialized log event: "+ice.getMessage());
								}
								catch (Exception e) {
									log.error("Problem in reading loggings", e);
								}
								return null;
							}
						});
					}
					return null;
				}
			});

			return true;
		} catch (Exception e) {
			log.error("Can't start log server", e);
			return false;
		}
	}

	/**
	 * <p>
	 * isServerClosed
	 * </p>
	 * 
	 * @return a boolean.
	 */
	public boolean isServerClosed() {
		return serverSocket == null || serverSocket.isClosed() || !serverSocket.isBound();
	}

	/**
	 * <p>
	 * getLogServerPort
	 * </p>
	 * 
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getLogServerPort() {
		if (isServerClosed()) {
			return null;
		}
		return serverSocket.getLocalPort();
	}

	/**
	 * <p>
	 * closeLogServer
	 * </p>
	 */
	public void closeLogServer() {
		if (serverSocket != null && !serverSocket.isClosed()) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				log.error("Error in closing log server", e);
			}
			serverSocket = null;
		}
	}

	/**
	 * Redirect current System.out and System.err to a buffer
	 */
	public static void muteCurrentOutAndErrStream() {
		if (alreadyMuted) {
			return;
		}

		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		PrintStream outStream = new PrintStream(byteStream);
		latestOut = System.out;
		latestErr = System.err;
		System.setOut(outStream);
		System.setErr(outStream);

		alreadyMuted = true;
	}

	/**
	 * Allow again printing to previous streams that were muted
	 */
	public static void restorePreviousOutAndErrStream() {
		if (!alreadyMuted) {
			return;
		}
		System.setOut(latestOut);
		System.setErr(latestErr);
		alreadyMuted = false;
	}

	/**
	 * Allow again printing to System.out and System.err
	 */
	public static void restoreDefaultOutAndErrStream() {
		System.setOut(DEFAULT_OUT);
		System.setErr(DEFAULT_ERR);
	}

	/**
	 * In logback.xml we use properties like ${log.level}. But before doing any
	 * logging, we need to be sure they have been set. If not, we put them to
	 * default values
	 * 
	 * NOTE: this functionality might be deprecated now, as one can set default
	 * values in the log xml files by using ":-"
	 * 
	 * @return a boolean.
	 */
	public static boolean checkAndSetLogLevel() {

		boolean usingDefault = false;

		System.setProperty(LOG_LEVEL, Properties.LOG_LEVEL);
		if (Properties.LOG_TARGET != null)
			System.setProperty(LOG_TARGET, Properties.LOG_TARGET);

		if (Properties.LOG_LEVEL.equals("WARN")) {
			usingDefault = true;
		}
		
		//No need to check/set for LOG_TARGET
		/*
		String logTarget = System.getProperty(LOG_TARGET);
		if(logTarget==null || logTarget.trim().equals("")){
			System.setProperty(LOG_TARGET,"");
			usingDefault = true;
		}
		*/

		return usingDefault;
	}

}
