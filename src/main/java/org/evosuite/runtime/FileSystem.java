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
package org.evosuite.runtime;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.vfs2.FileSystemException;

import org.evosuite.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.io.IOWrapper;

/**
 * provides file system operations for an <code>EvoSuiteFile</code>
 *
 * @author Daniel Muth
 */
public class FileSystem {

	private static Logger logger = LoggerFactory.getLogger(FileSystem.class);

	/**
	 * saves the files, that shall be deleted after test execution; this includes files created by setFileContent plus files accessed by class under
	 * test
	 */
	private static Set<String> filesToBeDeleted = new HashSet<String>();

	/**
	 * Test method that sets content of a file
	 * 
	 * @param fileName
	 * @param content
	 */
	public static void setFileContent(EvoSuiteFile fileName, String content) {
		// Put "content" into "file"
		try {
			logger.info("Writing \"" + content + "\" to (virtual) file "
					+ fileName.getPath());
			File ramFile = new File(fileName.getPath());
			IOWrapper.setOriginal(ramFile, false);
			Writer writer = new BufferedWriter(new FileWriter(ramFile));
			writer.write(content);
			writer.close();
			filesToBeDeleted.add(fileName.getPath());
		} catch (FileSystemException e) {
			logger.warn("Error while writing \"" + content + "\" to virtual file "
					+ fileName.getPath() + ":\n" + e.toString());
		} catch (IOException e) {
			logger.warn("Error while writing \"" + content + "\" to virtual file "
					+ fileName.getPath() + ":\n" + e.toString());
		}
	}

	// TODO
	// public static void setFilePermission(EvoSuiteFile file, ...);

	// TODO
	// public static void deleteFile(EvoSuiteFile file);

	/**
	 * Resets the vfs to a default state (all files deleted or all files in a specific state)
	 * 
	 */
	public static void reset() {
		if (Properties.VIRTUAL_FS) {
			try {
				IOWrapper.initVFS();

				// TODO: also delete folder structure? do not delete (virtual) files, but only 'reset' their content?
				IOWrapper.activeVFS = true;
				filesToBeDeleted.addAll(IOWrapper.getAccessedFiles());
				for (String fileName : filesToBeDeleted) {
					logger.info("Deleting virtual file: " + fileName);
					File f = new File(fileName);
					IOWrapper.setOriginal(f, false);
					f.delete();
				}
				IOWrapper.clearAccessedFiles();
				filesToBeDeleted.clear();
			} catch (FileSystemException e) {
				logger.warn("Error during initialization of virtual FS: " + e
						+ ", " + e.getCause());//
			} catch (Throwable t) {
				logger.warn("Error: " + t);
			}
		}
	}

	/**
	 * restores the original, 'real' file system
	 */
	public static void restoreOriginalFS() {
		if (Properties.VIRTUAL_FS) {
			IOWrapper.activeVFS = false;
		}
	}

	/**
	 * checks, if files were accessed in the vfs
	 * 
	 * @return true, if files were accessed, false otherwise
	 */
	public static boolean wasAccessed() {
		return IOWrapper.filesWereAccessed();
	}
}
