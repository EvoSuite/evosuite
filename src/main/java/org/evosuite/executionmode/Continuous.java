package org.evosuite.executionmode;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.continuous.ContinuousTestGeneration;
import org.evosuite.utils.ClassPathHacker;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.ResourceList;

public class Continuous {

	public enum Command {EXECUTE, INFO, CLEAN};
	
	public static final String NAME = "continuous";
	
	public static Option getOption(){
		return new Option(NAME,true,"Run Continuous Test Generation");
	}

	public static Object execute(Options options, List<String> javaOpts,
			CommandLine line, String cp) {
		
		String target = null;
		
		if (line.hasOption("target")) {
			target = line.getOptionValue("target");
		} else {
			LoggingUtils.getEvoLogger().error(
					"Please specify target ('-target' option) folder/jar classpath entry  to "+
					"indicate on which classes to apply Continuous Test Generation");
			return null;
		}
		
		/*
		 * We could issue a warning, but to make things easier (so user need to type less),
		 * let's just add the target automatically to the classpath.
		 * This is useful for when we do not want to specify the classpath (default '.'),
		 * and so just typing '-target' on command line
		 */
		if(!cp.contains(target)){
			cp += File.pathSeparator + target;
		}
		
		/*
		 * Setup the classpath
		 */
		for (String classPathElement : cp.split(File.pathSeparator)) {			
			try {
				ClassPathHacker.addFile(classPathElement);
			} catch (IOException e) {
				// Ignore?
			}
		}
		
		String opt = line.getOptionValue(NAME);
		if(opt == null){
			LoggingUtils.getEvoLogger().error("Missing option for -"+NAME+". Use any of "+Arrays.toString(Command.values()));
			return null;
		}
		
		Command command = null;
		try{
			command = Command.valueOf(opt);
		} catch(Exception e){
			LoggingUtils.getEvoLogger().error("Invalid option: "+opt+". Use any of "+Arrays.toString(Command.values()));
			return null;
		}
		
		
		ContinuousTestGeneration ctg = new ContinuousTestGeneration(
				target,
				cp,
				Properties.CTG_MEMORY, 
				Properties.CTG_CORES, 
				Properties.CTG_TIME, 
				false, /* just for now */
				Properties.CTG_SCHEDULE
				);
		
		/*
		 * Based on command line option, execute one of the different CTG command
		 */
		if(command.equals(Command.EXECUTE)){
			String result = ctg.execute();
			LoggingUtils.getEvoLogger().info(result);
		} else if(command.equals(Command.CLEAN)){
			boolean cleaned = ctg.clean();
			if(cleaned){
				LoggingUtils.getEvoLogger().info("Cleaned all project data");
			} else {
				LoggingUtils.getEvoLogger().info("Failed to clean project");
			}
		} else { //INFO
			String info = ctg.info();
			LoggingUtils.getEvoLogger().info(info);
		}
				
		return null;
	}
	
}
