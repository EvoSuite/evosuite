package org.evosuite.executionmode;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.evosuite.continuous.ContinuousTestGeneration;
import org.evosuite.continuous.CtgConfiguration;
import org.evosuite.utils.ClassPathHacker;
import org.evosuite.utils.LoggingUtils;

public class Continuous {

	public enum Command {EXECUTE, INFO, CLEAN};

	public static final String NAME = "continuous";

	public static Option getOption(){
		String description = "Run Continuous Test Generation (CTG).";
		description += " Valid values are: " + Arrays.toString(Command.values());
		return new Option(NAME,true,description);
	}

	public static Object execute(Options options, List<String> javaOpts,
			CommandLine line, String cp) {



		String opt = line.getOptionValue(NAME);
		if(opt == null){
			LoggingUtils.getEvoLogger().error("Missing option for -"+NAME+". Use any of "+Arrays.toString(Command.values()));
			return null;
		}

		Command command = null;
		try{
			command = Command.valueOf(opt.toUpperCase());
		} catch(Exception e){
			LoggingUtils.getEvoLogger().error("Invalid option: "+opt+". Use any of "+Arrays.toString(Command.values()));
			return null;
		}

		String target = null;
		
		//we need to define 'target' only for execute mode
		if(command.equals(Command.EXECUTE)){

			if (line.hasOption("target")) {
				target = line.getOptionValue("target");
				
				/*
				 * We could issue a warning, but to make things easier (so user need to type less),
				 * let's just add the target automatically to the classpath.
				 * This is useful for when we do not want to specify the classpath (default '.'),
				 * and so just typing '-target' on command line
				 */
				if(!cp.contains(target)){
					cp += File.pathSeparator + target;
				}
			} 
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

		String prefix = null;
		if (line.hasOption("prefix")) {
			prefix = line.getOptionValue("prefix");
		} 

		ContinuousTestGeneration ctg = new ContinuousTestGeneration(
				target,
				cp,
				prefix,
				CtgConfiguration.getFromParameters()
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
