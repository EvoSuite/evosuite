/*
 * Copyright (C) 2011 Saarland University
 * 
 * This file is part of Javalanche.
 * 
 * Javalanche is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Javalanche is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * Javalanche. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.utils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.unisb.cs.st.ds.util.io.DirectoryFileSource;
import de.unisb.cs.st.ds.util.io.Io;
import de.unisb.cs.st.evosuite.Properties;

public class HtmlAnalyzer {

	private static Logger logger = Logger.getLogger(HtmlAnalyzer.class);

	private Set<File> files;

	public Iterable<String> getClassContent(String fullClassName) {
		if (files == null) {
			initFiles();
		}
		// String className = fullClassName.substring(fullClassName
		// .lastIndexOf('.') + 1);
		String className = getClassName(fullClassName);
		logger.debug("Looking for content of class " + fullClassName + " in "
		        + files.size() + " files.");
		logger.debug("Files: " + files);
		for (File f : files) {
			String name = getContaingClassName(f);
			if (name.endsWith(className) && !name.endsWith("Test" + className)) {
				List<String> linesFromFile = Io.getLinesFromFile(f);
				logger.debug("Got file " + f + "for class " + fullClassName);
				return linesFromFile;
			}
		}
		String msg = "No source found for " + fullClassName;
		logger.debug(msg);
		return Arrays.asList(msg);
	}

	public static String getContaingClassName(File f) {
		String name = f.getAbsolutePath();
		String sep = System.getProperty("file.separator");
		name = name.replace(sep, ".");
		if (name.endsWith(".java")) {
			name = name.substring(0, name.length() - 5);
		}
		int i = name.lastIndexOf(Properties.PROJECT_PREFIX);
		if (i < 0) {
			name = "";
		} else {
			name = name.substring(i);
		}
		return name;
	}

	private void initFiles() {
		File startDirectory = new File(".");
		try {
			Collection<File> javaFiles = DirectoryFileSource.getFilesByExtension(startDirectory,
			                                                                     "java");
			files = new HashSet<File>(javaFiles);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getClassName(String name) {
		if (name.contains("$")) {
			name = name.substring(0, name.indexOf('$'));
		}
		return name;
	}
}
