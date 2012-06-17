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
package de.unisb.cs.st.evosuite.runtime;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.io.IOWrapper;

/**
 * @author fraser
 * 
 */
public class FileSystem {

	private static Logger logger = LoggerFactory.getLogger(FileSystem.class);

	public static FileSystemManager manager = null;

	/**
	 * Test method that sets content of a file
	 * 
	 * @param fileName
	 * @param content
	 */
	public static void setFileContent(EvoSuiteFile fileName, String content) {
		// Put "content" into "file"
		try {
			for (String canonicalPath : IOWrapper.getAccessedFiles()) {
				if (canonicalPath.equals(fileName.getPath())) {
					logger.info("Writing content to (virtual) file " + fileName.getPath());
					File ramFile = new File(fileName.getPath());
					Writer writer = new BufferedWriter(new FileWriter(ramFile));
					writer.write(content);
					writer.close();
				}
			}
		} catch (FileSystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// TODO
	// public static void setFilePermission(EvoSuiteFile file, ...);

	// TODO
	// public static void deleteFile(EvoSuiteFile file);

	/**
	 * Reset runtime to initial state
	 * 
	 */
	public static void reset() {
		if (Properties.VIRTUAL_FS) {
			try {
				// Class<?> clazz = TestCluster.classLoader
				// .loadClass("org.apache.commons.vfs2.VFS");
				// clazz.getMethod("getManager", new Class<?>[] {}).invoke(null); // throws FileSystemException

				IOWrapper.initVFS();

				// for (File f : File.createdFiles) {
				// f.delete();
				// }
				// TODO: Find proper way to clear filesystem

				IOWrapper.clearAccessedFiles();
				IOWrapper.activeVFS = true;
			} catch (FileSystemException e) {
				logger.warn("Error during initialization of virtual FS: " + e
						+ ", " + e.getCause());//
			} catch (Throwable t) {
				logger.warn("Error: " + t);
			}
			/*
			 * catch (ClassNotFoundException e) { // TODO Auto-generated catch block e.printStackTrace(); } catch (IllegalArgumentException e) { //
			 * TODO Auto-generated catch block e.printStackTrace(); } catch (SecurityException e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); } catch (IllegalAccessException e) { // TODO Auto-generated catch block e.printStackTrace(); } catch
			 * (InvocationTargetException e) { // TODO Auto-generated catch block e.printStackTrace(); } catch (NoSuchMethodException e) { // TODO
			 * Auto-generated catch block e.printStackTrace(); }
			 */
		}
	}

	public static void restoreOriginalFS() {
		if (Properties.VIRTUAL_FS) {
			IOWrapper.activeVFS = false;
		}
	}

	/**
	 * Getter to check whether this runtime replacement was accessed during test execution
	 * 
	 * @return
	 */
	public static boolean wasAccessed() {
		return IOWrapper.filesWereAccessed();
	}
}
