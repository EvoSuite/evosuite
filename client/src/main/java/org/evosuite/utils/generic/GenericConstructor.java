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
/**
 *
 */
package org.evosuite.utils.generic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;

import org.evosuite.TestGenerationContext;
import org.evosuite.setup.TestClusterUtils;
import org.evosuite.setup.TestUsageChecker;
import org.evosuite.testcase.variable.VariableReference;

import com.googlecode.gentyref.GenericTypeReflector;
import org.evosuite.utils.LoggingUtils;

/**
 * @author Gordon Fraser
 *
 */
public class GenericConstructor extends GenericAccessibleObject<GenericConstructor> {

	private static final long serialVersionUID = 1361882947700615341L;

	private transient Constructor<?> constructor;

	public GenericConstructor(Constructor<?> constructor, Class<?> clazz) {
		super(new GenericClass(clazz));
		this.constructor = constructor;
	}

	public GenericConstructor(Constructor<?> constructor, GenericClass owner) {
		super(new GenericClass(owner));
		this.constructor = constructor;
	}

	public GenericConstructor(Constructor<?> constructor, Type type) {
		super(new GenericClass(type));
		this.constructor = constructor;
	}

	@Override
	public void changeClassLoader(ClassLoader loader) {
		super.changeClassLoader(loader);
		try {
			Class<?> oldClass = constructor.getDeclaringClass();
			Class<?> newClass = loader.loadClass(oldClass.getName());
			for (Constructor<?> newConstructor : TestClusterUtils.getConstructors(newClass)) {
				boolean equals = true;
				Class<?>[] oldParameters = this.constructor.getParameterTypes();
				Class<?>[] newParameters = newConstructor.getParameterTypes();
				if (oldParameters.length != newParameters.length)
					continue;

				for (int i = 0; i < newParameters.length; i++) {
					if (!oldParameters[i].getName().equals(newParameters[i].getName())) {
						equals = false;
						break;
					}
				}
				if (equals) {
					this.constructor = newConstructor;
					this.constructor.setAccessible(true);
					break;
				}
			}
		} catch (ClassNotFoundException e) {
			LoggingUtils.getEvoLogger().info("Class not found - keeping old class loader ",
			                                 e);
		} catch (SecurityException e) {
			LoggingUtils.getEvoLogger().info("Class not found - keeping old class loader ",
			                                 e);
		}
	}

	@Override
	public GenericConstructor copy() {
		GenericConstructor copy = new GenericConstructor(constructor, new GenericClass(
		        owner));
		copyTypeVariables(copy);
		return copy;
	}

	@Override
	public GenericConstructor copyWithNewOwner(GenericClass newOwner) {
		GenericConstructor copy = new GenericConstructor(constructor, newOwner);
		copyTypeVariables(copy);
		return copy;
	}

	@Override
	public GenericConstructor copyWithOwnerFromReturnType(GenericClass returnType) {
		GenericConstructor copy = new GenericConstructor(constructor, returnType);
		copyTypeVariables(copy);
		return copy;
	}

	public Constructor<?> getConstructor() {
		return constructor;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.utils.GenericAccessibleObject#getAccessibleObject()
	 */
	@Override
	public AccessibleObject getAccessibleObject() {
		return constructor;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.utils.GenericAccessibleObject#getDeclaringClass()
	 */
	@Override
	public Class<?> getDeclaringClass() {
		return constructor.getDeclaringClass();
	}

	/**
	 * Returns the exact parameter types of the given method in the given type.
	 * This may be different from <tt>m.getGenericParameterTypes()</tt> when the
	 * method was declared in a superclass, or <tt>type</tt> has a type
	 * parameter that is used in one of the parameters, or <tt>type</tt> is a
	 * raw type.
	 */
	public Type[] getExactParameterTypes(Constructor<?> m, Type type) {
		Type[] parameterTypes = m.getGenericParameterTypes();
		Type exactDeclaringType = GenericTypeReflector.getExactSuperType(GenericTypeReflector.capture(type),
		                                                                 m.getDeclaringClass());
		if (exactDeclaringType == null) { // capture(type) is not a subtype of m.getDeclaringClass()
			throw new IllegalArgumentException("The constructor " + m
			        + " is not a member of type " + type);
		}

		Type[] result = new Type[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			result[i] = mapTypeParameters(parameterTypes[i], exactDeclaringType);
		}
		return result;
	}

	public Type[] getGenericParameterTypes() {
		return constructor.getGenericParameterTypes();
	}

	@Override
	public Type getGeneratedType() {
		return getReturnType();
	}

	@Override
	public Class<?> getRawGeneratedType() {
		return constructor.getDeclaringClass();
	}

	@Override
	public Type getGenericGeneratedType() {
		return getRawGeneratedType();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.utils.GenericAccessibleObject#getName()
	 */
	@Override
	public String getName() {
		return constructor.getName();
	}

	public String getNameWithDescriptor() {
		return "<init>" + org.objectweb.asm.Type.getConstructorDescriptor(constructor);
	}

	public String getDescriptor() {
		return org.objectweb.asm.Type.getConstructorDescriptor(constructor);
	}

	@Override
	public int getNumParameters() {
		return constructor.getGenericParameterTypes().length;
	}

	public Type[] getParameterTypes() {
		Type[] types = getExactParameterTypes(constructor, owner.getType());
		Type[] rawTypes = constructor.getParameterTypes();

		// Generic member classes should have the enclosing instance as a parameter
		// but don't for some reason
		if (rawTypes.length != types.length && owner.isParameterizedType()) {
			Type[] actualTypes = new Type[rawTypes.length];
			actualTypes[0] = owner.getOwnerType().getType();
			int pos = 1;
			for (Type parameterType : types) {
				actualTypes[pos++] = parameterType;
			}
			return actualTypes;
		}
		return types;
	}

	public Type[] getRawParameterTypes() {
		return constructor.getParameterTypes();
	}

	public Type getReturnType() {
		return owner.getType();
	}

	@Override
	public TypeVariable<?>[] getTypeParameters() {
		return constructor.getTypeParameters();
	}

	@Override
	public boolean isAccessible() {
		return TestUsageChecker.canUse(constructor);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.utils.GenericAccessibleObject#isConstructor()
	 */
	@Override
	public boolean isConstructor() {
		return true;
	}

	@Override
	public boolean isStatic() {
		return Modifier.isStatic(constructor.getModifiers());
	}

	public boolean isOverloaded(List<VariableReference> parameters) {
		Class<?> declaringClass = constructor.getDeclaringClass();
		Class<?>[] parameterTypes = constructor.getParameterTypes();
		boolean isExact = true;
		Class<?>[] parameterClasses = new Class<?>[parameters.size()];
		int num = 0;
		for (VariableReference parameter : parameters) {
			parameterClasses[num] = parameter.getVariableClass();
			if (!parameterClasses[num].equals(parameterTypes[num])) {
				isExact = false;
			}
		}
		if (isExact)
			return false;
		try {
			Constructor<?> otherConstructor = declaringClass.getConstructor(parameterTypes);
			if (otherConstructor != null)
				return true;
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		}

		return false;
	}

	// assumes "static java.util.Date aDate;" declared
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
	        IOException {
		ois.defaultReadObject();

		// Read/initialize additional fields
		Class<?> constructorClass = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass((String) ois.readObject());
		String constructorDesc = (String) ois.readObject();
		for (Constructor<?> constructor : constructorClass.getDeclaredConstructors()) {
			if (org.objectweb.asm.Type.getConstructorDescriptor(constructor).equals(constructorDesc)) {
				this.constructor = constructor;
				return;
			}
		}

		throw new IllegalStateException("Unknown constructor in class " + constructorClass.getCanonicalName());
	}

	/* (non-Javadoc)
	 * @see org.evosuite.utils.GenericAccessibleObject#toString()
	 */
	@Override
	public String toString() {
		return constructor.toGenericString();
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
		// Write/save additional fields
		oos.writeObject(constructor.getDeclaringClass().getName());
		oos.writeObject(org.objectweb.asm.Type.getConstructorDescriptor(constructor));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((constructor == null) ? 0 : constructor.hashCode());
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
		GenericConstructor other = (GenericConstructor) obj;
		if (constructor == null) {
			if (other.constructor != null)
				return false;
		} else if (!constructor.equals(other.constructor))
			return false;
		return true;
	}


}
