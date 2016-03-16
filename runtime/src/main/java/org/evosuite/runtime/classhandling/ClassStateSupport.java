/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.runtime.classhandling;

import java.lang.instrument.UnmodifiableClassException;
import java.util.ArrayList;
import java.util.List;

import org.evosuite.runtime.LoopCounter;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.agent.InstrumentingAgent;
import org.evosuite.runtime.instrumentation.InstrumentedClass;
import org.evosuite.runtime.sandbox.Sandbox;
import org.evosuite.runtime.util.AtMostOnceLogger;
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

    /**
     * Load all the classes with given name with the provided input classloader.
     * Those classes are all supposed to be instrumented.
	 *
	 * <p>
	 *     This method will usually be called in a @BeforeClass initialization
	 * </p>
	 *
     * @param classLoader
     * @param classNames
     */
	public static boolean initializeClasses(ClassLoader classLoader, String... classNames){

		boolean problem = false;

		List<Class<?>> classes = loadClasses(classLoader, classNames);
		if(classes.size() != classNames.length){
			problem = true;
		}

		if(RuntimeSettings.isUsingAnyMocking()) {

			for (Class<?> clazz : classes) {

                if(clazz.isInterface()){
                    /*
                        FIXME: once we ll start to support Java 8, in which interfaces can have code,
                        we ll need to instrument them as well
                     */
                    continue;
                }

                if (!InstrumentedClass.class.isAssignableFrom(clazz)) {
                    String msg = "Class " + clazz.getName() + " was not instrumented by EvoSuite. " +
                            "This could happen if you are running JUnit tests in a way that is not handled by EvoSuite, in " +
                            "which some classes are loaded be reflection before the tests are run. Consult the EvoSuite documentation " +
                            "for possible workarounds for this issue.";
                    logger.error(msg);
					problem = true;
                    //throw new IllegalStateException(msg); // throwing an exception might be a bit too extreme
                }
            }
        }

		return problem;

		//retransformIfNeeded(classes); // cannot do it, as retransformation does not really work :(
	}

	/**
	 * Reset the static state of all the given classes.
	 *
	 * <p>
	 *     This method will be usually called after a test is executed, ie in a @After
	 * </p>
	 *
	 * @param classNames
	 */
	public static void resetClasses(String... classNames) {
		for (int i=0; i< classNames.length;i++) {
			String classNameToReset = classNames[i];
			ClassResetter.getInstance().reset(classNameToReset); 
		}
	}


	private static List<Class<?>> loadClasses(ClassLoader classLoader, String... classNames) {

		List<Class<?>> classes = new ArrayList<>();

		InstrumentingAgent.activate();
		boolean safe = Sandbox.isSafeToExecuteSUTCode();

		//assert !Sandbox.isSecurityManagerInitialized() || Sandbox.isOnAndExecutingSUTCode();

		for (int i=0; i< classNames.length;i++) {

			org.evosuite.runtime.Runtime.getInstance().resetRuntime();

			String classNameToLoad = classNames[i];

			Sandbox.goingToExecuteSUTCode();
			boolean wasLoopCheckOn = LoopCounter.getInstance().isActivated();

			try {
				if(!safe){
					Sandbox.goingToExecuteUnsafeCodeOnSameThread();
				}
				LoopCounter.getInstance().setActive(false);
				Class<?> aClass = Class.forName(classNameToLoad, true, classLoader);
				classes.add(aClass);

			} catch (Exception | Error ex) {
				AtMostOnceLogger.error(logger,"Could not initialize " + classNameToLoad + ": " + ex.getMessage());
			} finally {
				if(!safe){
					Sandbox.doneWithExecutingUnsafeCodeOnSameThread();
				}
				Sandbox.doneWithExecutingSUTCode();
				LoopCounter.getInstance().setActive(wasLoopCheckOn);
			}
		}
		InstrumentingAgent.deactivate();
		return classes;
	}



	// deprecated ---------------------------------

	/**
	 * If any of the loaded class was not instrumented yet, then re-instrument them.
	 * Note: re-instrumentation is more limited, as cannot change class signature
	 */
	@Deprecated
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
	@Deprecated
	public static void retransformIfNeeded(List<Class<?>> classes) {

		if(classes==null || classes.isEmpty()){
			return;
		}

		List<Class<?>> classToReInstrument = new ArrayList<>();

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
				classToReInstrument.add(cl);
			}
		}

		if(classToReInstrument.isEmpty()){
			return;
		}

		InstrumentingAgent.setRetransformingMode(true);
		try {
			if(!classToReInstrument.isEmpty()){
				InstrumentingAgent.getInstrumentation().retransformClasses(classToReInstrument.toArray(new Class<?>[0]));
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
}
