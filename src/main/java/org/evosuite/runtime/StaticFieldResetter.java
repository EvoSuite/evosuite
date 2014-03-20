package org.evosuite.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.evosuite.TestGenerationContext;
import org.evosuite.agent.ToolsJarLocator;

/**
 * This class resets the static fields of a given class by invoking the <clinit> class initializer.
 * In order to re-invoke the <clinit> this is duplicated with the method name "__STATIC_RESET".
 * 
 * @author galeotti
 *
 */
public class StaticFieldResetter {

	/**
	 * The name of the instrumented duplication of the class initializer <clinit>
	 */
	public static final String STATIC_RESET = "__STATIC_RESET";

	private static final StaticFieldResetter instance = new StaticFieldResetter();

	private ClassLoader loader = TestGenerationContext.getInstance().getClassLoaderForSUT();
	
	private Map<ClassLoader, Map<String, Method>> resetMethodCache = new HashMap<ClassLoader, Map<String, Method>>();
	
	public void setClassLoader(ClassLoader loader) {
		this.loader = loader;
	}
	
	public static StaticFieldResetter getInstance() {
		return instance;
	}
	
	private void cacheResetMethod(String classNameWithDots) {
		if(!resetMethodCache.containsKey(loader)) {
			resetMethodCache.put(loader, new HashMap<String, Method>());
		}
		Map<String, Method> methodMap = resetMethodCache.get(loader);
		if(methodMap.containsKey(classNameWithDots))
			return;
		
		try {
			Class<?> clazz = loader.loadClass(classNameWithDots);
			Method m = clazz.getMethod(STATIC_RESET, (Class<?>[]) null);
			m.setAccessible(true);
			methodMap.put(classNameWithDots, m);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		
	}
	
	private Method getResetMethod(String classNameWithDots) {
		cacheResetMethod(classNameWithDots);
		return resetMethodCache.get(loader).get(classNameWithDots);
	}

	/**
	 * Invoke the duplicated version of class initializar <clinit> 
	 *  
	 * @param classNameWithDots the class for invoking the duplicated version of class initializer <clinit>
	 */
	public void resetStaticFields(String classNameWithDots) {
		Method m = getResetMethod(classNameWithDots);
		if(m == null)
			return; // TODO: Error handling
		try {
			m.invoke(null, (Object[]) null);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

}
