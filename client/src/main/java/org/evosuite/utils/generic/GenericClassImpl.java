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

import com.googlecode.gentyref.CaptureType;
import com.googlecode.gentyref.GenericTypeReflector;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.seeding.CastClassManager;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.ParameterizedTypeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class GenericClassImpl implements Serializable, GenericClass<GenericClassImpl> {

    private static final Logger logger = LoggerFactory.getLogger(GenericClass.class);

    private static final List<String> primitiveClasses = Arrays.asList("char", "int", "short",
            "long", "boolean",
            "float", "double",
            "byte");

    private static final long serialVersionUID = -3307107227790458308L;

    /**
     * Set of wrapper classes
     */
    private static final Set<Class<?>> WRAPPER_TYPES = new LinkedHashSet<>(
            Arrays.asList(Boolean.class, Character.class, Byte.class, Short.class,
                    Integer.class, Long.class, Float.class, Double.class,
                    Void.class));

    protected static Type addTypeParameters(Class<?> clazz) {
        if (clazz.isArray()) {
            return GenericArrayTypeImpl.createArrayType(addTypeParameters(clazz.getComponentType()));
        } else if (GenericClassUtils.isMissingTypeParameters(clazz)) {
            TypeVariable<?>[] vars = clazz.getTypeParameters();
            // Type[] arguments = new Type[vars.length];
            // Arrays.fill(arguments, UNBOUND_WILDCARD);
            Type owner = clazz.getDeclaringClass() == null ? null
                    : addTypeParameters(clazz.getDeclaringClass());
            return new ParameterizedTypeImpl(clazz, vars, owner);
        } else {
            return clazz;
        }
    }

    /**
     * Returns the erasure of the given type.
     */
    private static Class<?> erase(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        } else if (type instanceof TypeVariable) {
            TypeVariable<?> tv = (TypeVariable<?>) type;
            if (tv.getBounds().length == 0)
                return Object.class;
            else
                return erase(tv.getBounds()[0]);
        } else if (type instanceof GenericArrayType) {
            GenericArrayType aType = (GenericArrayType) type;
            return GenericArrayTypeImpl.createArrayType(erase(aType.getGenericComponentType()));
        } else if (type instanceof CaptureType) {
            CaptureType captureType = (CaptureType) type;
            if (captureType.getUpperBounds().length == 0)
                return Object.class;
            else
                return erase(captureType.getUpperBounds()[0]);
        } else {
            // TODO at least support CaptureType here
            throw new RuntimeException("not supported: " + type.getClass());
        }
    }

    private static Class<?> getClass(String name) throws ClassNotFoundException {
        return getClass(name, TestGenerationContext.getInstance().getClassLoaderForSUT());
    }

    private static Class<?> getClass(String name, ClassLoader loader)
            throws ClassNotFoundException {
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
            Class<?> componentType = getClass(name.substring(1), loader);
            Object array = Array.newInstance(componentType, 0);
            return array.getClass();
        } else if (name.startsWith("L") && name.endsWith(";")) {
            return getClass(name.substring(1, name.length() - 1), loader);
        } else if (name.endsWith(";")) {
            return getClass(name.substring(0, name.length() - 1), loader);
        } else if (name.endsWith(".class")) {
            return getClass(name.replace(".class", ""), loader);
        } else
            return loader.loadClass(name);
    }

    /**
     * Tells whether the type {@code rhsType} (on the right-hand side of an assignment) can be
     * assigned to the type {@code lhsType} (on the left-hand side of an assignment).
     *
     * @param lhsType the type on the left-hand side (target type)
     * @param rhsType the type on the right-hand side (subject type to be assigned to target type)
     *                a {@link java.lang.reflect.Type} object.
     * @return {@code true} if {@code rhsType} is assignable to {@code lhsType}
     */
    private static boolean isAssignable(Type lhsType, Type rhsType) {
        return GenericClassUtils.isAssignable(lhsType, rhsType);
    }

    transient Class<?> rawClass = null;

    transient Type type = null;

    /**
     * Generate a generic class by setting all generic parameters to their
     * parameter types
     *
     * @param clazz a {@link java.lang.Class} object.
     */
    public GenericClassImpl(Class<?> clazz) {
        this.type = addTypeParameters(clazz); //GenericTypeReflector.addWildcardParameters(clazz);
        this.rawClass = clazz;
    }

    public GenericClassImpl(GenericClassImpl copy) {
        this.type = copy.type;
        this.rawClass = copy.rawClass;
        if (copy.getTypeVariableMap() != null) {
            this.typeVariableMap = new LinkedHashMap<>(copy.getTypeVariableMap());
        }
    }

    /**
     * Generate a generic class by from a type
     *
     * @param type a {@link java.lang.reflect.Type} object.
     */
    public GenericClassImpl(Type type) {
        if (type instanceof Class<?>) {
            this.type = addTypeParameters((Class<?>) type); //GenericTypeReflector.addWildcardParameters((Class<?>) type);
            this.rawClass = (Class<?>) type;
        } else {
            if (!handleGenericArraySpecialCase(type)) {
                this.type = type;
                try {
                    this.rawClass = erase(type);
                } catch (RuntimeException e) {
                    // If there is an unresolved capture type in here
                    // we delete it and replace with a wildcard
                    this.rawClass = Object.class;
                }
            }
        }
    }

    /**
     * Generate a GenericClass with this exact generic type and raw class
     *
     * @param type
     * @param clazz
     */
    public GenericClassImpl(Type type, Class<?> clazz) {
        this.type = type;
        this.rawClass = clazz;
        handleGenericArraySpecialCase(type);
    }

    @Override
    public GenericClassImpl self() {
        return this;
    }

    /**
     * Determine if there exists an instantiation of the type variables such
     * that the class matches otherType
     *
     * @param otherType is the class we want to generate
     * @return
     */
    public boolean canBeInstantiatedTo(GenericClass<?> otherType) {

        if (isPrimitive() && otherType.isWrapperType())
            return false;

        if (isAssignableTo(otherType))
            return true;

        if (!isTypeVariable() && !otherType.isTypeVariable()) {
            try {
                if (otherType.isGenericSuperTypeOf(this))
                    return true;
            } catch (RuntimeException e) {
                // FIXME: GentyRef sometimes throws:
                // java.lang.RuntimeException: not implemented: class sun.reflect.generics.reflectiveObjects.TypeVariableImpl
                // While I have no idea why, it should be safe to proceed if we can ignore this type
                return false;
            }

        }

        Class<?> otherRawClass = otherType.getRawClass();
        if (otherRawClass.isAssignableFrom(rawClass)) {
            Map<TypeVariable<?>, Type> typeMap = otherType.getTypeVariableMap();
            if (otherType.isParameterizedType()) {
                typeMap.putAll(TypeUtils.determineTypeArguments(rawClass,
                        (ParameterizedType) otherType.getType()));
            }
            try {
                GenericClass instantiation = getGenericInstantiation(typeMap);
                if (equals(instantiation)) {
                    return !hasWildcardOrTypeVariables();
                }
                return instantiation.canBeInstantiatedTo(otherType);
            } catch (ConstructionFailedException e) {
                logger.debug("Failed to instantiate " + this);
                return false;
            }
        }

        return false;
    }

    /**
     * <p>
     * changeClassLoader
     * </p>
     *
     * @param loader a {@link ClassLoader} object.
     * @return
     */
    public boolean changeClassLoader(ClassLoader loader) {
        try {
            if (rawClass != null)
                rawClass = getClass(rawClass.getName(), loader);
            if (type instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) type;
                // GenericClass rawType = new GenericClass(pt.getRawType());
                // rawType.changeClassLoader(loader);
                GenericClass<?> ownerType = null;
                if (pt.getOwnerType() != null) {
                    ownerType = new GenericClassImpl(pt.getOwnerType());
                    // ownerType.type = pt.getOwnerType();
                    ownerType.changeClassLoader(loader);
                }
                List<GenericClass<?>> parameterClasses = new ArrayList<>();
                for (Type parameterType : pt.getActualTypeArguments()) {
                    GenericClass<?> parameter = new GenericClassImpl(parameterType);
                    // parameter.type = parameterType;
                    parameter.changeClassLoader(loader);
                    parameterClasses.add(parameter);
                }
                Type[] parameterTypes = new Type[parameterClasses.size()];
                for (int i = 0; i < parameterClasses.size(); i++)
                    parameterTypes[i] = parameterClasses.get(i).getType();
                this.type = new ParameterizedTypeImpl(rawClass, parameterTypes,
                        ownerType != null ? ownerType.getType() : null);
            } else if (type instanceof GenericArrayType) {
                GenericClass<?> componentClass = getComponentClass();
                componentClass.changeClassLoader(loader);
                this.type = GenericArrayTypeImpl.createArrayType(componentClass.getType());
            } else if (type instanceof WildcardType) {
                Type[] oldUpperBounds = ((WildcardType) type).getUpperBounds();
                Type[] oldLowerBounds = ((WildcardType) type).getLowerBounds();
                Type[] upperBounds = new Type[oldUpperBounds.length];
                Type[] lowerBounds = new Type[oldLowerBounds.length];

                for (int i = 0; i < oldUpperBounds.length; i++) {
                    GenericClass<?> bound = new GenericClassImpl(oldUpperBounds[i]);
                    // bound.type = oldUpperBounds[i];
                    bound.changeClassLoader(loader);
                    upperBounds[i] = bound.getType();
                }
                for (int i = 0; i < oldLowerBounds.length; i++) {
                    GenericClass<?> bound = new GenericClassImpl(oldLowerBounds[i]);
                    // bound.type = oldLowerBounds[i];
                    bound.changeClassLoader(loader);
                    lowerBounds[i] = bound.getType();
                }
                this.type = new WildcardTypeImpl(upperBounds, lowerBounds);
            } else if (type instanceof TypeVariable<?>) {
                for (TypeVariable<?> newVar : rawClass.getTypeParameters()) {
                    if (newVar.getName().equals(((TypeVariable<?>) type).getName())) {
                        this.type = newVar;
                        break;
                    }
                }
            } else {
                this.type = addTypeParameters(rawClass); //GenericTypeReflector.addWildcardParameters(raw_class);
            }
        } catch (ClassNotFoundException e) {
            logger.warn("Class not found: " + rawClass + " - keeping old class loader ",
                    e);
        } catch (SecurityException e) {
            logger.warn("Class not found: " + rawClass + " - keeping old class loader ",
                    e);
        }
        return true;
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
        GenericClassImpl other = (GenericClassImpl) obj;
        //return type.equals(other.type);
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

    public Class<?> getBoxedType() {
        if (isPrimitive()) {
            if (rawClass.equals(int.class))
                return Integer.class;
            else if (rawClass.equals(byte.class))
                return Byte.class;
            else if (rawClass.equals(short.class))
                return Short.class;
            else if (rawClass.equals(long.class))
                return Long.class;
            else if (rawClass.equals(float.class))
                return Float.class;
            else if (rawClass.equals(double.class))
                return Double.class;
            else if (rawClass.equals(char.class))
                return Character.class;
            else if (rawClass.equals(boolean.class))
                return Boolean.class;
            else if (rawClass.equals(void.class))
                return Void.class;
            else
                throw new RuntimeException("Unknown unboxed type: " + rawClass);
        }
        return rawClass;
    }

    /**
     * <p>
     * getClassName
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getClassName() {
        return rawClass.getName();
    }

    public GenericClass<?> getComponentClass() {
        if (type instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType) type;
            Type componentType = arrayType.getGenericComponentType();
            Class<?> rawComponentType = rawClass.getComponentType();
            return new GenericClassImpl(componentType, rawComponentType);
        } else {
            return new GenericClassImpl(rawClass.getComponentType());
        }
    }

    /**
     * <p>
     * getComponentName
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getComponentName() {
        return rawClass.getComponentType().getSimpleName();
    }

    /**
     * <p>
     * getComponentType
     * </p>
     *
     * @return a {@link java.lang.reflect.Type} object.
     */
    public Type getComponentType() {
        return GenericTypeReflector.getArrayComponentType(type);
    }

    public Collection<GenericClass<?>> getGenericBounds() {

        Set<GenericClass<?>> bounds = new LinkedHashSet<>();

        if (isRawClass() || !hasWildcardOrTypeVariables()) {
            return bounds;
        }

        if (isWildcardType()) {
            getGenericWildcardBounds(bounds);
        } else if (isArray()) {
            bounds.addAll(getComponentClass().getGenericBounds());
        } else if (isTypeVariable()) {
            getGenericTypeVarBounds(bounds);
        } else if (isParameterizedType()) {
            getGenericParameterizedTypeBounds(bounds);
        }
        return bounds;
    }

    private void getGenericWildcardBounds(Collection<GenericClass<?>> bounds) {
        for (Type t : ((WildcardType) type).getUpperBounds()) {
            bounds.add(new GenericClassImpl(t));
        }
        for (Type t : ((WildcardType) type).getLowerBounds()) {
            bounds.add(new GenericClassImpl(t));
        }
    }

    private void getGenericTypeVarBounds(Collection<GenericClass<?>> bounds) {
        for (Type t : ((TypeVariable<?>) type).getBounds()) {
            bounds.add(new GenericClassImpl(t));
        }
    }

    private void getGenericParameterizedTypeBounds(Collection<GenericClass<?>> bounds) {
        for (TypeVariable<?> typeVar : getTypeVariables()) {
            for (Type t : typeVar.getBounds()) {
                bounds.add(new GenericClassImpl(t));
            }
        }
    }

    /**
     * Instantiate all type variables randomly, but adhering to type boundaries
     *
     * @return
     * @throws ConstructionFailedException
     */
    public GenericClass<?> getGenericInstantiation() throws ConstructionFailedException {
        return getGenericInstantiation(new HashMap<>());
    }

    /**
     * Instantiate type variables using map, and anything not contained in the
     * map randomly
     *
     * @param typeMap
     * @return
     * @throws ConstructionFailedException
     */
    public GenericClass<?> getGenericInstantiation(Map<TypeVariable<?>, Type> typeMap)
            throws ConstructionFailedException {
        return getGenericInstantiation(typeMap, 0);
    }

    public GenericClass<?> getGenericInstantiation(Map<TypeVariable<?>, Type> typeMap, int recursionLevel) throws ConstructionFailedException {

        logger.debug("Instantiation " + this + " with type map " + typeMap);
        // If there are no type variables, create copy
        if (isRawClass() || !hasWildcardOrTypeVariables() || recursionLevel > Properties.MAX_GENERIC_DEPTH) {
            logger.debug("Nothing to replace: " + this + ", " + isRawClass() + ", "
                    + hasWildcardOrTypeVariables());
            return new GenericClassImpl(this);
        }

        if (isWildcardType()) {
            logger.debug("Is wildcard type.");
            return getGenericWildcardInstantiation(typeMap, recursionLevel);
        } else if (isArray()) {
            return getGenericArrayInstantiation(typeMap, recursionLevel);
        } else if (isTypeVariable()) {
            logger.debug("Is type variable ");
            return getGenericTypeVariableInstantiation(typeMap, recursionLevel);
        } else if (isParameterizedType()) {
            logger.debug("Is parameterized type");
            return getGenericParameterizedTypeInstantiation(typeMap, recursionLevel);
        }
        return null;
    }

    /**
     * Instantiate generic component type
     *
     * @param typeMap
     * @return
     * @throws ConstructionFailedException evel
     */
    private GenericClass<?> getGenericArrayInstantiation(Map<TypeVariable<?>, Type> typeMap,
                                                         int recursionLevel) throws ConstructionFailedException {
        GenericClass<?> componentClass = getComponentClass().getGenericInstantiation();
        return getWithComponentClass(componentClass);
    }

    /**
     * Instantiate type variable
     *
     * @param typeMap
     * @param recursionLevel
     * @return
     * @throws ConstructionFailedException
     */
    private GenericClass<?> getGenericTypeVariableInstantiation(
            Map<TypeVariable<?>, Type> typeMap, int recursionLevel)
            throws ConstructionFailedException {
        if (typeMap.containsKey(type)) {
            logger.debug("Type contains {}: {}", this, typeMap);
            if (typeMap.get(type) == type) {
                // FIXXME: How does this happen?
                throw new ConstructionFailedException("Type points to itself");
            }
            GenericClass<?> selectedClass = new GenericClassImpl(typeMap.get(type)).getGenericInstantiation(typeMap,
                    recursionLevel + 1);
            if (!selectedClass.satisfiesBoundaries((TypeVariable<?>) type)) {
                logger.debug("Cannot be instantiated to: {}", selectedClass);
            } else {
                logger.debug("Can be instantiated to: {}", selectedClass);
                return selectedClass;
            }
        }
        logger.debug("Type map does not contain {}: {}", this, typeMap);

        GenericClass<?> selectedClass = CastClassManager.getInstance().selectCastClass((TypeVariable<?>) type,
                recursionLevel < Properties.MAX_GENERIC_DEPTH,
                typeMap);

        if (selectedClass == null) {
            throw new ConstructionFailedException("Unable to instantiate "
                    + this);
        }
        logger.debug("Getting instantiation of type variable {}: {}", this, selectedClass);
        Map<TypeVariable<?>, Type> extendedMap = new HashMap<>(
                typeMap);
        extendedMap.putAll(getTypeVariableMap());
        for (Type bound : ((TypeVariable<?>) type).getBounds()) {
            logger.debug("Current bound of variable {}: {}", type, bound);
            GenericClass<?> boundClass = new GenericClassImpl(bound);
            extendedMap.putAll(boundClass.getTypeVariableMap());
            if (boundClass.isParameterizedType()) {
                Class<?> boundRawClass = boundClass.getRawClass();
                if (boundRawClass.isAssignableFrom(selectedClass.getRawClass())) {
                    Map<TypeVariable<?>, Type> xmap = TypeUtils.determineTypeArguments(selectedClass.getRawClass(), (ParameterizedType) boundClass.getType());
                    extendedMap.putAll(xmap);
                }
            }
        }

        logger.debug("Updated type variable map to {}", extendedMap);

        GenericClass<?> instantiation = selectedClass.getGenericInstantiation(extendedMap,
                recursionLevel + 1);
        typeMap.put((TypeVariable<?>) type, instantiation.getType());
        return instantiation;

    }

    /**
     * Instantiate wildcard type
     *
     * @param typeMap
     * @param recursionLevel
     * @return
     * @throws ConstructionFailedException
     */
    public GenericClass<?> getGenericWildcardInstantiation(Map<TypeVariable<?>, Type> typeMap, int recursionLevel)
            throws ConstructionFailedException {
        GenericClass<?> selectedClass = CastClassManager.getInstance().selectCastClass((WildcardType) type,
                recursionLevel < Properties.MAX_GENERIC_DEPTH,
                typeMap);
        return selectedClass.getGenericInstantiation(typeMap, recursionLevel + 1);
    }

    @Override
    public GenericClass<?> setType(Type type) {
        this.type = type;
        return this;
    }

    public List<GenericClass<?>> getInterfaces() {
        List<GenericClass<?>> ret = new ArrayList<>();
        for (Class<?> intf : rawClass.getInterfaces()) {
            ret.add(new GenericClassImpl(intf));
        }
        return ret;
    }

    /**
     * Instantiate all type parameters of a parameterized type
     *
     * @param typeMap
     * @param recursionLevel
     * @return
     * @throws ConstructionFailedException
     */
    private GenericClass<?> getGenericParameterizedTypeInstantiation(
            Map<TypeVariable<?>, Type> typeMap, int recursionLevel)
            throws ConstructionFailedException {

        // FIXME: This negatively affects coverage. Why was it added?
        //
        //		if(isClass() && !hasTypeVariables()) {
        //			return this;
        //		}

        List<TypeVariable<?>> typeParameters = getTypeVariables();

        Type[] parameterTypes = new Type[typeParameters.size()];
        Type ownerType = null;

        int numParam = 0;

        for (GenericClass<?> parameterClass : getParameterClasses()) {
            logger.debug("Current parameter to instantiate: {}", parameterClass);
            /*
             * If the parameter is a parameterized type variable such as T extends Map<String, K extends Number>
             * then the boundaries of the parameters of the type variable need to be respected
             */
            if (!parameterClass.hasWildcardOrTypeVariables()) {
                logger.debug("Parameter has no wildcard or type variable");
                parameterTypes[numParam++] = parameterClass.getType();
            } else {
                logger.debug("Current parameter has type variables: " + parameterClass);

                Map<TypeVariable<?>, Type> extendedMap = new HashMap<>(
                        typeMap);
                extendedMap.putAll(parameterClass.getTypeVariableMap());
                if (!extendedMap.containsKey(typeParameters.get(numParam)) && !parameterClass.isTypeVariable())
                    extendedMap.put(typeParameters.get(numParam), parameterClass.getType());
                logger.debug("New type map: " + extendedMap);

                if (parameterClass.isWildcardType()) {
                    logger.debug("Is wildcard type, here we should value the wildcard boundaries");
                    logger.debug("Wildcard boundaries: " + parameterClass.getGenericBounds());
                    logger.debug("Boundaries of underlying var: " + Arrays.asList(typeParameters.get(numParam).getBounds()));
                    GenericClass<?> parameterInstance = parameterClass.getGenericWildcardInstantiation(extendedMap, recursionLevel + 1);
                    //GenericClass parameterTypeClass = new GenericClass(typeParameters.get(numParam));
//					if(!parameterTypeClass.isAssignableFrom(parameterInstance)) {
                    if (!parameterInstance.satisfiesBoundaries(typeParameters.get(numParam))) {
                        throw new ConstructionFailedException("Invalid generic instance");
                    }
                    //GenericClass parameterInstance = new GenericClass(
                    //        typeParameters.get(numParam)).getGenericInstantiation(extendedMap,
                    //                                                              recursionLevel + 1);
                    parameterTypes[numParam++] = parameterInstance.getType();
                } else {
                    logger.debug("Is not wildcard but type variable? "
                            + parameterClass.isTypeVariable());

                    GenericClass<?> parameterInstance = parameterClass.getGenericInstantiation(extendedMap,
                            recursionLevel + 1);
                    parameterTypes[numParam++] = parameterInstance.getType();
                }
            }
        }

        if (hasOwnerType()) {
            GenericClass<?> ownerClass = getOwnerType().getGenericInstantiation(typeMap,
                    recursionLevel);
            ownerType = ownerClass.getType();
        }

        return new GenericClassImpl(new ParameterizedTypeImpl(rawClass, parameterTypes,
                ownerType));
    }

    /**
     * Retrieve number of generic type parameters
     *
     * @return
     */
    public int getNumParameters() {
        if (type instanceof ParameterizedType) {
            return Arrays.asList(((ParameterizedType) type).getActualTypeArguments()).size();
        }
        return 0;
    }

    /**
     * Retrieve the generic owner
     *
     * @return
     */
    public GenericClass<?> getOwnerType() {
        return new GenericClassImpl(((ParameterizedType) type).getOwnerType());
    }

    /**
     * Retrieve list of actual parameters
     *
     * @return
     */
    public List<Type> getParameterTypes() {
        if (type instanceof ParameterizedType) {
            return Arrays.asList(((ParameterizedType) type).getActualTypeArguments());
        }
        return new ArrayList<>();
    }

    /**
     * Retrieve list of parameter classes
     *
     * @return
     */
    public List<GenericClass<?>> getParameterClasses() {
        if (type instanceof ParameterizedType) {
            List<GenericClass<?>> parameters = new ArrayList<>();
            for (Type parameterType : ((ParameterizedType) type).getActualTypeArguments()) {
                parameters.add(new GenericClassImpl(parameterType));
            }
            return parameters;
        }
        return new ArrayList<>();
    }

    /**
     * <p>
     * getRawClass
     * </p>
     *
     * @return a {@link java.lang.Class} object.
     */
    public Class<?> getRawClass() {
        return rawClass;
    }

    /**
     * <p>
     * getComponentClass
     * </p>
     *
     * @return a {@link java.lang.reflect.Type} object.
     */
    public Type getRawComponentClass() {
        return GenericTypeReflector.erase(rawClass.getComponentType());
    }

    public GenericClass<?> getRawGenericClass() {
        return new GenericClassImpl(rawClass);
    }

    /**
     * <p>
     * getSimpleName
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSimpleName() {
        // return raw_class.getSimpleName();
        String name = ClassUtils.getShortClassName(rawClass).replace(";", "[]");
        if (!isPrimitive() && primitiveClasses.contains(name))
            return rawClass.getSimpleName().replace(";", "[]");

        return name;
    }

    public GenericClassImpl getSuperClass() {
        return new GenericClassImpl(
                GenericTypeReflector.getExactSuperType(type, rawClass.getSuperclass()));
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

    private Map<TypeVariable<?>, Type> typeVariableMap = null;

    public Map<TypeVariable<?>, Type> getTypeVariableMap() {
        if (typeVariableMap != null)
            return typeVariableMap;
        //logger.debug("Getting type variable map for " + type);
        List<TypeVariable<?>> typeVariables = getTypeVariables();
        List<Type> types = getParameterTypes();
        Map<TypeVariable<?>, Type> typeMap = new LinkedHashMap<>();
        try {
            if (rawClass.getSuperclass() != null
                    && !rawClass.isAnonymousClass()
                    && !rawClass.getSuperclass().isAnonymousClass()
                    && !(hasOwnerType() && getOwnerType().getRawClass().isAnonymousClass())) {
                GenericClass<?> superClass = getSuperClass();
                //logger.debug("Superclass of " + type + ": " + superClass);
                Map<TypeVariable<?>, Type> superMap = superClass.getTypeVariableMap();
                //logger.debug("Super map after " + superClass + ": " + superMap);
                typeMap.putAll(superMap);
            }
            for (Class<?> interFace : rawClass.getInterfaces()) {
                GenericClass<?> interFaceClass = new GenericClassImpl(interFace);
                //logger.debug("Interface of " + type + ": " + interFaceClass);
                Map<TypeVariable<?>, Type> superMap = interFaceClass.getTypeVariableMap();
                //logger.debug("Super map after " + superClass + ": " + superMap);
                typeMap.putAll(superMap);
            }
            if (isTypeVariable()) {
                for (Type boundType : ((TypeVariable<?>) type).getBounds()) {
                    GenericClass<?> boundClass = new GenericClassImpl(boundType);
                    typeMap.putAll(boundClass.getTypeVariableMap());
                }
            }

        } catch (Exception e) {
            logger.debug("Exception while getting type map: " + e);
        }
        for (int i = 0; i < typeVariables.size(); i++) {
            if (types.get(i) != typeVariables.get(i)) {
                typeMap.put(typeVariables.get(i), types.get(i));
            }
        }

        //logger.debug("Type map: " + typeMap);
        typeVariableMap = typeMap;
        return typeMap;
    }

    /**
     * Return a list of type variables of this type, or an empty list if this is
     * not a parameterized type
     *
     * @return
     */
    public List<TypeVariable<?>> getTypeVariables() {
        List<TypeVariable<?>> typeVariables = new ArrayList<>();
        if (type instanceof ParameterizedType) {
            //logger.debug("Type variables of "+rawClass+": ");
            //for(TypeVariable<?> var : rawClass.getTypeParameters()) {
            //	logger.debug("Var "+var+" of "+var.getGenericDeclaration());
            //}
            typeVariables.addAll(Arrays.asList(rawClass.getTypeParameters()));
        }
        return typeVariables;
    }

    public Class<?> getUnboxedType() {
        if (isWrapperType()) {
            if (rawClass.equals(Integer.class))
                return int.class;
            else if (rawClass.equals(Byte.class))
                return byte.class;
            else if (rawClass.equals(Short.class))
                return short.class;
            else if (rawClass.equals(Long.class))
                return long.class;
            else if (rawClass.equals(Float.class))
                return float.class;
            else if (rawClass.equals(Double.class))
                return double.class;
            else if (rawClass.equals(Character.class))
                return char.class;
            else if (rawClass.equals(Boolean.class))
                return boolean.class;
            else if (rawClass.equals(Void.class))
                return void.class;
            else
                throw new RuntimeException("Unknown boxed type: " + rawClass);
        }
        return rawClass;
    }

    public GenericClass<?> getWithComponentClass(GenericClass<?> componentClass) {
        if (type instanceof GenericArrayType) {
            return new GenericClassImpl(
                    GenericArrayTypeImpl.createArrayType(componentClass.getType()),
                    rawClass);
        } else {
            return new GenericClassImpl(type, rawClass);
        }
    }

    public GenericClass<?> getWithGenericParameterTypes(List<GenericClassImpl> parameters) {
        Type[] typeArray = new Type[parameters.size()];
        for (int i = 0; i < parameters.size(); i++) {
            typeArray[i] = parameters.get(i).getType();
        }
        Type ownerType = null;
        if (type instanceof ParameterizedType) {
            ownerType = ((ParameterizedType) type).getOwnerType();
        }

        return new GenericClassImpl(new ParameterizedTypeImpl(rawClass, typeArray, ownerType));
    }

    public GenericClass<?> getWithOwnerType(GenericClass<?> ownerClass) {
        if (type instanceof ParameterizedType) {
            ParameterizedType currentType = (ParameterizedType) type;
            return new GenericClassImpl(new ParameterizedTypeImpl(rawClass,
                    currentType.getActualTypeArguments(), ownerClass.getType()));
        }

        return new GenericClassImpl(type);
    }

    /**
     * If this is a LinkedList<?> and the super class is a List<Integer> then
     * this returns a LinkedList<Integer>
     *
     * @param superClass
     * @return
     * @throws ConstructionFailedException
     */
    public GenericClass<?> getWithParametersFromSuperclass(GenericClass<?> superClass)
            throws ConstructionFailedException {
        GenericClassImpl exactClass = new GenericClassImpl(type);
        if (!(type instanceof ParameterizedType)) {
            exactClass.type = type;
            return exactClass;
        }
        ParameterizedType pType = (ParameterizedType) type;

        if (superClass.isParameterizedType()) {
            Map<TypeVariable<?>, Type> typeMap = TypeUtils.determineTypeArguments(rawClass,
                    (ParameterizedType) superClass.getType());
            return getGenericInstantiation(typeMap);
        }

        Class<?> targetClass = superClass.getRawClass();
        Class<?> currentClass = rawClass;
        Type[] parameterTypes = new Type[superClass.getNumParameters()];
        superClass.getParameterTypes().toArray(parameterTypes);

        if (targetClass.equals(currentClass)) {
            logger.info("Raw classes match, setting parameters to: "
                    + superClass.getParameterTypes());
            exactClass.type = new ParameterizedTypeImpl(currentClass, parameterTypes,
                    pType.getOwnerType());
        } else {
            Type ownerType = pType.getOwnerType();
            Map<TypeVariable<?>, Type> superTypeMap = superClass.getTypeVariableMap();
            Type[] origArguments = pType.getActualTypeArguments();
            Type[] arguments = new Type[origArguments.length];
            // For some reason, doing this would lead to arguments being
            // of component type TypeVariable, which would lead to
            // ArrayStoreException if we try to assign a WildcardType
            //Type[] arguments = Arrays.copyOf(origArguments, origArguments.length);
            for (int i = 0; i < origArguments.length; i++)
                arguments[i] = origArguments[i];
            List<TypeVariable<?>> variables = getTypeVariables();
            for (int i = 0; i < arguments.length; i++) {
                TypeVariable<?> var = variables.get(i);
                if (superTypeMap.containsKey(var)) {
                    arguments[i] = superTypeMap.get(var);
                    logger.info("Setting type variable " + var + " to "
                            + superTypeMap.get(var));
                } else if (arguments[i] instanceof WildcardType
                        && i < parameterTypes.length) {
                    logger.info("Replacing wildcard with " + parameterTypes[i]);
                    logger.info("Lower Bounds: "
                            + Arrays.asList(TypeUtils.getImplicitLowerBounds((WildcardType) arguments[i])));
                    logger.info("Upper Bounds: "
                            + Arrays.asList(TypeUtils.getImplicitUpperBounds((WildcardType) arguments[i])));
                    logger.info("Type variable: " + variables.get(i));
                    if (!TypeUtils.isAssignable(parameterTypes[i], arguments[i])) {
                        logger.info("Not assignable to bounds!");
                        return null;
                    } else {
                        boolean assignable = false;
                        for (Type bound : variables.get(i).getBounds()) {
                            if (TypeUtils.isAssignable(parameterTypes[i], bound)) {
                                assignable = true;
                                break;
                            }
                        }
                        if (!assignable) {
                            logger.info("Not assignable to type variable!");
                            return null;
                        }
                    }
                    arguments[i] = parameterTypes[i];
                }
            }
            GenericClass<?> ownerClass = new GenericClassImpl(ownerType).getWithParametersFromSuperclass(superClass);
            if (ownerClass == null)
                return null;
            exactClass.type = new ParameterizedTypeImpl(currentClass, arguments,
                    ownerClass.getType());
        }

        return exactClass;
    }

    public GenericClass<?> getWithParameterTypes(List<Type> parameters) {
        Type[] typeArray = new Type[parameters.size()];
        for (int i = 0; i < parameters.size(); i++) {
            typeArray[i] = parameters.get(i);
        }
        Type ownerType = null;
        if (type instanceof ParameterizedType) {
            ownerType = ((ParameterizedType) type).getOwnerType();
        }
        return new GenericClassImpl(new ParameterizedTypeImpl(rawClass, typeArray, ownerType));
    }

    public GenericClass<?> getWithParameterTypes(Type[] parameters) {
        Type ownerType = null;
        if (type instanceof ParameterizedType) {
            ownerType = ((ParameterizedType) type).getOwnerType();
        }
        return new GenericClassImpl(
                new ParameterizedTypeImpl(rawClass, parameters, ownerType));
    }

    public GenericClass<?> getWithWildcardTypes() {
        Type ownerType = GenericTypeReflector.addWildcardParameters(rawClass);
        return new GenericClassImpl(ownerType);
    }

    private boolean handleGenericArraySpecialCase(Type type) {
        if (type instanceof GenericArrayType) {
            // There is some weird problem with generic methods and the component type can be null
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            if (componentType == null) {
                this.rawClass = Object[].class;
                this.type = this.rawClass;
                return true;
            }
        }

        return false;
    }

    /**
     * Determine if this class is a subclass of superType
     *
     * @param superType
     * @return
     */
    public boolean hasGenericSuperType(GenericClass superType) {
        return GenericTypeReflector.isSuperType(superType.getType(), type);
    }

    /**
     * Determine if this class is a subclass of superType
     *
     * @param superType
     * @return
     */
    public boolean hasGenericSuperType(Type superType) {
        return GenericTypeReflector.isSuperType(superType, type);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getTypeName().hashCode();
        //result = prime * result + ((raw_class == null) ? 0 : raw_class.hashCode());
        //result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    public boolean hasOwnerType() {
        if (type instanceof ParameterizedType)
            return ((ParameterizedType) type).getOwnerType() != null;
        else
            return false;
    }

    public boolean hasTypeVariables() {
        if (isParameterizedType()) {
            return hasTypeVariables((ParameterizedType) type);
        }

        return isTypeVariable();
    }

    private boolean hasTypeVariables(ParameterizedType parameterType) {
        for (Type t : parameterType.getActualTypeArguments()) {
            if (t instanceof TypeVariable)
                return true;
            else if (t instanceof ParameterizedType) {
                if (hasTypeVariables((ParameterizedType) t))
                    return true;
            }
        }

        return false;
    }

    public boolean hasWildcardOrTypeVariables() {
        if (isTypeVariable() || isWildcardType())
            return true;

        if (hasWildcardTypes())
            return true;

        if (hasTypeVariables())
            return true;

        if (hasOwnerType()) {
            if (getOwnerType().hasWildcardOrTypeVariables())
                return true;
        }

        return type instanceof GenericArrayType && getComponentClass().hasWildcardOrTypeVariables();
    }

    private boolean hasWildcardType(ParameterizedType parameterType) {
        for (Type t : parameterType.getActualTypeArguments()) {
            if (t instanceof WildcardType)
                return true;
            else if (t instanceof ParameterizedType) {
                if (hasWildcardType((ParameterizedType) t))
                    return true;
            }
        }

        return false;
    }

    public boolean hasWildcardTypes() {
        if (isParameterizedType()) {
            return hasWildcardType((ParameterizedType) type);
        }

        return isWildcardType();
    }

    /**
     * True if this represents an abstract class
     *
     * @return
     */
    public boolean isAbstract() {
        return Modifier.isAbstract(rawClass.getModifiers());
    }

    /**
     * True if this is an anonymous class
     *
     * @return
     */
    public boolean isAnonymous() {
        return rawClass.isAnonymousClass();
    }

    /**
     * Return true if variable is an array
     *
     * @return a boolean.
     */
    public boolean isArray() {
        return rawClass.isArray();
    }

    /**
     * <p>
     * isAssignableFrom
     * </p>
     *
     * @param rhsType a {@link GenericClass} object.
     * @return a boolean.
     */
    public boolean isAssignableFrom(GenericClass<?> rhsType) {
        return isAssignable(type, rhsType.getType());
    }

    /**
     * <p>
     * isAssignableFrom
     * </p>
     *
     * @param rhsType a {@link java.lang.reflect.Type} object.
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
     * @param lhsType a {@link GenericClass} object.
     * @return a boolean.
     */
    public boolean isAssignableTo(GenericClass<?> lhsType) {
        return isAssignable(lhsType.getType(), type);
    }

    /**
     * <p>
     * isAssignableTo
     * </p>
     *
     * @param lhsType a {@link java.lang.reflect.Type} object.
     * @return a boolean.
     */
    public boolean isAssignableTo(Type lhsType) {
        return isAssignable(lhsType, type);
    }

    /**
     * True if this represents java.lang.Class
     *
     * @return
     */
    public boolean isClass() {
        return rawClass.equals(Class.class);
    }

    /**
     * Return true if variable is an enumeration
     *
     * @return a boolean.
     */
    public boolean isEnum() {
        return rawClass.isEnum();
    }

    public boolean isGenericArray() {
        GenericClassImpl componentClass = new GenericClassImpl(rawClass.getComponentType());
        return componentClass.hasWildcardOrTypeVariables();
    }

    /**
     * Determine if subType is a generic subclass
     *
     * @param subType
     * @return
     */
    public boolean isGenericSuperTypeOf(GenericClass<?> subType) {
        return GenericTypeReflector.isSuperType(type, subType.getType());
    }

    /**
     * Determine if subType is a generic subclass
     *
     * @param subType
     * @return
     */
    public boolean isGenericSuperTypeOf(Type subType) {
        return GenericTypeReflector.isSuperType(type, subType);
    }

    /**
     * True is this represents java.lang.Object
     *
     * @return
     */
    public boolean isObject() {
        return rawClass.equals(Object.class);
    }

    /**
     * True if this represents a parameterized generic type
     *
     * @return
     */
    public boolean isParameterizedType() {
        return type instanceof ParameterizedType;
    }

    /**
     * Return true if variable is a primitive type
     *
     * @return a boolean.
     */
    public boolean isPrimitive() {
        return rawClass.isPrimitive();
    }

    /**
     * True if this is a non-generic type
     *
     * @return
     */
    public boolean isRawClass() {
        return type instanceof Class<?>;
    }

    /**
     * True if this is a type variable
     *
     * @return
     */
    public boolean isTypeVariable() {
        return type instanceof TypeVariable<?>;
    }

    /**
     * True if this is a wildcard type
     *
     * @return
     */
    public boolean isWildcardType() {
        return type instanceof WildcardType;
    }

    /**
     * True if this represents java.lang.String
     *
     * @return a boolean.
     */
    public boolean isString() {
        return rawClass.equals(String.class);
    }

    /**
     * Return true if variable is void
     *
     * @return a boolean.
     */
    public boolean isVoid() {
        return rawClass.equals(Void.class) || rawClass.equals(void.class);
    }

    /**
     * Return true if type of variable is a primitive wrapper
     *
     * @return a boolean.
     */
    public boolean isWrapperType() {
        return WRAPPER_TYPES.contains(rawClass);
    }

    public boolean satisfiesBoundaries(TypeVariable<?> typeVariable) {
        return satisfiesBoundaries(typeVariable, getTypeVariableMap());
    }

    /**
     * Determine whether the boundaries of the type variable are satisfied by
     * this class
     *
     * @param typeVariable
     * @return
     */
    public boolean satisfiesBoundaries(TypeVariable<?> typeVariable,
                                       Map<TypeVariable<?>, Type> typeMap) {
        boolean isAssignable = true;
        // logger.debug("Checking class: " + type + " against type variable " + typeVariable+" with map "+typeMap);
        Map<TypeVariable<?>, Type> ownerVariableMap = getTypeVariableMap();
        for (Type bound : typeVariable.getBounds()) {
            if (bound instanceof ParameterizedType) {
                Class<?> boundClass = GenericTypeReflector.erase(bound);
                if (boundClass.isAssignableFrom(rawClass)) {
                    Map<TypeVariable<?>, Type> xmap = TypeUtils.determineTypeArguments(rawClass, (ParameterizedType) bound);
                    ownerVariableMap.putAll(xmap);
                }
            }
        }
        ownerVariableMap.putAll(typeMap);
        boolean changed = true;

        while (changed) {
            changed = false;
            for (TypeVariable<?> var : ownerVariableMap.keySet()) {

                // If the type variable points to a typevariable, let it point to what the other typevariable points to
                // A -> B
                // B -> C
                // ==> A -> C
                if (ownerVariableMap.get(var) instanceof TypeVariable<?>) {
                    // Other type variable, i.e., the one this is currently pointing to
                    TypeVariable<?> value = (TypeVariable<?>) ownerVariableMap.get(var);
                    if (ownerVariableMap.containsKey(value)) {

                        Type other = ownerVariableMap.get(value);
                        if (other instanceof TypeVariable<?>) {
                            // If the value (C) is also a typevariable, check we don't have a recursion here
                            if (ownerVariableMap.containsKey(other)) {
                                Type x = ownerVariableMap.get(other);
                                if (x == var || x == value || x == other) {
                                    continue;
                                }
                            }
                        }
                        if (var != other && value != other) {
                            ownerVariableMap.put(var, other);
                            changed = true;
                        }
                    }
                }
            }
        }

        GenericClassImpl concreteClass = new GenericClassImpl(GenericUtils.replaceTypeVariables(type, ownerVariableMap));
        //logger.debug("Concrete class after variable replacement: " + concreteClass);

        for (Type theType : typeVariable.getBounds()) {
            //logger.debug("Current boundary of " + typeVariable + ": " + theType);
            // Special case: Enum is defined as Enum<T extends Enum>
            if (GenericTypeReflector.erase(theType).equals(Enum.class)) {
                //logger.debug("Is ENUM case");
                // if this is an enum then it's ok.
                if (isEnum()) {
                    //logger.debug("Class " + toString() + " is an enum!");

                    continue;
                } else {
                    // If it's not an enum, it cannot be assignable to enum!
                    //logger.debug("Class " + toString() + " is not an enum.");
                    isAssignable = false;
                    break;
                }
            }

            Type boundType = GenericUtils.replaceTypeVariables(theType, ownerVariableMap);
            boundType = GenericUtils.replaceTypeVariable(boundType, typeVariable,
                    getType());
            boundType = GenericUtils.replaceTypeVariablesWithWildcards(boundType);

            //logger.debug("Bound after variable replacement: " + boundType);
            if (!concreteClass.isAssignableTo(boundType) && !(boundType instanceof WildcardType)) {
                //logger.debug("Not assignable: " + type + " and " + boundType);
                // If the boundary is not assignable it may still be possible
                // to instantiate the generic to an assignable type
                if (GenericTypeReflector.erase(boundType).isAssignableFrom(getRawClass())) {
                    //logger.debug("Raw classes are assignable: " + boundType + ", "
                    //        + getRawClass());
                    Type instanceType = GenericTypeReflector.getExactSuperType(boundType,
                            getRawClass());
                    if (instanceType == null) {
                        // This happens when the raw class is not a supertype
                        // of the boundary
                        //logger.debug("Instance type is null");
                        isAssignable = false;
                        break;
                    }
//					GenericClass instanceClass = new GenericClass(instanceType,
//					        getRawClass());

//					logger.debug("Instance type is " + instanceType);
//					if (instanceClass.hasTypeVariables())
//						logger.debug("Instance type has type variables");
//					if (instanceClass.hasWildcardTypes())
//						logger.debug("Instance type has wildcard variables");

                    boundType = GenericUtils.replaceTypeVariable(theType, typeVariable,
                            instanceType);
                    // logger.debug("Instance type after replacement is " + boundType);
                    if (GenericClassImpl.isAssignable(boundType, instanceType)) {
                        //logger.debug("Found assignable generic exact type: "
                        //        + instanceType);
                        continue;
                    } else {
                        //logger.debug("Is not assignable: " + boundType + " and "
                        //        + instanceType);
                    }
                }
                isAssignable = false;
                break;
            }
        }
        //logger.debug("Result: is assignable " + isAssignable);
        return isAssignable;
    }

    public boolean satisfiesBoundaries(WildcardType wildcardType) {
        return satisfiesBoundaries(wildcardType, getTypeVariableMap());
    }

    /**
     * Determine whether the upper and lower boundaries are satisfied by this
     * class
     *
     * @param wildcardType
     * @return
     */
    public boolean satisfiesBoundaries(WildcardType wildcardType,
                                       Map<TypeVariable<?>, Type> typeMap) {
        boolean isAssignable = true;
        Map<TypeVariable<?>, Type> ownerVariableMap = getTypeVariableMap();
        ownerVariableMap.putAll(typeMap);

        // ? extends X
        for (Type theType : wildcardType.getUpperBounds()) {
            logger.debug("Checking upper bound " + theType);
            // Special case: Enum is defined as Enum<T extends Enum>
            if (GenericTypeReflector.erase(theType).equals(Enum.class)) {
                // if this is an enum then it's ok.
                if (isEnum())
                    continue;
                else {
                    // If it's not an enum, it cannot be assignable to enum!
                    isAssignable = false;
                    break;
                }
            }

            Type type = GenericUtils.replaceTypeVariables(theType, ownerVariableMap);
            //logger.debug("Bound after variable replacement: " + type);
            if (!isAssignableTo(type)) {
                // If the boundary is not assignable it may still be possible
                // to instantiate the generic to an assignable type
                if (type instanceof WildcardType) {
                    // TODO i don't know exactly how to handle this case, but it is necessary to prevent an Exception
                    isAssignable = false;
                    break;
                }
                if (GenericTypeReflector.erase(type).isAssignableFrom(getRawClass())) {
                    Type instanceType = GenericTypeReflector.getExactSuperType(type,
                            getRawClass());
                    if (instanceType == null) {
                        // This happens when the raw class is not a supertype
                        // of the boundary
                        isAssignable = false;
                        break;
                    }

                    if (GenericClassImpl.isAssignable(type, instanceType)) {
                        logger.debug("Found assignable generic exact type: "
                                + instanceType);
                        continue;
                    }
                }
                isAssignable = false;
                break;
            }
        }

        // ? super X
        Type[] lowerBounds = wildcardType.getLowerBounds();
        if (lowerBounds != null && lowerBounds.length > 0) {
            for (Type theType : wildcardType.getLowerBounds()) {
                logger.debug("Checking lower bound " + theType);
                Type type = GenericUtils.replaceTypeVariables(theType, ownerVariableMap);
                logger.debug("Bound after variable replacement: " + type);
                logger.debug("Is assignable from " + this + "?");
                if (!isAssignableFrom(type)) {
                    logger.debug("Not assignable from " + this);
                    // If the boundary is not assignable it may still be possible
                    // to instantiate the generic to an assignable type
                    if (type instanceof WildcardType)
                        continue;
                    if (GenericTypeReflector.erase(type).isAssignableFrom(getRawClass())) {
                        Type instanceType = GenericTypeReflector.getExactSuperType(type,
                                getRawClass());
                        if (instanceType == null) {
                            // This happens when the raw class is not a supertype
                            // of the boundary
                            isAssignable = false;
                            break;
                        }

                        if (GenericClassImpl.isAssignable(type, instanceType)) {
                            logger.debug("Found assignable generic exact type: "
                                    + instanceType);
                            continue;
                        }
                    }
                    isAssignable = false;
                    break;
                } else {
                    logger.debug("Is assignable from " + this);
                }
            }
        }
        return isAssignable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (type == null) {
            LoggingUtils.getEvoLogger().info("Type is null for raw class " + rawClass);
            for (StackTraceElement elem : Thread.currentThread().getStackTrace()) {
                LoggingUtils.getEvoLogger().info(elem.toString());
            }
            assert (false);
        }
        return type.toString();
    }

    /**
     * De-serialize. Need to use current classloader.
     *
     * @param ois
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
            IOException {
		/*
		// ProjectCP is added to ClassLoader to ensure Dependencies of the class can be loaded.
		*/
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        URL cpURL = new File(Properties.CP).toURI().toURL();
        // If the ContextClassLoader contains already the project cp, we don't add another one
        // We assume, that if the contextClassLoader is no URLClassLoader, it does not contain the projectCP
        if (!(contextClassLoader instanceof URLClassLoader) ||
                !Arrays.asList(((URLClassLoader) contextClassLoader).getURLs()).contains(cpURL)) {
            URL[] urls;
            urls = new URL[]{cpURL};
            URLClassLoader urlClassLoader = new URLClassLoader(urls, contextClassLoader);
            Thread.currentThread().setContextClassLoader(urlClassLoader);
        }

        String name = (String) ois.readObject();
        if (name == null) {
            this.rawClass = null;
            this.type = null;
            return;
        }
        this.rawClass = getClass(name);

        Boolean isParameterized = (Boolean) ois.readObject();
        if (isParameterized) {
            // GenericClass rawType = (GenericClass) ois.readObject();
            GenericClass ownerType = (GenericClass) ois.readObject();
            @SuppressWarnings("unchecked")
            List<GenericClass> parameterClasses = (List<GenericClass>) ois.readObject();
            Type[] parameterTypes = new Type[parameterClasses.size()];
            for (int i = 0; i < parameterClasses.size(); i++)
                parameterTypes[i] = parameterClasses.get(i).getType();
            this.type = new ParameterizedTypeImpl(rawClass, parameterTypes,
                    ownerType.getType());
        } else {
            this.type = addTypeParameters(rawClass); //GenericTypeReflector.addWildcardParameters(raw_class);
        }
    }

    /**
     * Serialize, but need to abstract classloader away
     *
     * @param oos
     * @throws IOException
     */
    private void writeObject(ObjectOutputStream oos) throws IOException {
        if (rawClass == null) {
            oos.writeObject(null);
        } else {
            oos.writeObject(rawClass.getName());
            if (type instanceof ParameterizedType) {
                oos.writeObject(Boolean.TRUE);
                ParameterizedType pt = (ParameterizedType) type;
                // oos.writeObject(new GenericClass(pt.getRawType()));
                oos.writeObject(new GenericClassImpl(pt.getOwnerType()));
                List<GenericClassImpl> parameterClasses = new ArrayList<>();
                for (Type parameterType : pt.getActualTypeArguments()) {
                    parameterClasses.add(new GenericClassImpl(parameterType));
                }
                oos.writeObject(parameterClasses);
            } else {
                oos.writeObject(Boolean.FALSE);
            }
        }
    }

}
