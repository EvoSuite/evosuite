package org.evosuite.utils.generic;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.evosuite.ga.ConstructionFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ParameterizedGenericClass extends AbstractGenericClass<ParameterizedType> {
    private static final Logger logger = LoggerFactory.getLogger(ParameterizedGenericClass.class);

    ParameterizedGenericClass(ParameterizedType type, Class<?> clazz) {
        super(type, clazz);
    }

    @Override
    public void changeClassLoader(ClassLoader loader) {
        throw new UnsupportedOperationException("Not Implemented: ParameterizedGenericClass#changeClassLoader");
    }

    @Override
    public Collection<GenericClass<?>> getGenericBounds() {
        throw new UnsupportedOperationException("Not Implemented: ParameterizedGenericClass#getGenericBounds");
    }

    @Override
    public GenericClass<?> getGenericInstantiation(Map<TypeVariable<?>, Type> typeMap, int recursionLevel) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: ParameterizedGenericClass#getGenericInstantiation");
    }

    @Override
    public List<AbstractGenericClass<ParameterizedType>> getInterfaces() {
        throw new UnsupportedOperationException("Not Implemented: ParameterizedGenericClass#getInterfaces");
    }

    @Override
    public int getNumParameters() {
        throw new UnsupportedOperationException("Not Implemented: ParameterizedGenericClass#getNumParameters");
    }

    @Override
    public AbstractGenericClass<ParameterizedType> getOwnerType() {
        throw new UnsupportedOperationException("Not Implemented: ParameterizedGenericClass#getOwnerType");
    }

    @Override
    public List<Type> getParameterTypes() {
        return Arrays.asList(type.getActualTypeArguments());
    }

    @Override
    public Type getRawComponentClass() {
        throw new UnsupportedOperationException("Not Implemented: ParameterizedGenericClass#getRawComponentClass");
    }

    @Override
    public String getSimpleName() {
        throw new UnsupportedOperationException("Not Implemented: ParameterizedGenericClass#getSimpleName");
    }

    @Override
    public AbstractGenericClass<ParameterizedType> getSuperClass() {
        throw new UnsupportedOperationException("Not Implemented: ParameterizedGenericClass#getSuperClass");
    }

    @Override
    public String getTypeName() {
        throw new UnsupportedOperationException("Not Implemented: ParameterizedGenericClass#getTypeName");
    }

    @Override
    public Map<TypeVariable<?>, Type> getTypeVariableMap() {
        throw new UnsupportedOperationException("Not Implemented: ParameterizedGenericClass#getTypeVariableMap");
    }

    @Override
    public List<TypeVariable<?>> getTypeVariables() {
        return Arrays.asList(rawClass.getTypeParameters());
    }

    @Override
    public Class<?> getUnboxedType() {
        throw new UnsupportedOperationException("Not Implemented: ParameterizedGenericClass#getUnboxedType");
    }

    @Override
    public GenericClass<?> getWithComponentClass(GenericClass<?> componentClass) {
        return new ParameterizedGenericClass(type, rawClass);
    }

    @Override
    public GenericClass<?> getWithGenericParameterTypes(List<AbstractGenericClass<ParameterizedType>> parameters) {
        throw new UnsupportedOperationException("Not Implemented: " +
                "ParameterizedGenericClass#getWithGenericParameterTypes");
    }

    @Override
    public boolean hasOwnerType() {
        return type.getOwnerType() != null;
    }

    @Override
    public boolean hasWildcardOrTypeVariables() {
        throw new UnsupportedOperationException("Not Implemented: " + "ParameterizedGenericClass" +
                "#hasWildcardOrTypeVariables");
    }

    @Override
    public boolean hasTypeVariables() {
        return GenericClassUtils.hasTypeVariables(type);
    }

    @Override
    public boolean hasWildcardTypes() {
        return GenericClassUtils.hasWildcardTypes(type);
    }

    @Override
    public boolean isGenericArray() {
        throw new UnsupportedOperationException("Not Implemented: ParameterizedGenericClass#isGenericArray");
    }

    @Override
    public boolean isParameterizedType() {
        return true;
    }

    @Override
    public boolean isRawClass() {
        return false;
    }

    @Override
    public boolean isTypeVariable() {
        return false;
    }

    @Override
    public boolean isWildcardType() {
        throw new UnsupportedOperationException("Not Implemented: ParameterizedGenericClass#isWildcardType");
    }

    @Override
    public boolean satisfiesBoundaries(TypeVariable<?> typeVariable, Map<TypeVariable<?>, Type> typeMap) {
        throw new UnsupportedOperationException("Not Implemented: ParameterizedGenericClass#satisfiesBoundaries");
    }

    @Override
    public boolean satisfiesBoundaries(WildcardType wildcardType, Map<TypeVariable<?>, Type> typeMap) {
        throw new UnsupportedOperationException("Not Implemented: ParameterizedGenericClass#satisfiesBoundaries");
    }

    @Override
    public GenericClass<?> getGenericWildcardInstantiation(Map<TypeVariable<?>, Type> typeMap, int recursionLevel) throws ConstructionFailedException {
        throw new UnsupportedOperationException("A parameterized has no generic wildcard instantiation");
    }

    @Override
    public GenericClass<?> getWithParametersFromSuperclass(GenericClass<?> superClass) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: " + "ParameterizedGenericClass" +
                "#getWithParametersFromSuperclass");
    }

    @Override
    boolean canBeInstantiatedTo(TypeVariableGenericClass otherType) {
        throw new UnsupportedOperationException("Not Implemented: ParameterizedGenericClass#canBeInstantiatedTo");
    }

    @Override
    boolean canBeInstantiatedTo(WildcardGenericClass otherType) {
        throw new UnsupportedOperationException("Not Implemented: ParameterizedGenericClass#canBeInstantiatedTo");
    }

    @Override
    boolean canBeInstantiatedTo(ArrayGenericClass otherType) {
        throw new UnsupportedOperationException("Not Implemented: ParameterizedGenericClass#canBeInstantiatedTo");
    }

    @Override
    boolean canBeInstantiatedTo(RawClassGenericClass otherType) {
        throw new UnsupportedOperationException("Not Implemented: ParameterizedGenericClass#canBeInstantiatedTo");
    }

    @Override
    boolean canBeInstantiatedTo(ParameterizedGenericClass otherType) {
        throw new UnsupportedOperationException("Not Implemented: ParameterizedGenericClass#canBeInstantiatedTo");
    }

    @Override
    GenericClass<?> getWithParametersFromSuperclass(TypeVariableGenericClass otherType) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: " + "ParameterizedGenericClass" +
                "#getWithParametersFromSuperclass");
    }

    @Override
    GenericClass<?> getWithParametersFromSuperclass(WildcardGenericClass otherType) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: " +
                "ParameterizedGenericClass#getWithParametersFromSuperclass");
    }

    @Override
    GenericClass<?> getWithParametersFromSuperclass(ArrayGenericClass otherType) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: ParameterizedGenericClass" +
                "#getWithParametersFromSuperclass");
    }

    @Override
    GenericClass<?> getWithParametersFromSuperclass(RawClassGenericClass otherType) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: " + "ParameterizedGenericClass" +
                "#getWithParametersFromSuperclass");
    }

    @Override
    GenericClass<?> getWithParametersFromSuperclass(ParameterizedGenericClass otherType) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: " +
                "ParameterizedGenericClass#getWithParametersFromSuperclass");
    }
}
