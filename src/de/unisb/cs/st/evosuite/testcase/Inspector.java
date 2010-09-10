package de.unisb.cs.st.evosuite.testcase;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Inspector {

	Method method;
	
	public Inspector(Method m) {
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
	
	public Class<?> getReturnType() {
		return method.getReturnType();
	}
	
}
