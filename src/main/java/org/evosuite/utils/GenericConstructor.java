/**
 * 
 */
package org.evosuite.utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.evosuite.TestGenerationContext;
import org.evosuite.setup.TestClusterGenerator;

import com.googlecode.gentyref.GenericTypeReflector;

/**
 * @author Gordon Fraser
 * 
 */
public class GenericConstructor extends GenericAccessibleObject {

	private transient Constructor<?> constructor;

	public GenericConstructor(Constructor<?> constructor, GenericClass owner) {
		super(owner);
		this.constructor = constructor;
	}

	public GenericConstructor(Constructor<?> constructor, Class<?> clazz) {
		super(new GenericClass(clazz));
		this.constructor = constructor;
	}

	public GenericConstructor(Constructor<?> constructor, Type type) {
		super(new GenericClass(type));
		this.constructor = constructor;
	}

	public Constructor<?> getConstructor() {
		return constructor;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.utils.GenericAccessibleObject#getDeclaringClass()
	 */
	@Override
	public Class<?> getDeclaringClass() {
		return constructor.getDeclaringClass();
	}

	public Type[] getParameterTypes() {
		return getExactParameterTypes(constructor, owner.getType());
	}

	public Type getReturnType() {
		return owner.getType();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.utils.GenericAccessibleObject#isConstructor()
	 */
	@Override
	public boolean isConstructor() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.utils.GenericAccessibleObject#getName()
	 */
	@Override
	public String getName() {
		return constructor.getName();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.utils.GenericAccessibleObject#toString()
	 */
	@Override
	public String toString() {
		return constructor.toGenericString();
	}

	/**
	 * Maps type parameters in a type to their values.
	 * 
	 * @param toMapType
	 *            Type possibly containing type arguments
	 * @param typeAndParams
	 *            must be either ParameterizedType, or (in case there are no
	 *            type arguments, or it's a raw type) Class
	 * @return toMapType, but with type parameters from typeAndParams replaced.
	 */
	private static Type mapTypeParameters(Type toMapType, Type typeAndParams) {
		if (isMissingTypeParameters(typeAndParams)) {
			return GenericTypeReflector.erase(toMapType);
		} else {
			VarMap varMap = new VarMap();
			Type handlingTypeAndParams = typeAndParams;
			while (handlingTypeAndParams instanceof ParameterizedType) {
				ParameterizedType pType = (ParameterizedType) handlingTypeAndParams;
				Class<?> clazz = (Class<?>) pType.getRawType(); // getRawType should always be Class
				varMap.addAll(clazz.getTypeParameters(), pType.getActualTypeArguments());
				handlingTypeAndParams = pType.getOwnerType();
			}
			return varMap.map(toMapType);
		}
	}

	/**
	 * Checks if the given type is a class that is supposed to have type
	 * parameters, but doesn't. In other words, if it's a really raw type.
	 */
	private static boolean isMissingTypeParameters(Type type) {
		if (type instanceof Class) {
			for (Class<?> clazz = (Class<?>) type; clazz != null; clazz = clazz.getEnclosingClass()) {
				if (clazz.getTypeParameters().length != 0)
					return true;
			}
			return false;
		} else if (type instanceof ParameterizedType) {
			return false;
		} else {
			throw new AssertionError("Unexpected type " + type.getClass());
		}
	}

	/**
	 * Returns the exact parameter types of the given method in the given type.
	 * This may be different from <tt>m.getGenericParameterTypes()</tt> when the
	 * method was declared in a superclass, or <tt>type</tt> has a type
	 * parameter that is used in one of the parameters, or <tt>type</tt> is a
	 * raw type.
	 */
	public static Type[] getExactParameterTypes(Constructor<?> m, Type exactDeclaringType) {
		Type[] parameterTypes = m.getGenericParameterTypes();
//		Type exactDeclaringType = GenericTypeReflector.getExactSuperType(GenericTypeReflector.capture(type),
//		                                                                 m.getDeclaringClass());
//		if (exactDeclaringType == null) { // capture(type) is not a subtype of m.getDeclaringClass()
//			throw new IllegalArgumentException("The constructor " + m
//			        + " is not a member of type " + type);
//		}

		Type[] result = new Type[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			result[i] = mapTypeParameters(parameterTypes[i], exactDeclaringType);
		}
		return result;
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
		// Write/save additional fields
		oos.writeObject(constructor.getDeclaringClass().getName());
		oos.writeObject(org.objectweb.asm.Type.getConstructorDescriptor(constructor));
	}

	// assumes "static java.util.Date aDate;" declared
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
	        IOException {
		ois.defaultReadObject();

		// Read/initialize additional fields
		Class<?> constructorClass = TestGenerationContext.getClassLoader().loadClass((String) ois.readObject());
		String constructorDesc = (String) ois.readObject();
		for (Constructor<?> constructor : constructorClass.getDeclaredConstructors()) {
			if (org.objectweb.asm.Type.getConstructorDescriptor(constructor).equals(constructorDesc)) {
				this.constructor = constructor;
				return;
			}
		}
	}

	@Override
	public void changeClassLoader(ClassLoader loader) {
		super.changeClassLoader(loader);
		try {
			Class<?> oldClass = constructor.getDeclaringClass();
			Class<?> newClass = loader.loadClass(oldClass.getName());
			for (Constructor<?> newConstructor : TestClusterGenerator.getConstructors(newClass)) {
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

}
