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

import com.googlecode.gentyref.GenericTypeReflector;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.utils.ParameterizedTypeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.*;
import java.util.*;

/**
 * This class is meant to mimic {@link java.lang.reflect.AccessibleObject AccessibleObject} from
 * the Java Reflections API, enhanced with a few additions and convenience methods to work
 * around the limitations of type erasure with regards to generics and to provide means for
 * serialization. A {@code GenericAccessibleObject} is the object-representation of one of the
 * following: a reflected field, reflected method, or reflected constructor of a class.
 *
 * @author Gordon Fraser
 */
public abstract class GenericAccessibleObject<T extends GenericAccessibleObject<T>>
        implements Serializable {

    protected static final Logger logger = LoggerFactory.getLogger(GenericAccessibleObject.class);

    private static final long serialVersionUID = 7069749492563662621L;

    /**
     * The class in which this GenericAccessibleObject (i.e. field, method or constructor) is
     * located in.
     */
    protected GenericClass<?> owner;

    protected List<GenericClass<?>> typeVariables = new ArrayList<>();

    protected static Type getTypeFromExactReturnType(GenericArrayType returnType,
                                                     GenericArrayType type) {
        return GenericArrayTypeImpl.createArrayType(getTypeFromExactReturnType(returnType.getGenericComponentType(),
                type.getGenericComponentType()));
    }

    protected static Type getTypeFromExactReturnType(GenericArrayType returnType,
                                                     ParameterizedType type) {
        return GenericArrayTypeImpl.createArrayType(getTypeFromExactReturnType(returnType.getGenericComponentType(),
                type));
    }

    protected static Type getTypeFromExactReturnType(ParameterizedType returnType,
                                                     GenericArrayType type) {
        return GenericArrayTypeImpl.createArrayType(getTypeFromExactReturnType(returnType,
                type.getGenericComponentType()));
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
        else if (returnType instanceof Class<?>)
            return returnType;
        else if (type instanceof Class<?>)
            return type;
        else
            throw new RuntimeException("Incompatible types: " + returnType.getClass()
                    + " and " + type.getClass() + ": " + returnType + " and " + type);
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

    /**
     * Constructs a new GenericAccessibleObject with the given {@code owner} class.
     *
     * @param owner the class where this accessible object is located in
     */
    public GenericAccessibleObject(GenericClass<?> owner) {
        this.owner = owner;
    }

    /**
     * Changes the class loader for the owning class of this {@code GenericAccessibleObject} and for
     * all of its type variables.
     *
     * @param loader the new class loader to set
     */
    public void changeClassLoader(ClassLoader loader) {
        owner.changeClassLoader(loader);
        for (GenericClass<?> typeVariable : typeVariables) {
            typeVariable.changeClassLoader(loader);
        }
    }

    protected void copyTypeVariables(GenericAccessibleObject<?> copy) {
        for (GenericClass<?> variable : typeVariables) {
            copy.typeVariables.add(GenericClassFactory.get(variable));
        }
    }

    /**
     * Creates and returns a copy of this {@code GenericAccessibleObject}.
     */
    public abstract T copy();

    public abstract T copyWithNewOwner(GenericClass<?> newOwner);

    public abstract T copyWithOwnerFromReturnType(GenericClass<?> returnType)
            throws ConstructionFailedException;

    public abstract AccessibleObject getAccessibleObject();

    public abstract Class<?> getDeclaringClass();

    public abstract Type getGeneratedType();

    public GenericClass<?> getGeneratedClass() {
        return GenericClassFactory.get(getGeneratedType());
    }

    public Type[] getGenericParameterTypes() {
        return new Type[]{};
    }

    public abstract Type getGenericGeneratedType();

    /**
     * Instantiate all generic type parameters
     *
     * @return
     * @throws ConstructionFailedException
     */
    public T getGenericInstantiation() throws ConstructionFailedException {
        T copy = copy();

        if (!hasTypeParameters()) {
            copy.owner = copy.getOwnerClass().getGenericInstantiation();
            return copy;
        }

        Map<TypeVariable<?>, Type> typeMap = copy.getOwnerClass().getTypeVariableMap();

        logger.debug("Getting random generic instantiation of method: " + this
                + " with owner type map: " + typeMap);
        List<GenericClass<?>> typeParameters = new ArrayList<>();

        // TODO: The bounds of this type parameter need to be updataed for the owner of the call
        // which may instantiate some of the type parameters
        for (TypeVariable<?> parameter : getTypeParameters()) {
            GenericClass<?> genericType = GenericClassFactory.get(parameter);
            GenericClass<?> concreteType = genericType.getGenericInstantiation(typeMap);
            logger.debug("Setting parameter " + parameter + " to type "
                    + concreteType.getTypeName());
            typeParameters.add(concreteType);
        }
        copy.setTypeParameters(typeParameters);
        copy.owner = copy.getOwnerClass().getGenericInstantiation(typeMap);
        return copy;
    }

    /**
     * Instantiate all generic type parameters based on a new callee type
     *
     * @param calleeType
     * @return
     * @throws ConstructionFailedException
     */
    public T getGenericInstantiation(GenericClass<?> calleeType)
            throws ConstructionFailedException {

        T copy = copy();

        logger.debug("Getting generic instantiation for callee " + calleeType
                + " of method: " + this + " for callee " + calleeType);
        Map<TypeVariable<?>, Type> typeMap = calleeType.getTypeVariableMap();
        if (!hasTypeParameters()) {
            logger.debug("Have no type parameters, just using typeMap of callee");
            copy.owner = copy.getOwnerClass().getGenericInstantiation(typeMap);
            return copy;
        }

        List<GenericClass<?>> typeParameters = new ArrayList<>();
        for (TypeVariable<?> parameter : getTypeParameters()) {
            GenericClass<?> concreteType = GenericClassFactory.get(parameter);
            logger.debug("(I) Setting parameter " + parameter + " to type "
                    + concreteType.getTypeName());
            typeParameters.add(concreteType.getGenericInstantiation(typeMap));
        }
        copy.setTypeParameters(typeParameters);
        copy.owner = copy.getOwnerClass().getGenericInstantiation(typeMap);

        return copy;
    }

    /**
     * Set type parameters based on return type
     *
     * @param generatedType
     * @return
     * @throws ConstructionFailedException
     */
    public T getGenericInstantiationFromReturnValue(GenericClass<?> generatedType)
            throws ConstructionFailedException {

        logger.debug("Instantiating generic return for generated Type " + generatedType);
        T copy = copy();

        // We just want to have the type variables defined in the generic method here
        // and not type variables defined in the owner
        Map<TypeVariable<?>, Type> concreteTypes = new HashMap<>();
        logger.debug("Getting type map of generated type");
        Map<TypeVariable<?>, Type> generatorTypes = generatedType.getTypeVariableMap();
        logger.debug("Got type map of generated type: " + generatorTypes);
        Type genericReturnType = getGenericGeneratedType();

        logger.debug("Getting generic instantiation for return type " + generatedType
                + " of method: " + this);

        if (genericReturnType instanceof ParameterizedType
                && generatedType.isParameterizedType()) {
            logger.debug("Return value is a parameterized type, matching variables");
            generatorTypes.putAll(GenericUtils.getMatchingTypeParameters((ParameterizedType) generatedType.getType(),
                    (ParameterizedType) genericReturnType));
        } else if (genericReturnType instanceof TypeVariable<?>) {
            generatorTypes.put((TypeVariable<?>) genericReturnType,
                    generatedType.getType());
        }

        if (genericReturnType instanceof ParameterizedType) {
            for (Type parameterType : getGenericParameterTypes()) {
                logger.debug("Checking parameter " + parameterType);
                if (parameterType instanceof ParameterizedType) {
                    Map<TypeVariable<?>, Type> matchedMap = GenericUtils.getMatchingTypeParameters((ParameterizedType) parameterType,
                            (ParameterizedType) genericReturnType);
                    for (TypeVariable<?> var : matchedMap.keySet()) {
                        if (!generatorTypes.containsKey(var))
                            generatorTypes.put(var, matchedMap.get(var));
                    }
                    logger.debug("Map is now " + generatorTypes);
                }
            }
        }
        logger.debug("GeneratorTypes is now: " + generatorTypes);
        List<TypeVariable<?>> parameters = Arrays.asList(getTypeParameters());
        for (TypeVariable<?> var : generatorTypes.keySet()) {
            if (parameters.contains(var) && !(generatorTypes.get(var) instanceof WildcardType)) {
                logger.debug("Parameter " + var + " in map, adding to concrete types: " + generatorTypes.get(var));
                concreteTypes.put(var, generatorTypes.get(var));
            } else {
                logger.debug("Parameter " + var + " not in map, not adding to concrete types: " + generatorTypes.get(var));
                logger.debug("Key: " + var.getGenericDeclaration());
                for (TypeVariable<?> k : parameters) {
                    logger.debug("Param: " + k.getGenericDeclaration());
                }
            }
        }

        // When resolving the type variables on a non-static generic method
        // we need to look at the owner type, and not the return type!

        List<GenericClass<?>> typeParameters = new ArrayList<>();
        logger.debug("Setting parameters with map: " + concreteTypes);
        for (TypeVariable<?> parameter : getTypeParameters()) {
            GenericClass<?> concreteType = GenericClassFactory.get(parameter);
            logger.debug("(I) Setting parameter " + parameter + " to type "
                    + concreteType.getTypeName());
            GenericClass<?> instantiation = concreteType.getGenericInstantiation(concreteTypes);
            logger.debug("Got instantiation for " + parameter + ": " + instantiation);
            if (!instantiation.satisfiesBoundaries(parameter, concreteTypes)) {
                logger.info("Type parameter does not satisfy boundaries: " + parameter
                        + " " + instantiation);
                logger.info(Arrays.asList(parameter.getBounds()).toString());
                logger.info(instantiation.toString());
                throw new ConstructionFailedException(
                        "Type parameter does not satisfy boundaries: " + parameter);
            }
            typeParameters.add(instantiation);
        }
        copy.setTypeParameters(typeParameters);
        copy.owner = copy.getOwnerClass().getGenericInstantiation(concreteTypes);

        return copy;
    }

    public abstract String getName();

    public int getNumParameters() {
        return 0;
    }

    public GenericClass<?> getOwnerClass() {
        return owner;
    }

    public Type getOwnerType() {
        return owner.getType();
    }

    public abstract Class<?> getRawGeneratedType();

    public abstract TypeVariable<?>[] getTypeParameters();

    protected Map<TypeVariable<?>, GenericClass<?>> getTypeVariableMap() {
        Map<TypeVariable<?>, GenericClass<?>> typeMap = new HashMap<>();
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

    public abstract boolean isAccessible();

    public abstract boolean isConstructor();

    public abstract boolean isField();

    public abstract boolean isMethod();

    public abstract boolean isStatic();

    public abstract boolean isPublic();

    public abstract boolean isPrivate();

    public abstract boolean isProtected();

    public abstract boolean isDefault();

    /**
     * Maps type parameters in a type to their values.
     *
     * @param toMapType     Type possibly containing type arguments
     * @param typeAndParams must be either ParameterizedType, or (in case there are no
     *                      type arguments, or it's a raw type) Class
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

    public void setTypeParameters(List<GenericClass<?>> parameterTypes) {
        typeVariables.clear();
        for (GenericClass<?> parameter : parameterTypes)
            typeVariables.add(GenericClassFactory.get(parameter));
    }

    @Override
    public abstract String toString();

    @Override
    public abstract boolean equals(Object other);

    @Override
    public abstract int hashCode();
}
