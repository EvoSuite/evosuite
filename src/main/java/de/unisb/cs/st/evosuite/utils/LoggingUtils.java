package de.unisb.cs.st.evosuite.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * this class is used to get help on some customization of logging facility
 * @author arcuri
 *
 */
public class LoggingUtils {

	public static final PrintStream DEFAULT_OUT = System.out;
	public static final PrintStream DEFAULT_ERR = System.err;
	
	protected static  PrintStream latestOut = null;
	protected static  PrintStream latestErr = null;
	
	public static final String LOG_TARGET = "log.target";
	public static final String LOG_LEVEL = "log.level";
	
	private static volatile boolean alreadyMuted = false;
	
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
