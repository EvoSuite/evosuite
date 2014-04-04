package org.evosuite.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.evosuite.instrumentation.BytecodeInstrumentation;
import org.evosuite.reset.ClassResetter;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Once the agent is hooked to the current JVM, each time a class is
 * loaded, its byte[] representation will be first given as input
 * to this class, which can modify/instrument it 
 * 
 * @author arcuri
 *
 */
public class TransformerForTests implements ClassFileTransformer {

	private static final Logger logger = LoggerFactory.getLogger(TransformerForTests.class);

	private volatile boolean active;
	private BytecodeInstrumentation instrumenter;
	
	public TransformerForTests(){
		active = false;
		instrumenter = new BytecodeInstrumentation();
		instrumenter.setIntrumentationUnderJavaAgent(true);
	}
	
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer)
					throws IllegalClassFormatException {
		
		String classWithDots = className.replace("/", ".");
		if(!active || !BytecodeInstrumentation.checkIfCanInstrument(classWithDots) || classWithDots.startsWith("org.evosuite")){	
			return classfileBuffer;
		} else {
			ClassResetter.getInstance().setClassLoader(loader);
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
