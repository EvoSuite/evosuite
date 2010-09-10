/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

import de.unisb.cs.st.javalanche.mutation.javaagent.classFileTransfomer.DistanceTransformer;

/**
 * @author Gordon Fraser
 *
 */
public class TestSuitePreMain {

	/**
	 * Decides which transformers to add depending on the {@link RunMode}.
	 * 
	 */
	public static void premain(String agentArguments,
			Instrumentation instrumentation) {
		System.out.println("EvoSuite rocks!");
		
		if(agentArguments.equals("generate")) {
			System.out.println("Creating tests");
			addClassFileTransformer(instrumentation, new CoverageInstrumentation());
			
		} else if (agentArguments.equals("scan")) {
			System.out.println("Scanning project for classes");
			addClassFileTransformer(instrumentation,
					new DistanceTransformer());
			//addClassFileTransformer(instrumentation,
			//		new ScanProjectTransformer());			
		} else if (agentArguments.equals("tasks")) {
			// Do nothing?
		}
	}
	
	private static void addClassFileTransformer(
			Instrumentation instrumentation, ClassFileTransformer clt) {
		instrumentation.addTransformer(clt);
	}
}
