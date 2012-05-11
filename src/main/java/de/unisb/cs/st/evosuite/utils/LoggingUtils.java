package de.unisb.cs.st.evosuite.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uispec4j.utils.Log;

import ch.qos.logback.classic.net.SocketNode;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * this class is used to get help on some customization of logging facility
 * @author arcuri
 *
 */
public class LoggingUtils {

	private static final Logger log = LoggerFactory.getLogger(LoggingUtils.class);
	
	public static final PrintStream DEFAULT_OUT = System.out;
	public static final PrintStream DEFAULT_ERR = System.err;

	private static final String EVO_LOGGER = "evo_logger";
	
	protected static  PrintStream latestOut = null;
	protected static  PrintStream latestErr = null;
	
	public static final String LOG_TARGET = "log.target";
	public static final String LOG_LEVEL = "log.level";
	
	private static volatile boolean alreadyMuted = false;
	
	
	
	private ServerSocket serverSocket;
	
	private final ExecutorService logConnections = Executors.newSingleThreadExecutor();
	private final ExecutorService logHandler = Executors.newCachedThreadPool();
	
	
	
	public LoggingUtils(){
		
	}
	
	public static Logger getEvoLogger(){
		return LoggerFactory.getLogger(EVO_LOGGER);
	}
	
	public boolean startLogServer(){
		try{
			serverSocket = new ServerSocket();
			serverSocket.bind(null);
			
			logConnections.submit(new Callable<Void>(){
				public Void call() throws Exception {
					while(!isServerClosed()){
						final Socket socket = serverSocket.accept();
						
						logHandler.submit(new Callable<Void>(){
							public Void call() {
								try {
									ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
									while(socket!=null && socket.isConnected() && !isServerClosed()){
										ILoggingEvent event = (ILoggingEvent) ois.readObject();
										// get a logger from the hierarchy. The name of the logger is taken to
										// be the name contained in the event.
										ch.qos.logback.classic.Logger remoteLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(event.getLoggerName());
										// apply the logger-level filter
										if (remoteLogger.isEnabledFor(event.getLevel())) {
											// finally log the event as if was generated locally
											remoteLogger.callAppenders(event);
										}
									}
								} catch (Exception e) {
									log.error("Problem in reading loggings",e);
								}
								return null;
							}});
					}
					return null;
				}});
			
			return true;
		} catch(Exception e){
			log.error("Can't start log server",e);
			return false;
		}
	}
	
	public boolean isServerClosed(){
		return serverSocket==null || serverSocket.isClosed() || !serverSocket.isBound();
	}
	
	public Integer getLogServerPort(){
		if(isServerClosed()){
			return null;
		}
		return serverSocket.getLocalPort();
	}
	
	public void closeLogServer(){
		if(serverSocket!=null && !serverSocket.isClosed()){
			try {
				serverSocket.close();
			} catch (IOException e) {
				log.error("Error in closing log server",e);
			}
			serverSocket = null;
		}
	}
	
	/**
	 * Redirect current System.out and System.err to a buffer
	 */
	public static void muteCurrentOutAndErrStream(){
		if(alreadyMuted) {return;}
		
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
	public static void restorePreviousOutAndErrStream(){
		if(!alreadyMuted) {return;}
		System.setOut(latestOut);
		System.setErr(latestErr);
		alreadyMuted = false;
	}
	
	/**
	 * Allow again printing to System.out and System.err
	 */
	public static void restoreDefaultOutAndErrStream(){
		System.setOut(DEFAULT_OUT);
		System.setErr(DEFAULT_ERR);
	}
	
	/**
	 * In logback.xml we use properties like ${log.level}. But before doing any logging, we
	 * need to be sure they have been set. If not, we put them to default values
	 * 
	 * NOTE: this functionality might be deprecated now, as one can set default values
	 * in the log xml files by using ":-"
	 * 
	 * @return
	 */
	public static boolean checkAndSetLogLevel(){
		
		boolean usingDefault = false;
		
		String logLevel = System.getProperty(LOG_LEVEL);
		if(logLevel == null || logLevel.trim().equals("")){
			System.setProperty(LOG_LEVEL, "WARN");
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
