package de.unisb.cs.st.evosuite.utils;

/**
 * this class is used to get help on some customization of logging facility
 * @author arcuri
 *
 */
public class LoggingUtils {

	public static final String LOG_TARGET = "log.target";
	public static final String LOG_LEVEL = "log.level";
	
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
