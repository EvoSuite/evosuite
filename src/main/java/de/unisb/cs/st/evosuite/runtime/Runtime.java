/**
 * 
 */
package de.unisb.cs.st.evosuite.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.testcase.TestCluster;

/**
 * @author Gordon Fraser
 * 
 */
public class Runtime {

	private static Logger logger = LoggerFactory.getLogger(Runtime.class);

	public static void resetRuntime() {
		if (!Properties.REPLACE_CALLS)
			return;

		Random.reset();
		System.reset();
		if (Properties.VIRTUAL_FS) {
			FileSystem.reset();
		}
	}

	public static void handleRuntimeAccesses() {
		if (!Properties.REPLACE_CALLS)
			return;

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
		if (Properties.VIRTUAL_FS && FileSystem.wasAccessed()) {
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
