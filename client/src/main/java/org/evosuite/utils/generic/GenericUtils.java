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
package org.evosuite.utils.generic;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.evosuite.utils.ParameterizedTypeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class GenericUtils {

    /**
     * Constant to represent @NotNull annotation
     */
    public final static String NONNULL = "Nonnull";

    public static boolean isAssignable(Type type, TypeVariable<?> typeVariable) {
        boolean isAssignable = true;
        for (Type boundType : typeVariable.getBounds()) {
            // Have to resolve the type because a typevariable may have a reference to itself
            // in its bounds
            Type resolvedBoundType = GenericUtils.replaceTypeVariable(boundType,
                    typeVariable, type);
            if (!GenericClassUtils.isAssignable(resolvedBoundType, type)) {
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
        for (Entry<TypeVariable<?>, Type> entry : typeMap.entrySet()) {
            returnType = replaceTypeVariable(returnType, entry.getKey(), entry.getValue());
        }
//		for (TypeVariable<?> var : typeMap.keySet()) {
//			//logger.debug("Current variable: "+var+" of type "+typeMap.get(var)+" in "+returnType);
//			returnType = replaceTypeVariable(returnType, var, typeMap.get(var));
//		}

        return returnType;
    }

    public static Type replaceTypeVariablesWithWildcards(Type targetType) {
        if (targetType instanceof TypeVariable) {
            TypeVariable<?> typeVariable = (TypeVariable<?>) targetType;
            return new WildcardTypeImpl(typeVariable.getBounds(), new Type[]{});
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
                //logger.debug("Do equal: " + variable + "/" + targetType);
                return variableType;
            } else {
                //logger.debug("Do not equal: " + variable + "/" + targetType);
                //logger.debug("Do not equal: " + variable.getGenericDeclaration() + "/"
                //        + ((TypeVariable<?>) targetType).getGenericDeclaration());
                return targetType;
            }
        } else {
            //logger.debug("Unknown type of class " + targetType.getClass() + ": "
            //        + targetType);
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
            Map<TypeVariable<?>, Type> map = new HashMap<>();
            return map;
        }
    }

    /**
     * TODO: Try to match p2 superclasses?
     *
     * @param p1 Desired TypeVariable assignment
     * @param p2 Generic type with the TypeVariables that need assignment
     * @return
     */
    public static Map<TypeVariable<?>, Type> getMatchingTypeParameters(
            ParameterizedType p1, ParameterizedType p2) {
        logger.debug("Matching generic types between " + p1 + " and " + p2);
        Map<TypeVariable<?>, Type> map = new HashMap<>();
        if (!p1.getRawType().equals(p2.getRawType())) {
            logger.debug("Raw types do not match!");

            GenericClass<?> ownerClass = GenericClassFactory.get(p2);

            if (GenericClassUtils.isSubclass(p1.getRawType(), p2.getRawType())) {
                logger.debug(p1 + " is a super type of " + p2);
                Map<TypeVariable<?>, Type> commonsMap = TypeUtils.determineTypeArguments((Class<?>) p2.getRawType(), p1);
                logger.debug("Adding to map: " + commonsMap);
                // TODO: Now we would need to iterate over the type parameters, and update the map?
                //map.putAll(commonsMap);

                for (TypeVariable<?> t : map.keySet()) {
                    logger.debug(t + ": " + t.getGenericDeclaration());
                }

                // For each type variable of the raw type, map the parameter type to that type
                Type[] p2TypesA = ((Class<?>) p2.getRawType()).getTypeParameters();
                Type[] p2TypesB = p2.getActualTypeArguments();
                for (int i = 0; i < p2TypesA.length; i++) {
                    Type a = p2TypesA[i];
                    Type b = p2TypesB[i];
                    logger.debug("Should be mapping " + a + " and " + b);
                    if (a instanceof TypeVariable<?>) {
                        logger.debug(a + " is a type variable: " + ((TypeVariable<?>) a).getGenericDeclaration());
                        if (b instanceof TypeVariable<?>) {
                            logger.debug(b + " is a type variable: " + ((TypeVariable<?>) b).getGenericDeclaration());
                            if (commonsMap.containsKey(a) && !(commonsMap.get(a) instanceof WildcardType) && !(commonsMap.get(a) instanceof TypeVariable<?>))
                                map.put((TypeVariable<?>) b, commonsMap.get(a));
                            //else
                            //	map.put((TypeVariable<?>)a, b);
                        }
                    }

//					if(b instanceof TypeVariable<?>) {
//						if(map.containsKey(a))
//							map.put((TypeVariable<?>)b, map.get(a));
//						//else
//						//	map.put((TypeVariable<?>)b, a);
//					}

                    logger.debug("Updated map: " + map);
                }

            }


            for (GenericClass<?> interfaceClass : ownerClass.getInterfaces()) {
                if (interfaceClass.isParameterizedType())
                    map.putAll(getMatchingTypeParameters(p1, (ParameterizedType) interfaceClass.getType()));
                else
                    logger.debug("Interface " + interfaceClass + " is not parameterized");
            }
            if (ownerClass.getRawClass().getSuperclass() != null) {
                GenericClass<?> ownerSuperClass = ownerClass.getSuperClass();
                if (ownerSuperClass.isParameterizedType())
                    map.putAll(getMatchingTypeParameters(p1, (ParameterizedType) ownerSuperClass.getType()));
                else
                    logger.debug("Super type " + ownerSuperClass + " is not parameterized");
            }
            return map;
        }

        for (int i = 0; i < p1.getActualTypeArguments().length; i++) {
            Type t1 = p1.getActualTypeArguments()[i];
            Type t2 = p2.getActualTypeArguments()[i];
            if (t1 == t2)
                continue;
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
            logger.debug("Updated map: " + map);

        }

        if (p1.getOwnerType() != null && p1.getOwnerType() instanceof ParameterizedType
                && p2.getOwnerType() instanceof ParameterizedType) {
            map.putAll(getMatchingTypeParameters((ParameterizedType) p1.getOwnerType(),
                    (ParameterizedType) p2.getOwnerType()));
        }

        return map;
    }

    /**
     * Returns true if the annotation is present in the annotationList, false otherwise.
     *
     * @param annotationList
     * @param annotationTypeName
     * @return boolean
     */
    public static boolean isAnnotationTypePresent(Annotation[] annotationList, String annotationTypeName) {
        for (Annotation annotation : annotationList) {

            if ((null != annotationTypeName)
                    && annotationTypeName.equalsIgnoreCase(annotation.annotationType().getSimpleName())) {
                return true;
            }
        }
        return false;
    }


}
