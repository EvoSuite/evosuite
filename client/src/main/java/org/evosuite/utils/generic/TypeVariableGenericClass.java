package org.evosuite.utils.generic;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.evosuite.Properties;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.seeding.CastClassManager;
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
        return type.toString();
    }

    @Override
    public List<TypeVariable<?>> getTypeVariables() {
        return Collections.emptyList();
    }

    @Override
    public Class<?> getUnboxedType() {;
        return rawClass;
    }

    @Override
    public GenericClass<?> getWithComponentClass(GenericClass<?> componentClass) {
        return new TypeVariableGenericClass(type, rawClass);
    }

    @Override
    public boolean hasOwnerType() {
        return false;
    }

    @Override
    public boolean hasWildcardOrTypeVariables() {
        return true;
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
        return false;
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
    public GenericClass<?> getGenericWildcardInstantiation(Map<TypeVariable<?>, Type> typeMap, int recursionLevel) throws ConstructionFailedException {
        throw new UnsupportedOperationException("A type variable has no generic wildcard instantiation");
    }

    @Override
    protected Map<TypeVariable<?>, Type> computeTypeVariableMapIfTypeVariable() {
        return Arrays.stream(type.getBounds()).map(GenericClassFactory::get).map(GenericClass::getTypeVariableMap).map(Map::entrySet).flatMap(Collection::stream).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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
        Class<?> otherRawClass = otherType.getRawClass();
        if (otherRawClass.isAssignableFrom(rawClass)) {
            Map<TypeVariable<?>, Type> typeMap = otherType.getTypeVariableMap();
            try {
                GenericClass<?> instantiation = getGenericInstantiation(typeMap);
                if (equals(instantiation)) {
                    return !hasWildcardOrTypeVariables();
                }
                return instantiation.canBeInstantiatedTo(otherType);
            } catch (ConstructionFailedException e) {
                logger.debug("Failed to instantiate " + toString());
                return false;
            }
        }
        return false;
    }

    @Override
    boolean canBeInstantiatedTo(GenericArrayGenericClass otherType) {
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
    boolean canBeInstantiatedTo(ParameterizedGenericClass otherType) {
        Class<?> otherRawClass = otherType.getRawClass();
        if (otherRawClass.isAssignableFrom(rawClass)) {
            Map<TypeVariable<?>, Type> typeMap = otherType.getTypeVariableMap();
            if (otherType.isParameterizedType())
                typeMap.putAll(TypeUtils.determineTypeArguments(rawClass, otherType.getType()));
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

    private GenericClass<?> getGenericTypeVariableInstantiation(Map<TypeVariable<?>, Type> typeMap,
                                                                int recursionLevel) throws ConstructionFailedException {
        if (typeMap.containsKey(type)) {
            if (typeMap.get(type) == type) throw new ConstructionFailedException("Type points to itself");
            GenericClass<?> selectedClass = GenericClassFactory.get(typeMap.get(type)).getGenericInstantiation(typeMap,
                    recursionLevel + 1);
            if (selectedClass.satisfiesBoundaries(type))
                return selectedClass;
        }
        GenericClass<?> selectedClass = CastClassManager.getInstance().selectCastClass(type,
                recursionLevel < Properties.MAX_GENERIC_DEPTH, typeMap);

        if (selectedClass == null) throw new ConstructionFailedException("Unable to instantiate " + toString());
        Map<TypeVariable<?>, Type> extendedMap = new HashMap<>(typeMap);
        extendedMap.putAll(getTypeVariableMap());
        for (Type bound : type.getBounds()) {
            GenericClass<?> boundClass = GenericClassFactory.get(bound);
            extendedMap.putAll(boundClass.getTypeVariableMap());
            if (boundClass.isParameterizedType()) {
                Class<?> boundRawClass = boundClass.getRawClass();
                if (boundRawClass.isAssignableFrom(selectedClass.getRawClass())) {
                    Map<TypeVariable<?>, Type> xmap = TypeUtils.determineTypeArguments(selectedClass.getRawClass(),
                            (ParameterizedType) boundClass.getType());
                    extendedMap.putAll(xmap);
                }
            }
        }
        GenericClass<?> instantiation = selectedClass.getGenericInstantiation(extendedMap, recursionLevel + 1);
        typeMap.put(type, instantiation.getType());
        return instantiation;
    }
}
