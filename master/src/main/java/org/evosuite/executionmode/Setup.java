package org.evosuite.executionmode;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.io.FileUtils;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.NoSuchParameterException;
import org.evosuite.utils.LoggingUtils;

public class Setup {

	public static final String NAME = "setup";
	
	public static Option getOption(){
		return new Option(NAME,true,"Create evosuite-files with property file");
	}

	public static Object execute(List<String> javaOpts, CommandLine line) {
		boolean inheritanceTree = line.hasOption("inheritanceTree");
		setup(line.getOptionValue("setup"), line.getArgs(), javaOpts, inheritanceTree);
		return null;
	}

	private static void addEntryToCP(String entry){
		if (!Properties.CP.isEmpty()){
			Properties.CP += File.pathSeparator;
		}
		Properties.CP += entry;
	}

	private static void setup(String target, String[] args, List<String> javaArgs,
	        boolean doInheritance) {

		Properties.CP = "";

		/*
			Important that target will be first on the CP.
			Otherwise, if for some reasons a dependency uses a same class,
			that would take precedence
		 */
		File targetFile = new File(target);
		if (targetFile.exists()) {
			if (targetFile.isDirectory() || target.endsWith(".jar")) {
				addEntryToCP(target);
			} else if (target.endsWith(".class")) {
				String pathName = targetFile.getParent();
				addEntryToCP(pathName);
			} else {
				LoggingUtils.getEvoLogger().info("Failed to set up classpath for "
						+ target);
				return;
			}
		}

		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				String element = args[i].trim();
				if(element.isEmpty()){
					continue;
				}
				addEntryToCP(element);
			}
		}

		Properties.MIN_FREE_MEM = 0; //TODO why this is done???
		File directory = new File(EvoSuite.base_dir_path + File.separator + Properties.OUTPUT_DIR);
		if (!directory.exists()) {
			directory.mkdir();
		}

		if (doInheritance) {
			try {
				String fileName = EvoSuite.generateInheritanceTree(Properties.CP);
				FileUtils.copyFile(new File(fileName), new File(Properties.OUTPUT_DIR
				        + File.separator + "inheritance.xml.gz"));
				
				 /* 
				  * we need to use '/' instead of File.separator because this value will be written on a text file.
				  * As the relative path will be given to a File object, this will work also on a Windows machine 
				  */
				Properties.getInstance().setValue("inheritance_file",
				                                  Properties.OUTPUT_DIR + "/"
				                                          + "inheritance.xml.gz");
			} catch (IOException | IllegalArgumentException | NoSuchParameterException | IllegalAccessException e) {
				LoggingUtils.getEvoLogger().error("* Error while creating inheritance tree: " + e.getMessage());
			}
		}

		LoggingUtils.getEvoLogger().info("* Creating new evosuite.properties in "
		                                         + EvoSuite.base_dir_path + File.separator
		                                         + Properties.OUTPUT_DIR);
		LoggingUtils.getEvoLogger().info("* Classpath: " + Properties.CP);
		Properties.getInstance().writeConfiguration(EvoSuite.base_dir_path + File.separator
		                                                    + Properties.OUTPUT_DIR
		                                                    + File.separator
		                                                    + "evosuite.properties");
	}
	

}
