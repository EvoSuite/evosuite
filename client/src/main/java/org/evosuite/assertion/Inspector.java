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
package org.evosuite.assertion;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.evosuite.TestGenerationContext;
import org.evosuite.runtime.LoopCounter;
import org.evosuite.runtime.sandbox.Sandbox;
import org.evosuite.setup.TestClusterUtils;
import org.evosuite.utils.LoggingUtils;
import org.objectweb.asm.Type;

public class Inspector implements Serializable {

	private static final long serialVersionUID = -6865880297202184953L;

	private transient Class<?> clazz;

	private transient Method method;

	/**
	 * <p>
	 * Constructor for Inspector.
	 * </p>
	 * 
	 * @param clazz
	 *            a {@link java.lang.Class} object.
	 * @param m
	 *            a {@link java.lang.reflect.Method} object.
	 */
	public Inspector(Class<?> clazz, Method m) {
		this.clazz = clazz;
		method = m;
		method.setAccessible(true);
	}

	/**
	 * <p>
	 * getValue
	 * </p>
	 * 
	 * @param object
	 *            a {@link java.lang.Object} object.
	 * @return a {@link java.lang.Object} object.
	 * @throws java.lang.IllegalArgumentException
	 *             if any.
	 * @throws java.lang.IllegalAccessException
	 *             if any.
	 * @throws java.lang.reflect.InvocationTargetException
	 *             if any.
	 */
	public Object getValue(Object object) throws IllegalArgumentException,
	        IllegalAccessException, InvocationTargetException {

		boolean needsSandbox = !Sandbox.isOnAndExecutingSUTCode();
		boolean safe = Sandbox.isSafeToExecuteSUTCode();

		if(needsSandbox) {
			Sandbox.goingToExecuteSUTCode();
			TestGenerationContext.getInstance().goingToExecuteSUTCode();
			if(!safe)
				 Sandbox.goingToExecuteUnsafeCodeOnSameThread();
		}
		Object ret = null;

		try {
			ret = this.method.invoke(object);
		} finally {
			if(needsSandbox) {
				if(!safe)
					Sandbox.doneWithExecutingUnsafeCodeOnSameThread();
				Sandbox.doneWithExecutingSUTCode();
				TestGenerationContext.getInstance().doneWithExecutingSUTCode();
			}
		}

		return ret;
	}

	/**
	 * <p>
	 * Getter for the field <code>method</code>.
	 * </p>
	 * 
	 * @return a {@link java.lang.reflect.Method} object.
	 */
	public Method getMethod() {
		return method;
	}

	/**
	 * <p>
	 * getMethodCall
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getMethodCall() {
		return method.getName();
	}

	/**
	 * <p>
	 * getClassName
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getClassName() {
		return clazz.getName();
	}

	/**
	 * <p>
	 * getReturnType
	 * </p>
	 * 
	 * @return a {@link java.lang.Class} object.
	 */
	public Class<?> getReturnType() {
		return method.getReturnType();
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Inspector other = (Inspector) obj;
		if (clazz == null) {
			if (other.clazz != null)
				return false;
		} else if (!clazz.equals(other.clazz))
			return false;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		return true;
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
		// Write/save additional fields
		oos.writeObject(clazz.getName());
		oos.writeObject(method.getDeclaringClass().getName());
		oos.writeObject(method.getName());
		oos.writeObject(Type.getMethodDescriptor(method));
	}

	// assumes "static java.util.Date aDate;" declared
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
	        IOException {
		ois.defaultReadObject();

		// Read/initialize additional fields
		this.clazz = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass((String) ois.readObject());
		Class<?> methodClass = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass((String) ois.readObject());

		String methodName = (String) ois.readObject();
		String methodDesc = (String) ois.readObject();

		for (Method method : methodClass.getDeclaredMethods()) {
			if (method.getName().equals(methodName)) {
				if (Type.getMethodDescriptor(method).equals(methodDesc)) {
					this.method = method;
					return;
				}
			}
		}
	}
	
	public void changeClassLoader(ClassLoader loader) {

		try {
			Class<?> oldClass = method.getDeclaringClass();
			Class<?> newClass = loader.loadClass(oldClass.getName());
			for (Method newMethod : TestClusterUtils.getMethods(newClass)) {
				if (newMethod.getName().equals(this.method.getName())) {
					boolean equals = true;
					Class<?>[] oldParameters = this.method.getParameterTypes();
					Class<?>[] newParameters = newMethod.getParameterTypes();
					if (oldParameters.length != newParameters.length)
						continue;

					if(!newMethod.getDeclaringClass().getName().equals(method.getDeclaringClass().getName()))
						continue;
					
					if(!newMethod.getReturnType().getName().equals(method.getReturnType().getName()))
						continue;

					for (int i = 0; i < newParameters.length; i++) {
						if (!oldParameters[i].getName().equals(newParameters[i].getName())) {
							equals = false;
							break;
						}
					}
					if (equals) {
						this.method = newMethod;
						this.method.setAccessible(true);
						return;
					}
				}
			}
			LoggingUtils.getEvoLogger().info("Method not found - keeping old class loader ");
		} catch (ClassNotFoundException e) {
			LoggingUtils.getEvoLogger().info("Class not found - keeping old class loader ", e);
		} catch (SecurityException e) {
			LoggingUtils.getEvoLogger().info("Class not found - keeping old class loader ",e);
		}
	}
}
