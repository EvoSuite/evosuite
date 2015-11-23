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
package org.evosuite.junit.xml;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.evosuite.Properties;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.junit.JUnitExecutionException;
import org.evosuite.junit.JUnitResult;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.ProcessLauncher;
import org.evosuite.utils.ProcessTimeoutException;
import org.evosuite.utils.FileIOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JUnitProcessLauncher {

	public static final String JUNIT_ANALYZER_XML_FILENAME = "junitanalyzer.xml";

	static Logger logger = LoggerFactory.getLogger(JUnitProcessLauncher.class);

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

		Vector<String> command = new Vector<String>();
		command.add("java");
		command.add("-cp");
		command.add(junitClassPath);
		command.add("-Djava.awt.headless=true");
		command.add(JUnitXmlDocMain.class.getCanonicalName());
		String testClassesString = "";
		for (Class<?> testClass : testClasses) {
			command.add(testClass.getCanonicalName());
			testClassesString += " " + testClass.getCanonicalName();
		}

		logger.debug("Checking XML file already exists " + xmlFileName);
		File xmlFile = new File(xmlFileName);
		if (xmlFile.exists()) {
			logger.debug("Deleting XML file " + xmlFileName);
			xmlFile.delete();
		}

		command.add(xmlFileName);

		String[] parsedCommand = command.toArray(new String[] {});

		LoggingUtils.getEvoLogger().info(
				"Going to start process for running JUnit for test classes : "
						+ testClassesString);
		logger.debug("Base directory: " + baseDirName);
		logger.debug("Command: " + command);

		try {
			int timeout = Properties.TIMEOUT * testClasses.length;
			List<String> bufferStdOut = new LinkedList<String>();
			ProcessLauncher launcher = new ProcessLauncher();
			int exitValue = launcher.launchNewProcess(baseDir, parsedCommand,
					timeout);
			LoggingUtils.getEvoLogger().info("JUnit process finished");
			logger.debug("JUnit process exit code was " + exitValue);
			if (exitValue != 0) {
				logger.warn("JUnit process XML did not finish correctly. Exit code: "
						+ exitValue);

				logger.debug("Standard Output/Error from JUnit processs");
				for (String stdLine : bufferStdOut) {
					logger.debug(stdLine);
				}
				throw new JUnitExecutionException(
						"Execution of java command did not end correctly");
			}

			if (xmlFile.exists()) {
				logger.debug("Reading JUnitResult from file: " + xmlFileName);
				JUnitResult result = FileIOUtils.<JUnitResult> readXML(xmlFileName);
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
		} catch (ProcessTimeoutException e) {
			logger.warn("A timeout occurred during JUnit process execution ");
			throw new JUnitExecutionException(e);
		}
	}
}
