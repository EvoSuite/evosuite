/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.runtime.mock;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.objectweb.asm.Type;

/**
 * 
 * @author gordon
 *
 */
public class InvokeSpecialMock {

	/*
	 * There is the possibility that invokespecial refers to a superclass that itself
	 * is mocked. Therefore, we dynamically determine the first superclass that defines
	 * the target method and invoke it.
	 * 
	 * The difficulty is that reflection does dynamic binding, which means it can only
	 * do invokevirtual, not invokespecial. To overcome this, we use this ugly hack.
	 */
	public static Object invokeSpecial(Object receiver, Object[] parameters, String methodName, String descriptor) throws Throwable {
		
		// Determine the first superclass that defines the target method 
		Class<?> superClass = receiver.getClass().getSuperclass();
		Method targetMethod = null;
		while(targetMethod == null && superClass != null) {
			for(Method method : superClass.getDeclaredMethods()) {
				if(method.getName().equals(methodName) && 
						descriptor.equals(Type.getMethodDescriptor(method))) {
					targetMethod = method;
					break;
				}
			}
			superClass = superClass.getSuperclass();
		}
		if(targetMethod == null)
			throw new IllegalArgumentException("No such method: "+methodName);

		// Now create a method handle through reflection, because otherwise
		// we would not have permission to invoke methods on the target class
		Constructor<MethodHandles.Lookup> methodHandlesLookupConstructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class);
		methodHandlesLookupConstructor.setAccessible(true);
		MethodHandles.Lookup lookup = methodHandlesLookupConstructor.newInstance(targetMethod.getDeclaringClass());
		MethodHandle methodHandle = lookup.findSpecial(targetMethod.getDeclaringClass(), 
				methodName,
				MethodType.methodType(targetMethod.getReturnType(), targetMethod.getParameterTypes()),
				targetMethod.getDeclaringClass());
			
		//methodHandle.bindTo(receiver);
		// For some reason bindTo doesn't work as expected
		// and we therefore need to put the receiver into the
		// array of parameters.
		Object[] parameterObjects = new Object[parameters.length + 1];
		parameterObjects[0] = receiver;
		for(int i = 0; i < parameters.length; i++)
			parameterObjects[i+1] = parameters[i];
		return methodHandle.invokeWithArguments(parameterObjects);
	}	
}
