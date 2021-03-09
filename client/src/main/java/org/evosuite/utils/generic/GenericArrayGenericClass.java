package org.evosuite.utils.generic;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.evosuite.Properties;
import org.evosuite.ga.ConstructionFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.*;

public class GenericArrayGenericClass extends AbstractGenericClass<GenericArrayType> {
    private static final Logger logger = LoggerFactory.getLogger(GenericArrayGenericClass.class);

    public GenericArrayGenericClass(GenericArrayType type, Class<?> rawClass) {
        super(type, rawClass);
    }

    @Override
    public boolean changeClassLoader(ClassLoader loader) {
        try {
            if (rawClass != null) {
                rawClass = GenericClassUtils.getClass(rawClass.getName(), loader);
            }
            if (type != null) {
                GenericClass<?> componentClass = getComponentClass();
                componentClass.changeClassLoader(loader);
                this.type = TypeUtils.genericArrayType(componentClass.getType());
            } else {
                // TODO what to do if type == null?
                throw new IllegalStateException("Type of generic class is null. Don't know what to do.");
            }
            return true;
        } catch (ClassNotFoundException | SecurityException e) {
            logger.warn("Class not found: " + rawClass + " - keeping old class loader ", e);
        }
        return false;
    }

    @Override
    public Collection<GenericClass<?>> getGenericBounds() {
        if (!hasWildcardOrTypeVariables())
            return Collections.emptySet();
        return getComponentClass().getGenericBounds();
    }

    @Override
    public GenericClass<?> getGenericInstantiation(Map<TypeVariable<?>, Type> typeMap, int recursionLevel) throws ConstructionFailedException {
        logger.debug("Instantiation " + toString() + " with type map " + typeMap);
        // If there are no type variables, create copy
        if (!hasWildcardOrTypeVariables() || recursionLevel > Properties.MAX_GENERIC_DEPTH) {
            logger.debug("Nothing to replace: " + toString() + ", " + isRawClass() + ", " + hasWildcardOrTypeVariables());
            return GenericClassFactory.get(this);
        }

        return getGenericTypeVariableInstantiation(typeMap, recursionLevel);
    }

    @Override
    public AbstractGenericClass<GenericArrayType> getOwnerType() {
        throw new UnsupportedOperationException("A generic array has no owner type");
    }

    @Override
    public List<Type> getParameterTypes() {
        return Collections.emptyList();
    }

    @Override
    public String getTypeName() {
        return type.toString();
    }

    @Override
    public List<TypeVariable<?>> getTypeVariables() {
        return Collections.emptyList();
    }

    @Override
    public Class<?> getUnboxedType() {
        return rawClass;
    }

    @Override
    public GenericClass<?> getWithComponentClass(GenericClass<?> componentClass) {
        return new GenericArrayGenericClass(TypeUtils.genericArrayType(componentClass.getType()), rawClass);
    }

    @Override
    public boolean hasOwnerType() {
        return false;
    }

    @Override
    public boolean hasWildcardOrTypeVariables() {
        return getComponentClass().hasWildcardOrTypeVariables();
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
        return true;
    }

    @Override
    public boolean isParameterizedType() {
        return false;
    }

    @Override
    public boolean isRawClass() {
        return false;
    }

    @Override
    public boolean isTypeVariable() {
        throw new UnsupportedOperationException("Not Implemented: ArrayGenericClass#isTypeVariable");
    }

    @Override
    public boolean isWildcardType() {
        return false;
    }

    @Override
    public boolean satisfiesBoundaries(TypeVariable<?> typeVariable, Map<TypeVariable<?>, Type> typeMap) {
        throw new UnsupportedOperationException("Not Implemented: ArrayGenericClass#satisfiesBoundaries");
    }

    @Override
    public boolean satisfiesBoundaries(WildcardType wildcardType, Map<TypeVariable<?>, Type> typeMap) {
        throw new UnsupportedOperationException("Not Implemented: ArrayGenericClass#satisfiesBoundaries");
    }

    @Override
    public GenericClass<?> getGenericWildcardInstantiation(Map<TypeVariable<?>, Type> typeMap, int recursionLevel) throws ConstructionFailedException {
        throw new UnsupportedOperationException("A generic array has no generic wildcard instantiation");
    }

    @Override
    public GenericClass<?> getComponentClass() {
        Type arrayComponentType = TypeUtils.getArrayComponentType(this.type);
        return GenericClassFactory.get(arrayComponentType);
    }

    @Override
    public GenericClass<?> getWithParametersFromSuperclass(GenericClass<?> superClass) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: ArrayGenericClass#getWithParametersFromSuperclass");
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
        throw new UnsupportedOperationException("Not Implemented: ArrayGenericClass#canBeInstantiatedTo");
    }

    @Override
    boolean canBeInstantiatedTo(WildcardGenericClass otherType) {
        throw new UnsupportedOperationException("Not Implemented: ArrayGenericClass#canBeInstantiatedTo");
    }

    @Override
    boolean canBeInstantiatedTo(GenericArrayGenericClass otherType) {
        throw new UnsupportedOperationException("Not Implemented: ArrayGenericClass#canBeInstantiatedTo");
    }

    @Override
    boolean canBeInstantiatedTo(RawClassGenericClass otherType) {
        throw new UnsupportedOperationException("Not Implemented: ArrayGenericClass#canBeInstantiatedTo");
    }

    @Override
    boolean canBeInstantiatedTo(ParameterizedGenericClass otherType) {
        throw new UnsupportedOperationException("Not Implemented: ArrayGenericClass#canBeInstantiatedTo");
    }

    @Override
    GenericClass<?> getWithParametersFromSuperclass(TypeVariableGenericClass otherType) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: ArrayGenericClass#getWithParametersFromSuperclass");
    }

    @Override
    GenericClass<?> getWithParametersFromSuperclass(WildcardGenericClass otherType) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: ArrayGenericClass#getWithParametersFromSuperclass");
    }

    @Override
    GenericClass<?> getWithParametersFromSuperclass(GenericArrayGenericClass otherType) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: ArrayGenericClass#getWithParametersFromSuperclass");
    }

    @Override
    GenericClass<?> getWithParametersFromSuperclass(RawClassGenericClass otherType) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: ArrayGenericClass#getWithParametersFromSuperclass");
    }

    @Override
    GenericClass<?> getWithParametersFromSuperclass(ParameterizedGenericClass otherType) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: ArrayGenericClass#getWithParametersFromSuperclass");
    }

    private GenericClass<?> getGenericTypeVariableInstantiation(Map<TypeVariable<?>, Type> typeMap,
                                                                int recursionLevel) {
        throw new UnsupportedOperationException("Not Implemented: " + "ArrayGenericClass" +
                "#getGenericTypeVariableInstantiation");
    }
}
