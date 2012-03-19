/**
 * 
 */
package de.unisb.cs.st.evosuite.runtime;

import de.unisb.cs.st.evosuite.testcase.TestCluster;

/**
 * @author Gordon Fraser
 * 
 */
public class Runtime {

	public static void resetRuntime() {
		Random.reset();
		System.reset();
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
	}
}
