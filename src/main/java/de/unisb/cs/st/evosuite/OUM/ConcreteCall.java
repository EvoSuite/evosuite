/**
 * 
 */
package de.unisb.cs.st.evosuite.OUM;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Gordon Fraser
 *
 */
public class ConcreteCall {
	
	private String className;
	
	private AccessibleObject call;
	
	private Class<?> clazz = null;
	
	private static Map<String, Class<?>> classMap = new HashMap<String, Class<?>>();
	
	public ConcreteCall(String className, AccessibleObject call) {
		this.className = className;
		this.call = call;
		if(!classMap.containsKey(className)) {
			try {
				this.clazz = Class.forName(className);
				classMap.put(className, clazz);
			} catch(ClassNotFoundException e) {
				
			}
		}
		clazz = classMap.get(className);
	}
	
	public boolean isPrivate() {
		if(call instanceof Method)
			return Modifier.isPrivate(((Method)call).getModifiers());
		else if(call instanceof Field)
			return Modifier.isPrivate(((Field)call).getModifiers());
		else if(call instanceof Constructor<?>)
			return Modifier.isPrivate(((Constructor<?>)call).getModifiers());
		else
			return false;
	}
	
	public Class<?> getCallClass() {
		return clazz;
	}
	
	public boolean isValid() {
		return clazz != null;
	}
	
	public boolean isConstructor() {
		return call instanceof Constructor<?>;
	}
	
	public boolean isMethod() {
		return call instanceof Method;
	}
	
	public boolean isField() {
		return call instanceof Field;
	}
	
	public Method getMethod() {
		return (Method)call;
	}
	
	public Constructor<?> getConstructor() {
		return (Constructor<?>)call;
	}
	
	public Field getField() {
		return (Field)call;
	}
	
	public AccessibleObject getCall() {
		return call;
	}
	
	public String getClassName() {
		return className;
	}

	public String toString() {
		return className+"."+getName();
	}
	
	public String getName() {
		if(call instanceof java.lang.reflect.Method) {
			java.lang.reflect.Method method = (java.lang.reflect.Method)call;
			return method.getName() + org.objectweb.asm.Type.getMethodDescriptor(method);
		} else if(call instanceof java.lang.reflect.Constructor<?>) {
			java.lang.reflect.Constructor<?> constructor = (java.lang.reflect.Constructor<?>)call;
			return "<init>" + org.objectweb.asm.Type.getConstructorDescriptor(constructor);
		} else if(call instanceof java.lang.reflect.Field) {
			java.lang.reflect.Field field = (Field)call;
			return field.getName();
		}
		else return "";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((call == null) ? 0 : call.hashCode());
		result = prime * result
				+ ((className == null) ? 0 : className.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConcreteCall other = (ConcreteCall) obj;
		if (call == null) {
			if (other.call != null)
				return false;
		} else if (!call.equals(other.call))
			return false;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		return true;
	}
	
	
}
