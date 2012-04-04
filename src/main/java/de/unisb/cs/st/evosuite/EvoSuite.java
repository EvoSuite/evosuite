/**
 * 
 */
package de.unisb.cs.st.evosuite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.utils.ClassPathHacker;
import de.unisb.cs.st.evosuite.utils.ExternalProcessHandler;
import de.unisb.cs.st.evosuite.utils.LoggingUtils;

/**
 * @author Gordon Fraser
 * 
 */
public class EvoSuite {

	private static final boolean logLevelSet = LoggingUtils.checkAndSetLogLevel();
	
	private static Logger logger = LoggerFactory.getLogger(EvoSuite.class);

	private static String separator = System.getProperty("file.separator");
	private static String javaHome = System.getProperty("java.home");
	public final static String JAVA_CMD = javaHome + separator + "bin" + separator
	        + "java";

	private static void setup(String target, String[] args, List<String> javaArgs) {
		String classPath = System.getProperty("java.class.path");

		Properties.CP = "";
		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				classPath += File.pathSeparator + args[i];
				if (!Properties.CP.equals(""))
					Properties.CP += File.pathSeparator;
				Properties.CP += args[i];
			}
		}
		Properties.MIN_FREE_MEM = 0;
		File directory = new File(Properties.OUTPUT_DIR);
		if (!directory.exists()) {
			directory.mkdir();
		}

		String targetParam = "";
		String prefix = "";
		File targetFile = new File(target);
		if (targetFile.exists()) {
			classPath += File.pathSeparator + target;
			targetParam = target;
			if (!Properties.CP.equals(""))
				Properties.CP += File.pathSeparator;

			Properties.CP += target;
		} else {
			prefix = target; // TODO: Should be proper prefix!
		}
		Properties.PROJECT_PREFIX = prefix;

		List<String> parameters = new ArrayList<String>();
		parameters.add(JAVA_CMD);
		parameters.add("-cp");
		parameters.add(classPath);
		parameters.add("-DPROJECT_PREFIX=" + prefix);
		parameters.add("-DCP=" + Properties.CP);
		parameters.add("-Dclassloader=true");
		parameters.add("-Dshow_progress=true");
		parameters.add("-Djava.awt.headless=true");
		parameters.add("-Dlogback.configurationFile=logback.xml");
		parameters.addAll(javaArgs);
		parameters.add("de.unisb.cs.st.evosuite.setup.ScanProject");
		parameters.add(targetParam);

		try {
			ProcessBuilder builder = new ProcessBuilder(parameters);

			File dir = new File(System.getProperty("user.dir"));
			builder.directory(dir);
			builder.redirectErrorStream(true);

			Process process = builder.start();

			BufferedReader input = new BufferedReader(new InputStreamReader(
			        process.getInputStream()));
			String line = null;

			while ((line = input.readLine()) != null) {
				System.out.println(line);
			}
			process.waitFor();

		} catch (IOException e) {
			System.out.println("Failed to start external process" + e);
		} catch (InterruptedException e) {
			System.out.println("Interrupted");
		}
		//File propertyFile = new File(Properties.OUTPUT_DIR + separator
		//        + "evosuite.properties");
		//if (propertyFile.exists()) {

		//} else {
		//Properties.PROJECT_PREFIX = prefix;
		//System.out.println("Determined prefix: " + prefix);
		//Properties.getInstance().writeConfiguration(Properties.OUTPUT_DIR
		//                                                   + File.separator
		//                                                  + "evosuite.properties");
		//}

	}

	private static void generateTests(boolean wholeSuite, List<String> args) {
		File directory = new File(Properties.OUTPUT_DIR);
		if (!directory.exists()) {
			System.out.println("* Found no EvoSuite data in current directory. Run -setup first!");
			return;
		} else if (!directory.isDirectory()) {
			System.out.println("* Found no EvoSuite data in " + directory
			        + ". Run -setup first!");
			return;
		}
		int num = 0;
		String[] extensions = { "task" };
		for (File file : FileUtils.listFiles(directory, extensions, false)) {
			generateTests(wholeSuite, file.getName().replace(".task", ""), args);
			num++;
		}
		if (num == 0) {
			System.out.println("* Found no class information in " + directory
			        + ". Check that the classpath is correct when calling -setup.");
			return;
		}
	}

	private static void listClasses() {
		System.out.println("* The following classes are known: ");
		File directory = new File(Properties.OUTPUT_DIR);
		logger.debug("Going to scan output directory {}", Properties.OUTPUT_DIR);

		String[] extensions = { "task" };
		for (File file : FileUtils.listFiles(directory, extensions, false)) {
			System.out.println("   " + file.getName().replace(".task", ""));
		}

	}

	private static Object generateTests(boolean wholeSuite, String target,
	        List<String> args) {
		File taskFile = new File(Properties.OUTPUT_DIR + File.separator + target
		        + ".task");
		if (!taskFile.exists()) {
			System.out.println("* Unknown class: " + target);
			listClasses();
			System.out.println("* If the class is missing but should be there, consider rerunning -setup, or adapting evosuite-files/evosuite.properties");
			return null;
		}
		String classPath = System.getProperty("java.class.path");
		if (Properties.CP.length() > 0 && Properties.CP.charAt(0) == '"')
			Properties.CP = Properties.CP.substring(1, Properties.CP.length() - 1);
		classPath += File.pathSeparator + Properties.CP;
		ExternalProcessHandler handler = new ExternalProcessHandler();
		handler.openServer();
		int port = handler.getServerPort();
		List<String> cmdLine = new ArrayList<String>();
		cmdLine.add(JAVA_CMD);
		cmdLine.add("-cp");
		cmdLine.add(classPath);
		// TODO: Do this properly, and also need to support running outside of jar
		if (Properties.VIRTUAL_FS) {
			String jarName = setupIOJar();
			cmdLine.add("-Xbootclasspath/p:" + jarName);
			System.out.println("* Setting up virtual FS for testing");
		}
		cmdLine.add("-Dprocess_communication_port=" + port);
		cmdLine.add("-Dinline=true");
		cmdLine.add("-Djava.awt.headless=true");
		cmdLine.add("-Dlogback.configurationFile=logback.xml");
		//cmdLine.add("-Dminimize_values=true");
		
		if (Properties.DEBUG) {
			// enabling debugging mode to e.g. connect the eclipse remote debugger to the given port
			cmdLine.add("-Xdebug");
			cmdLine.add("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address="
					+ Properties.PORT);
		}
		
		cmdLine.addAll(args);
		if (wholeSuite)
			cmdLine.add("-Dstrategy=EvoSuite");
		else
			cmdLine.add("-Dstrategy=OneBranch");
		cmdLine.add("-DTARGET_CLASS=" + target);
		if (Properties.PROJECT_PREFIX != null)
			cmdLine.add("-DPROJECT_PREFIX=" + Properties.PROJECT_PREFIX);

		cmdLine.add("-Dclassloader=true");
		cmdLine.add("de.unisb.cs.st.evosuite.ClientProcess");

		/*
		 * TODO: here we start the client with several properties that are set through -D.
		 * These properties are not visible to the master process (ie this process), when
		 * we access the Properties file. 
		 * At the moment, we only need few parameters, so we can hack them
		 */
		Properties.getInstance();//should force the load, just to be sure
		Properties.TARGET_CLASS = target;
		Properties.PROCESS_COMMUNICATION_PORT = port;

		/*
		 * The use of "assertions" in the client is pretty tricky, as those properties
		 * need to be transformed into JVM options before starting the client.
		 * Furthermore, the properties in the property file might be overwritten from the
		 * commands coming from shell
		 */

		String definedEAforClient = null;
		String definedEAforSUT = null;

		final String DISABLE_ASSERTIONS_EVO = "-da:de.unisb.cs.st...";
		final String ENABLE_ASSERTIONS_EVO = "-ea:de.unisb.cs.st...";
		final String DISABLE_ASSERTIONS_SUT = "-da:" + Properties.PROJECT_PREFIX + "...";
		final String ENABLE_ASSERTIONS_SUT = "-ea:" + Properties.PROJECT_PREFIX + "...";

		for (String s : cmdLine) {
			//first check client
			if (s.startsWith("-Denable_asserts_for_evosuite")) {
				if (s.endsWith("false")) {
					definedEAforClient = DISABLE_ASSERTIONS_EVO;
				} else if (s.endsWith("true")) {
					definedEAforClient = ENABLE_ASSERTIONS_EVO;
				}
			}
			//then check SUT
			if (s.startsWith("-Denable_asserts_for_sut")) {
				if (s.endsWith("false")) {
					definedEAforSUT = DISABLE_ASSERTIONS_SUT;
				} else if (s.endsWith("true")) {
					definedEAforSUT = ENABLE_ASSERTIONS_SUT;
				}
			}
		}

		/*
		 * the assertions might not be defined in the command line, but they might be in the property
		 * file, or just use default values.
		 * NOTE: if those are defined in the command line, then they overwrite whatever we had in the
		 * conf file
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
		 * We add them in first position, after the java command
		 * To avoid confusion, we only add them if they are enabled.
		 * NOTE: this might have side effects "if" in the future we have something like 
		 * a generic "-ea"
		 */
		if (definedEAforClient.equals(ENABLE_ASSERTIONS_EVO)) {
			cmdLine.add(1, definedEAforClient);
		}
		if (definedEAforSUT.equals(ENABLE_ASSERTIONS_SUT)) {
			cmdLine.add(1, definedEAforSUT);
		}

		String[] newArgs = cmdLine.toArray(new String[cmdLine.size()]);

		for (String entry : Properties.CP.split(File.pathSeparator)) {
			try {
				ClassPathHacker.addFile(entry);
			} catch (IOException e) {
				System.out.println("* Error while adding classpath entry: " + entry);
			}
		}

		Object result = null;
		if (handler.startProcess(newArgs)) {
			result = handler.waitForResult((Properties.GLOBAL_TIMEOUT
			        + Properties.MINIMIZATION_TIMEOUT + Properties.EXTRA_TIMEOUT) * 1000); // FIXXME: search timeout plus 100 seconds?	
			handler.killProcess();
			handler.closeServer();
		} else {
			System.out.println("* Could not connect to client process");
		}

		if (Properties.CLIENT_ON_THREAD) {
			/*
			 * FIXME: this is done only to avoid current problems with serialization
			 */
			result = ClientProcess.geneticAlgorithmStatus;
		}

		return result;
	}

	private static void writeFile(InputStream in, File dest) {
		try {
			dest.deleteOnExit();
			System.out.println("Creating file: " + dest.getPath());
			if (!dest.exists()) {
				OutputStream out = new FileOutputStream(dest);
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				out.close();
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String setupIOJar() {
		String tmpDir = System.getProperty("java.io.tmpdir");
		String jarName = tmpDir + File.separator + "evosuite-io.jar";
		writeFile(EvoSuite.class.getClassLoader().getResourceAsStream("evosuite-0.1-SNAPSHOT-evosuite-io.jar"),
		          new File(jarName));
		return jarName;
	}

	@SuppressWarnings("static-access")
	public Object parseCommandLine(String[] args) {
		Options options = new Options();

		Option help = new Option("help", "print this message");
		Option generateSuite = new Option("generateSuite", "use whole suite generation");
		Option generateTests = new Option("generateTests",
		        "use individual test generation");
		Option setup = OptionBuilder.withArgName("target").hasArg().withDescription("use given directory/jar file/package prefix for test generation").create("setup");
		Option targetClass = OptionBuilder.withArgName("class").hasArg().withDescription("target class for test generation").create("class");
		Option criterion = OptionBuilder.withArgName("criterion").hasArg().withDescription("target criterion for test generation").create("criterion");
		Option seed = OptionBuilder.withArgName("seed").hasArg().withDescription("seed for random number generator").create("seed");
		Option mem = OptionBuilder.withArgName("mem").hasArg().withDescription("heap size for client process (in megabytes)").create("mem");

		Option sandbox = new Option("sandbox", "Run tests in sandbox");
		Option mocks = new Option("mocks", "Use mock classes");
		Option stubs = new Option("stubs", "Use stubs");
		Option assertions = new Option("assertions", "Add assertions");
		Option signature = new Option("signature",
		        "Allow manual tweaking of method signatures");

		options.addOption(help);
		options.addOption(generateSuite);
		options.addOption(generateTests);
		options.addOption(setup);
		options.addOption(targetClass);
		options.addOption(criterion);
		options.addOption(seed);
		options.addOption(mem);
		options.addOption(assertions);
		options.addOption(signature);

		options.addOption(sandbox);
		options.addOption(mocks);
		options.addOption(stubs);

		List<String> javaOpts = new ArrayList<String>();
		List<String> cmdOpts = new ArrayList<String>();
		for (String arg : args) {
			if (arg.startsWith("-D")) {
				javaOpts.add(arg);
			} else {
				cmdOpts.add(arg);
			}
		}

		Object result = null;

		// create the parser
		CommandLineParser parser = new GnuParser();
		try {
			// parse the command line arguments
			String[] cargs = new String[cmdOpts.size()];
			cmdOpts.toArray(cargs);
			CommandLine line = parser.parse(options, cargs);
			//javaOpts.addAll(Arrays.asList(line.getArgs()));

			/*
			 * NOTE: JVM arguments will not be passed over from the master to the client.
			 * So for -Xmx, we need to use "mem"
			 */

			if (line.hasOption("mem"))
				javaOpts.add("-Xmx" + line.getOptionValue("mem") + "M");
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

			if (line.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("EvoSuite", options);
			} else if (line.hasOption("setup")) {
				setup(line.getOptionValue("setup"), line.getArgs(), javaOpts);
			} else if (line.hasOption("generateTests")) {
				if (line.hasOption("class"))
					result = generateTests(false, line.getOptionValue("class"), javaOpts);
				else
					generateTests(false, javaOpts);
			} else if (line.hasOption("generateSuite")) {
				if (line.hasOption("class"))
					result = generateTests(true, line.getOptionValue("class"), javaOpts);
				else
					generateTests(true, javaOpts);
			} else {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("EvoSuite", options);
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
	 * @param args
	 */
	public static void main(String[] args) {
		EvoSuite evosuite = new EvoSuite();
		evosuite.parseCommandLine(args);
	}

}
