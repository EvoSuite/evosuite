package org.evosuite.runtime;

import java.lang.instrument.UnmodifiableClassException;
import java.util.ArrayList;
import java.util.List;

import org.evosuite.runtime.agent.InstrumentingAgent;
import org.evosuite.runtime.reset.ClassResetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Class used to handle the static state of classes, their intialization and
 * re-instrumentation 
 * @author arcuri
 *
 */
public class ClassStateSupport {

	private static final Logger logger = LoggerFactory.getLogger(ClassStateSupport.class);
	
	public static void initializeClasses(ClassLoader classLoader, String... classNames){

		List<Class<?>> classes = loadClasses(classLoader, classNames); 
		//retransformIfNeeded(classes); //can't use it, as input contains only classes with static state
	}

	/**
	 * Reset the static state of all the given classes
	 * @param classNames
	 */
	public static void resetClasses(String... classNames) {
		for (int i=0; i< classNames.length;i++) {
			String classNameToReset = classNames[i];
			ClassResetter.getInstance().reset(classNameToReset); 
		}
	}

	/*
	public static void instrumentAlreadyLoadedClasses(){
		//this is too extreme: eg it picks up all Eclipse classes when tests are run from Eclipse... 
		Class<?>[] classes = InstrumentingAgent.getInstumentation().getAllLoadedClasses();
		retransformIfNeeded(Arrays.asList(classes));
	}
	*/

	/**
	 * If any of the loaded class was not instrumented yet, then re-instrument them.
	 * Note: re-instrumentation is more limited, as cannot change class signature
	 */
	public static void retransformIfNeeded(ClassLoader classLoader, String... classNames){
		List<Class<?>> classes = new ArrayList<>();
		for(String name : classNames){
			try {
				classes.add(classLoader.loadClass(name));
			} catch (ClassNotFoundException e) {
				java.lang.System.err.println("Could not load: "+name);
			}
		}
		retransformIfNeeded(classes);
	}
	
	/**
	 * If any of the loaded class was not instrumented yet, then re-instrument them.
	 * Note: re-instrumentation is more limited, as cannot change class signature
	 * @param classes
	 */
	public static void retransformIfNeeded(List<Class<?>> classes) {

		if(classes==null || classes.isEmpty()){
			return;
		}
		
		List<Class<?>> classToReInstument = new ArrayList<>();

		/*
		InstrumentingAgent.activate(); 
		for(Class<?> cl : classes){

			try{
				InstrumentingAgent.getInstumentation().retransformClasses(cl);
			} catch(UnsupportedOperationException e){ 
				/ *
				 * this happens if class was already loaded by JUnit (eg the abstract class problem)
				 * and re-instrumentation do change the signature 
				 * /
				classToReInstument.add(cl);
			} catch(Exception | Error e){
				//this shouldn't really happen
				java.lang.System.err.println("Could not instrument "+cl.getName()+". Exception "+e.toString());
			}

		}
		*/
		
		for(Class<?> cl : classes){
			if(! InstrumentingAgent.getTransformer().isClassAlreadyTransformed(cl.getName())){
				classToReInstument.add(cl);
			}
		}
		
		if(classToReInstument.isEmpty()){
			return;
		}
		
		InstrumentingAgent.setRetransformingMode(true);
		try {
			if(!classToReInstument.isEmpty()){
				InstrumentingAgent.getInstumentation().retransformClasses(classToReInstument.toArray(new Class<?>[0]));
			}
		} catch (UnmodifiableClassException e) {
			//this shouldn't really happen, as already checked in previous loop
			java.lang.System.err.println("Could not re-instrument classes");
		} catch(UnsupportedOperationException e){  
			//if this happens, then it is a bug in EvoSuite :( 
			logger.error("EvoSuite wrong re-instrumentation: "+e.getMessage());
		}finally{
			InstrumentingAgent.setRetransformingMode(false);
		}

		InstrumentingAgent.deactivate();
	}

	private static List<Class<?>> loadClasses(ClassLoader classLoader, String... classNames) {

		List<Class<?>> classes = new ArrayList<>();

		InstrumentingAgent.activate(); 
		for (int i=0; i< classNames.length;i++) {
			org.evosuite.runtime.Runtime.getInstance().resetRuntime(); 
			String classNameToLoad = classNames[i];
			try {
				Class<?> aClass = Class.forName(classNameToLoad, true, classLoader);
				classes.add(aClass);
			} catch (Exception | Error ex) {
				logger.error("Could not initialize " + classNameToLoad+": "+ex.getMessage());
			} 
		}
		InstrumentingAgent.deactivate();
		return classes;
	}

}
