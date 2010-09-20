package de.unisb.cs.st.evosuite.testcase;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Inspector {

	Class<?> clazz;
	Method method;
	
	public Inspector(Class<?> clazz, Method m) {
		this.clazz= clazz;
		method = m;
	}
	
	public Object getValue(Object object) {
		try {
			Object ret = this.method.invoke(object);
			return ret;
			
		} catch (IllegalArgumentException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		} catch (InvocationTargetException e) {
			return null;
		}
	}
	
	public String getMethodCall() {
		return method.getName();
	}
	
	public String getClassName() {
		return clazz.getName();
	}
	
	public Class<?> getReturnType() {
		return method.getReturnType();
	}
	
}
