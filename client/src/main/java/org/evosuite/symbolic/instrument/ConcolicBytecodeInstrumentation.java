package org.evosuite.symbolic.instrument;

import org.evosuite.junit.writer.TestSuiteWriterUtils;
import org.evosuite.runtime.instrumentation.MethodCallReplacementClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * This class performns the bytecode transformation. It adds new bytecode for
 * registering the constraints during execution.
 * 
 * @author galeotti
 * 
 */
public class ConcolicBytecodeInstrumentation {

	//private static Logger logger = LoggerFactory.getLogger(DscBytecodeInstrumentation.class);

	/**
	 * Applies DscClassAdapter to the className in the argument
	 * 
	 */
	public byte[] transformBytes(String className, ClassReader reader) {
		int readFlags = ClassReader.SKIP_FRAMES;

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

		ClassVisitor cv = writer;

		// Apply transformations to class under test and its owned
		// classes
		// cv = new TraceClassVisitor(cv, new PrintWriter(System.err));
		cv = new ConcolicClassAdapter(cv, className);

        // Mock instrumentation (eg File and TCP).
        if (TestSuiteWriterUtils.needToUseAgent()) {
            cv = new MethodCallReplacementClassAdapter(cv, className);
        }
		
		reader.accept(cv, readFlags);

		return writer.toByteArray();
	}
}
