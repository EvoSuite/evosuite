/**
 * 
 */
package de.unisb.cs.st.evosuite;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
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

import de.unisb.cs.st.evosuite.utils.ExternalProcessHandler;

/**
 * @author Gordon Fraser
 * 
 */
public class EvoSuite {

	private static String separator = System.getProperty("file.separator");
	private static String javaHome = System.getProperty("java.home");
	private static String javaCmd = javaHome + separator + "bin" + separator + "java";

	private static void setup(String target, String[] args) {
		String classPath = System.getProperty("java.class.path");
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
			targetParam = target;
			classPath += File.pathSeparator + target;
			if (!Properties.CP.equals(""))
				Properties.CP += File.pathSeparator;

			Properties.CP += target;
		} else {
			prefix = target;
		}

		List<String> parameters = new ArrayList<String>();
		parameters.add(javaCmd);
		parameters.add("-cp");
		parameters.add(classPath);
		parameters.add("-DPROJECT_PREFIX=" + prefix);
		parameters.add("-DCP=" + Properties.CP);
		parameters.add("-Dclassloader=true");
		parameters.add("-Dshow_progress=true");
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
		File propertyFile = new File(Properties.OUTPUT_DIR + separator
		        + "evosuite.properties");
		if (propertyFile.exists()) {

		} else {
			Properties.PROJECT_PREFIX = prefix;
			Properties.getInstance().writeConfiguration(Properties.OUTPUT_DIR
			                                                    + File.separator
			                                                    + "evosuite.properties");
		}

	}

	private static void generateTests(boolean wholeSuite, List<String> args) {
		File directory = new File(Properties.OUTPUT_DIR);
		String[] extensions = { "task" };
		for (File file : FileUtils.listFiles(directory, extensions, false)) {
			generateTests(wholeSuite, file.getName().replace(".task", ""), args);
		}
	}

	private static void listClasses() {
		System.out.println("* The following classes are known: ");
		File directory = new File(Properties.OUTPUT_DIR);
		String[] extensions = { "task" };
		for (File file : FileUtils.listFiles(directory, extensions, false)) {
			System.out.println("   " + file.getName().replace(".task", ""));
		}

	}

	private static void generateTests(boolean wholeSuite, String target, List<String> args) {
		File taskFile = new File(Properties.OUTPUT_DIR + File.separator + target
		        + ".task");
		if (!taskFile.exists()) {
			System.out.println("* Unknown class: " + target);
			listClasses();
			System.out.println("* If the class is missing but should be there, consider rerunning -setup, or adapting evosuite-files/evosuite.properties");
			return;
		}
		String classPath = System.getProperty("java.class.path");
		if (Properties.CP.charAt(0) == '"')
			Properties.CP = Properties.CP.substring(1, Properties.CP.length() - 1);
		classPath += File.pathSeparator + Properties.CP;
		ExternalProcessHandler handler = new ExternalProcessHandler();
		handler.openServer();
		int port = handler.getServerPort();
		List<String> cmdLine = new ArrayList<String>();
		cmdLine.add(javaCmd);
		cmdLine.add("-cp");
		cmdLine.add(classPath);
		cmdLine.add("-Dprocess_communication_port=" + port);
		cmdLine.add("-Dinline=true");
		//cmdLine.add("-Dminimize_values=true");
		cmdLine.addAll(args);
		if (wholeSuite)
			cmdLine.add("-DSTRATEGY=EvoSuite");
		else
			cmdLine.add("-DSTRATEGY=OneBranch");
		cmdLine.add("-DTARGET_CLASS=" + target);
		if (Properties.PROJECT_PREFIX != null)
			cmdLine.add("-DPROJECT_PREFIX=" + Properties.PROJECT_PREFIX);

		cmdLine.add("-Dclassloader=true");
		cmdLine.add("de.unisb.cs.st.evosuite.ClientProcess");
		String[] newArgs = cmdLine.toArray(new String[cmdLine.size()]);
		if (handler.startProcess(newArgs)) {
			handler.waitForResult((Properties.GLOBAL_TIMEOUT
			        + Properties.MINIMIZATION_TIMEOUT + 120) * 1000); // FIXXME: search timeout plus 100 seconds?			
		} else {
			System.out.println("* Could not connect to client process");
		}
	}

	@SuppressWarnings("static-access")
	private void parseCommandLine(String[] args) {
		Options options = new Options();

		Option help = new Option("help", "print this message");
		Option generateSuite = new Option("generateSuite", "use whole suite generation");
		Option generateTests = new Option("generateTests",
		        "use individual test generation");
		Option setup = OptionBuilder.withArgName("target").hasArg().withDescription("use given directory/jar file/package prefix for test generation").create("setup");
		Option targetClass = OptionBuilder.withArgName("class").hasArg().withDescription("target class for test generation").create("class");
		Option criterion = OptionBuilder.withArgName("criterion").hasArg().withDescription("target criterion for test generation").create("criterion");

		Option sandbox = new Option("sandbox", "Run tests in sandbox");
		Option mocks = new Option("mocks", "Use mock classes");
		Option stubs = new Option("stubs", "Use stubs");

		options.addOption(help);
		options.addOption(generateSuite);
		options.addOption(generateTests);
		options.addOption(setup);
		options.addOption(targetClass);
		options.addOption(criterion);

		options.addOption(sandbox);
		options.addOption(mocks);
		options.addOption(stubs);

		List<String> javaOpts = new ArrayList<String>();
		List<String> cmdOpts = new ArrayList<String>();
		for (String arg : args) {
			if (arg.startsWith("-D"))
				javaOpts.add(arg);
			else
				cmdOpts.add(arg);
		}

		// create the parser
		CommandLineParser parser = new GnuParser();
		try {
			// parse the command line arguments
			String[] cargs = new String[cmdOpts.size()];
			cmdOpts.toArray(cargs);
			CommandLine line = parser.parse(options, cargs);
			javaOpts.addAll(Arrays.asList(line.getArgs()));

			if (line.hasOption("criterion"))
				javaOpts.add("-Dcriterion=" + line.getOptionValue("criterion"));
			if (line.hasOption("sandbox"))
				javaOpts.add("-Dsandbox=true");
			if (line.hasOption("mocks"))
				javaOpts.add("-Dmocks=true");
			if (line.hasOption("stubs"))
				javaOpts.add("-Dstubs=true");

			if (line.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("EvoSuite", options);
			} else if (line.hasOption("setup")) {
				setup(line.getOptionValue("setup"), line.getArgs());
			} else if (line.hasOption("generateTests")) {
				if (line.hasOption("class"))
					generateTests(false, line.getOptionValue("class"), javaOpts);
				else
					generateTests(false, javaOpts);
			} else if (line.hasOption("generateSuite")) {
				if (line.hasOption("class"))
					generateTests(true, line.getOptionValue("class"), javaOpts);
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
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		EvoSuite evosuite = new EvoSuite();
		evosuite.parseCommandLine(args);
	}

}
