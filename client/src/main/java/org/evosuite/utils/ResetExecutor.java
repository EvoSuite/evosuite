package org.evosuite.utils;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.evosuite.TestGenerationContext;
import org.evosuite.TimeController;
import org.evosuite.coverage.mutation.MutationObserver;
import org.evosuite.runtime.Runtime;
import org.evosuite.runtime.reset.ClassResetter;
import org.evosuite.runtime.reset.ResetManager;
import org.evosuite.runtime.sandbox.Sandbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResetExecutor {

	private final static Logger logger = LoggerFactory.getLogger(ResetExecutor.class);

	/**
	 * Keep track of all classes for which we have already issued a warning
	 * if problems.
	 */
	private final Set<String> alreadyLoggedErrors;
	
	private ResetExecutor() {
		alreadyLoggedErrors = new HashSet<>();
	}
	
	private static ResetExecutor instance;
	
	public synchronized static ResetExecutor getInstance() {
		if (instance == null) {
			instance = new ResetExecutor();
		}
		return instance;
	}

	public void resetAllClasses() {
		List<String> classesToReset = ResetManager.getInstance().getClassResetOrder();
		resetClasses(classesToReset);
	}

	public void resetClasses(List<String> classesToReset) {
		//try to reset each collected class
		for (String className : classesToReset) {
			//this can be expensive
			if(! TimeController.getInstance().isThereStillTimeInThisPhase()){
				logger.warn("Stopped resetting of classes due to phase timeout");
				break;
			}
			resetClass(className);
		}
	}

	private final HashSet<String> confirmedResettableClasses = new HashSet<String>();

	private Method getResetMethod(String className) {
		try {
			ClassLoader classLoader = TestGenerationContext.getInstance().getClassLoaderForSUT();
			Class<?> clazz = Class.forName(className, true, classLoader);
			Method m = clazz.getDeclaredMethod(ClassResetter.STATIC_RESET, (Class<?>[]) null);
			m.setAccessible(true);
			return m;
		
		} catch (NoSuchMethodException e) {
			//this can happen if class was not instrumented with a static reset
			logger.debug("__STATIC_RESET() method does not exists in class " + className);
		} catch (Exception | Error e) {
			logWarn(className,e.getClass()+" thrown while loading method  __STATIC_RESET() for class " + className);
		} 
		return null;
	}

	/**
	 * Only log once for a class
	 * @param className
	 * @param msg
	 */
	private synchronized void logWarn(String className, String msg){
		if(alreadyLoggedErrors.contains(className)){
			return; // do not log a second time
		}
		alreadyLoggedErrors.add(className);
		logger.warn(msg);
	}
	
	private void resetClass(String className) {
		//className.__STATIC_RESET() exists
		logger.debug("Resetting class "+className);
		
		int mutationActive = MutationObserver.activeMutation;
		MutationObserver.deactivateMutation();
		confirmedResettableClasses.add(className);
		//execute __STATIC_RESET()
		Sandbox.goingToExecuteSUTCode();
        TestGenerationContext.getInstance().goingToExecuteSUTCode();

		Runtime.getInstance().resetRuntime(); //it is important to initialize the VFS
		try {			
			Method resetMethod = getResetMethod(className);
			if (resetMethod!=null) {
				resetMethod.invoke(null, (Object[]) null);
			}
		} catch (Exception  e) {
			logWarn(className,e.getClass() + " thrown during execution of method  __STATIC_RESET() for class " + className+", "+e.getCause());
		}  finally {
			Sandbox.doneWithExecutingSUTCode();
            TestGenerationContext.getInstance().doneWithExecuteingSUTCode();
			MutationObserver.activateMutation(mutationActive);
		}
	}

	public void reloadClasses() {
		for (String className : ResetManager.getInstance().getClassResetOrder()) {
			Runtime.getInstance().resetRuntime(); //it is important to initialize the VFS
			try {
				ClassLoader classLoader = TestGenerationContext.getInstance().getClassLoaderForSUT();
				Class.forName(className, true, classLoader);
			} catch (ClassNotFoundException e) {
				logWarn(className,"Class " + className + " could not be found during setting up of assertion generation ");;
			} catch (ExceptionInInitializerError ex) {
				logWarn(className,"Class " + className + " could not be initialized during setting up of assertion generation ");;
			} catch (LinkageError ex) {
				logWarn(className,"Class " + className + "  initialization led to a Linkage error ");;
			}
		}
	
	}

}
