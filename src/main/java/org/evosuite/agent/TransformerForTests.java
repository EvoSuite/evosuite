package org.evosuite.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.evosuite.instrumentation.BytecodeInstrumentation;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransformerForTests implements ClassFileTransformer {

	private static final Logger logger = LoggerFactory.getLogger(TransformerForTests.class);

	private volatile boolean active;
	private BytecodeInstrumentation instrumenter;
	
	public TransformerForTests(){
		active = false;
		instrumenter = new BytecodeInstrumentation();
	}
	
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer)
					throws IllegalClassFormatException {

		String classWithDots = className.replace("/", ".");
		if(!active || BytecodeInstrumentation.isSharedClass(classWithDots) || classWithDots.startsWith("org.evosuite")){	
			return classfileBuffer;
		} else {
			logger.debug("Going to instrument: "+className);
			ClassReader reader = new ClassReader(classfileBuffer);
			return instrumenter.transformBytes(loader, className, reader); 
		}
	}
	
	public void activate(){
		active = true;
	}
	
	public void deacitvate(){
		active = false;
	}
}
