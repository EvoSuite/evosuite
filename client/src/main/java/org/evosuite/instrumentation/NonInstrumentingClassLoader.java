package org.evosuite.instrumentation;

import java.io.IOException;
import java.io.InputStream;

import org.evosuite.TestGenerationContext;
import org.evosuite.runtime.util.ComputeClassWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import javax.persistence.Entity;

public class NonInstrumentingClassLoader extends InstrumentingClassLoader {

	public NonInstrumentingClassLoader(){
		super();
	}

	/*
	public NonInstrumentingClassLoader(ClassLoader parent) {
		super(parent);
		setClassAssertionStatus(Properties.TARGET_CLASS, true);
		classLoader = parent; //NonInstrumentingClassLoader.class.getClassLoader();

	}
	*/

	@Override
	protected byte[] getTransformedBytes( String className, InputStream is) throws IOException{

		ClassReader reader = new ClassReader(is);
		int readFlags = ClassReader.SKIP_FRAMES;

		/*
		 *  To use COMPUTE_FRAMES we need to remove JSR commands.
		 *  Therefore, we have a JSRInlinerAdapter in NonTargetClassAdapter
		 *  as well as CFGAdapter.
		 */
		int asmFlags = ClassWriter.COMPUTE_FRAMES;
		ClassWriter writer = new ComputeClassWriter(asmFlags);

		ClassVisitor cv = writer;
		cv = new NonTargetClassAdapter(cv, className);
		reader.accept(cv, readFlags);
		return writer.toByteArray();
	}
}
