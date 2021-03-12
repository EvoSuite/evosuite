package org.evosuite.utils.generic;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.evosuite.ga.ConstructionFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.*;

public class RawClassGenericClass extends AbstractGenericClass<Class<?>> {
    private static final Logger logger = LoggerFactory.getLogger(RawClassGenericClass.class);

    public RawClassGenericClass(Class<?> type) {
        super(type, type);
    }

    @Override
    public boolean changeClassLoader(ClassLoader loader) {
        try {
            if (rawClass != null) {
                rawClass = GenericClassUtils.getClassByFullyQualifiedName(rawClass.getName(), loader);
            }
            if (rawClass != null && type != null) {
                this.type = rawClass;
            }
            return true;
        } catch (ClassNotFoundException | SecurityException e) {
            logger.warn("Class not found: " + rawClass + " - keeping old class loader ", e);
        }
        return false;
    }

    @Override
    public Collection<GenericClass<?>> getGenericBounds() {
        return Collections.emptySet();
    }

    @Override
    public GenericClass<?> getGenericInstantiation(Map<TypeVariable<?>, Type> typeMap, int recursionLevel) throws ConstructionFailedException {
        logger.debug("Instantiation " + toString() + " with type map " + typeMap);
        // If there are no type variables, create copy
        logger.debug("Nothing to replace: " + toString() + ", " + isRawClass() + ", " + hasWildcardOrTypeVariables());
        return GenericClassFactory.get(this);
    }

    @Override
    public AbstractGenericClass<Class<?>> getOwnerType() {
        throw new UnsupportedOperationException("A raw class has no owner type");
    }

    @Override
    public List<Type> getParameterTypes() {
        return Collections.emptyList();
    }

    @Override
    public String getTypeName() {
        // TODO: Check if this is actually identical to GenericTypeReflector#getTypeName(rawClass);
        return rawClass.isArray() ? rawClass.getComponentType().getName() + "[]" : rawClass.getName();
    }

    @Override
    public List<TypeVariable<?>> getTypeVariables() {
        return Collections.emptyList();
    }

    @Override
    public GenericClass<?> getWithComponentClass(GenericClass<?> componentClass) {
        return new RawClassGenericClass(type);
    }

    @Override
    public boolean hasOwnerType() {
        return false;
    }

    @Override
    public boolean hasWildcardOrTypeVariables() {
        return false;
    }

    @Override
    public boolean hasTypeVariables() {
        return false;
    }

    @Override
    public boolean hasWildcardTypes() {
        return false;
    }

    @Override
    public boolean isGenericArray() {
        return isArray() && GenericClassFactory.get(rawClass.getComponentType()).hasWildcardOrTypeVariables();
    }

    @Override
    public boolean isParameterizedType() {
        return false;
    }

    @Override
    public boolean isRawClass() {
        return true;
    }

    @Override
    public boolean isTypeVariable() {
        return false;
    }

    @Override
    public boolean isWildcardType() {
        return false;
    }

    @Override
    public boolean satisfiesBoundaries(TypeVariable<?> typeVariable, Map<TypeVariable<?>, Type> typeMap) {
        throw new UnsupportedOperationException("Not Implemented: RawClassGenericClass#satisfiesBoundaries");
    }

    @Override
    public boolean satisfiesBoundaries(WildcardType wildcardType, Map<TypeVariable<?>, Type> typeMap) {
        throw new UnsupportedOperationException("Not Implemented: RawClassGenericClass#satisfiesBoundaries");
    }

    @Override
    public GenericClass<?> getGenericWildcardInstantiation(Map<TypeVariable<?>, Type> typeMap, int recursionLevel) throws ConstructionFailedException {
        throw new UnsupportedOperationException("A raw class has no generic wildcard instantiation");
    }

    @Override
    public Class<?> getUnboxedType() {
        if (isWrapperType()) {
            if (rawClass.equals(Integer.class)) {
                return int.class;
            } else if (rawClass.equals(Byte.class)) {
                return byte.class;
            } else if (rawClass.equals(Short.class)) {
                return short.class;
            } else if (rawClass.equals(Long.class)) {
                return long.class;
            } else if (rawClass.equals(Float.class)) {
                return float.class;
            } else if (rawClass.equals(Double.class)) {
                return double.class;
            } else if (rawClass.equals(Character.class)) {
                return char.class;
            } else if (rawClass.equals(Boolean.class)) {
                return boolean.class;
            } else if (rawClass.equals(Void.class)) {
                return void.class;
            } else {
                throw new RuntimeException("Unknown boxed type: " + rawClass);
            }
        }
        return rawClass;
    }

    @Override
    protected Map<TypeVariable<?>, Type> computeTypeVariableMapIfTypeVariable() {
        return Collections.emptyMap();
    }

    @Override
    protected Map<TypeVariable<?>, Type> updateInheritedTypeVariables(Map<TypeVariable<?>, Type> typeMap) {
        return typeMap;
    }

    @Override
    boolean canBeInstantiatedTo(TypeVariableGenericClass otherType) {
        Class<?> otherRawClass = otherType.getRawClass();
        if (otherRawClass.isAssignableFrom(rawClass)) {
            Map<TypeVariable<?>, Type> typeMap = otherType.getTypeVariableMap();
            try {
                GenericClass<?> instantiation = getGenericInstantiation(typeMap);
                return equals(instantiation) ? !hasWildcardOrTypeVariables() : instantiation.canBeInstantiatedTo(otherType);
            } catch (ConstructionFailedException e) {
                logger.debug("Failed to instantiate " + toString());
                return false;
            }
        }
        return false;
    }

    @Override
    boolean canBeInstantiatedTo(WildcardGenericClass otherType) {
        try {
            if (otherType.isGenericSuperTypeOf(this)) {
                return true;
            }
        } catch (RuntimeException e) {
            // FIXME: GentyRef sometimes throws:
            // java.lang.RuntimeException: not implemented: class sun.reflect.generics.reflectiveObjects
            // .TypeVariableImpl
            // While I have no idea why, it should be safe to proceed if we can ignore this type
            // return false;
            throw new IllegalStateException("GentryRef threw an exception", e);
        }

        Class<?> otherRawClass = otherType.getRawClass();
        if (otherRawClass.isAssignableFrom(rawClass)) {
            Map<TypeVariable<?>, Type> typeMap = otherType.getTypeVariableMap();
            try {
                GenericClass<?> instantiation = getGenericInstantiation(typeMap);
                return equals(instantiation) ? !hasWildcardOrTypeVariables() : instantiation.canBeInstantiatedTo(otherType);
            } catch (ConstructionFailedException e) {
                logger.debug("Failed to instantiate " + toString());
                return false;
            }
        }
        return false;
    }

    @Override
    boolean canBeInstantiatedTo(GenericArrayGenericClass otherType) {
        try {
            if (otherType.isGenericSuperTypeOf(this)) {
                return true;
            }
        } catch (RuntimeException e) {
            // FIXME: GentyRef sometimes throws:
            // java.lang.RuntimeException: not implemented: class sun.reflect.generics.reflectiveObjects
            // .TypeVariableImpl
            // While I have no idea why, it should be safe to proceed if we can ignore this type
            // return false;
            throw new IllegalStateException("GentryRef threw an exception", e);
        }

        Class<?> otherRawClass = otherType.getRawClass();
        if (otherRawClass.isAssignableFrom(rawClass)) {
            Map<TypeVariable<?>, Type> typeMap = otherType.getTypeVariableMap();
            try {
                GenericClass<?> instantiation = getGenericInstantiation(typeMap);
                return equals(instantiation) ? !hasWildcardOrTypeVariables() : instantiation.canBeInstantiatedTo(otherType);
            } catch (ConstructionFailedException e) {
                logger.debug("Failed to instantiate " + toString());
                return false;
            }
        }
        return false;
    }

    @Override
    boolean canBeInstantiatedTo(RawClassGenericClass otherType) {
        try {
            if (otherType.isGenericSuperTypeOf(this)) {
                return true;
            }
        } catch (RuntimeException e) {
            // FIXME: GentyRef sometimes throws:
            // java.lang.RuntimeException: not implemented: class sun.reflect.generics.reflectiveObjects
            // .TypeVariableImpl
            // While I have no idea why, it should be safe to proceed if we can ignore this type
            // return false;
            throw new IllegalStateException("GentryRef threw an exception", e);
        }

        Class<?> otherRawClass = otherType.getRawClass();
        if (otherRawClass.isAssignableFrom(rawClass)) {
            Map<TypeVariable<?>, Type> typeMap = otherType.getTypeVariableMap();;
            try {
                GenericClass<?> instantiation = getGenericInstantiation(typeMap);
                return equals(instantiation) ? !hasWildcardOrTypeVariables() : instantiation.canBeInstantiatedTo(otherType);
            } catch (ConstructionFailedException e) {
                logger.debug("Failed to instantiate " + toString());
                return false;
            }
        }
        return false;
    }

    @Override
    boolean canBeInstantiatedTo(ParameterizedGenericClass otherType) {
        try {
            if (otherType.isGenericSuperTypeOf(this)) {
                return true;
            }
        } catch (RuntimeException e) {
            // FIXME: GentyRef sometimes throws:
            // java.lang.RuntimeException: not implemented: class sun.reflect.generics.reflectiveObjects
            // .TypeVariableImpl
            // While I have no idea why, it should be safe to proceed if we can ignore this type
            // return false;
            throw new IllegalStateException("GentryRef threw an exception", e);
        }
        Class<?> otherRawClass = otherType.getRawClass();
        if (otherRawClass.isAssignableFrom(rawClass)) {
            Map<TypeVariable<?>, Type> typeMap = otherType.getTypeVariableMap();
            if (otherType.isParameterizedType())
                typeMap.putAll(TypeUtils.determineTypeArguments(rawClass, (ParameterizedType) otherType.getType()));
            try {
                GenericClass<?> instantiation = getGenericInstantiation(typeMap);
                return equals(instantiation) ? !hasWildcardOrTypeVariables() : instantiation.canBeInstantiatedTo(otherType);
            } catch (ConstructionFailedException e) {
                logger.debug("Failed to instantiate " + toString());
                return false;
            }
        }
        return false;
    }

    @Override
    GenericClass<?> getWithParametersFromSuperclass(TypeVariableGenericClass otherType) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: " + "RawClassGenericClass" +
                "#getWithParametersFromSuperclass");
    }

    @Override
    GenericClass<?> getWithParametersFromSuperclass(WildcardGenericClass otherType) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: " + "RawClassGenericClass" +
                "#getWithParametersFromSuperclass");
    }

    @Override
    GenericClass<?> getWithParametersFromSuperclass(GenericArrayGenericClass otherType) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: RawClassGenericClass" +
                "#getWithParametersFromSuperclass");
    }

    @Override
    GenericClass<?> getWithParametersFromSuperclass(RawClassGenericClass otherType) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: " + "RawClassGenericClass" +
                "#getWithParametersFromSuperclass");
    }

    @Override
    GenericClass<?> getWithParametersFromSuperclass(ParameterizedGenericClass otherType) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: " + "RawClassGenericClass" +
                "#getWithParametersFromSuperclass");
    }
}
