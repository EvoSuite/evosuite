package org.evosuite.utils;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericUtils {

	public static boolean isAssignable(Type type, TypeVariable<?> typeVariable) {
		boolean isAssignable = true;
		for (Type boundType : typeVariable.getBounds()) {
			// Have to resolve the type because a typevariable may have a reference to itself
			// in its bounds
			Type resolvedBoundType = GenericUtils.replaceTypeVariable(boundType,
			                                                          typeVariable, type);
			if (!GenericClass.isAssignable(resolvedBoundType, type)) {
				isAssignable = false;
				break;
			}
		}
		return isAssignable;
	}

	private static final Logger logger = LoggerFactory.getLogger(GenericUtils.class);

	public static Type replaceTypeVariables(Type targetType,
	        Map<TypeVariable<?>, Type> typeMap) {
		Type returnType = targetType;
		for (TypeVariable<?> var : typeMap.keySet()) {
			//logger.debug("Current variable: "+var+" of type "+typeMap.get(var)+" in "+returnType);
			returnType = replaceTypeVariable(returnType, var, typeMap.get(var));
		}

		return returnType;
	}

	public static Type replaceTypeVariablesWithWildcards(Type targetType) {
		if (targetType instanceof TypeVariable) {
			TypeVariable<?> typeVariable = (TypeVariable<?>) targetType;
			return new WildcardTypeImpl(typeVariable.getBounds(), new Type[] {});
		} else if (targetType instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) targetType;
			Type owner = null;
			if (parameterizedType.getOwnerType() != null)
				owner = replaceTypeVariablesWithWildcards(parameterizedType.getOwnerType());
			Type[] currentParameters = parameterizedType.getActualTypeArguments();
			Type[] parameters = new Type[currentParameters.length];
			for (int i = 0; i < parameters.length; i++) {
				parameters[i] = replaceTypeVariablesWithWildcards(currentParameters[i]);
			}
			return new ParameterizedTypeImpl((Class<?>) parameterizedType.getRawType(),
			        parameters, owner);
		}
		return targetType;
	}

	public static Type replaceTypeVariable(Type targetType, TypeVariable<?> variable,
	        Type variableType) {
		if (targetType instanceof Class<?>)
			return targetType;
		else if (targetType instanceof GenericArrayType) {
			GenericArrayType gType = (GenericArrayType) targetType;
			Type componentType = replaceTypeVariable(gType.getGenericComponentType(),
			                                         variable, variableType);
			return GenericArrayTypeImpl.createArrayType(componentType);

		} else if (targetType instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) targetType;
			Type ownerType = null;
			if (pType.getOwnerType() != null) {
				ownerType = replaceTypeVariable(pType.getOwnerType(), variable,
				                                variableType);
			}
			Type[] originalParameterTypes = pType.getActualTypeArguments();
			Type[] parameterTypes = new Type[originalParameterTypes.length];
			for (int i = 0; i < originalParameterTypes.length; i++) {
				parameterTypes[i] = replaceTypeVariable(originalParameterTypes[i],
				                                        variable, variableType);
			}

			/*
			if (variableType instanceof ParameterizedType) {
				ParameterizedType parameterizedVars = (ParameterizedType) variableType;
				Map<TypeVariable<?>, Type> subTypes = TypeUtils.getTypeArguments(parameterizedVars);
				for (Entry<TypeVariable<?>, Type> subTypeEntry : subTypes.entrySet()) {
					if (pType.getOwnerType() != null) {
						ownerType = replaceTypeVariable(pType.getOwnerType(),
						                                subTypeEntry.getKey(),
						                                subTypeEntry.getValue());
					}
					for (int i = 0; i < originalParameterTypes.length; i++) {
						parameterTypes[i] = replaceTypeVariable(originalParameterTypes[i],
						                                        subTypeEntry.getKey(),
						                                        subTypeEntry.getValue());
					}

				}
			}
			*/

			return new ParameterizedTypeImpl((Class<?>) pType.getRawType(),
			        parameterTypes, ownerType);

		} else if (targetType instanceof WildcardType) {
			WildcardType wType = (WildcardType) targetType;
			Type[] originalUpperBounds = wType.getUpperBounds();
			Type[] originalLowerBounds = wType.getLowerBounds();
			Type[] upperBounds = new Type[originalUpperBounds.length];
			Type[] lowerBounds = new Type[originalLowerBounds.length];

			for (int i = 0; i < originalUpperBounds.length; i++) {
				upperBounds[i] = replaceTypeVariable(originalUpperBounds[i], variable,
				                                     variableType);
			}
			for (int i = 0; i < originalLowerBounds.length; i++) {
				lowerBounds[i] = replaceTypeVariable(originalLowerBounds[i], variable,
				                                     variableType);
			}

			return new WildcardTypeImpl(upperBounds, lowerBounds);
		} else if (targetType instanceof TypeVariable<?>) {
			if (targetType.equals(variable)) {
				logger.debug("Do equal: " + variable + "/" + targetType);
				return variableType;
			} else {
				logger.debug("Do not equal: " + variable + "/" + targetType);
				logger.debug("Do not equal: " + variable.getGenericDeclaration() + "/"
				        + ((TypeVariable<?>) targetType).getGenericDeclaration());
				return targetType;
			}
		} else {
			logger.debug("Unknown type of class " + targetType.getClass() + ": "
			        + targetType);
			return targetType;
		}
	}

	public Map<TypeVariable<?>, Type> getMatchingTypeParameters(GenericArrayType p1,
	        GenericArrayType p2) {
		if (p1.getGenericComponentType() instanceof ParameterizedType
		        && p2.getGenericComponentType() instanceof ParameterizedType) {
			return getMatchingTypeParameters((ParameterizedType) p1.getGenericComponentType(),
			                                 (ParameterizedType) p2.getGenericComponentType());
		} else {
			Map<TypeVariable<?>, Type> map = new HashMap<TypeVariable<?>, Type>();
			return map;
		}
	}

	public static Map<TypeVariable<?>, Type> getMatchingTypeParameters(
	        ParameterizedType p1, ParameterizedType p2) {
		Map<TypeVariable<?>, Type> map = new HashMap<TypeVariable<?>, Type>();
		if (!p1.getRawType().equals(p2.getRawType()))
			return map;

		for (int i = 0; i < p1.getActualTypeArguments().length; i++) {
			Type t1 = p1.getActualTypeArguments()[i];
			Type t2 = p2.getActualTypeArguments()[i];
			logger.debug("First match: " + t1 + " - " + t2);
			if (t1 instanceof TypeVariable<?>) {
				map.put((TypeVariable<?>) t1, t2);
			}
			if (t2 instanceof TypeVariable<?>) {
				map.put((TypeVariable<?>) t2, t1);
			} else if (t2 instanceof ParameterizedType && t1 instanceof ParameterizedType) {
				map.putAll(getMatchingTypeParameters((ParameterizedType) t1,
				                                     (ParameterizedType) t2));
			}
		}

		if (p1.getOwnerType() != null && p1.getOwnerType() instanceof ParameterizedType
		        && p2.getOwnerType() instanceof ParameterizedType) {
			map.putAll(getMatchingTypeParameters((ParameterizedType) p1.getOwnerType(),
			                                     (ParameterizedType) p2.getOwnerType()));
		}

		return map;
	}
}
