/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.executionmode;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.evosuite.Properties;
import org.evosuite.classpath.ClassPathHacker;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.continuous.ContinuousTestGeneration;
import org.evosuite.continuous.CtgConfiguration;
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
			CommandLine line) {

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
		if(line.hasOption("target") && command.equals(Command.EXECUTE)){
			target = line.getOptionValue("target");				
		}


		String cp = ClassPathHandler.getInstance().getTargetProjectClasspath();

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

		String prefix = "";
		if (line.hasOption("prefix")) {
			prefix = line.getOptionValue("prefix");
		} 

		String[] cuts = null;
		if(Properties.CTG_SELECTED_CUTS != null && !Properties.CTG_SELECTED_CUTS.isEmpty()){
			cuts  = Properties.CTG_SELECTED_CUTS.trim().split(",");
		} 

		ContinuousTestGeneration ctg = new ContinuousTestGeneration(
				target,
				cp,
				prefix,
				CtgConfiguration.getFromParameters(),
				cuts,
				Properties.CTG_EXPORT_FOLDER
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
