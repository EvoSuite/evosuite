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
import java.io.EvoSuiteIO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.vfs2.FileSystemException;
import org.evosuite.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * provides file system operations for an <code>EvoSuiteFile</code>
 * 
 * @author Daniel Muth
 */
public class FileSystem {

	private static Logger logger = LoggerFactory.getLogger(FileSystem.class);

	/**
	 * Resets the vfs to a default state (all files deleted or all files in a specific state)
	 * 
	 */
	public static void reset() {
		if (Properties.VIRTUAL_FS) {
			try {
				logger.info("Resetting and enabling the VFS...");
				EvoSuiteIO.resetVFS();
				EvoSuiteIO.enableVFS();
			} catch (FileSystemException e) {
				logger.warn("Error during initialization of virtual FS: " + e
						+ ", " + e.getCause());//
			} catch (Throwable t) {
				logger.warn("Error: " + t);
			}
		}
	}

	/**
	 * Test method that sets content of a file
	 * 
	 * @param evoSuiteFile
	 * @param content
	 * @throws IOException
	 */
	public static void setFileContent(EvoSuiteFile evoSuiteFile, String content)
			throws IOException {
		if (evoSuiteFile == null)
			throw new NullPointerException("evoSuiteFile must not be null!");

		// Put "content" into "file"
		File file = new File(evoSuiteFile.getPath());

		if (!file.exists())
			throw new FileNotFoundException();

		logger.info("Writing \"" + content + "\" to (virtual) file "
				+ evoSuiteFile.getPath());
		EvoSuiteIO.setOriginal(file, false);
		Writer writer = new BufferedWriter(new FileWriter(file));
		writer.write(content);
		writer.close();
	}

	public static void setReadPermission(EvoSuiteFile evoSuiteFile,
			boolean readable) {
		if (evoSuiteFile == null)
			throw new NullPointerException("evoSuiteFile must not be null!");

		File file = new File(evoSuiteFile.getPath());
		EvoSuiteIO.setOriginal(file, false);
		if (file.setReadable(readable)) {
			logger.debug("Read permisson of " + evoSuiteFile.getPath()
					+ " was successfully set to " + readable + " !");
		} else {
			logger.debug("Setting read permission of " + evoSuiteFile.getPath()
					+ " to " + readable + " failed!");
		}
	}

	public static void setWritePermission(EvoSuiteFile evoSuiteFile,
			boolean writable) {
		if (evoSuiteFile == null)
			throw new NullPointerException("evoSuiteFile must not be null!");

		File file = new File(evoSuiteFile.getPath());
		EvoSuiteIO.setOriginal(file, false);
		if (file.setWritable(writable)) {
			logger.debug("Write permisson of " + evoSuiteFile.getPath()
					+ " was successfully set to " + writable + " !");
		} else {
			logger.debug("Setting write permission of "
					+ evoSuiteFile.getPath() + " to " + writable + " failed!");
		}
	}

	public static void setExecutePermission(EvoSuiteFile evoSuiteFile,
			boolean executable) {
		if (evoSuiteFile == null)
			throw new NullPointerException("evoSuiteFile must not be null!");

		File file = new File(evoSuiteFile.getPath());
		EvoSuiteIO.setOriginal(file, false);
		if (file.setExecutable(executable)) {
			logger.debug("Execute permisson of " + evoSuiteFile.getPath()
					+ " was successfully set to " + executable + " !");
		} else {
			logger.debug("Setting execute permission of "
					+ evoSuiteFile.getPath() + " to " + executable + " failed!");
		}
	}

	/**
	 * Test method that tries to delete a file or directory by calling {@link EvoSuiteIO#deepDelete(File)}. In contrast to normal deletion this method
	 * also deletes a directory that contains children (by deleting them as well).
	 * 
	 * @param evoSuiteFile
	 * @throws IOException
	 */
	public static void deepDelete(EvoSuiteFile evoSuiteFile) throws IOException {
		if (evoSuiteFile == null)
			throw new NullPointerException("evoSuiteFile must not be null!");

		File file = new File(evoSuiteFile.getPath());
		EvoSuiteIO.setOriginal(file, false);
		if (EvoSuiteIO.deepDelete(file)) {
			logger.debug("Deep-deleting \"" + evoSuiteFile.getPath()
					+ "\" was successful!");
		} else {
			logger.debug("Deep-deleting \"" + evoSuiteFile.getPath()
					+ "\" failed!");
		}
	}

	public static void createFile(EvoSuiteFile evoSuiteFile) throws IOException {
		if (evoSuiteFile == null)
			throw new NullPointerException("evoSuiteFile must not be null!");

		File file = new File(evoSuiteFile.getPath());
		EvoSuiteIO.setOriginal(file, false);
		if (file.createNewFile()) {
			logger.debug(evoSuiteFile.getPath()
					+ " was successfully created as a file!");
		} else {
			logger.debug("Creation of " + evoSuiteFile.getPath()
					+ " as a file failed!");
		}
	}

	/**
	 * Test method that tries to create this file as a directory. Also creates any necessary but nonexistent parent directories.
	 * 
	 * @param evoSuiteFile
	 */
	public static void createDirectory(EvoSuiteFile evoSuiteFile) {
		if (evoSuiteFile == null)
			throw new NullPointerException("evoSuiteFile must not be null!");

		File file = new File(evoSuiteFile.getPath());
		EvoSuiteIO.setOriginal(file, false);
		if (file.mkdirs()) {
			logger.debug(evoSuiteFile.getPath()
					+ " was successfully created as a directory!");
		} else {
			logger.debug("Creation of " + evoSuiteFile.getPath()
					+ " as a directory failed!");
		}
	}

	/**
	 * Test method that tries to fill a directory with a subFile and a subDirectory
	 * 
	 * @param evoSuiteFile
	 * @return true if the directory was filled successfully
	 * @throws IOException
	 */
	public static void createAndFillDirectory(EvoSuiteFile evoSuiteFile)
			throws IOException {
		if (evoSuiteFile == null)
			throw new NullPointerException("evoSuiteFile must not be null!");

		File file = new File(evoSuiteFile.getPath());
		EvoSuiteIO.setOriginal(file, false);
		file.mkdirs();

		File subDirectory = new File(file, "EvoSuiteTestSubDirectory");
		EvoSuiteIO.setOriginal(subDirectory, false);

		File subFile = new File(file, "EvoSuiteTestSubFile");
		EvoSuiteIO.setOriginal(subFile, false);

		boolean successful = true;
		successful &= subDirectory.mkdir();
		successful &= subFile.createNewFile();

		if (successful) {
			logger.debug("creating and filling directory "
					+ evoSuiteFile.getPath() + " was successful!");
		} else {
			logger.debug("creating and filling directory "
					+ evoSuiteFile.getPath() + " failed!");
		}
	}

	/**
	 * 
	 * @param evoSuiteFile
	 * @return <code>true</code>, if creation was successful, <code>false</code> otherwise
	 * @throws IOException
	 */
	public static void createParent(EvoSuiteFile evoSuiteFile) // TODO unnecessary?
			throws IOException {
		if (evoSuiteFile == null)
			throw new NullPointerException("evoSuiteFile must not be null!");

		File file = new File(evoSuiteFile.getPath());
		EvoSuiteIO.setOriginal(file, false);

		File parent = file.getCanonicalFile().getParentFile();
		EvoSuiteIO.setOriginal(parent, false);
		parent.mkdirs();
	}

	public static void deepDeleteParent(EvoSuiteFile evoSuiteFile) // TODO unnecessary?
			throws IOException {
		if (evoSuiteFile == null)
			throw new NullPointerException("evoSuiteFile must not be null!");

		File file = new File(evoSuiteFile.getPath());
		EvoSuiteIO.setOriginal(file, false);

		File parent = file.getCanonicalFile().getParentFile();
		EvoSuiteIO.setOriginal(parent, false);
		EvoSuiteIO.deepDelete(parent);
	}

}
