package org.evosuite.utils.generic;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.evosuite.Properties;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.utils.ParameterizedTypeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
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
    public boolean hasOwnerType() {
        return type.getOwnerType() != null;
    }

    @Override
    public boolean hasWildcardOrTypeVariables() {
        if (hasWildcardTypes() || hasTypeVariables()) return true;
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
    public GenericClass<?> getGenericWildcardInstantiation(Map<TypeVariable<?>, Type> typeMap, int recursionLevel) throws ConstructionFailedException {
        throw new UnsupportedOperationException("A parameterized has no generic wildcard instantiation");
    }

    @Override
    public int getNumParameters() {
        return type.getActualTypeArguments().length;
    }

    @Override
    public GenericClass<?> getWithGenericParameterTypes(List<AbstractGenericClass<ParameterizedType>> parameters) {
        Type[] typeArray = parameters.stream().map(GenericClass::getType).toArray(Type[]::new);
        return GenericClassFactory.get(TypeUtils.parameterizeWithOwner(type.getOwnerType(), rawClass, typeArray));

    }

    @Override
    public GenericClass<?> getWithParametersFromSuperclass(GenericClass<?> superClass) throws ConstructionFailedException {
        GenericClass<?> exactClass = GenericClassFactory.get(type);
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
            exactClass= exactClass.setType(TypeUtils.parameterizeWithOwner(type.getOwnerType(), currentClass, parameterTypes));
        } else {
            Type ownerType = type.getOwnerType();
            Map<TypeVariable<?>, Type> superTypeMap = superClass.getTypeVariableMap();
            Type[] origArguments = type.getActualTypeArguments();
            Type[] arguments = new Type[origArguments.length];
            System.arraycopy(origArguments, 0, arguments, 0, origArguments.length);

            List<TypeVariable<?>> variables = getTypeVariables();
            for (int i = 0; i < arguments.length; i++) {
                TypeVariable<?> var = variables.get(i);
                if (superTypeMap.containsKey(var)) {
                    arguments[i] = superTypeMap.get(var);
                } else if (arguments[i] instanceof WildcardType && i < parameterTypes.length) {
                    if (!TypeUtils.isAssignable(parameterTypes[i], arguments[i])) {
                        return null;
                    } else {
                        final int x = i;
                        boolean assignable =
                                Arrays.stream(variables.get(i).getBounds())
                                        .anyMatch(bound -> TypeUtils.isAssignable(parameterTypes[x], bound));
                        if (!assignable)
                            return null;
                    }
                    arguments[i] = parameterTypes[i];
                }
            }
            GenericClass<?> ownerClass = GenericClassFactory.get(ownerType).getWithParametersFromSuperclass(superClass);
            if (ownerClass == null) return null;
            exactClass = exactClass.setType(TypeUtils.parameterizeWithOwner(ownerType, currentClass, arguments));
        }

        return exactClass;
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
        Class<?> otherRawClass = otherType.getRawClass();
        if (otherRawClass.isAssignableFrom(rawClass)) {
            Map<TypeVariable<?>, Type> typeMap = otherType.getTypeVariableMap();
            try {
                GenericClass<?> instantiation = getGenericInstantiation(typeMap);
                return equals(instantiation) ? !hasWildcardOrTypeVariables() :
                        instantiation.canBeInstantiatedTo(otherType);
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
            //return false;
            throw new IllegalStateException("GentryRef threw an exception", e);
        }

        Class<?> otherRawClass = otherType.getRawClass();
        if (otherRawClass.isAssignableFrom(rawClass)) {
            Map<TypeVariable<?>, Type> typeMap = otherType.getTypeVariableMap();
            try {
                GenericClass<?> instantiation = getGenericInstantiation(typeMap);
                return equals(instantiation) ? !hasWildcardOrTypeVariables() :
                        instantiation.canBeInstantiatedTo(otherType);
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
            //return false;
            throw new IllegalStateException("GentryRef threw an exception", e);
        }

        Class<?> otherRawClass = otherType.getRawClass();
        if (otherRawClass.isAssignableFrom(rawClass)) {
            Map<TypeVariable<?>, Type> typeMap = otherType.getTypeVariableMap();
            try {
                GenericClass<?> instantiation = getGenericInstantiation(typeMap);
                return equals(instantiation) ? !hasWildcardOrTypeVariables() :
                        instantiation.canBeInstantiatedTo(otherType);
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
            Map<TypeVariable<?>, Type> typeMap = otherType.getTypeVariableMap();
            try {
                GenericClass<?> instantiation = getGenericInstantiation(typeMap);
                return equals(instantiation) ? !hasWildcardOrTypeVariables() :
                        instantiation.canBeInstantiatedTo(otherType);
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
            Map<TypeVariable<?>, Type> typeMap = new HashMap<>(otherType.getTypeVariableMap());
            if (otherType.isParameterizedType())
                typeMap.putAll(TypeUtils.determineTypeArguments(rawClass, otherType.getType()));
            try {
                GenericClass<?> instantiation = getGenericInstantiation(typeMap);
                return equals(instantiation) ? !hasWildcardOrTypeVariables() :
                        instantiation.canBeInstantiatedTo(otherType);
            } catch (ConstructionFailedException e) {
                logger.debug("Failed to instantiate " + toString());
                return false;
            }
        }
        return false;
    }

    private GenericClass<?> getGenericParameterizedTypeInstantiation(Map<TypeVariable<?>, Type> typeMap,
                                                                     int recursionLevel) throws ConstructionFailedException {
        List<TypeVariable<?>> typeParameters = getTypeVariables();

        Type[] parameterTypes = new Type[typeParameters.size()];
        Type ownerType = null;

        int numParam = 0;

        for (GenericClass<?> parameterClass : getParameterClasses()) {
            /*
             * If the parameter is a parameterized type variable such as T extends Map<String, K extends Number>
             * then the boundaries of the parameters of the type variable need to be respected
             */
            if (!parameterClass.hasWildcardOrTypeVariables()) {
                parameterTypes[numParam++] = parameterClass.getType();
            } else {
                Map<TypeVariable<?>, Type> extendedMap = new HashMap<>(typeMap);
                extendedMap.putAll(parameterClass.getTypeVariableMap());
                if (!extendedMap.containsKey(typeParameters.get(numParam)) && !parameterClass.isTypeVariable())
                    extendedMap.put(typeParameters.get(numParam), parameterClass.getType());

                if (parameterClass.isWildcardType()) {
                    GenericClass<?> parameterInstance = parameterClass.getGenericWildcardInstantiation(extendedMap,
                            recursionLevel + 1);
                    if (!parameterInstance.satisfiesBoundaries(typeParameters.get(numParam))) {
                        throw new ConstructionFailedException("Invalid generic instance");
                    }
                    parameterTypes[numParam++] = parameterInstance.getType();
                } else {
                    GenericClass<?> parameterInstance = parameterClass.getGenericInstantiation(extendedMap,
                            recursionLevel + 1);
                    parameterTypes[numParam++] = parameterInstance.getType();
                }
            }
        }

        if (hasOwnerType()) {
            GenericClass<?> ownerClass = getOwnerType().getGenericInstantiation(typeMap, recursionLevel);
            ownerType = ownerClass.getType();
        }

        return GenericClassFactory.get(TypeUtils.parameterizeWithOwner(ownerType, rawClass, parameterTypes));
    }
}
