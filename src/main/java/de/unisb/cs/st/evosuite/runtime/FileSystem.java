/**
 * 
 */
package de.unisb.cs.st.evosuite.runtime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.Properties;

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
		logger.info("Writing content to file " + fileName);
		try {
			for (File file : File.createdFiles) {
				if (file.getCanonicalPath().equals(fileName)) {
					PrintStream stream = new PrintStream(
					        file.getRamFile().getContent().getOutputStream());
					stream.print(content);
					break;
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
			//		try {
			File.USE_SIMULATION = false;
			FileInputStream.USE_SIMULATION = false;
			FileOutputStream.USE_SIMULATION = false;
			//Class<?> clazz = TestCluster.classLoader.loadClass("org.apache.commons.vfs2.VFS");
			//clazz.getMethod("getManager", new Class<?>[] {}).invoke(null);
			//VFS.getManager();
			//for (File f : File.createdFiles) {
			//	f.delete();
			//}
			File.USE_SIMULATION = true;
			// TODO: Find proper way to clear filesystem

			File.createdFiles.clear();
			FileInputStream.USE_SIMULATION = true;
			FileOutputStream.USE_SIMULATION = true;
			//	} catch (FileSystemException e) {
			//		logger.warn("Error during initialization of virtual FS: " + e + ", "
			//		        + e.getCause());//
			//	} catch (Throwable t) {
			//		logger.warn("Error: " + t);
			//	} 
			/*catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}*/
		}
	}

	public static void restoreOriginalFS() {
		if (Properties.VIRTUAL_FS) {
			File.USE_SIMULATION = false;
			FileInputStream.USE_SIMULATION = false;
			FileOutputStream.USE_SIMULATION = false;
		}
	}

	/**
	 * Getter to check whether this runtime replacement was accessed during test
	 * execution
	 * 
	 * @return
	 */
	public static boolean wasAccessed() {
		return !File.createdFiles.isEmpty();
	}
}
