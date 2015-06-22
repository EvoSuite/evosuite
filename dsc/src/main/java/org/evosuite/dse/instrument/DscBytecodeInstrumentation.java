package org.evosuite.dse.instrument;

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
public class DscBytecodeInstrumentation {

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
		cv = new DscClassAdapter(cv, className);

		reader.accept(cv, readFlags);

		return writer.toByteArray();
	}
}
