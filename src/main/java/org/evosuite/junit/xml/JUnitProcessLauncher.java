package org.evosuite.junit.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.evosuite.junit.JUnitExecutionException;
import org.evosuite.junit.JUnitResult;
import org.evosuite.utils.ClassPathHandler;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JUnitProcessLauncher {

	public static final String JUNIT_ANALYZER_XML_FILENAME = "junitanalyzer.xml";

	private static Logger logger = LoggerFactory
			.getLogger(JUnitProcessLauncher.class);

	private static int dirCounter = 0;

	private static File createNewTmpDir() {
		File dir = null;
		String dirName = FileUtils.getTempDirectoryPath() + File.separator
				+ "EvoSuite_" + (dirCounter++) + "_"
				+ System.currentTimeMillis();

		//first create a tmp folder
		dir = new File(dirName);
		if (!dir.mkdirs()) {
			logger.error("Cannot create tmp dir: " + dirName);
			return null;
		}

		if (!dir.exists()) {
			logger.error("Weird behavior: we created folder, but Java cannot determine if it exists? Folder: "
					+ dirName);
			return null;
		}

		return dir;
	}

	private String[] parseCommand(String command) {
		List<String> list = new ArrayList<String>();
		for (String token : command.split(" ")) {
			String entry = token.trim();
			if (!entry.isEmpty()) {
				list.add(entry);
			}
		}
		String[] parsedCommand = list.toArray(new String[0]);
		return parsedCommand;
	}

	public JUnitResult startNewJUnitProcess(Class<?>[] testClasses,
			File testClassDir) throws JUnitExecutionException {

		if (testClasses.length == 0) {
			throw new IllegalArgumentException(
					"Cannot invoke startNewJUnitProcess with no test classes");
		}

		String baseDirName = System.getProperty("user.dir");
		File baseDir = new File(baseDirName);
		File tempDir = createNewTmpDir();
		String xmlFileName = tempDir.getAbsolutePath() + File.separatorChar
				+ JUNIT_ANALYZER_XML_FILENAME;

		String junitClassPath;
		if (testClassDir != null) {
			junitClassPath = testClassDir.getAbsolutePath()
					+ File.pathSeparatorChar;
		} else {
			junitClassPath = "";
		}
		junitClassPath += ClassPathHandler.getInstance().getEvoSuiteClassPath();
		junitClassPath += File.pathSeparatorChar;
		junitClassPath += ClassPathHandler.getInstance()
				.getTargetProjectClasspath();

		String command = "java";
		command += " -cp " + junitClassPath;
		command += " " + JUnitXmlDocMain.class.getCanonicalName();
		String testClassesString = "";
		for (Class<?> testClass : testClasses) {
			command += " " + testClass.getCanonicalName();
			testClassesString += " " + testClass.getCanonicalName();
		}

		logger.debug("Checking XML file already exists " + xmlFileName);
		File xmlFile = new File(xmlFileName);
		if (xmlFile.exists()) {
			logger.debug("Deleting XML file " + xmlFileName);
			xmlFile.delete();
		}

		command += " " + xmlFileName;

		String[] parsedCommand = parseCommand(command);

		ProcessBuilder builder = new ProcessBuilder(parsedCommand);
		builder.directory(baseDir);
		builder.redirectErrorStream(true);

		LoggingUtils.getEvoLogger().info(
				"Going to start process for running JUnit for test classes : "
						+ testClassesString);
		logger.debug("Base directory: " + baseDirName);
		logger.debug("Command: " + command);

		try {
			Process process = builder.start();

			InputStream stdout = process.getInputStream();
			logger.debug("JUnit process output:");

			List<String> bufferStdOut = new LinkedList<String>();
			do {
				readInputStream("Finished JUnit process output - ", stdout,
						bufferStdOut);
			} while (!isFinished(process));

			int exitValue = process.exitValue();
			LoggingUtils.getEvoLogger().info("JUnit process finished");
			logger.debug("JUnit process exit code was " + exitValue);
			if (exitValue != 0) {
				logger.warn("JUnit process XML did not finish correctly. Exit code: "
						+ exitValue);

				logger.warn("Standard Output/Error from JUnit processs");
				for (String stdLine : bufferStdOut) {
					logger.warn(stdLine);
				}
				throw new JUnitExecutionException(
						"Execution of java command did not end correctly");
			}

			if (xmlFile.exists()) {
				logger.debug("Reading JUnitResult from file: " + xmlFileName);
				JUnitXmlResultProxy proxy = new JUnitXmlResultProxy();
				JUnitResult result = proxy.readFromXmlFile(xmlFileName);
				xmlFile.delete();
				LoggingUtils.getEvoLogger().info(
						"JUnit finished correctly and created JUnit result.");
				return result;
			} else {
				logger.warn("JUnit process XML file does not exists: "
						+ xmlFile.getAbsolutePath());
				logger.debug("XML file was expected because JUnit process finished correctly with exit code was "
						+ exitValue + " ");
				throw new JUnitExecutionException(
						"Expected result of JUnitXmlProxy was not found "
								+ xmlFileName);
			}

		} catch (IOException e) {
			logger.warn("IOException during JUnit process execution ");
			throw new JUnitExecutionException(e);
		} catch (JUnitXmlResultProxyException e) {
			logger.warn("JUnitXmlResultProxyException during JUnit process execution ");
			throw new JUnitExecutionException(e);
		}
	}

	private boolean isFinished(Process process) {
		try {
			process.exitValue();
			return true;
		} catch (IllegalThreadStateException ex) {
			return false;
		}
	}

	private void readInputStream(String prefix, InputStream in,
			List<String> buffer) throws IOException {
		InputStreamReader is = new InputStreamReader(in);
		BufferedReader br = new BufferedReader(is);
		String read = br.readLine();
		while (read != null) {
			logger.debug(prefix + read);
			buffer.add(prefix + read);
			read = br.readLine();
		}
	}
}
