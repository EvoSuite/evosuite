package org.evosuite.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
	private static final String STATIC_RESET = "__STATIC_RESET";

	private static final StaticFieldResetter instance = new StaticFieldResetter();

	public static StaticFieldResetter getInstance() {
		return instance;
	}

	/**
	 * Invoke the duplicated version of class initializar <clinit> 
	 *  
	 * @param classNameWithDots the class for invoking the duplicated version of class initializer <clinit>
	 */
	public void resetStaticFields(String classNameWithDots) {
		try {
			Class<?> clazz = Class.forName(classNameWithDots);
			Method m = clazz.getMethod(STATIC_RESET, (Class<?>[]) null);
			m.invoke(null, (Object[]) null);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}
