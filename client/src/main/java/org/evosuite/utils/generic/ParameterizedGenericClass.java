package org.evosuite.utils.generic;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.evosuite.Properties;
import org.evosuite.ga.ConstructionFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public class ParameterizedGenericClass extends AbstractGenericClass<ParameterizedType> {
    private static final Logger logger = LoggerFactory.getLogger(ParameterizedGenericClass.class);

    ParameterizedGenericClass(ParameterizedType type, Class<?> clazz) {
        super(type, clazz);
    }

    @Override
    public boolean changeClassLoader(ClassLoader loader) {
        try {
            if (rawClass != null) {
                // TODO check if this is even possible...
                //      Every generic class should have a raw class?
                rawClass = GenericClassUtils.getClassByFullyQualifiedName(rawClass.getName(), loader);
            }
            if (type != null) {
                GenericClass<?> ownerType = null;
                if (type.getOwnerType() != null) {
                    ownerType = GenericClassFactory.get(type.getOwnerType());
                    ownerType.changeClassLoader(loader);
                }
                List<GenericClass<?>> parameterClasses = new ArrayList<>();
                for (Type parameterType : type.getActualTypeArguments()) {
                    GenericClass<?> parameter = GenericClassFactory.get(parameterType);
                    parameter.changeClassLoader(loader);
                    parameterClasses.add(parameter);
                }
                Type[] parameterTypes = parameterClasses.stream().map(GenericClass::getType).toArray(Type[]::new);
                if (ownerType == null) this.type = TypeUtils.parameterize(rawClass, parameterTypes);
                else this.type = TypeUtils.parameterizeWithOwner(ownerType.getType(), rawClass, parameterTypes);
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
        if (!hasWildcardOrTypeVariables()) {
            return Collections.emptySet();
        }
        return getTypeVariables().stream().map(TypeVariable::getBounds).map(Arrays::asList).flatMap(Collection::stream).map(GenericClassFactory::get).collect(Collectors.toList());
    }

    @Override
    public GenericClass<?> getGenericInstantiation(Map<TypeVariable<?>, Type> typeMap, int recursionLevel) throws ConstructionFailedException {

        logger.debug("Instantiation " + toString() + " with type map " + typeMap);
        // If there are no type variables, create copy
        if (!hasWildcardOrTypeVariables() || recursionLevel > Properties.MAX_GENERIC_DEPTH) {
            logger.debug("Nothing to replace: " + toString() + ", " + isRawClass() + ", " + hasWildcardOrTypeVariables());
            return GenericClassFactory.get(this);
        }

        logger.debug("Is parameterized type");
        return getGenericParameterizedTypeInstantiation(typeMap, recursionLevel);
    }

    @Override
    public GenericClass<?> getOwnerType() {
        return GenericClassFactory.get(type.getOwnerType());
    }

    @Override
    public List<Type> getParameterTypes() {
        return Arrays.asList(type.getActualTypeArguments());
    }

    @Override
    public String getTypeName() {
        return type.toString();
    }

    @Override
    public List<TypeVariable<?>> getTypeVariables() {
        return Arrays.asList(rawClass.getTypeParameters());
    }

    @Override
    public Class<?> getUnboxedType() {
        return rawClass;
    }

    @Override
    public GenericClass<?> getWithComponentClass(GenericClass<?> componentClass) {
        return new ParameterizedGenericClass(type, rawClass);
    }

    @Override
    public GenericClass<?> getWithGenericParameterTypes(List<AbstractGenericClass<ParameterizedType>> parameters) {
        Type[] typeArray = parameters.stream().map(GenericClass::getType).toArray(Type[]::new);
        return GenericClassFactory.get(TypeUtils.parameterizeWithOwner(type.getOwnerType(), rawClass, typeArray));

    }

    @Override
    public boolean hasOwnerType() {
        return type.getOwnerType() != null;
    }

    @Override
    public boolean hasWildcardOrTypeVariables() {
        if (hasWildcardTypes() || hasTypeVariables())
            return true;
        return hasOwnerType() && getOwnerType().hasWildcardOrTypeVariables();
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
        return false;
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
        return false;
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
    public int getNumParameters() {
        return type.getActualTypeArguments().length;
    }

    @Override
    public Map<TypeVariable<?>, Type> getTypeVariableMap() {
        throw new UnsupportedOperationException("Not Implemented: ParameterizedGenericClass#getTypeVariableMap");
    }

    @Override
    public GenericClass<?> getWithParametersFromSuperclass(GenericClass<?> superClass) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: " + "ParameterizedGenericClass" +
                "#getWithParametersFromSuperclass");
    }

    @Override
    protected Map<TypeVariable<?>, Type> computeTypeVariableMapIfTypeVariable() {
        return Collections.emptyMap();
    }

    @Override
    protected Map<TypeVariable<?>, Type> updateInheritedTypeVariables(Map<TypeVariable<?>, Type> typeMap) {
        Map<TypeVariable<?>, Type> result = new HashMap<>(typeMap);
        List<TypeVariable<?>> typeVariables = getTypeVariables();
        List<Type> types = getParameterTypes();
        boolean changed = false;
        for (int i = 0; i < typeVariables.size(); i++) {
            if (types.get(i) != typeVariables.get(i)) {
                typeMap.put(typeVariables.get(i), types.get(i));
                changed = true;
            }
        }
        return changed ? result : typeMap;
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
        throw new UnsupportedOperationException("Not Implemented: " + "ParameterizedGenericClass" +
                "#getWithParametersFromSuperclass");
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
        throw new UnsupportedOperationException("Not Implemented: " + "ParameterizedGenericClass" +
                "#getWithParametersFromSuperclass");
    }

    private GenericClass<?> getGenericParameterizedTypeInstantiation(Map<TypeVariable<?>, Type> typeMap,
                                                                     int recursionLevel) {
        throw new UnsupportedOperationException("Not Implemented: " + "ParameterizedGenericClass" +
                "#getGenericParameterizedTypeInstantiation");
    }
}
