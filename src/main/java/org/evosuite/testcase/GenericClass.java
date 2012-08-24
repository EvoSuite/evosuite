/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Gordon Fraser
 */
package org.evosuite.testcase;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ClassUtils;
import org.evosuite.setup.TestCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.gentyref.GenericTypeReflector;

public class GenericClass implements Serializable {

	private static final long serialVersionUID = -3307107227790458308L;

	private static final Logger logger = LoggerFactory.getLogger(GenericClass.class);

	/**
	 * <p>
	 * isAssignableTo
	 * </p>
	 * 
	 * @param lhsType
	 *            a {@link java.lang.reflect.Type} object.
	 * @return a boolean.
	 */
	public boolean isAssignableTo(Type lhsType) {
		return isAssignable(lhsType, type);
	}

	/**
	 * <p>
	 * isAssignableFrom
	 * </p>
	 * 
	 * @param rhsType
	 *            a {@link java.lang.reflect.Type} object.
	 * @return a boolean.
	 */
	public boolean isAssignableFrom(Type rhsType) {
		return isAssignable(type, rhsType);
	}

	/**
	 * <p>
	 * isAssignableTo
	 * </p>
	 * 
	 * @param lhsType
	 *            a {@link org.evosuite.testcase.GenericClass} object.
	 * @return a boolean.
	 */
	public boolean isAssignableTo(GenericClass lhsType) {
		return isAssignable(lhsType.type, type);
	}

	/**
	 * <p>
	 * isAssignableFrom
	 * </p>
	 * 
	 * @param rhsType
	 *            a {@link org.evosuite.testcase.GenericClass} object.
	 * @return a boolean.
	 */
	public boolean isAssignableFrom(GenericClass rhsType) {
		return isAssignable(type, rhsType.type);
	}

	/**
	 * Return true if variable is an enumeration
	 * 
	 * @return a boolean.
	 */
	public boolean isEnum() {
		return raw_class.isEnum();
	}

	/**
	 * Return true if variable is a primitive type
	 * 
	 * @return a boolean.
	 */
	public boolean isPrimitive() {
		return raw_class.isPrimitive();
	}

	/**
	 * <p>
	 * isString
	 * </p>
	 * 
	 * @return a boolean.
	 */
	public boolean isString() {
		return raw_class.equals(String.class);
	}

	/**
	 * Return true if variable is void
	 * 
	 * @return a boolean.
	 */
	public boolean isVoid() {
		return raw_class.equals(Void.class) || raw_class.equals(void.class);
	}

	/**
	 * Return true if variable is an array
	 * 
	 * @return a boolean.
	 */
	public boolean isArray() {
		return raw_class.isArray();
	}

	public boolean isObject() {
		return raw_class.equals(Object.class);
	}

	/**
	 * <p>
	 * getComponentType
	 * </p>
	 * 
	 * @return a {@link java.lang.reflect.Type} object.
	 */
	public Type getComponentType() {
		return raw_class.getComponentType();
	}

	/**
	 * <p>
	 * getComponentClass
	 * </p>
	 * 
	 * @return a {@link java.lang.reflect.Type} object.
	 */
	public Type getComponentClass() {
		return GenericTypeReflector.erase(raw_class.getComponentType());
	}

	/**
	 * Set of wrapper classes
	 */
	@SuppressWarnings("unchecked")
	private static final Set<Class<?>> WRAPPER_TYPES = new HashSet<Class<?>>(
	        Arrays.asList(Boolean.class, Character.class, Byte.class, Short.class,
	                      Integer.class, Long.class, Float.class, Double.class,
	                      Void.class));

	/**
	 * Return true if type of variable is a primitive wrapper
	 * 
	 * @return a boolean.
	 */
	public boolean isWrapperType() {
		return WRAPPER_TYPES.contains(raw_class);
	}

	public Class<?> getUnboxedType() {
		if (isWrapperType()) {
			if (raw_class.equals(Integer.class))
				return int.class;
			else if (raw_class.equals(Byte.class))
				return byte.class;
			else if (raw_class.equals(Short.class))
				return short.class;
			else if (raw_class.equals(Long.class))
				return long.class;
			else if (raw_class.equals(Float.class))
				return float.class;
			else if (raw_class.equals(Double.class))
				return double.class;
			else if (raw_class.equals(Character.class))
				return char.class;
			else if (raw_class.equals(Boolean.class))
				return boolean.class;
			else if (raw_class.equals(Void.class))
				return void.class;
			else
				throw new RuntimeException("Unknown boxed type: " + raw_class);
		}
		return raw_class;
	}

	public Class<?> getBoxedType() {
		if (isPrimitive()) {
			if (raw_class.equals(int.class))
				return Integer.class;
			else if (raw_class.equals(byte.class))
				return Byte.class;
			else if (raw_class.equals(short.class))
				return Short.class;
			else if (raw_class.equals(long.class))
				return Long.class;
			else if (raw_class.equals(float.class))
				return Float.class;
			else if (raw_class.equals(double.class))
				return Double.class;
			else if (raw_class.equals(char.class))
				return Character.class;
			else if (raw_class.equals(boolean.class))
				return Boolean.class;
			else if (raw_class.equals(void.class))
				return Void.class;
			else
				throw new RuntimeException("Unknown unboxed type: " + raw_class);
		}
		return raw_class;
	}

	/**
	 * <p>
	 * isSubclass
	 * </p>
	 * 
	 * @param superclass
	 *            a {@link java.lang.reflect.Type} object.
	 * @param subclass
	 *            a {@link java.lang.reflect.Type} object.
	 * @return a boolean.
	 */
	public static boolean isSubclass(Type superclass, Type subclass) {
		List<Class<?>> superclasses = ClassUtils.getAllSuperclasses((Class<?>) subclass);
		List<Class<?>> interfaces = ClassUtils.getAllInterfaces((Class<?>) subclass);
		if (superclasses.contains(superclass) || interfaces.contains(superclass)) {
			return true;
		}

		return false;
	}

	/**
	 * <p>
	 * isAssignable
	 * </p>
	 * 
	 * @param lhsType
	 *            a {@link java.lang.reflect.Type} object.
	 * @param rhsType
	 *            a {@link java.lang.reflect.Type} object.
	 * @return a boolean.
	 */
	public static boolean isAssignable(Type lhsType, Type rhsType) {
		if (lhsType.equals(rhsType)) {
			return true;
		}

		if (lhsType instanceof Class<?> && rhsType instanceof Class<?>) {
			//if(ClassUtils.isAssignable((Class<?>) rhsType, (Class<?>) lhsType)) {
			//	logger.info("Classes are assignable: "+lhsType+" / "+rhsType);
			//}
			// Only allow void to void assignments
			if (((Class<?>) rhsType).equals(void.class)
			        || ((Class<?>) lhsType).equals(void.class))
				return false;
			return ClassUtils.isAssignable((Class<?>) rhsType, (Class<?>) lhsType);
		}

		//	if(lhsType instanceof ParameterizedType && rhsType instanceof ParameterizedType) {
		//		return isAssignable((ParameterizedType) lhsType, (ParameterizedType) rhsType);
		//	}
		if (lhsType instanceof TypeVariable<?>) {
			if (((TypeVariable<?>) lhsType).getBounds().length == 0)
				return isAssignable(Object.class, rhsType);
			return isAssignable(((TypeVariable<?>) lhsType).getBounds()[0], rhsType);
		}
		if (rhsType instanceof TypeVariable<?>) {
			if (((TypeVariable<?>) rhsType).getBounds().length == 0)
				return isAssignable(lhsType, Object.class);
			return isAssignable(lhsType, ((TypeVariable<?>) rhsType).getBounds()[0]);
		}
		if (rhsType instanceof ParameterizedType) {
			return isAssignable(lhsType, ((ParameterizedType) rhsType).getRawType());
		}
		if (lhsType instanceof ParameterizedType) {
			return isAssignable(((ParameterizedType) lhsType).getRawType(), rhsType);
		}
		if (lhsType instanceof WildcardType) {
			return isAssignable((WildcardType) lhsType, rhsType);
		}
		//if(rhsType instanceof WildcardType) {
		//	return isAssignable(lhsType, (WildcardType) rhsType);
		//}
		if (lhsType instanceof GenericArrayType && rhsType instanceof GenericArrayType) {
			//logger.warn("Checking generic array 1 "+lhsType+"/"+rhsType);
			return isAssignable(((GenericArrayType) lhsType).getGenericComponentType(),
			                    ((GenericArrayType) rhsType).getGenericComponentType());
		}
		if (lhsType instanceof Class<?> && ((Class<?>) lhsType).isArray()
		        && rhsType instanceof GenericArrayType) {
			//logger.warn("Checking generic array 2 "+lhsType+"/"+rhsType);
			return isAssignable(((Class<?>) lhsType).getComponentType(),
			                    ((GenericArrayType) rhsType).getGenericComponentType());
		}
		if (rhsType instanceof Class<?> && ((Class<?>) rhsType).isArray()
		        && lhsType instanceof GenericArrayType) {
			//logger.warn("Checking generic array 3 "+lhsType+"/"+rhsType);
			return isAssignable(((GenericArrayType) lhsType).getGenericComponentType(),
			                    ((Class<?>) rhsType).getComponentType());
		}
		/*
		String message = "Not assignable: ";
		if (lhsType instanceof Class<?>)
			message += "Class ";
		else if (lhsType instanceof ParameterizedType)
			message += "ParameterizedType ";
		else if (lhsType instanceof WildcardType)
			message += "WildcardType ";
		else if (lhsType instanceof GenericArrayType)
			message += "GenericArrayType ";
		else if (lhsType instanceof TypeVariable<?>)
			message += "TypeVariable ";
		else
			message += "Unknown type ";
		message += lhsType;
		message += " / ";
		if (rhsType instanceof Class<?>)
			message += "Class ";
		else if (rhsType instanceof ParameterizedType)
			message += "ParameterizedType ";
		else if (rhsType instanceof WildcardType)
			message += "WildcardType ";
		else if (rhsType instanceof GenericArrayType)
			message += "GenericArrayType ";
		else if (rhsType instanceof TypeVariable<?>)
			message += "TypeVariable ";
		else
			message += "Unknown type ";
		message += rhsType;
		logger.warn(message);
		 */

		//Thread.dumpStack();
		return false;
	}

	private static boolean isAssignable(WildcardType lhsType, Type rhsType) {
		// TODO - what should go here?

		/*
		Type[] upperBounds = lhsType.getUpperBounds();
		Type[] lowerBounds = lhsType.getLowerBounds();
		for (int size = upperBounds.length, i = 0; i < size; ++i) {
			if (!isAssignable(upperBounds[i], rhsType)) {
				return false;
			}
		}
		for (int size = lowerBounds.length, i = 0; i < size; ++i) {
			if (!isAssignable(rhsType, lowerBounds[i])) {
				return false;
			}
		}
		*/
		return true;
	}

	transient Class<?> raw_class = null;
	transient Type type = null;

	/**
	 * Generate a generic class by from a type
	 * 
	 * @param type
	 *            a {@link java.lang.reflect.Type} object.
	 */
	public GenericClass(Type type) {
		this.type = type;
		this.raw_class = GenericTypeReflector.erase(type);
	}

	/**
	 * Generate a generic class by setting all generic parameters to the unbound
	 * wildcard ("?")
	 * 
	 * @param clazz
	 *            a {@link java.lang.Class} object.
	 */
	public GenericClass(Class<?> clazz) {
		this.type = GenericTypeReflector.addWildcardParameters(clazz);
		this.raw_class = clazz;
	}

	/**
	 * <p>
	 * getRawClass
	 * </p>
	 * 
	 * @return a {@link java.lang.Class} object.
	 */
	public Class<?> getRawClass() {
		return raw_class;
	}

	/**
	 * <p>
	 * Getter for the field <code>type</code>.
	 * </p>
	 * 
	 * @return a {@link java.lang.reflect.Type} object.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * <p>
	 * getTypeName
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getTypeName() {
		return GenericTypeReflector.getTypeName(type);
	}

	/**
	 * <p>
	 * getComponentName
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getComponentName() {
		return raw_class.getComponentType().getSimpleName();
	}

	/**
	 * <p>
	 * getClassName
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getClassName() {
		return raw_class.getName();
	}

	private static List<String> primitiveClasses = Arrays.asList("char", "int", "short",
	                                                             "long", "boolean",
	                                                             "float", "double",
	                                                             "byte");

	/**
	 * <p>
	 * getSimpleName
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getSimpleName() {
		// return raw_class.getSimpleName();
		String name = ClassUtils.getShortClassName(raw_class).replace(";", "[]");
		if (!isPrimitive() && primitiveClasses.contains(name))
			return raw_class.getSimpleName().replace(";", "[]");

		return name;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return type.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getTypeName().hashCode();
		// result = prime * result + ((raw_class == null) ? 0 : raw_class.hashCode());
		// result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GenericClass other = (GenericClass) obj;
		return getTypeName().equals(other.getTypeName());
		/*
		if (raw_class == null) {
			if (other.raw_class != null)
				return false;
		} else if (!raw_class.equals(other.raw_class))
			return false;
			*/
		/*
		if (type == null) {
		    if (other.type != null)
			    return false;
		} else if (!type.equals(other.type))
		    return false;
		    */
		// return true;
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.writeObject(raw_class.getName());
	}

	private static Class<?> getClass(String name) throws ClassNotFoundException {
		if (name.equals("void"))
			return void.class;
		else if (name.equals("int") || name.equals("I"))
			return int.class;
		else if (name.equals("short") || name.equals("S"))
			return short.class;
		else if (name.equals("long") || name.equals("J"))
			return long.class;
		else if (name.equals("float") || name.equals("F"))
			return float.class;
		else if (name.equals("double") || name.equals("D"))
			return double.class;
		else if (name.equals("boolean") || name.equals("Z"))
			return boolean.class;
		else if (name.equals("byte") || name.equals("B"))
			return byte.class;
		else if (name.equals("char") || name.equals("C"))
			return char.class;
		else if (name.startsWith("[")) {
			Class<?> componentType = getClass(name.substring(1, name.length()));
			Object array = Array.newInstance(componentType, 0);
			return array.getClass();
		} else if (name.startsWith("L")) {
			return getClass(name.substring(1));
		} else if (name.endsWith(".class")) {
			return getClass(name.replace(".class", ""));
		} else if (name.equals("java.lang.String;")) {
			// TODO: This is a workaround and the bug should be fixed
			return getClass("java.lang.String");
		} else if (name.equals("java.lang.Object;")) {
			// TODO: This is a workaround and the bug should be fixed
			return getClass("java.lang.Object");
		} else
			return TestCluster.classLoader.loadClass(name);
	}

	private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
	        IOException {
		String name = (String) ois.readObject();
		this.raw_class = getClass(name);
		// TODO: Currently, type information gets lost by serialization
		this.type = raw_class;

	}

	/**
	 * <p>
	 * changeClassLoader
	 * </p>
	 * 
	 * @param loader
	 *            a {@link java.lang.ClassLoader} object.
	 */
	public void changeClassLoader(ClassLoader loader) {
		try {
			raw_class = getClass(raw_class.getName());
			this.type = raw_class;
		} catch (ClassNotFoundException e) {
			logger.warn("Class not found - keeping old class loader ", e);
		} catch (SecurityException e) {
			logger.warn("Class not found - keeping old class loader ", e);
		}
	}

}
