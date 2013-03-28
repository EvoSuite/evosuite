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
/**
 * 
 */
package org.evosuite;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.evosuite.executionmode.Help;
import org.evosuite.executionmode.ListClasses;
import org.evosuite.executionmode.ListParameters;
import org.evosuite.executionmode.MeasureCoverage;
import org.evosuite.executionmode.PrintStats;
import org.evosuite.executionmode.Setup;
import org.evosuite.executionmode.TestGeneration;
import org.evosuite.setup.InheritanceTree;
import org.evosuite.setup.InheritanceTreeGenerator;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.Randomness;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * <p>
 * EvoSuite class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class EvoSuite {

	static {
		LoggingUtils.checkAndSetLogLevel();
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		// Only overrule default configurations
		// TODO: Find better way to allow external logback configuration
		if (context.getName().equals("default")) {
			try {
				JoranConfigurator configurator = new JoranConfigurator();
				configurator.setContext(context);
				InputStream f = EvoSuite.class.getClassLoader().getResourceAsStream("logback-evosuite.xml");
				if (f == null) {
					System.err.println("logback-evosuite.xml not found on classpath");
				}
				context.reset();
				configurator.doConfigure(f);
			} catch (JoranException je) {
				// StatusPrinter will handle this
			}
			StatusPrinter.printInCaseOfErrorsOrWarnings(context);
		}

	}

	private static Logger logger = LoggerFactory.getLogger(EvoSuite.class);

	private static String separator = System.getProperty("file.separator");
	private static String javaHome = System.getProperty("java.home");

	public static String evosuiteJar = "";

	/**
	 * Constant
	 * <code>JAVA_CMD="javaHome + separator + bin + separatorj"{trunked}</code>
	 */
	public final static String JAVA_CMD = javaHome + separator + "bin" + separator
	        + "java";

	public static String base_dir_path = System.getProperty("user.dir");

	public static boolean isInterface(String resource) throws IOException {

		ClassReader reader = new ClassReader(
		        EvoSuite.class.getClassLoader().getResourceAsStream(resource));
		ClassNode cn = new ClassNode();
		reader.accept(cn, ClassReader.SKIP_FRAMES);
		return (cn.access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE;
	}

	public static String generateInheritanceTree(String cp) throws IOException {
		LoggingUtils.getEvoLogger().info("* Analyzing classpath");
		List<String> cpList = Arrays.asList(cp.split(File.pathSeparator));
		InheritanceTree tree = InheritanceTreeGenerator.analyze(cpList);
		File outputFile = File.createTempFile("ES_inheritancetree", ".xml.gz");
		outputFile.deleteOnExit();
		InheritanceTreeGenerator.writeInheritanceTree(tree, outputFile);
		return outputFile.getAbsolutePath();
	}

	private void setupProperties() {
		if (base_dir_path.equals("")) {
			Properties.getInstanceSilent();
		} else {
			Properties.getInstanceSilent().loadProperties(base_dir_path
			                                                      + separator
			                                                      + Properties.PROPERTIES_FILE,
			                                              true);
		}
	}

	/**
	 * <p>
	 * parseCommandLine
	 * </p>
	 * 
	 * @param args
	 *            an array of {@link java.lang.String} objects.
	 * @return a {@link java.lang.Object} object.
	 */
	public Object parseCommandLine(String[] args) {
		Options options = getCommandLineOptions();

		List<String> javaOpts = new ArrayList<String>();

		String version = EvoSuite.class.getPackage().getImplementationVersion();
		if (version == null) {
			version = "";
		}
		LoggingUtils.getEvoLogger().info("* EvoSuite " + version);

		// create the parser
		CommandLineParser parser = new GnuParser();
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			setupProperties();

			addJavaDOptions(javaOpts, line);

			/*
			 * NOTE: JVM arguments will not be passed over from the master to the client. So for -Xmx, we need to use "mem"
			 */
			if (line.hasOption("mem")) {
				javaOpts.add("-Xmx" + line.getOptionValue("mem") + "M");
			}

			if (line.hasOption("heapdump")) {
				javaOpts.add("-XX:+HeapDumpOnOutOfMemoryError");
			}

			if (line.hasOption("jar")) {
				evosuiteJar = line.getOptionValue("jar");
			}

			if (!line.hasOption("regressionSuite")) {
				if (line.hasOption("criterion")) {
					javaOpts.add("-Dcriterion=" + line.getOptionValue("criterion"));
				}
			} else {
				javaOpts.add("-Dcriterion=regression");
			}

			if (line.hasOption("seed")) {
				javaOpts.add("-Drandom.seed=" + line.getOptionValue("seed"));
			}

			if (line.hasOption("base_dir")) {
				base_dir_path = line.getOptionValue("base_dir");
				File baseDir = new File(base_dir_path);
				if (!baseDir.exists()) {
					LoggingUtils.getEvoLogger().error("Base directory does not exist: "
					                                          + base_dir_path);
					return null;
				}
				if (!baseDir.isDirectory()) {
					LoggingUtils.getEvoLogger().error("Specified base directory is not a directory: "
					                                          + base_dir_path);
					return null;
				}
			}

			/*
			 * Following "options" are the actual (mutually exclusive) execution modes of EvoSuite
			 */

			String cp = getClassPath(line);

			if (line.hasOption(Help.NAME)) {
				return Help.execute(options);
			}

			if (line.hasOption(Setup.NAME)) {
				return Setup.execute(javaOpts, line);
			}

			if (line.hasOption(MeasureCoverage.NAME)) {
				return MeasureCoverage.execute(options, javaOpts, line, cp);
			}

			if (line.hasOption(ListClasses.NAME)) {
				return ListClasses.execute(options, line, cp);
			}

			if (line.hasOption(PrintStats.NAME)) {
				return PrintStats.execute(options, javaOpts, line, cp);
			}

			if (line.hasOption(ListParameters.NAME)) {
				return ListParameters.execute();
			}

			return TestGeneration.executeTestGeneration(options, javaOpts, line, cp);

		} catch (ParseException exp) {
			// oops, something went wrong
			logger.error("Parsing failed.  Reason: " + exp.getMessage());
			// automatically generate the help statement
			Help.execute(options);
		}

		return null;
	}

	public static boolean hasLegacyTargets() {
		File directory = new File(Properties.OUTPUT_DIR);
		if(!directory.exists()){
			return false;
		}
		String[] extensions = { "task" };
		return !FileUtils.listFiles(directory, extensions, false).isEmpty();
	}

	/**
	 * Add all the properties that were set with -D
	 * 
	 * @param javaOpts
	 * @param line
	 * @throws Error
	 */
	private void addJavaDOptions(List<String> javaOpts, CommandLine line) throws Error {
		java.util.Properties properties = line.getOptionProperties("D");
		Set<String> propertyNames = new HashSet<String>(Properties.getParameters());
		for (String propertyName : properties.stringPropertyNames()) {
			if (!propertyNames.contains(propertyName)) {
				LoggingUtils.getEvoLogger().error("* Unknown property: " + propertyName);
				throw new Error("Unknown property: " + propertyName);
			}
			String propertyValue = properties.getProperty(propertyName);
			javaOpts.add("-D" + propertyName + "=" + propertyValue);
			System.setProperty(propertyName, propertyValue);
			try {
				Properties.getInstance().setValue(propertyName, propertyValue);
			} catch (Exception e) {
				// Ignore?
			}
		}
	}

	private String getClassPath(CommandLine line) {
		String cp = "";
		if (line.hasOption("cp")) {
			String[] cpEntries = line.getOptionValues("cp");
			if (cpEntries.length > 0) {
				boolean first = true;
				for (String entry : cpEntries) {
					if (first) {
						first = false;
					} else {
						cp += ":";
					}
					cp += entry;
				}
			}
		} else {
			cp = Properties.CP;
		}
		return cp;
	}

	private Options getCommandLineOptions() {
		Options options = new Options();

		Option help = Help.getOption();
		Option setup = Setup.getOption();
		Option measureCoverage = MeasureCoverage.getOption();
		Option listClasses = ListClasses.getOption();
		Option printStats = PrintStats.getOption();
		Option listParameters = ListParameters.getOption();

		Option[] generateOptions = TestGeneration.getOptions();

		Option targetClass = new Option("class", true, "target class for test generation");
		Option targetPrefix = new Option("prefix", true,
		        "target prefix for test generation");
		Option targetCP = new Option("target", true,
		        "target classpath for test generation");
		Option classPath = new Option("cp", true,
		        "classpath of the project under test and dependencies");
		classPath.setValueSeparator(':');
		Option junitPrefix = new Option("junit", true, "junit prefix");
		Option criterion = new Option("criterion", true,
		        "target criterion for test generation");
		Option seed = new Option("seed", true, "seed for random number generator");
		Option mem = new Option("mem", true,
		        "heap size for client process (in megabytes)");
		Option jar = new Option("jar", true,
		        "location of EvoSuite jar file to use in client process");
		Option extendSuite = new Option("extend", true, "extend an existing test suite");

		Option inheritance = new Option("inheritanceTree",
		        "Cache inheritance tree during setup");
		Option heapDump = new Option("heapdump",
		        "Create heap dump on client VM out of memory error");

		Option base_dir = new Option("base_dir", true,
		        "Working directory in which tests and reports will be placed");

		Option property = OptionBuilder.withArgName("property=value").hasArgs(2).withValueSeparator().withDescription("use value for given property").create("D");

		for (Option option : generateOptions) {
			options.addOption(option);
		}

		options.addOption(listParameters);
		options.addOption(help);
		options.addOption(extendSuite);
		options.addOption(measureCoverage);
		options.addOption(listClasses);
		options.addOption(printStats);
		options.addOption(setup);
		options.addOption(targetClass);
		options.addOption(targetPrefix);
		options.addOption(targetCP);
		options.addOption(junitPrefix);
		options.addOption(criterion);
		options.addOption(seed);
		options.addOption(mem);
		options.addOption(jar);
		options.addOption(inheritance);
		options.addOption(base_dir);
		options.addOption(property);
		options.addOption(classPath);
		options.addOption(heapDump);

		return options;
	}

	/**
	 * <p>
	 * main
	 * </p>
	 * 
	 * @param args
	 *            an array of {@link java.lang.String} objects.
	 */
	public static void main(String[] args) {
		try {
			EvoSuite evosuite = new EvoSuite();
			evosuite.parseCommandLine(args);
		} catch (Throwable t) {
			logger.error("Fatal crash on main EvoSuite process. Class "
			        + Properties.TARGET_CLASS + " using seed " + Randomness.getSeed()
			        + ". Configuration id : " + Properties.CONFIGURATION_ID, t);
			System.exit(-1);
		}

		/*
		 * Some threads could still be running, so we need to kill the process explicitly
		 */
		System.exit(0);
	}

}
