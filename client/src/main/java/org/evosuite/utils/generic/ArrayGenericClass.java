package org.evosuite.utils.generic;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.evosuite.ga.ConstructionFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ArrayGenericClass extends AbstractGenericClass<GenericArrayType> {
    private static final Logger logger = LoggerFactory.getLogger(ArrayGenericClass.class);

    public ArrayGenericClass(GenericArrayType type, Class<?> rawClass) {
        super(type, rawClass);
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
    boolean canBeInstantiatedTo(ArrayGenericClass otherType) {
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
    GenericClass<?> getWithParametersFromSuperclass(ArrayGenericClass otherType) throws ConstructionFailedException {
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

    @Override
    public void changeClassLoader(ClassLoader loader) {
        throw new UnsupportedOperationException("Not Implemented: ArrayGenericClass#changeClassLoader");
    }

    @Override
    public GenericClass<?> getComponentClass() {
        Type arrayComponentType = TypeUtils.getArrayComponentType(this.type);
        return GenericClassFactory.get(arrayComponentType);
    }

    @Override
    public Collection<GenericClass<?>> getGenericBounds() {
        throw new UnsupportedOperationException("Not Implemented: ArrayGenericClass#getGenericBounds");
    }

    @Override
    public GenericClass<?> getGenericInstantiation(Map<TypeVariable<?>, Type> typeMap, int recursionLevel) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: ArrayGenericClass#getGenericInstantiation");
    }

    @Override
    public List<AbstractGenericClass<GenericArrayType>> getInterfaces() {
        throw new UnsupportedOperationException("Not Implemented: ArrayGenericClass#getInterfaces");
    }

    @Override
    public int getNumParameters() {
        throw new UnsupportedOperationException("Not Implemented: ArrayGenericClass#getNumParameters");
    }

    @Override
    public AbstractGenericClass<GenericArrayType> getOwnerType() {
        throw new UnsupportedOperationException("Not Implemented: ArrayGenericClass#getOwnerType");
    }

    @Override
    public List<Type> getParameterTypes() {
        return Collections.emptyList();
    }

    @Override
    public Type getRawComponentClass() {
        throw new UnsupportedOperationException("Not Implemented: ArrayGenericClass#getRawComponentClass");
    }

    @Override
    public String getSimpleName() {
        throw new UnsupportedOperationException("Not Implemented: ArrayGenericClass#getSimpleName");
    }

    @Override
    public AbstractGenericClass<GenericArrayType> getSuperClass() {
        throw new UnsupportedOperationException("Not Implemented: ArrayGenericClass#getSuperClass");
    }

    @Override
    public String getTypeName() {
        throw new UnsupportedOperationException("Not Implemented: ArrayGenericClass#getTypeName");
    }

    @Override
    public Map<TypeVariable<?>, Type> getTypeVariableMap() {
        throw new UnsupportedOperationException("Not Implemented: ArrayGenericClass#getTypeVariableMap");
    }

    @Override
    public List<TypeVariable<?>> getTypeVariables() {
        return Collections.emptyList();
    }

    @Override
    public Class<?> getUnboxedType() {
        throw new UnsupportedOperationException("Not Implemented: ArrayGenericClass#getUnboxedType");
    }

    @Override
    public GenericClass<?> getWithComponentClass(GenericClass<?> componentClass) {
        return new ArrayGenericClass(TypeUtils.genericArrayType(componentClass.getType()), rawClass);
    }

    @Override
    public GenericClass<?> getWithGenericParameterTypes(List<AbstractGenericClass<GenericArrayType>> parameters) {
        throw new UnsupportedOperationException("Not Implemented: ArrayGenericClass#getWithGenericParameterTypes");
    }

    @Override
    public GenericClass<?> getWithParametersFromSuperclass(GenericClass<?> superClass) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: ArrayGenericClass#getWithParametersFromSuperclass");
    }

    @Override
    public boolean hasOwnerType() {
        return false;
    }

    @Override
    public boolean hasWildcardOrTypeVariables() {
        throw new UnsupportedOperationException("Not Implemented: ArrayGenericClass#hasWildcardOrTypeVariables");
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
        throw new UnsupportedOperationException("Not Implemented: ArrayGenericClass#isGenericArray");
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
}
