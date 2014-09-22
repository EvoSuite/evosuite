package org.evosuite.runtime.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.LinkedHashSet;
import java.util.Set;

import org.evosuite.runtime.instrumentation.RuntimeInstrumentation;
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

	protected static final Logger logger = LoggerFactory.getLogger(TransformerForTests.class);

	private volatile boolean active;
	private RuntimeInstrumentation instrumenter;

	private Set<String> instrumentedClasses;
	
	public TransformerForTests(){
		active = false;
		instrumenter = new RuntimeInstrumentation();
		instrumentedClasses = new LinkedHashSet<>();
	}
	
	public void setRetransformingMode(boolean on){
		instrumenter.setRetransformingMode(on);
	}
	
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer)
					throws IllegalClassFormatException {
		
		String classWithDots = className.replace("/", ".");
		if(!active || !RuntimeInstrumentation.checkIfCanInstrument(classWithDots) || classWithDots.startsWith("org.evosuite")){
			return classfileBuffer;
		} else {
			//ClassResetter.getInstance().setClassLoader(loader);
			
			ClassReader reader = new ClassReader(classfileBuffer);
			synchronized(instrumentedClasses){
				instrumentedClasses.add(classWithDots);
			}
			return instrumenter.transformBytes(loader, className, reader); 
		}
	}
	
	public boolean isClassAlreadyTransformed(String className){
		synchronized(instrumentedClasses){
			return instrumentedClasses.contains(className);
		}
	}
	
	public void activate(){
		active = true;
	}
	
	public void deactivate(){
		active = false;
	}
}
