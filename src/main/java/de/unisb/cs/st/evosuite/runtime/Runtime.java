/**
 * 
 */
package de.unisb.cs.st.evosuite.runtime;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.testcase.TestCluster;

/**
 * @author Gordon Fraser
 * 
 */
public class Runtime {

	private static Logger logger = LoggerFactory.getLogger(Runtime.class);

	public static void resetRuntime() {
		Random.reset();
		System.reset();
		FileSystem.reset();
	}

	public static void handleRuntimeAccesses() {
		if (Random.wasAccessed()) {
			try {
				TestCluster.getInstance().addTestCall(Random.class.getMethod("setNextRandom",
				                                                             new Class<?>[] { int.class }));
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (System.wasAccessed()) {
			try {
				TestCluster.getInstance().addTestCall(System.class.getMethod("setCurrentTimeMillis",
				                                                             new Class<?>[] { long.class }));
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (FileSystem.wasAccessed()) {
			logger.info("Files accessed: " + File.createdFiles);
			try {
				TestCluster.getInstance().addTestCall(FileSystem.class.getMethod("setFileContent",
				                                                                 new Class<?>[] {
				                                                                         EvoSuiteFile.class,
				                                                                         String.class }));
				// TODO: Add other methods (setFilePermission, etc)

			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
