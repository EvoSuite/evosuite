package org.evosuite.utils.generic;

import org.evosuite.Properties;
import org.evosuite.ga.ConstructionFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public class TypeVariableGenericClass extends AbstractGenericClass<TypeVariable<?>> {
    private static final Logger logger = LoggerFactory.getLogger(TypeVariableGenericClass.class);

    public TypeVariableGenericClass(TypeVariable<?> type, Class<?> rawClass) {
        super(type, rawClass);
    }

    @Override
    public boolean changeClassLoader(ClassLoader loader) {
        try {
            if (rawClass != null) {
                rawClass = GenericClassUtils.getClassByFullyQualifiedName(rawClass.getName(), loader);
            }
            if (type != null) {
                for (TypeVariable<?> newVar : rawClass.getTypeParameters()) {
                    if (newVar.getName().equals(type.getName())) {
                        this.type = newVar;
                        break;
                    }
                }
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
        return Arrays.stream(type.getBounds()).map(GenericClassFactory::get).collect(Collectors.toList());
    }

    @Override
    public GenericClass<?> getGenericInstantiation(Map<TypeVariable<?>, Type> typeMap, int recursionLevel) throws ConstructionFailedException {

        logger.debug("Instantiation " + toString() + " with type map " + typeMap);
        if (recursionLevel > Properties.MAX_GENERIC_DEPTH) {
            logger.debug("Nothing to replace: " + toString() + ", " + isRawClass() + ", " + hasWildcardOrTypeVariables());
            return GenericClassFactory.get(this);
        }

        logger.debug("Is type variable ");
        return getGenericTypeVariableInstantiation(typeMap, recursionLevel);
    }

    @Override
    public AbstractGenericClass<TypeVariable<?>> getOwnerType() {
        throw new UnsupportedOperationException("A type variable has no owner type");
    }

    @Override
    public List<Type> getParameterTypes() {
        return Collections.emptyList();
    }

    @Override
    public String getTypeName() {
        throw new UnsupportedOperationException("Not Implemented: TypeVariableGenericClass#getTypeName");
    }

    @Override
    public Map<TypeVariable<?>, Type> getTypeVariableMap() {
        throw new UnsupportedOperationException("Not Implemented: TypeVariableGenericClass#getTypeVariableMap");
    }

    @Override
    public List<TypeVariable<?>> getTypeVariables() {
        return Collections.emptyList();
    }

    @Override
    public Class<?> getUnboxedType() {
        throw new UnsupportedOperationException("Not Implemented: TypeVariableGenericClass#getUnboxedType");
    }

    @Override
    public GenericClass<?> getWithComponentClass(GenericClass<?> componentClass) {
        return new TypeVariableGenericClass(type, rawClass);
    }

    @Override
    public GenericClass<?> getWithGenericParameterTypes(List<AbstractGenericClass<TypeVariable<?>>> parameters) {
        throw new UnsupportedOperationException("Not Implemented: " +
                "TypeVariableGenericClass#getWithGenericParameterTypes");
    }

    @Override
    public boolean hasOwnerType() {
        return false;
    }

    @Override
    public boolean hasWildcardOrTypeVariables() {
        throw new UnsupportedOperationException("Not Implemented: TypeVariableGenericClass#hasWildcardOrTypeVariables");
    }

    @Override
    public boolean hasTypeVariables() {
        return true;
    }

    @Override
    public boolean hasWildcardTypes() {
        return false;
    }

    @Override
    public boolean isGenericArray() {
        throw new UnsupportedOperationException("Not Implemented: TypeVariableGenericClass#isGenericArray");
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
        return true;
    }

    @Override
    public boolean isWildcardType() {
        return false;
    }

    @Override
    public boolean satisfiesBoundaries(TypeVariable<?> typeVariable, Map<TypeVariable<?>, Type> typeMap) {
        throw new UnsupportedOperationException("Not Implemented: TypeVariableGenericClass#satisfiesBoundaries");
    }

    @Override
    public boolean satisfiesBoundaries(WildcardType wildcardType, Map<TypeVariable<?>, Type> typeMap) {
        throw new UnsupportedOperationException("Not Implemented: TypeVariableGenericClass#satisfiesBoundaries");
    }

    @Override
    public GenericClass<?> getGenericWildcardInstantiation(Map<TypeVariable<?>, Type> typeMap, int recursionLevel) throws ConstructionFailedException {
        throw new UnsupportedOperationException("A type variable has no generic wildcard instantiation");
    }

    @Override
    public GenericClass<?> getWithParametersFromSuperclass(GenericClass<?> superClass) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: " + "TypeVariableGenericClass" +
                "#getWithParametersFromSuperclass");
    }

    @Override
    boolean canBeInstantiatedTo(TypeVariableGenericClass otherType) {
        throw new UnsupportedOperationException("Not Implemented: TypeVariableGenericClass#canBeInstantiatedTo");
    }

    @Override
    boolean canBeInstantiatedTo(WildcardGenericClass otherType) {
        throw new UnsupportedOperationException("Not Implemented: TypeVariableGenericClass#canBeInstantiatedTo");
    }

    @Override
    boolean canBeInstantiatedTo(ArrayGenericClass otherType) {
        throw new UnsupportedOperationException("Not Implemented: TypeVariableGenericClass#canBeInstantiatedTo");
    }

    @Override
    boolean canBeInstantiatedTo(RawClassGenericClass otherType) {
        throw new UnsupportedOperationException("Not Implemented: TypeVariableGenericClass#canBeInstantiatedTo");
    }

    @Override
    boolean canBeInstantiatedTo(ParameterizedGenericClass otherType) {
        throw new UnsupportedOperationException("Not Implemented: TypeVariableGenericClass#canBeInstantiatedTo");
    }

    @Override
    GenericClass<?> getWithParametersFromSuperclass(TypeVariableGenericClass otherType) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: " + "TypeVariableGenericClass" +
                "#getWithParametersFromSuperclass");
    }

    @Override
    GenericClass<?> getWithParametersFromSuperclass(WildcardGenericClass otherType) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: " +
                "TypeVariableGenericClass#getWithParametersFromSuperclass");
    }

    @Override
    GenericClass<?> getWithParametersFromSuperclass(ArrayGenericClass otherType) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: TypeVariableGenericClass" +
                "#getWithParametersFromSuperclass");
    }

    @Override
    GenericClass<?> getWithParametersFromSuperclass(RawClassGenericClass otherType) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: " + "TypeVariableGenericClass" +
                "#getWithParametersFromSuperclass");
    }

    @Override
    GenericClass<?> getWithParametersFromSuperclass(ParameterizedGenericClass otherType) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: " +
                "TypeVariableGenericClass#getWithParametersFromSuperclass");
    }

    private GenericClass<?> getGenericTypeVariableInstantiation(Map<TypeVariable<?>, Type> typeMap,
                                                                int recursionLevel) {
        throw new UnsupportedOperationException("Not implemented: " +
                "TypeVariableGenericClass:getGenericTypeVariableInstantiation");
    }
}
