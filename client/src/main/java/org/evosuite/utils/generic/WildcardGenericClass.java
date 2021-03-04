package org.evosuite.utils.generic;

import org.evosuite.Properties;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.seeding.CastClassManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.*;
import java.util.stream.Collectors;

public class WildcardGenericClass extends AbstractGenericClass<WildcardType> {
    private static final Logger logger = LoggerFactory.getLogger(WildcardGenericClass.class);

    public WildcardGenericClass(WildcardType type, Class<?> rawClass) {
        super(type, rawClass);
    }

    @Override
    public void changeClassLoader(ClassLoader loader) {
        throw new UnsupportedOperationException("Not Implemented: WildcardGenericClass#changeClassLoader");
    }

    @Override
    public Collection<GenericClass<?>> getGenericBounds() {
        List<Type> types = Arrays.asList(type.getUpperBounds());
        types.addAll(Arrays.asList(type.getLowerBounds()));
        return types.stream().map(GenericClassFactory::get).collect(Collectors.toList());
    }

    @Override
    public GenericClass<?> getGenericInstantiation(Map<TypeVariable<?>, Type> typeMap, int recursionLevel) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: WildcardGenericClass#getGenericInstantiation");
    }

    @Override
    public List<AbstractGenericClass<WildcardType>> getInterfaces() {
        throw new UnsupportedOperationException("Not Implemented: WildcardGenericClass#getInterfaces");
    }

    @Override
    public int getNumParameters() {
        throw new UnsupportedOperationException("Not Implemented: WildcardGenericClass#getNumParameters");
    }

    @Override
    public AbstractGenericClass<WildcardType> getOwnerType() {
        throw new UnsupportedOperationException("Not Implemented: WildcardGenericClass#getOwnerType");
    }

    @Override
    public List<Type> getParameterTypes() {
        return Collections.emptyList();
    }

    @Override
    public Type getRawComponentClass() {
        throw new UnsupportedOperationException("Not Implemented: WildcardGenericClass#getRawComponentClass");
    }

    @Override
    public String getSimpleName() {
        throw new UnsupportedOperationException("Not Implemented: WildcardGenericClass#getSimpleName");
    }

    @Override
    public AbstractGenericClass<WildcardType> getSuperClass() {
        throw new UnsupportedOperationException("Not Implemented: WildcardGenericClass#getSuperClass");
    }

    @Override
    public String getTypeName() {
        throw new UnsupportedOperationException("Not Implemented: WildcardGenericClass#getTypeName");
    }

    @Override
    public Map<TypeVariable<?>, Type> getTypeVariableMap() {
        throw new UnsupportedOperationException("Not Implemented: WildcardGenericClass#getTypeVariableMap");
    }

    @Override
    public List<TypeVariable<?>> getTypeVariables() {
        return Collections.emptyList();
    }

    @Override
    public Class<?> getUnboxedType() {
        throw new UnsupportedOperationException("Not Implemented: WildcardGenericClass#getUnboxedType");
    }

    @Override
    public GenericClass<?> getWithComponentClass(GenericClass<?> componentClass) {
        return new WildcardGenericClass(type, rawClass);
    }

    @Override
    public GenericClass<?> getWithGenericParameterTypes(List<AbstractGenericClass<WildcardType>> parameters) {
        throw new UnsupportedOperationException("Not Implemented: WildcardGenericClass#getWithGenericParameterTypes");
    }

    @Override
    public boolean hasOwnerType() {
        return false;
    }

    @Override
    public boolean hasWildcardOrTypeVariables() {
        throw new UnsupportedOperationException("Not Implemented: WildcardGenericClass#hasWildcardOrTypeVariables");
    }

    @Override
    public boolean hasTypeVariables() {
        return false;
    }

    @Override
    public boolean hasWildcardTypes() {
        return true;
    }

    @Override
    public boolean isGenericArray() {
        throw new UnsupportedOperationException("Not Implemented: WildcardGenericClass#isGenericArray");
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
        return false;
    }

    @Override
    public boolean isWildcardType() {
        return true;
    }

    @Override
    public boolean satisfiesBoundaries(TypeVariable<?> typeVariable, Map<TypeVariable<?>, Type> typeMap) {
        throw new UnsupportedOperationException("Not Implemented: WildcardGenericClass#satisfiesBoundaries");
    }

    @Override
    public boolean satisfiesBoundaries(WildcardType wildcardType, Map<TypeVariable<?>, Type> typeMap) {
        throw new UnsupportedOperationException("Not Implemented: WildcardGenericClass#satisfiesBoundaries");
    }

    @Override
    public GenericClass<?> getGenericWildcardInstantiation(Map<TypeVariable<?>, Type> typeMap, int recursionLevel) throws ConstructionFailedException {
        GenericClass<?> selectedClass = CastClassManager.getInstance().selectCastClass(type,
                recursionLevel < Properties.MAX_GENERIC_DEPTH, typeMap);
        return selectedClass.getGenericInstantiation(typeMap, recursionLevel + 1);
    }

    @Override
    public GenericClass<?> getWithParametersFromSuperclass(GenericClass<?> superClass) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: " + "WildcardGenericClass" +
                "#getWithParametersFromSuperclass");
    }

    @Override
    boolean canBeInstantiatedTo(TypeVariableGenericClass otherType) {
        throw new UnsupportedOperationException("Not Implemented: WildcardGenericClass#canBeInstantiatedTo");
    }

    @Override
    boolean canBeInstantiatedTo(WildcardGenericClass otherType) {
        throw new UnsupportedOperationException("Not Implemented: WildcardGenericClass#canBeInstantiatedTo");
    }

    @Override
    boolean canBeInstantiatedTo(ArrayGenericClass otherType) {
        throw new UnsupportedOperationException("Not Implemented: WildcardGenericClass#canBeInstantiatedTo");
    }

    @Override
    boolean canBeInstantiatedTo(RawClassGenericClass otherType) {
        throw new UnsupportedOperationException("Not Implemented: WildcardGenericClass#canBeInstantiatedTo");
    }

    @Override
    boolean canBeInstantiatedTo(ParameterizedGenericClass otherType) {
        throw new UnsupportedOperationException("Not Implemented: WildcardGenericClass#canBeInstantiatedTo");
    }

    @Override
    GenericClass<?> getWithParametersFromSuperclass(TypeVariableGenericClass otherType) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: " + "WildcardGenericClass" +
                "#getWithParametersFromSuperclass");
    }

    @Override
    GenericClass<?> getWithParametersFromSuperclass(WildcardGenericClass otherType) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: " +
                "WildcardGenericClass#getWithParametersFromSuperclass");
    }

    @Override
    GenericClass<?> getWithParametersFromSuperclass(ArrayGenericClass otherType) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: WildcardGenericClass" +
                "#getWithParametersFromSuperclass");
    }

    @Override
    GenericClass<?> getWithParametersFromSuperclass(RawClassGenericClass otherType) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: " + "WildcardGenericClass" +
                "#getWithParametersFromSuperclass");
    }

    @Override
    GenericClass<?> getWithParametersFromSuperclass(ParameterizedGenericClass otherType) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: " +
                "WildcardGenericClass#getWithParametersFromSuperclass");
    }
}