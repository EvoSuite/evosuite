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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.evosuite.Properties.StoppingCondition;
import org.evosuite.Properties.Strategy;
import org.evosuite.javaagent.InstrumentingClassLoader;
import org.evosuite.utils.ClassPathHacker;
import org.evosuite.utils.ExternalProcessHandler;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.ResourceList;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
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

	private static final boolean logLevelSet = LoggingUtils
			.checkAndSetLogLevel();

	private static Logger logger = LoggerFactory.getLogger(EvoSuite.class);

	private static String separator = System.getProperty("file.separator");
	private static String javaHome = System.getProperty("java.home");
	private static String evosuiteJar = "";
	/**
	 * Constant <code>JAVA_CMD="javaHome + separator + bin + separatorj"{trunked}</code>
	 */
	public final static String JAVA_CMD = javaHome + separator + "bin"
			+ separator + "java";

	private static String base_dir_path = System.getProperty("user.dir");

	private static void setup(String target, String[] args,
			List<String> javaArgs) {

		Properties.CP = "";

		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				if (!Properties.CP.equals(""))
					Properties.CP += File.pathSeparator;
				Properties.CP += args[i];
			}
		}

		Properties.MIN_FREE_MEM = 0;
		File directory = new File(base_dir_path + separator
				+ Properties.OUTPUT_DIR);
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
		System.out.println("* Creating new evosuite.properties in "
				+ base_dir_path + separator + Properties.OUTPUT_DIR);
		Properties.getInstance().writeConfiguration(
				base_dir_path + separator + Properties.OUTPUT_DIR + separator
						+ "evosuite.properties");
	}

	private static boolean isInterface(String resource) throws IOException {

		ClassReader reader = new ClassReader(EvoSuite.class.getClassLoader()
				.getResourceAsStream(resource));
		ClassNode cn = new ClassNode();
		reader.accept(cn, ClassReader.SKIP_FRAMES);
		return (cn.access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE;
	}

	private static void listClassesPrefix(String prefix, String cp) {
		Pattern pattern = Pattern.compile(prefix.replace("\\.", "/")
				+ "[^\\$]*.class");
		Set<String> resources = new HashSet<String>();
		for (String classPathElement : cp.split(File.pathSeparator)) {
			resources.addAll(ResourceList.getResources(pattern,
					classPathElement));
			try {
				ClassPathHacker.addFile(classPathElement);
			} catch (IOException e) {
				// Ignore?
			}
		}
		for (String resource : resources) {
			try {
				if (isInterface(resource)) {
					continue;
				}
			} catch (IOException e) {
				System.err.println("Could not load class: " + resource);
				continue;
			}
			System.out
					.println(resource.replace(".class", "").replace('/', '.'));
		}
	}

	// TODO this method may need the same fixing as generateTestsTarget, by replacing '/' with File.separatorChar in call to generateTests. - Done by
	// Daniel (Windows-user :-x )
	private static void generateTestsPrefix(Properties.Strategy strategy,
			String prefix, List<String> args, String cp) {

		Pattern pattern = Pattern.compile(prefix.replace('.',
				File.separatorChar) + "[^\\$]*.class");
		Set<String> resources = new HashSet<String>();
		for (String classPathElement : cp.split(File.pathSeparator)) {
			resources.addAll(ResourceList.getResources(pattern,
					classPathElement));
			try {
				ClassPathHacker.addFile(classPathElement);
			} catch (IOException e) {
				// Ignore?
			}
		}
		System.out.println("* Found " + resources.size()
				+ " matching classes for prefix " + prefix);
		for (String resource : resources) {
			try {
				if (isInterface(resource)) {
					System.out.println("* Skipping interface: "
							+ resource.replace(".class", "").replace(
									File.separatorChar, '.'));
					continue;
				}
			} catch (IOException e) {
				System.out.println("Could not load class: " + resource);
				continue;
			}
			System.out.println("* Current class: "
					+ resource.replace(".class", "").replace(
							File.separatorChar, '.'));
			generateTests(Strategy.EVOSUITE, resource.replace(".class", "")
					.replace(File.separatorChar, '.'), args, cp);
		}

	}

	private static void listClassesTarget(String target) {
		Pattern pattern = Pattern.compile("[^\\$]*.class");
		Collection<String> resources = ResourceList.getResources(pattern,
				target);
		try {
			ClassPathHacker.addFile(target);
		} catch (IOException e) {
			// Ignore?
		}
		for (String resource : resources) {
			try {
				if (isInterface(resource)) {
					continue;
				}
			} catch (IOException e) {
				System.err.println("Could not load class: " + resource);
				continue;
			}
			System.out
					.println(resource.replace(".class", "").replace('/', '.'));
		}
	}

	private static void generateTestsTarget(Properties.Strategy strategy,
			String target, List<String> args, String cp) {

		Pattern pattern = Pattern.compile("[^\\$]*.class");
		Collection<String> resources = ResourceList.getResources(pattern,
				target);
		System.out.println("* Found " + resources.size()
				+ " matching classes in target " + target);
		try {
			ClassPathHacker.addFile(target);
		} catch (IOException e) {
			// Ignore?
		}
		for (String resource : resources) {
			try {
				if (isInterface(resource)) {
					System.out.println("* Skipping interface: "
							+ resource.replace(".class", "").replace(
									File.separatorChar, '.'));
					continue;
				}
			} catch (IOException e) {
				System.out.println("Could not load class: " + resource);
				continue;
			}
			System.out.println("* Current class: "
					+ resource.replace(".class", "").replace(
							File.separatorChar, '.'));
			generateTests(Strategy.EVOSUITE, resource.replace(".class", "")
					.replace(File.separatorChar, '.'), args, cp);
		}
	}

	private static void listClassesLegacy() {
		File directory = new File(Properties.OUTPUT_DIR);
		String[] extensions = { "task" };
		for (File file : FileUtils.listFiles(directory, extensions, false)) {
			System.out.println(file.getName().replace(".task", ""));
		}
	}

	private static void generateTestsLegacy(Properties.Strategy strategy,
			List<String> args, String cp) {
		LoggingUtils.getEvoLogger().info(
				"* Using .task files in " + Properties.OUTPUT_DIR
						+ " [deprecated]");
		File directory = new File(Properties.OUTPUT_DIR);
		String[] extensions = { "task" };
		for (File file : FileUtils.listFiles(directory, extensions, false)) {
			generateTests(strategy, file.getName().replace(".task", ""), args,
					cp);
		}
	}

	private static boolean hasLegacyTargets() {
		File directory = new File(Properties.OUTPUT_DIR);
		String[] extensions = { "task" };
		return !FileUtils.listFiles(directory, extensions, false).isEmpty();
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

	private static Object generateTests(Properties.Strategy strategy,
			String target, List<String> args, String cp) {
		String classPath = System.getProperty("java.class.path");
		if (!evosuiteJar.equals("")) {
			classPath += File.pathSeparator + evosuiteJar;
		}

		if (!findTargetClass(target, cp)) {
			return null;
		}

		if (!classPath.isEmpty())
			classPath += File.pathSeparator;
		classPath += Properties.CP;

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
		if (cp.isEmpty()) {
			cmdLine.add("-DCP=" + classPath);
		} else {
			cmdLine.add("-DCP=" + cp);
		}

		if (Properties.VIRTUAL_FS) {
			LoggingUtils.getEvoLogger().info(
					"* Setting up virtual FS for testing");
			String stringToBePrependedToBootclasspath = locateEvoSuiteIOClasses();
			if (stringToBePrependedToBootclasspath == null)
				throw new IllegalStateException(
						"Could not prepend needed classes for VFS functionality to bootclasspath of client!");
			cmdLine.add("-Xbootclasspath/p:"
					+ stringToBePrependedToBootclasspath);
			cmdLine.add("-Dvirtual_fs=true");
		}

		cmdLine.add("-Dprocess_communication_port=" + port);
		cmdLine.add("-Dinline=true");
		cmdLine.add("-Djava.awt.headless=true");
		cmdLine.add("-Dlogback.configurationFile=logback.xml");
		cmdLine.add("-Djava.library.path=lib");
		// cmdLine.add("-Dminimize_values=true");

		if (Properties.DEBUG) {
			// enabling debugging mode to e.g. connect the eclipse remote debugger to the given port
			cmdLine.add("-Ddebug=true");
			cmdLine.add("-Xdebug");
			cmdLine.add("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address="
					+ Properties.PORT);
			LoggingUtils.getEvoLogger().info(
					"* Waiting for remote debugger to connect on port "
							+ Properties.PORT + "..."); // TODO find the right place for this
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
		case REGRESSION:
			cmdLine.add("-Dstrategy=Regression");
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

		/*
		 * The use of "assertions" in the client is pretty tricky, as those properties need to be transformed into JVM options before starting the
		 * client. Furthermore, the properties in the property file might be overwritten from the commands coming from shell
		 */

		String definedEAforClient = null;
		String definedEAforSUT = null;

		final String DISABLE_ASSERTIONS_EVO = "-da:org...";
		final String ENABLE_ASSERTIONS_EVO = "-ea:org...";
		final String DISABLE_ASSERTIONS_SUT = "-da:"
				+ Properties.PROJECT_PREFIX + "...";
		final String ENABLE_ASSERTIONS_SUT = "-ea:" + Properties.PROJECT_PREFIX
				+ "...";

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
			 * We want to completely mute the SUT. So, we block all outputs from client, and use a remote logging
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
				LoggingUtils.getEvoLogger().info(
						"* Error while adding classpath entry: " + entry);
			}
		}

		handler.setBaseDir(base_dir_path);
		Object result = null;
		if (handler.startProcess(newArgs)) {
			int time = Properties.EXTRA_TIMEOUT;
			if (Properties.STOPPING_CONDITION == StoppingCondition.MAXTIME) {
				time += Math.max(Properties.GLOBAL_TIMEOUT,
						Properties.SEARCH_BUDGET);
			} else {
				time += Properties.GLOBAL_TIMEOUT;
			}
			if (Properties.MINIMIZE) {
				time += Properties.MINIMIZATION_TIMEOUT;
			}
			if (Properties.ASSERTIONS) {
				time += Properties.ASSERTION_TIMEOUT;
			}
			result = handler.waitForResult(time * 1000); // FIXXME: search timeout plus 100 seconds?
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}

			handler.killProcess();
			handler.closeServer();
		} else {
			LoggingUtils.getEvoLogger().info(
					"* Could not connect to client process");
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

		LoggingUtils logUtils = new LoggingUtils();

		if (!Properties.CLIENT_ON_THREAD) {
			/*
			 * We want to completely mute the SUT. So, we block all outputs from client, and use a remote logging
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
				LoggingUtils.getEvoLogger().info(
						"* Error while adding classpath entry: " + entry);
			}
		}

		handler.setBaseDir(base_dir_path);
		Object result = null;
		if (handler.startProcess(newArgs)) {
			result = handler
					.waitForResult((Properties.GLOBAL_TIMEOUT
							+ Properties.MINIMIZATION_TIMEOUT + Properties.EXTRA_TIMEOUT) * 1000); // FIXXME: search timeout plus 100 seconds?
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}

			handler.killProcess();
			handler.closeServer();
		} else {
			LoggingUtils.getEvoLogger().info(
					"* Could not connect to client process");
		}

		if (!Properties.CLIENT_ON_THREAD) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			logUtils.closeLogServer();
		}
	}

	/**
	 * Locates the resources that have to be prepended to bootclasspath in order to have all classes in the Client JVM that are needed for VFS
	 * functionality. Extracts and creates it if necessary.
	 * 
	 * @return a string denoting one or more with the system's pathSeparator separated pathes to one or more jars containing evosuite-io, commons-vfs2
	 *         and commons-logging; <code>null</code> if one or more resources could not be found or accessed
	 */
	private static String locateEvoSuiteIOClasses() {
		String stringToBePrependedToBootclasspath = null;

		// try to find it inside the jar // FIXME this does still not seem to be the golden solution
		InputStream evosuiteIOjarInputStream = EvoSuite.class.getClassLoader()
				.getResourceAsStream("evosuite-io.jar"); // created by maven with the jar-minimal.xml assembly file - contains the evosuite-io classes
															// plus the needed commons-vfs2 and commons-logging dependencies
		if (evosuiteIOjarInputStream != null) {
			// extract evosuite-io.jar into the system-default temporary directory
			String tmpFilePath = System.getProperty("java.io.tmpdir")
					+ File.separator + "evosuite-io.jar";
			File tmpFile = new File(tmpFilePath);
			tmpFile.deleteOnExit();

			try {
				IOUtils.copy(evosuiteIOjarInputStream, new FileOutputStream(
						tmpFile));
				stringToBePrependedToBootclasspath = tmpFilePath;
			} catch (IOException e) {
				throw new IllegalStateException(
						"Error while extracing the evosuite-io JAR file!", e);
			}
		} else {
			// if not found try to locate all needed jars in classpath
			logger.info("\"evosuite-io.jar\" could not be found by EvoSuite.class.getClassLoader().getResource. "
					+ "EvoSuite is likely not executing out of an executable jar file at the moment. "
					+ "Now trying to locate all needed jars for VFS functionality in classpath instead...");
			URL[] urls = ((URLClassLoader) ClassLoader.getSystemClassLoader())
					.getURLs();
			URL evosuiteIOjar = null;
			URL commonsVFSjar = null;
			URL commonsLoggingjar = null;
			for (URL url : urls) {
				if (evosuiteIOjar != null && commonsVFSjar != null
						&& commonsLoggingjar != null)
					break;

				if (url.getPath().matches(".*evosuite-io.*\\.jar")) {
					evosuiteIOjar = url;
					continue;
				}

				if (url.getPath().matches(".*commons-vfs2.*\\.jar")) {
					commonsVFSjar = url;
					continue;
				}

				if (url.getPath().matches(".*commons-logging.*\\.jar")) {
					commonsLoggingjar = url;
					continue;
				}
			}

			if (evosuiteIOjar == null
					|| !(new File(evosuiteIOjar.getPath())).canRead()) {
				throw new IllegalStateException(
						"The evosuite-io JAR cannot be read!");
			} else if (commonsVFSjar == null
					|| !(new File(commonsVFSjar.getPath())).canRead()) {
				throw new IllegalStateException(
						"The commons-vfs2 JAR cannot be read!");
			} else if (commonsLoggingjar == null
					|| !(new File(commonsLoggingjar.getPath())).canRead()) {
				throw new IllegalStateException(
						"The commons-logging JAR cannot be read!");
			} else {
				logger.info("All needed jars for VFS functionality are in classpath and readable!");
				stringToBePrependedToBootclasspath = evosuiteIOjar.getPath()
						+ File.pathSeparator + commonsVFSjar.getPath()
						+ File.pathSeparator + commonsLoggingjar.getPath();
			}
		}

		return stringToBePrependedToBootclasspath;
	}

	private void setupProperties() {
		if (base_dir_path.equals("")) {
			Properties.getInstanceSilent();
		} else {
			Properties.getInstanceSilent().loadProperties(
					base_dir_path + separator + Properties.PROPERTIES_FILE,
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
		Option generateSuite = new Option("generateSuite",
				"use whole suite generation");
		Option generateTests = new Option("generateTests",
				"use individual test generation");
		Option measureCoverage = new Option("measureCoverage",
				"measure coverage on existing test cases");
		Option listClasses = new Option("listClasses",
				"list the testable classes found in the specified classpath/prefix");
		Option setup = OptionBuilder.withArgName("target").hasArg()
				.withDescription("Create evosuite-files with property file")
				.create("setup");
		Option generateRandom = new Option("generateRandom",
				"use random test generation");
		Option generateRegressionSuite = new Option("regressionSuite",
				"generate a regression test suite");
		Option targetClass = OptionBuilder.withArgName("class").hasArg()
				.withDescription("target class for test generation")
				.create("class");
		Option targetPrefix = OptionBuilder.withArgName("prefix").hasArg()
				.withDescription("target prefix for test generation")
				.create("prefix");
		Option targetCP = OptionBuilder.withArgName("target").hasArg()
				.withDescription("target classpath for test generation")
				.create("target");
		Option classPath = OptionBuilder.withArgName("cp").hasArg()
				.withDescription("classpath of the project under test")
				.withValueSeparator(':').create("cp");
		Option junitPrefix = OptionBuilder.withArgName("junit").hasArg()
				.withDescription("junit prefix").create("junit");
		Option criterion = OptionBuilder.withArgName("criterion").hasArg()
				.withDescription("target criterion for test generation")
				.create("criterion");
		Option seed = OptionBuilder.withArgName("seed").hasArg()
				.withDescription("seed for random number generator")
				.create("seed");
		Option mem = OptionBuilder.withArgName("mem").hasArg()
				.withDescription("heap size for client process (in megabytes)")
				.create("mem");
		Option jar = OptionBuilder
				.withArgName("jar")
				.hasArg()
				.withDescription(
						"location of EvoSuite jar file to use in client process")
				.create("jar");

		Option sandbox = new Option("sandbox", "Run tests in sandbox");
		Option mocks = new Option("mocks", "Use mock classes");
		Option stubs = new Option("stubs", "Use stubs");
		Option assertions = new Option("assertions", "Add assertions");
		Option signature = new Option("signature",
				"Allow manual tweaking of method signatures");
		Option heapDump = new Option("heapdump",
				"Create heap dump on client VM out of memory error");

		Option base_dir = OptionBuilder.withArgName("base_dir").hasArg()
				.withDescription("Working directory").create("base_dir");

		Option property = OptionBuilder.withArgName("property=value")
				.hasArgs(2).withValueSeparator()
				.withDescription("use value for given property").create("D");

		options.addOption(help);
		options.addOption(generateSuite);
		options.addOption(generateTests);
		options.addOption(generateRandom);
		options.addOption(generateRegressionSuite);
		options.addOption(measureCoverage);
		options.addOption(listClasses);
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
		options.addOption(heapDump);

		options.addOption(sandbox);
		options.addOption(mocks);
		options.addOption(stubs);

		List<String> javaOpts = new ArrayList<String>();

		Object result = null;
		String version = EvoSuite.class.getPackage().getImplementationVersion();
		if (version == null)
			version = "";

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
			Set<String> propertyNames = new HashSet<String>(
					Properties.getParameters());
			for (String propertyName : properties.stringPropertyNames()) {
				if (!propertyNames.contains(propertyName)) {
					LoggingUtils.getEvoLogger().error("* EvoSuite " + version);
					LoggingUtils.getEvoLogger().error(
							"* Unknown property: " + propertyName);
					throw new Error("Unknown property: " + propertyName);
				}
				String propertyValue = properties.getProperty(propertyName);
				javaOpts.add("-D" + propertyName + "=" + propertyValue);
				System.setProperty(propertyName, propertyValue);
				try {
					Properties.getInstance().setValue(propertyName,
							propertyValue);
				} catch (Exception e) {
					// Ignore?
				}
			}

			if (line.hasOption("mem"))
				javaOpts.add("-Xmx" + line.getOptionValue("mem") + "M");
			if (line.hasOption("heapdump"))
				javaOpts.add("-XX:+HeapDumpOnOutOfMemoryError");
			if (line.hasOption("jar"))
				evosuiteJar = line.getOptionValue("jar");
			if (!line.hasOption("regressionSuite")) {
				if (line.hasOption("criterion"))
					javaOpts.add("-Dcriterion="
							+ line.getOptionValue("criterion"));
			} else {
				javaOpts.add("-Dcriterion=regression");
			}
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
					System.out.println("* EvoSuite " + version);
					LoggingUtils.getEvoLogger().error(
							"Base directory does not exist: " + base_dir_path);
					return null;
				}
				if (!baseDir.isDirectory()) {
					System.out.println("* EvoSuite " + version);
					LoggingUtils.getEvoLogger().error(
							"Specified base directory is not a directory: "
									+ base_dir_path);
					return null;
				}
			}

			String cp = "";
			if (line.hasOption("cp")) {
				String[] cpEntries = line.getOptionValues("cp");
				if (cpEntries.length > 0) {
					boolean first = true;
					// if (!Properties.CP.isEmpty()) {
					// first = false;
					// }
					for (String entry : cpEntries) {
						if (first) {
							first = false;
						} else {
							// Properties.CP += ":";
							cp += ":";
						}
						// Properties.CP += entry;
						cp += entry;
					}
				}
			} else {
				cp = Properties.CP;
			}

			if (line.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				System.out.println("* EvoSuite " + version);
				formatter.printHelp("EvoSuite", options);
			} else if (line.hasOption("setup")) {
				System.out.println("* EvoSuite " + version);
				setup(line.getOptionValue("setup"), line.getArgs(), javaOpts);
			} else if (line.hasOption("measureCoverage")) {
				System.out.println("* EvoSuite " + version);
				if (line.hasOption("class"))
					measureCoverage(line.getOptionValue("class"),
							line.getOptionValue("junit"), javaOpts, cp);
				else {
					System.err.println("Please specify target class");
					HelpFormatter formatter = new HelpFormatter();
					formatter.printHelp("EvoSuite", options);
				}
			} else if (line.hasOption("listClasses")) {
				if (line.hasOption("prefix"))
					listClassesPrefix(line.getOptionValue("prefix"), cp);
				else if (line.hasOption("target"))
					listClassesTarget(line.getOptionValue("target"));
				else if (hasLegacyTargets())
					listClassesLegacy();
				else {
					System.err
							.println("Please specify target prefix or classpath entry to list testable classes");
					HelpFormatter formatter = new HelpFormatter();
					formatter.printHelp("EvoSuite", options);
				}
			} else {
				System.out.println("* EvoSuite " + version);

				Strategy strategy = null;
				if (line.hasOption("generateTests")) {
					strategy = Strategy.ONEBRANCH;
				} else if (line.hasOption("generateSuite")) {
					strategy = Strategy.EVOSUITE;
				} else if (line.hasOption("generateRandom")) {
					strategy = Strategy.RANDOM;
				} else if (line.hasOption("regressionSuite")) {
					strategy = Strategy.REGRESSION;
				}
				if (strategy == null) {
					System.err
							.println("Please specify strategy: -generateSuite, -generateTests, -generateRandom");
					HelpFormatter formatter = new HelpFormatter();
					formatter.printHelp("EvoSuite", options);
				} else {
					if (line.hasOption("class"))
						result = generateTests(strategy,
								line.getOptionValue("class"), javaOpts, cp);
					else if (line.hasOption("prefix"))
						generateTestsPrefix(strategy,
								line.getOptionValue("prefix"), javaOpts, cp);
					else if (line.hasOption("target"))
						generateTestsTarget(strategy,
								line.getOptionValue("target"), javaOpts, cp);
					else if (hasLegacyTargets())
						generateTestsLegacy(strategy, javaOpts, cp);
					else {
						System.err
								.println("Please specify target class, prefix, or classpath entry");
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

		try {
			EvoSuite evosuite = new EvoSuite();
			evosuite.parseCommandLine(args);
		} catch (Throwable t) {
			logger.error(
					"Fatal crash on main EvoSuite process. Class "
							+ Properties.TARGET_CLASS + " using seed "
							+ Randomness.getSeed() + ". Configuration id : "
							+ Properties.CONFIGURATION_ID, t);
			System.exit(-1);
		}

		/*
		 * Some threads could still be running, so we need to kill the process explicitly
		 */
		System.exit(0);
	}

}
