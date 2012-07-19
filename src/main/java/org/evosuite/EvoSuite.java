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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.evosuite.Properties.Strategy;
import org.evosuite.javaagent.InstrumentingClassLoader;
import org.evosuite.utils.ClassPathHacker;
import org.evosuite.utils.ExternalProcessHandler;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.ResourceList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * EvoSuite class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class EvoSuite {

	private static final boolean logLevelSet = LoggingUtils.checkAndSetLogLevel();

	private static Logger logger = LoggerFactory.getLogger(EvoSuite.class);

	private static String separator = System.getProperty("file.separator");
	private static String javaHome = System.getProperty("java.home");
	private static String evosuiteJar = "";
	/**
	 * Constant
	 * <code>JAVA_CMD="javaHome + separator + bin + separatorj"{trunked}</code>
	 */
	public final static String JAVA_CMD = javaHome + separator + "bin" + separator
	        + "java";

	private static String base_dir_path = System.getProperty("user.dir");

	private static void setup(String target, String[] args, List<String> javaArgs) {

		Properties.CP = "";

		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				if (!Properties.CP.equals(""))
					Properties.CP += File.pathSeparator;
				Properties.CP += args[i];
			}
		}

		Properties.MIN_FREE_MEM = 0;
		File directory = new File(base_dir_path + separator + Properties.OUTPUT_DIR);
		if (!directory.exists()) {
			directory.mkdir();
		}

		File targetFile = new File(target);
		if (targetFile.exists()) {
			if (targetFile.isDirectory() || target.endsWith(".jar")) {
				Properties.CP += File.pathSeparator;
				Properties.CP += target;
			} else if (target.endsWith(".class")) {
				String pathName = targetFile.getParent();
				Properties.CP += File.pathSeparator;
				Properties.CP += pathName;
			} else {
				System.out.println("Failed to set up classpath for " + target);
				return;
			}
		}
		System.out.println("* Creating new evosuite.properties in " + base_dir_path
		        + separator + Properties.OUTPUT_DIR);
		Properties.getInstance().writeConfiguration(base_dir_path + separator
		                                                    + Properties.OUTPUT_DIR
		                                                    + separator
		                                                    + "evosuite.properties");
	}

	private static void generateTestsPrefix(Properties.Strategy strategy, String prefix,
	        List<String> args, String cp) {

		Pattern pattern = Pattern.compile(prefix.replace("\\.", "/") + "[^\\$]*.class");
		Collection<String> resources = ResourceList.getResources(pattern);
		System.out.println("* Found " + resources.size()
		        + " matching classes for prefix " + prefix);
		for (String resource : resources) {
			System.out.println("* Current class: "
			        + resource.replace(".class", "").replace('/', '.'));
			generateTests(Strategy.EVOSUITE,
			              resource.replace(".class", "").replace('/', '.'), args, cp);
		}
	}

	private static void generateTestsTarget(Properties.Strategy strategy, String target,
	        List<String> args, String cp) {

		Pattern pattern = Pattern.compile("[^\\$]*.class");
		Collection<String> resources = ResourceList.getResources(pattern, target);
		System.out.println("* Found " + resources.size() + " matching classes in target "
		        + target);

		for (String resource : resources) {
			System.out.println("* Current class: "
			        + resource.replace(".class", "").replace('/', '.'));
			generateTests(Strategy.EVOSUITE,
			              resource.replace(".class", "").replace('/', '.'), args, cp);
		}
	}

	private static boolean findTargetClass(String target, String cp) {

		String oldCP = Properties.CP;

		Properties.CP = cp;
		if (Properties.CP != null && !Properties.CP.isEmpty()
		        && ResourceList.hasClass(target)) {
			return true;
		}

		Properties.CP = oldCP;
		if (Properties.CP != null && !Properties.CP.isEmpty()
		        && ResourceList.hasClass(target)) {
			return true;
		}

		Properties.CP = System.getProperty("java.class.path");
		if (Properties.CP != null && !Properties.CP.isEmpty()
		        && ResourceList.hasClass(target)) {
			return true;
		}

		Properties.CP = System.getenv("CLASSPATH");
		if (Properties.CP != null && !Properties.CP.isEmpty()
		        && ResourceList.hasClass(target)) {
			return true;
		}

		LoggingUtils.getEvoLogger().info("* Unknown class: " + target);

		return false;
	}

	private static Object generateTests(Properties.Strategy strategy, String target,
	        List<String> args, String cp) {
		String classPath = System.getProperty("java.class.path");
		if (!evosuiteJar.equals("")) {
			classPath += File.pathSeparator + evosuiteJar;
		}

		if (!findTargetClass(target, cp)) {
			return null;
		}

		classPath += File.pathSeparator + Properties.CP;

		if (!InstrumentingClassLoader.checkIfCanInstrument(target)) {
			throw new IllegalArgumentException(
			        "Cannot consider "
			                + target
			                + " because it belongs to one of the packages EvoSuite cannot currently handle");
		}
		ExternalProcessHandler handler = new ExternalProcessHandler();
		handler.openServer();
		int port = handler.getServerPort();
		List<String> cmdLine = new ArrayList<String>();
		cmdLine.add(JAVA_CMD);
		cmdLine.add("-cp");
		cmdLine.add(classPath);
		if (cp.isEmpty())
			cmdLine.add("-DCP=" + classPath);
		else
			cmdLine.add("-DCP=" + cp);

		// TODO: Do this properly, and also need to support running outside of jar
		if (Properties.VIRTUAL_FS) {
			String jarName = setupIOJar();
			cmdLine.add("-Xbootclasspath/p:" + jarName);
			LoggingUtils.getEvoLogger().info("* Setting up virtual FS for testing");
			cmdLine.add("-Dvirtual_fs=true");
		}
		if (Properties.REPLACE_CALLS) { // TODO perhaps just hand over all properties to client vm? ask Gordon
			cmdLine.add("-Dreplace_calls=true");
		}

		cmdLine.add("-Dprocess_communication_port=" + port);
		cmdLine.add("-Dinline=true");
		cmdLine.add("-Djava.awt.headless=true");
		cmdLine.add("-Dlogback.configurationFile=logback.xml");
		cmdLine.add("-Djava.library.path=lib");
		// cmdLine.add("-Dminimize_values=true");

		if (Properties.DEBUG) {
			// enabling debugging mode to e.g. connect the eclipse remote debugger to the given port
			cmdLine.add("-Xdebug");
			cmdLine.add("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address="
			        + Properties.PORT);
		}

		for (String arg : args) {
			if (!arg.startsWith("-DCP=")) {
				cmdLine.add(arg);
			}
		}

		switch (strategy) {
		case EVOSUITE:
			cmdLine.add("-Dstrategy=EvoSuite");
			break;
		case ONEBRANCH:
			cmdLine.add("-Dstrategy=OneBranch");
			break;
		case RANDOM:
			cmdLine.add("-Dstrategy=Random");
			break;
		default:
			throw new RuntimeException("Unsupported strategy: " + strategy);
		}
		cmdLine.add("-DTARGET_CLASS=" + target);
		if (Properties.PROJECT_PREFIX != null) {
			cmdLine.add("-DPROJECT_PREFIX=" + Properties.PROJECT_PREFIX);
		}

		cmdLine.add("org.evosuite.ClientProcess");

		/*
		 * TODO: here we start the client with several properties that are set through -D. These properties are not visible to the master process (ie
		 * this process), when we access the Properties file. At the moment, we only need few parameters, so we can hack them
		 */
		Properties.getInstance();// should force the load, just to be sure
		Properties.TARGET_CLASS = target;
		Properties.PROCESS_COMMUNICATION_PORT = port;
		if (cmdLine.contains("-Dprint_to_system=true")) {
			Properties.PRINT_TO_SYSTEM = true;
		} else {
			Properties.PRINT_TO_SYSTEM = false;
		}

		/*
		 * The use of "assertions" in the client is pretty tricky, as those properties need to be transformed into JVM options before starting the
		 * client. Furthermore, the properties in the property file might be overwritten from the commands coming from shell
		 */

		String definedEAforClient = null;
		String definedEAforSUT = null;

		final String DISABLE_ASSERTIONS_EVO = "-da:org...";
		final String ENABLE_ASSERTIONS_EVO = "-ea:org...";
		final String DISABLE_ASSERTIONS_SUT = "-da:" + Properties.PROJECT_PREFIX + "...";
		final String ENABLE_ASSERTIONS_SUT = "-ea:" + Properties.PROJECT_PREFIX + "...";

		for (String s : cmdLine) {
			// first check client
			if (s.startsWith("-Denable_asserts_for_evosuite")) {
				if (s.endsWith("false")) {
					definedEAforClient = DISABLE_ASSERTIONS_EVO;
				} else if (s.endsWith("true")) {
					definedEAforClient = ENABLE_ASSERTIONS_EVO;
				}
			}
			// then check SUT
			if (s.startsWith("-Denable_asserts_for_sut")) {
				if (s.endsWith("false")) {
					definedEAforSUT = DISABLE_ASSERTIONS_SUT;
				} else if (s.endsWith("true")) {
					definedEAforSUT = ENABLE_ASSERTIONS_SUT;
				}
			}
		}

		/*
		 * the assertions might not be defined in the command line, but they might be in the property file, or just use default values. NOTE: if those
		 * are defined in the command line, then they overwrite whatever we had in the conf file
		 */

		if (definedEAforSUT == null) {
			if (Properties.ENABLE_ASSERTS_FOR_SUT) {
				definedEAforSUT = ENABLE_ASSERTIONS_SUT;
			} else {
				definedEAforSUT = DISABLE_ASSERTIONS_SUT;
			}
		}

		if (definedEAforClient == null) {
			if (Properties.ENABLE_ASSERTS_FOR_EVOSUITE) {
				definedEAforClient = ENABLE_ASSERTIONS_EVO;
			} else {
				definedEAforClient = DISABLE_ASSERTIONS_EVO;
			}
		}

		/*
		 * We add them in first position, after the java command To avoid confusion, we only add them if they are enabled. NOTE: this might have side
		 * effects "if" in the future we have something like a generic "-ea"
		 */
		if (definedEAforClient.equals(ENABLE_ASSERTIONS_EVO)) {
			cmdLine.add(1, definedEAforClient);
		}
		if (definedEAforSUT.equals(ENABLE_ASSERTIONS_SUT)) {
			cmdLine.add(1, definedEAforSUT);
		}

		LoggingUtils logUtils = new LoggingUtils();

		if (!Properties.CLIENT_ON_THREAD) {
			/*
			 * We want to completely mute the SUT. So, we block all outputs from client, and
			 * use a remote logging
			 */
			boolean logServerStarted = logUtils.startLogServer();
			if (!logServerStarted) {
				logger.error("Cannot start the log server");
				return null;
			}
			int logPort = logUtils.getLogServerPort(); //
			cmdLine.add(1, "-Dmaster_log_port=" + logPort);
			cmdLine.add(1, "-Devosuite.log.appender=CLIENT");
		}

		String[] newArgs = cmdLine.toArray(new String[cmdLine.size()]);

		for (String entry : Properties.CP.split(File.pathSeparator)) {
			try {
				ClassPathHacker.addFile(entry);
			} catch (IOException e) {
				LoggingUtils.getEvoLogger().info("* Error while adding classpath entry: "
				                                         + entry);
			}
		}

		handler.setBaseDir(base_dir_path);
		Object result = null;
		if (handler.startProcess(newArgs)) {
			result = handler.waitForResult((Properties.GLOBAL_TIMEOUT
			        + Properties.MINIMIZATION_TIMEOUT + Properties.EXTRA_TIMEOUT) * 1000); // FIXXME: search timeout plus 100 seconds?
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}

			handler.killProcess();
			handler.closeServer();
		} else {
			LoggingUtils.getEvoLogger().info("* Could not connect to client process");
		}

		if (Properties.CLIENT_ON_THREAD) {
			/*
			 * FIXME: this is done only to avoid current problems with serialization
			 */
			result = ClientProcess.geneticAlgorithmStatus;
		}

		if (!Properties.CLIENT_ON_THREAD) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			logUtils.closeLogServer();
		}

		return result;
	}

	private static void measureCoverage(String targetClass, String junitPrefix,
	        List<String> args, String cp) {
		if (!InstrumentingClassLoader.checkIfCanInstrument(targetClass)) {
			throw new IllegalArgumentException(
			        "Cannot consider "
			                + targetClass
			                + " because it belongs to one of the packages EvoSuite cannot currently handle");
		}
		String classPath = System.getProperty("java.class.path");
		if (!evosuiteJar.equals("")) {
			classPath += File.pathSeparator + evosuiteJar;
		}

		classPath += File.pathSeparator + cp;
		ExternalProcessHandler handler = new ExternalProcessHandler();
		int port = handler.openServer();
		List<String> cmdLine = new ArrayList<String>();
		cmdLine.add(JAVA_CMD);
		cmdLine.add("-cp");
		cmdLine.add(classPath);
		cmdLine.add("-Dprocess_communication_port=" + port);
		cmdLine.add("-Djava.awt.headless=true");
		cmdLine.add("-Dlogback.configurationFile=logback.xml");
		cmdLine.add("-Djava.library.path=lib");
		cmdLine.add("-DCP=" + cp);
		// cmdLine.add("-Dminimize_values=true");

		for (String arg : args) {
			if (!arg.startsWith("-DCP=")) {
				cmdLine.add(arg);
			}
		}

		cmdLine.add("-DTARGET_CLASS=" + targetClass);
		cmdLine.add("-Djunit_prefix=" + junitPrefix);
		if (Properties.PROJECT_PREFIX != null) {
			cmdLine.add("-DPROJECT_PREFIX=" + Properties.PROJECT_PREFIX);
		}

		cmdLine.add("-Dclassloader=true");
		cmdLine.add("org.evosuite.junit.CoverageAnalysis");

		/*
		 * TODO: here we start the client with several properties that are set through -D. These properties are not visible to the master process (ie
		 * this process), when we access the Properties file. At the moment, we only need few parameters, so we can hack them
		 */
		Properties.getInstance();// should force the load, just to be sure
		Properties.TARGET_CLASS = targetClass;
		Properties.PROCESS_COMMUNICATION_PORT = port;
		if (cmdLine.contains("-Dprint_to_system=true")) {
			Properties.PRINT_TO_SYSTEM = true;
		} else {
			Properties.PRINT_TO_SYSTEM = false;
		}

		LoggingUtils logUtils = new LoggingUtils();

		if (!Properties.CLIENT_ON_THREAD) {
			/*
			 * We want to completely mute the SUT. So, we block all outputs from client, and
			 * use a remote logging
			 */
			boolean logServerStarted = logUtils.startLogServer();
			if (!logServerStarted) {
				logger.error("Cannot start the log server");
				return;
			}
			int logPort = logUtils.getLogServerPort(); //
			cmdLine.add(1, "-Dmaster_log_port=" + logPort);
			cmdLine.add(1, "-Devosuite.log.appender=CLIENT");
		}

		String[] newArgs = cmdLine.toArray(new String[cmdLine.size()]);

		for (String entry : Properties.CP.split(File.pathSeparator)) {
			try {
				ClassPathHacker.addFile(entry);
			} catch (IOException e) {
				LoggingUtils.getEvoLogger().info("* Error while adding classpath entry: "
				                                         + entry);
			}
		}

		handler.setBaseDir(base_dir_path);
		Object result = null;
		if (handler.startProcess(newArgs)) {
			result = handler.waitForResult((Properties.GLOBAL_TIMEOUT
			        + Properties.MINIMIZATION_TIMEOUT + Properties.EXTRA_TIMEOUT) * 1000); // FIXXME: search timeout plus 100 seconds?
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}

			handler.killProcess();
			handler.closeServer();
		} else {
			LoggingUtils.getEvoLogger().info("* Could not connect to client process");
		}

		if (!Properties.CLIENT_ON_THREAD) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			logUtils.closeLogServer();
		}
	}

	private static void writeFile(InputStream in, File dest) {
		try {
			dest.deleteOnExit();
			System.out.println("Creating file: " + dest.getPath());
			// since deleteOnExit() only works for normal termination of the VM, we should perhaps also copy the file, if dest already exists
			// (because it may be faulty)
			// if (!dest.exists()) {
			OutputStream out = new FileOutputStream(dest);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			// }
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String setupIOJar() {
		String tmpDir = System.getProperty("java.io.tmpdir");
		String jarName = tmpDir + File.separator + "evosuite-io.jar";

		// FIXME hack! remove on the long run!
		URL[] urls = ((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs();
		URL evosuiteIO = null;
		for (URL url : urls) {
			if (url.getPath().endsWith("evosuite-io-0.2.jar")) {
				evosuiteIO = url;
				break;
			}
		}

		try {
			writeFile(evosuiteIO.openStream(), // EvoSuite.class.getClassLoader().getResourceAsStream("evosuite-0.1-SNAPSHOT-evosuite-io.jar")
			          new File(jarName));

		} catch (IOException e) {
			e.printStackTrace();
		}
		return jarName;
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
	@SuppressWarnings("static-access")
	public Object parseCommandLine(String[] args) {
		Options options = new Options();

		Option help = new Option("help", "print this message");
		Option generateSuite = new Option("generateSuite", "use whole suite generation");
		Option generateTests = new Option("generateTests",
		        "use individual test generation");
		Option measureCoverage = new Option("measureCoverage",
		        "measure coverage on existing test cases");
		Option setup = OptionBuilder.withArgName("target").hasArg().withDescription("Create evosuite-files with property file").create("setup");
		Option generateRandom = new Option("generateRandom", "use random test generation");
		Option targetClass = OptionBuilder.withArgName("class").hasArg().withDescription("target class for test generation").create("class");
		Option targetPrefix = OptionBuilder.withArgName("prefix").hasArg().withDescription("target prefix for test generation").create("prefix");
		Option targetCP = OptionBuilder.withArgName("target").hasArg().withDescription("target classpath for test generation").create("target");
		Option classPath = OptionBuilder.withArgName("cp").hasArg().withDescription("classpath of the project under test").withValueSeparator(':').create("cp");
		Option junitPrefix = OptionBuilder.withArgName("junit").hasArg().withDescription("junit prefix").create("junit");
		Option criterion = OptionBuilder.withArgName("criterion").hasArg().withDescription("target criterion for test generation").create("criterion");
		Option seed = OptionBuilder.withArgName("seed").hasArg().withDescription("seed for random number generator").create("seed");
		Option mem = OptionBuilder.withArgName("mem").hasArg().withDescription("heap size for client process (in megabytes)").create("mem");
		Option jar = OptionBuilder.withArgName("jar").hasArg().withDescription("location of EvoSuite jar file to use in client process").create("jar");

		Option sandbox = new Option("sandbox", "Run tests in sandbox");
		Option mocks = new Option("mocks", "Use mock classes");
		Option stubs = new Option("stubs", "Use stubs");
		Option assertions = new Option("assertions", "Add assertions");
		Option signature = new Option("signature",
		        "Allow manual tweaking of method signatures");

		Option base_dir = OptionBuilder.withArgName("base_dir").hasArg().withDescription("Working directory").create("base_dir");

		Option property = OptionBuilder.withArgName("property=value").hasArgs(2).withValueSeparator().withDescription("use value for given property").create("D");

		options.addOption(help);
		options.addOption(generateSuite);
		options.addOption(generateTests);
		options.addOption(generateRandom);
		options.addOption(measureCoverage);
		options.addOption(setup);
		options.addOption(targetClass);
		options.addOption(targetPrefix);
		options.addOption(targetCP);
		options.addOption(junitPrefix);
		options.addOption(criterion);
		options.addOption(seed);
		options.addOption(mem);
		options.addOption(jar);
		options.addOption(assertions);
		options.addOption(signature);
		options.addOption(base_dir);
		options.addOption(property);
		options.addOption(classPath);

		options.addOption(sandbox);
		options.addOption(mocks);
		options.addOption(stubs);

		List<String> javaOpts = new ArrayList<String>();

		Object result = null;
		String version = EvoSuite.class.getPackage().getImplementationVersion();
		if (version == null)
			version = "";
		System.out.println("* EvoSuite " + version);

		// create the parser
		CommandLineParser parser = new GnuParser();
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			/*
			 * NOTE: JVM arguments will not be passed over from the master to the client. So for -Xmx, we need to use "mem"
			 */
			setupProperties();

			java.util.Properties properties = line.getOptionProperties("D");
			Set<String> propertyNames = new HashSet<String>(Properties.getParameters());
			propertyNames.add("log.level"); // TODO: Maybe this should be an official parameter?
			for (String propertyName : properties.stringPropertyNames()) {
				if (!propertyNames.contains(propertyName)) {
					System.err.println("* Unknown property: " + propertyName);
					System.exit(1);
				}
				String propertyValue = properties.getProperty(propertyName);
				javaOpts.add("-D" + propertyName + "=" + propertyValue);
				System.setProperty(propertyName, propertyValue);
			}

			if (line.hasOption("mem"))
				javaOpts.add("-Xmx" + line.getOptionValue("mem") + "M");
			if (line.hasOption("jar"))
				evosuiteJar = line.getOptionValue("jar");
			if (line.hasOption("criterion"))
				javaOpts.add("-Dcriterion=" + line.getOptionValue("criterion"));
			if (line.hasOption("sandbox"))
				javaOpts.add("-Dsandbox=true");
			if (line.hasOption("mocks"))
				javaOpts.add("-Dmocks=true");
			if (line.hasOption("stubs"))
				javaOpts.add("-Dstubs=true");
			if (line.hasOption("seed"))
				javaOpts.add("-Drandom.seed=" + line.getOptionValue("seed"));
			if (line.hasOption("assertions"))
				javaOpts.add("-Dassertions=true");
			if (line.hasOption("signature"))
				javaOpts.add("-Dgenerate_objects=true");
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

			String cp = "";
			if (line.hasOption("cp")) {
				String[] cpEntries = line.getOptionValues("cp");
				if (cpEntries.length > 0) {
					boolean first = true;
					//if (!Properties.CP.isEmpty()) {
					//	first = false;
					//}
					for (String entry : cpEntries) {
						if (first) {
							first = false;
						} else {
							//Properties.CP += ":";
							cp += ":";
						}
						// Properties.CP += entry;
						cp += entry;
					}
				}
			}

			if (line.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("EvoSuite", options);
			} else if (line.hasOption("setup")) {
				setup(line.getOptionValue("setup"), line.getArgs(), javaOpts);
			} else if (line.hasOption("measureCoverage")) {
				if (line.hasOption("class"))
					measureCoverage(line.getOptionValue("class"),
					                line.getOptionValue("junit"), javaOpts, cp);
				else {
					System.err.println("Please specify target class");
					HelpFormatter formatter = new HelpFormatter();
					formatter.printHelp("EvoSuite", options);
				}

			} else {

				Strategy strategy = null;
				if (line.hasOption("generateTests")) {
					strategy = Strategy.ONEBRANCH;
				} else if (line.hasOption("generateSuite")) {
					strategy = Strategy.EVOSUITE;
				} else if (line.hasOption("generateRandom")) {
					strategy = Strategy.RANDOM;
				}
				if (strategy == null) {
					System.err.println("Please specify strategy: -generateSuite, -generateTests, -generateRandom");
					HelpFormatter formatter = new HelpFormatter();
					formatter.printHelp("EvoSuite", options);
				} else {
					if (line.hasOption("class"))
						result = generateTests(strategy, line.getOptionValue("class"),
						                       javaOpts, cp);
					else if (line.hasOption("prefix"))
						generateTestsPrefix(strategy, line.getOptionValue("prefix"),
						                    javaOpts, cp);
					else if (line.hasOption("target"))
						generateTestsTarget(strategy, line.getOptionValue("target"),
						                    javaOpts, cp);
					else {
						System.err.println("Please specify target class, prefix, or classpath entry");
						HelpFormatter formatter = new HelpFormatter();
						formatter.printHelp("EvoSuite", options);
					}
				}
			}
		} catch (ParseException exp) {
			// oops, something went wrong
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
			// automatically generate the help statement
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("EvoSuite", options);
		}

		return result;
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
		EvoSuite evosuite = new EvoSuite();
		evosuite.parseCommandLine(args);

		/*
		 * Some threads could still be running, so we need to kill the process explicitly
		 */
		System.exit(0);
	}

}
