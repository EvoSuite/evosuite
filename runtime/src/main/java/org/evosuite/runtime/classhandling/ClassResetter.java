package org.evosuite.runtime.classhandling;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.evosuite.runtime.*;
import org.evosuite.runtime.agent.InstrumentingAgent;
import org.evosuite.runtime.sandbox.Sandbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class resets the static fields of a given class by invoking the <clinit> class initializer.
 * In order to re-invoke the <clinit> this is duplicated with the method name "__STATIC_RESET".
 * 
 * @author galeotti
 *
 */
public class ClassResetter {

    private static Logger logger = LoggerFactory.getLogger(ClassResetter.class);

	/**
	 * The name of the instrumented duplication of the class initializer <clinit>
	 */
	public static final String STATIC_RESET = "__STATIC_RESET";

	/**
	 * Singleton instance of this class
	 */
	private static final ClassResetter instance = new ClassResetter();

	private ClassLoader loader;
	
	private final Map<ClassLoader, Map<String, Method>> resetMethodCache;

	/**
	 * Keep track of all classes for which we have already issued a warning
	 * if problems.
	 */
	private final Set<String> alreadyLoggedErrors;

	private ClassResetter(){
		resetMethodCache = new HashMap<>();
		alreadyLoggedErrors = new HashSet<>();
	}

	/**
	 * Return singleton instance
	 * @return
	 */
	public static ClassResetter getInstance() {
		return instance;
	}

	public void setClassLoader(ClassLoader loader) throws IllegalArgumentException{
		if(loader==null){
			throw new IllegalArgumentException("Null class loader");
		}
		this.loader = loader;
	}


	/**
	 * Only log once for a class
	 * @param className
	 * @param msg
	 */
	public synchronized void logWarn(String className, String msg){
		if(alreadyLoggedErrors.contains(className)){
			return; // do not log a second time
		}
		alreadyLoggedErrors.add(className);
		logger.warn(msg);
	}

	private void cacheResetMethod(String classNameWithDots) {

		if (!resetMethodCache.containsKey(loader)) {
            resetMethodCache.put(loader, new HashMap<String, Method>());
        }

		Map<String, Method> methodMap = resetMethodCache.get(loader);
        if (methodMap.containsKey(classNameWithDots)) {
			return;
		}

        try {
            Class<?> clazz = loader.loadClass(classNameWithDots);

			if(clazz.isInterface() || clazz.isAnonymousClass()) {
				return;
			}
            
            Method m = clazz.getDeclaredMethod(STATIC_RESET, (Class<?>[]) null);
            m.setAccessible(true);
            methodMap.put(classNameWithDots, m);

        } catch (NoSuchMethodException e) {
			//this can happen if class was not instrumented with a static reset
			logger.debug("__STATIC_RESET() method does not exists in class " + classNameWithDots);
		} catch (Exception | Error e) {
			logWarn(classNameWithDots, e.getClass() + " thrown while loading method  __STATIC_RESET() for class " + classNameWithDots);
		}
	}
	
	public Method getResetMethod(String classNameWithDots) {
		cacheResetMethod(classNameWithDots);
		return resetMethodCache.get(loader).get(classNameWithDots);
	}

	/**
	 * Invoke the duplicated version of class initializar <clinit> 
	 *  
	 * @param classNameWithDots the class for invoking the duplicated version of class initializer <clinit>
	 */
	public void reset(String classNameWithDots) throws IllegalArgumentException, IllegalStateException{
		if(classNameWithDots==null || classNameWithDots.isEmpty()){
			throw new IllegalArgumentException("Empty class name in input");
		}
		
		if(loader == null){					
			throw new IllegalStateException("No specified loader");
		}
		
		Method m = getResetMethod(classNameWithDots);
		if(m == null) {
            return;
        }

		boolean safe = Sandbox.isSafeToExecuteSUTCode();

		assert !Sandbox.isSecurityManagerInitialized() || Sandbox.isOnAndExecutingSUTCode();

		InstrumentingAgent.activate();
		org.evosuite.runtime.Runtime.getInstance().resetRuntime();

		try {
			if(!safe){
				Sandbox.goingToExecuteUnsafeCodeOnSameThread();
			}
			m.invoke(null, (Object[]) null);
		} catch (IllegalAccessException | IllegalArgumentException e) {
            logger.error(""+e,e);
        } catch (NoClassDefFoundError e){
            logger.error(e.toString()); //no point in getting stack trace here, as it gives no info
        } catch(InvocationTargetException e){

			Throwable cause = e.getCause();
			if(cause instanceof TooManyResourcesException){
				logWarn(classNameWithDots, e.toString() + ", caused by: "+cause.toString());
			} else {
				StringWriter errors = new StringWriter();
				cause.printStackTrace(new PrintWriter(errors));
				logWarn(classNameWithDots, e.toString() + ", caused by: "+cause.toString()+"\n"+errors.toString());
				// we are only interested in the stack trace of the cause
			}
        } finally {
			if(!safe){
				Sandbox.doneWithExecutingUnsafeCodeOnSameThread();
			}
		}

		InstrumentingAgent.deactivate();
	}

}
