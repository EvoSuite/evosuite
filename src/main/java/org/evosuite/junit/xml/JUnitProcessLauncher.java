package org.evosuite.junit.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.evosuite.junit.JUnitExecutionException;
import org.evosuite.junit.JUnitResult;
import org.evosuite.utils.ClassPathHandler;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JUnitProcessLauncher {

	private static final String JUNIT_ANALYZER_XML = "junitanalyzer.xml";

	private static Logger logger = LoggerFactory
			.getLogger(JUnitProcessLauncher.class);

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
		String baseDir = System.getProperty("user.dir");
		File dir = new File(baseDir);
		String xmlFileName = baseDir + File.separatorChar + JUNIT_ANALYZER_XML;

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
		for (Class<?> testClass : testClasses) {
			command += " " + testClass.getCanonicalName();
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
		builder.directory(dir);
//		builder.redirectErrorStream(true);

		LoggingUtils.getEvoLogger().info(
				"Going to start process for running JUnit on : " + command);
		logger.debug("Base directory: " + baseDir);
		logger.debug("Command: " + command);

		try {
			Process process = builder.start();
			int exitCode = process.waitFor();
			InputStream stdout = process.getInputStream();
			String stdoutStr = readInputStream("Finished JUnit process stdout - " , stdout);
			logger.debug("JUnit process stdout:");
			logger.debug(stdoutStr);
			InputStream stderr = process.getErrorStream();
			String stderrStr = readInputStream("Finished JUnit process stderr - " , stderr);
			logger.debug("JUnit process stderr:");
			logger.debug(stderrStr);
			
			logger.debug("JUnit process exit code was " + exitCode);
			if (exitCode != 0) {
				logger.warn("JUnit process XML did not finish correctly. Exit code: "
						+ exitCode);
				throw new JUnitExecutionException(
						"Execution of java command did not end correctly: "
								+ command);
			}

			if (xmlFile.exists()) {
				logger.debug("Reading JUnitResult from file: " + xmlFileName);
				JUnitXmlResultProxy proxy = new JUnitXmlResultProxy();
				JUnitResult result = proxy.readFromXmlFile(xmlFileName);
				xmlFile.delete();
				return result;
			} else {
				logger.warn("JUnit process XML file does not exists: "
						+ xmlFile.getAbsolutePath());
				logger.debug("XML file was expected because JUnit process finished correctly with exit code was "
						+ exitCode + " ");
				throw new JUnitExecutionException(
						"Expected result of JUnitXmlProxy was not found "
								+ xmlFileName);
			}

		} catch (IOException e) {
			logger.warn("IOException during JUnit process execution ");
			throw new JUnitExecutionException(e);
		} catch (InterruptedException e) {
			logger.warn("InterruptedException during JUnit process execution ");
			throw new JUnitExecutionException(e);
		} catch (JUnitXmlResultProxyException e) {
			logger.warn("JUnitXmlResultProxyException during JUnit process execution ");
			throw new JUnitExecutionException(e);
		}
	}

	private String readInputStream(String prefix, InputStream in) throws IOException {
		InputStreamReader is = new InputStreamReader(in);
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(is);
		String read = br.readLine();
		while (read != null) {
			sb.append("\n");
			sb.append(prefix);
			sb.append(read);
			read = br.readLine();
		}
		return sb.toString();
	}
}
