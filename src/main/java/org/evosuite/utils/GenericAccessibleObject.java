/**
 * 
 */
package org.evosuite.utils;

import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.gentyref.GenericTypeReflector;

/**
 * @author Gordon Fraser
 * 
 */
public abstract class GenericAccessibleObject<T extends GenericAccessibleObject<?>>
        implements Serializable {

	private static final long serialVersionUID = 7069749492563662621L;

	protected static final Logger logger = LoggerFactory.getLogger(GenericAccessibleObject.class);

	protected static Type getTypeFromExactReturnType(Type returnType, Type type) {
		if (returnType instanceof ParameterizedType && type instanceof ParameterizedType)
			return getTypeFromExactReturnType((ParameterizedType) returnType,
			                                  (ParameterizedType) type);
		else if (returnType instanceof GenericArrayType
		        && type instanceof GenericArrayType)
			return getTypeFromExactReturnType((GenericArrayType) returnType,
			                                  (GenericArrayType) type);
		else if (returnType instanceof ParameterizedType
		        && type instanceof GenericArrayType)
			return getTypeFromExactReturnType((ParameterizedType) returnType,
			                                  (GenericArrayType) type);
		else if (returnType instanceof GenericArrayType
		        && type instanceof ParameterizedType)
			return getTypeFromExactReturnType((GenericArrayType) returnType,
			                                  (ParameterizedType) type);
		else
			throw new RuntimeException("Incompatible types: " + returnType.getClass()
			        + " and " + type.getClass() + ": " + returnType + " and " + type);
	}

	protected static Type getTypeFromExactReturnType(GenericArrayType returnType,
	        GenericArrayType type) {
		return GenericArrayTypeImpl.createArrayType(getTypeFromExactReturnType(returnType.getGenericComponentType(),
		                                                                       type.getGenericComponentType()));
	}

	protected static Type getTypeFromExactReturnType(ParameterizedType returnType,
	        GenericArrayType type) {
		return GenericArrayTypeImpl.createArrayType(getTypeFromExactReturnType(returnType,
		                                                                       type.getGenericComponentType()));
	}

	protected static Type getTypeFromExactReturnType(GenericArrayType returnType,
	        ParameterizedType type) {
		return GenericArrayTypeImpl.createArrayType(getTypeFromExactReturnType(returnType.getGenericComponentType(),
		                                                                       type));
	}

	/**
	 * Returns the exact return type of the given method in the given type. This
	 * may be different from <tt>m.getGenericReturnType()</tt> when the method
	 * was declared in a superclass, or <tt>type</tt> has a type parameter that
	 * is used in the return type, or <tt>type</tt> is a raw type.
	 */
	protected static Type getTypeFromExactReturnType(ParameterizedType returnType,
	        ParameterizedType type) {
		Map<TypeVariable<?>, Type> typeMap = TypeUtils.getTypeArguments(returnType);
		Type[] actualParameters = new Type[type.getActualTypeArguments().length];
		int num = 0;
		for (TypeVariable<?> parameterType : ((Class<?>) type.getRawType()).getTypeParameters()) {
			//for(Type parameterType : type.getActualTypeArguments()) {
			//	if(parameterType instanceof TypeVariable<?>) {
			boolean replaced = false;
			for (TypeVariable<?> var : typeMap.keySet()) {
				// D'oh! Why the heck do we need this?? 
				if (var.getName().equals(parameterType.getName())) {
					//if(typeMap.containsKey(parameterType)) {
					actualParameters[num] = typeMap.get(var);
					replaced = true;
					break;
					//} else {
				}
			}
			if (!replaced) {
				actualParameters[num] = parameterType;
			}
			//}
			//    	} else {
			//    		LoggingUtils.getEvoLogger().info("Not a type variable "+parameterType);
			//    		actualParameters[num] = parameterType;
			//    		}
			num++;
		}

		return new ParameterizedTypeImpl((Class<?>) type.getRawType(), actualParameters,
		        null);
	}

	/**
	 * Checks if the given type is a class that is supposed to have type
	 * parameters, but doesn't. In other words, if it's a really raw type.
	 */
	protected static boolean isMissingTypeParameters(Type type) {
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

	protected GenericClass owner;

	protected List<GenericClass> typeVariables = new ArrayList<GenericClass>();

	public GenericAccessibleObject(GenericClass owner) {
		this.owner = owner;
	}

	public void changeClassLoader(ClassLoader loader) {
		owner.changeClassLoader(loader);
		for (GenericClass typeVariable : typeVariables) {
			typeVariable.changeClassLoader(loader);
		}
	}

	public abstract T copy();

	public abstract T copyWithNewOwner(GenericClass newOwner);

	public abstract T copyWithOwnerFromReturnType(GenericClass returnType);

	public abstract Class<?> getDeclaringClass();

	public abstract Type getGeneratedType();

	public abstract Class<?> getRawGeneratedType();

	public abstract Type getGenericGeneratedType();

	public abstract String getName();

	public abstract AccessibleObject getAccessibleObject();

	public int getNumParameters() {
		return 0;
	}

	public GenericClass getOwnerClass() {
		return owner;
	}

	public Type getOwnerType() {
		return owner.getType();
	}

	public TypeVariable<?>[] getTypeParameters() {
		return new TypeVariable<?>[] {};
	}

	protected Map<TypeVariable<?>, GenericClass> getTypeVariableMap() {
		Map<TypeVariable<?>, GenericClass> typeMap = new HashMap<TypeVariable<?>, GenericClass>();
		int pos = 0;
		for (TypeVariable<?> variable : getTypeParameters()) {
			if (typeVariables.size() <= pos)
				break;
			typeMap.put(variable, typeVariables.get(pos));
			pos++;
		}
		return typeMap;
	}

	public boolean hasTypeParameters() {
		return getTypeParameters().length != 0;
	}

	public boolean isConstructor() {
		return false;
	}

	public boolean isField() {
		return false;
	}

	public boolean isMethod() {
		return false;
	}

	public boolean isStatic() {
		return false;
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
	protected Type mapTypeParameters(Type toMapType, Type typeAndParams) {
		if (isMissingTypeParameters(typeAndParams)) {
			logger.debug("Is missing type parameters, so erasing types");
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
			varMap.addAll(getTypeVariableMap());
			return varMap.map(toMapType);
		}
	}

	public void setTypeParameters(List<GenericClass> parameterTypes) {
		typeVariables.clear();
		typeVariables.addAll(parameterTypes);
	}

	@Override
	public abstract String toString();
}
