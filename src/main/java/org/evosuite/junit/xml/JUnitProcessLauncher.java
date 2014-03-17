package org.evosuite.junit.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.evosuite.junit.JUnitAnalyzer;
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

		String junitClassPath = testClassDir.getAbsolutePath()
				+ File.pathSeparatorChar
				+ ClassPathHandler.getInstance().getEvoSuiteClassPath()
				+ File.pathSeparatorChar
				+ ClassPathHandler.getInstance().getTargetProjectClasspath();

		String command = "java";
		command += " -cp " + junitClassPath;
		command += " " + JUnitXmlDocMain.class.getCanonicalName();
		for (Class<?> testClass : testClasses) {
			command += " " + testClass.getCanonicalName();
		}

		File xmlFile = new File(xmlFileName);
		if (xmlFile.exists()) {
			xmlFile.delete();
		}

		command += " " + xmlFileName;

		String[] parsedCommand = parseCommand(command);

		ProcessBuilder builder = new ProcessBuilder(parsedCommand);
		builder.directory(dir);
		builder.redirectErrorStream(true);

		LoggingUtils.getEvoLogger().info(
				"Going to start process for running JUnit on : " + command);
		logger.debug("Base directory: " + baseDir);
		logger.debug("Command: " + command);

		try {
			Process process = builder.start();
			int exitCode = process.waitFor();
			if (exitCode != 0) {
				throw new JUnitExecutionException(
						"Execution of java command did not end correctly: "
								+ command);
			}

			if (xmlFile.exists()) {
				JUnitXmlResultProxy proxy = new JUnitXmlResultProxy();
				JUnitResult result = proxy.readFromXmlFile(xmlFileName);
				xmlFile.delete();
				return result;
			} else {
				throw new JUnitExecutionException("Expected result of JUnitXmlProxy was not found " + xmlFileName);
			}

		} catch (IOException e) {
			throw new JUnitExecutionException(e);
		} catch (InterruptedException e) {
			throw new JUnitExecutionException(e);
		} catch (JUnitXmlResultProxyException e) {
			throw new JUnitExecutionException(e);
		}
	}

}
