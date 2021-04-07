package org.evosuite.utils.generic;

import com.googlecode.gentyref.GenericTypeReflector;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.utils.ParameterizedTypeImpl;
import org.evosuite.utils.TypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.evosuite.utils.generic.GenericClassUtils.*;

public abstract class AbstractGenericClass<T extends Type> implements GenericClass<AbstractGenericClass<T>> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractGenericClass.class);
    protected T type;
    protected Class<?> rawClass;
    private Map<TypeVariable<?>, Type> typeVariableMap;

    public AbstractGenericClass(T type, Class<?> rawClass) {
        this.type = type;
        this.rawClass = rawClass;
    }

    @Override
    public AbstractGenericClass<T> self() {
        return this;
    }

    @Override
    public boolean canBeInstantiatedTo(GenericClass<?> otherType) {
        if (isPrimitive() && otherType.isWrapperType()) return false;

        // If this can be assigned to the other type, it can also be instantiated to the other type.
        if (isAssignableTo(otherType)) return true;

        // Delegate to implementations of classes
        if (otherType.isTypeVariable()) return canBeInstantiatedTo((TypeVariableGenericClass) otherType);
        else if (otherType.isWildcardType()) return canBeInstantiatedTo((WildcardGenericClass) otherType);
        else if (otherType.isGenericArray()) return canBeInstantiatedTo((GenericArrayGenericClass) otherType);
        else if (otherType.isRawClass()) return canBeInstantiatedTo((RawClassGenericClass) otherType);
        else if (otherType.isParameterizedType()) return canBeInstantiatedTo((ParameterizedGenericClass) otherType);
        else throw new IllegalArgumentException(otherType.getClass().getSimpleName() + " is not supported");
    }

    @Override
    public Class<?> getBoxedType() {
        return GenericClassUtils.getBoxedType(this.rawClass);
    }

    @Override
    public String getClassName() {
        return rawClass.getName();
    }

    @Override
    public GenericClass<?> getComponentClass() {
        if (!isArray()) throw new IllegalStateException("getComponentClass is called on a non-array type");
        return new RawClassGenericClass(rawClass.getComponentType());
    }

    @Override
    public String getComponentName() {
        return rawClass.getComponentType().getSimpleName();
    }

    @Override
    public Type getComponentType() {
        return TypeUtils.getArrayComponentType(type);
    }

    @Override
    public GenericClass<?> getGenericInstantiation() throws ConstructionFailedException {
        return getGenericInstantiation(new HashMap<>());
    }

    @Override
    public GenericClass<?> getGenericInstantiation(Map<TypeVariable<?>, Type> typeMap) throws ConstructionFailedException {
        return getGenericInstantiation(typeMap, 0);
    }

    @Override
    public List<GenericClass<?>> getInterfaces() {
        return Arrays.stream(rawClass.getInterfaces()).map(GenericClassFactory::get).collect(Collectors.toList());
    }

    @Override
    public int getNumParameters() {
        return 0;
    }

    @Override
    public List<GenericClass<?>> getParameterClasses() {
        return getParameterTypes().stream().map(GenericClassFactory::get).collect(Collectors.toList());
    }

    @Override
    public Class<?> getRawClass() {
        return rawClass;
    }

    @Override
    public Type getRawComponentClass() {
        // TODO: TypeUtils#getComponentType can give us the generic Type of the component class, but not the raw class.
        return GenericTypeReflector.erase(rawClass.getComponentType());
    }

    @Override
    public String getSimpleName() {
        // TODO: No idea what this method is supposed to do???
        //       Looks like it is a special case for arrays???
        final String name = ClassUtils.getShortClassName(rawClass).replace(";", "[]");
        if (!isPrimitive() && primitiveClasses.contains(name)) {
            return rawClass.getSimpleName().replace(";", "[]");
        }

        return name;
    }

    @Override
    public GenericClass<?> getSuperClass() {
        return GenericClassFactory.get(GenericTypeReflector.getExactSuperType(type, rawClass.getSuperclass()));
    }

    @Override
    public T getType() {
        return type;
    }

    @Override
    public Map<TypeVariable<?>, Type> getTypeVariableMap() {
        if (typeVariableMap == null) {
            typeVariableMap = computeTypeVariableMap();
        }
        return Collections.unmodifiableMap(typeVariableMap);
    }

    @Override
    public GenericClass<?> getWithGenericParameterTypes(List<AbstractGenericClass<T>> parameters) {
        Type[] typeArray = parameters.stream().map(GenericClass::getType).toArray(Type[]::new);
        Type ownerType = null;
        if (type instanceof ParameterizedType) {
            ownerType = ((ParameterizedType) type).getOwnerType();
        }
        return GenericClassFactory.get(parameterizeWithOwner(ownerType, rawClass, typeArray));
    }

    @Override
    public GenericClass<?> getWithParametersFromSuperclass(GenericClass<?> superClass) throws ConstructionFailedException {
        return GenericClassFactory.get(type);
    }

    @Override
    public GenericClass<?> getWithParameterTypes(List<Type> parameters) {
        Type[] typeArray = new Type[parameters.size()];
        Type[] typeArguments = parameters.toArray(typeArray);
        Type ownerType = null;
        if (type instanceof ParameterizedType) {
            ownerType = ((ParameterizedType) type).getOwnerType();
        }
        return GenericClassFactory.get(parameterizeWithOwner(ownerType,rawClass, typeArguments));
    }

    @Override
    public GenericClass<?> getWithParameterTypes(Type[] parameters) {
        Type ownerType = null;
        if (type instanceof ParameterizedType) {
            ownerType = ((ParameterizedType) type).getOwnerType();
        }
        return GenericClassFactory.get(parameterizeWithOwner(ownerType, rawClass, parameters));
    }

    @Override
    public GenericClass<?> getWithWildcardTypes() {
        return GenericClassFactory.get(GenericTypeReflector.addWildcardParameters(rawClass));
    }

    @Override
    public boolean hasGenericSuperType(GenericClass<?> superType) {
        return hasGenericSuperType(superType.getType());
    }

    @Override
    public boolean hasGenericSuperType(Type superType) {
        return GenericTypeReflector.isSuperType(superType, type);
    }

    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(rawClass.getModifiers());
    }

    @Override
    public boolean isAnonymous() {
        return rawClass.isAnonymousClass();
    }

    @Override
    public boolean isArray() {
        return rawClass.isArray();
    }

    @Override
    public boolean isAssignableFrom(GenericClass<?> rhsType) {
        return GenericClassUtils.isAssignable(type, rhsType.getType());
    }

    @Override
    public boolean isAssignableFrom(Type rhsType) {
        return GenericClassUtils.isAssignable(type, rhsType);
    }

    @Override
    public boolean isAssignableTo(GenericClass<?> lhsType) {
        return GenericClassUtils.isAssignable(lhsType.getType(), type);
    }

    @Override
    public boolean isAssignableTo(Type lhsType) {
        return GenericClassUtils.isAssignable(lhsType, type);
    }

    @Override
    public boolean isClass() {
        return rawClass.equals(Class.class);
    }

    @Override
    public boolean isEnum() {
        return rawClass.isEnum();
    }

    @Override
    public boolean isGenericSuperTypeOf(GenericClass<?> subType) {
        return isGenericSuperTypeOf(subType.getType());
    }

    @Override
    public boolean isGenericSuperTypeOf(Type subType) {
        return GenericTypeReflector.isSuperType(type, subType);
    }

    @Override
    public boolean isObject() {
        return rawClass.equals(Object.class);
    }

    @Override
    public boolean isPrimitive() {
        return rawClass.isPrimitive();
    }

    @Override
    public boolean isString() {
        return rawClass.equals(String.class);
    }

    @Override
    public boolean isVoid() {
        return rawClass.equals(Void.class) || rawClass.equals(void.class);
    }

    @Override
    public boolean isWrapperType() {
        return WRAPPER_TYPES.contains(rawClass);
    }

    @Override
    public boolean satisfiesBoundaries(TypeVariable<?> typeVariable) {
        return satisfiesBoundaries(typeVariable, getTypeVariableMap());
    }

    @Override
    public boolean satisfiesBoundaries(TypeVariable<?> typeVariable, Map<TypeVariable<?>, Type> typeMap) {
        // Compute the owner variable map, if this generic class can satisfy the type variable.
        Map<TypeVariable<?>, Type> ownerVariableMap = mergeVariableTypeMap(Arrays.asList(typeVariable.getBounds()),
                typeMap);
        ownerVariableMap = GenericClassUtils.resolveTypeVariableRedirects(ownerVariableMap);

        // TODO: check if GenericUtils.replaceTypeVariables can really be replaced by TypeUtils.unrollVariables.
        GenericClass<?> concreteClass = GenericClassFactory.get(TypeUtils.unrollVariables(ownerVariableMap, type));
        Type[] bounds = typeVariable.getBounds();
        for (Type bound : bounds) {
            if (isWildcardType())
                // TODO from reference implementation:
                // TODO i don't know exactly how to handle this case, but it is necessary to prevent an Exception
                return false;
            if (GenericTypeReflector.erase(bound).equals(Enum.class)) {
                // TODO: WTF??? Why should this not be redundant?
                if (isEnum()) continue;
                else return false;
            }
            Type boundType = TypeUtils.unrollVariables(ownerVariableMap, bound);
            boundType = TypeUtils.unrollVariables(Collections.singletonMap(typeVariable, getType()), boundType);
            if (TypeUtils.containsTypeVariables(boundType))
                // TODO replace remaining Type Variables with bounded Wildcards.
                throw new IllegalStateException("TODO: replace type variables with Wildcards here");
            if (!concreteClass.isAssignableTo(boundType) && !(boundType instanceof WildcardType)) {
                if (GenericTypeReflector.erase(boundType).isAssignableFrom(getRawClass())) {
                    Type instanceType = GenericTypeReflector.getExactSuperType(boundType, getRawClass());
                    if (instanceType == null) return false;

                    boundType = TypeUtils.unrollVariables(Collections.singletonMap(typeVariable, instanceType), bound);
                    if (GenericClassUtils.isAssignable(boundType, instanceType)) continue;
                }
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean satisfiesBoundaries(WildcardType wildcardType) {
        return satisfiesBoundaries(wildcardType, getTypeVariableMap());
    }

    @Override
    public boolean satisfiesBoundaries(WildcardType wildcardType, Map<TypeVariable<?>, Type> typeMap) {
        HashMap<TypeVariable<?>, Type> ownerVariableMap = new HashMap<>(getTypeVariableMap());
        ownerVariableMap.putAll(typeMap);
        return satisfiesUpperWildcardBoundaries(wildcardType, ownerVariableMap) &&
                satisfiesLowerWildcardBoundary(wildcardType, ownerVariableMap);
    }

    @Override
    public GenericClass<?> getRawGenericClass() {
        return new RawClassGenericClass(rawClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTypeName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractGenericClass<?> that = (AbstractGenericClass<?>) o;
        return Objects.equals(getTypeName(), that.getTypeName());
    }

    /**
     * Computes a mapping from the type variables of this type to a {@code Type} object.
     * <p>
     * This function also resolves the types of surrounding classes, superclasses and interfaces.
     *
     * @return the mapping.
     */
    protected Map<TypeVariable<?>, Type> computeTypeVariableMap() {
        Map<TypeVariable<?>, Type> typeMap = new HashMap<>();
        try {
            typeMap.putAll(computeTypeVariableMapOfSuperClass());
            typeMap.putAll(computeTypeVariableMapOfInterfaces());
            typeMap.putAll(computeTypeVariableMapIfTypeVariable());
        } catch (Exception e) {
            logger.debug("Exception while getting type map: " + e);
        }
        return updateInheritedTypeVariables(typeMap);
    }

    /**
     * Computes the type variable map of the super class of this generic type.
     * <p>
     * Only if the following 4 conditions are met, the class "sees" the type variables of it's super type.
     * - Super class of the raw class must exist.
     * - The raw class mustn't be an anonymous class.
     * - The super class of the raw class mustn't be an anonymous class.
     * - If this type has an owner type (e.g. outer class), the owner type mustn't be an anonymous class.
     *
     * @return the type variable map of the super class if this class sees these type variables, else an empty map.
     */
    protected Map<TypeVariable<?>, Type> computeTypeVariableMapOfSuperClass() {
        // TODO: Why do we need this 4 conditions. Are anonymous classes always independent of surrounding type
        //  variables?
        if (rawClass.getSuperclass() != null && !rawClass.isAnonymousClass() && !rawClass.getSuperclass().isAnonymousClass() && !(hasOwnerType() && getOwnerType().getRawClass().isAnonymousClass())) {
            return getSuperClass().getTypeVariableMap();
        }
        return Collections.emptyMap();
    }

    /**
     * Computes the type variable map of all interfaces of this generic type and merges them into one map.
     *
     * @return the merged map.
     */
    protected Map<TypeVariable<?>, Type> computeTypeVariableMapOfInterfaces() {
        return Arrays.stream(rawClass.getInterfaces()).map(GenericClassFactory::get).map(GenericClass::getTypeVariableMap).map(Map::entrySet).flatMap(Collection::stream) // merges the List of EntrySets to one stream of entries.
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Computes the type variable map of the boundaries, if this generic class is a type variable.
     *
     * @return The merged map of the type variables, if this generic class is a type variable, else an empty map.
     */
    protected abstract Map<TypeVariable<?>, Type> computeTypeVariableMapIfTypeVariable();

    /**
     * Update the inherited type variables, if this generic class adds constraints to the type variables.
     *
     * @param typeMap the inherited type variables.
     * @return an updated copy of the Map, if any changes were made, else the Map itself.
     */
    protected abstract Map<TypeVariable<?>, Type> updateInheritedTypeVariables(Map<TypeVariable<?>, Type> typeMap);

    /**
     * Check whether the represented generic class can be instantiated to {@param otherType}
     *
     * @param otherType the type as which the generic class should be instantiated.
     * @return whether this generic class can be instantiated as otherType.
     */
    abstract boolean canBeInstantiatedTo(TypeVariableGenericClass otherType);

    /**
     * Check whether the represented generic class can be instantiated to {@param otherType}
     *
     * @param otherType the type as which the generic class should be instantiated.
     * @return whether this generic class can be instantiated as otherType.
     */
    abstract boolean canBeInstantiatedTo(WildcardGenericClass otherType);

    /**
     * Check whether the represented generic class can be instantiated to {@param otherType}
     *
     * @param otherType the type as which the generic class should be instantiated.
     * @return whether this generic class can be instantiated as otherType.
     */
    abstract boolean canBeInstantiatedTo(GenericArrayGenericClass otherType);

    /**
     * Check whether the represented generic class can be instantiated to {@param otherType}
     *
     * @param otherType the type as which the generic class should be instantiated.
     * @return whether this generic class can be instantiated as otherType.
     */
    abstract boolean canBeInstantiatedTo(RawClassGenericClass otherType);

    /**
     * Check whether the represented generic class can be instantiated to {@param otherType}
     *
     * @param otherType the type as which the generic class should be instantiated.
     * @return whether this generic class can be instantiated as otherType.
     */
    abstract boolean canBeInstantiatedTo(ParameterizedGenericClass otherType);

    /**
     * Merge the type variable map with a collection of boundaries and enforce some type variables to be a given type.
     * <p>
     * Only if the boundary is a parameterized type, the type variable map of this generic class is affected, since
     * only parameterized types can fix type variables.
     *
     * @param bounds  the collection of boundaries.
     * @param typeMap Enforce type variables to be of a given type.
     * @return the merged type variable map.
     */
    Map<TypeVariable<?>, Type> mergeVariableTypeMap(Collection<Type> bounds, Map<TypeVariable<?>, Type> typeMap) {
        Map<TypeVariable<?>, Type> ownerTypeVariableMap = new HashMap<>(getTypeVariableMap());
        bounds.stream().filter(b -> b instanceof ParameterizedType).forEachOrdered(b -> {
            Class<?> boundClass = GenericTypeReflector.erase(b);
            if (boundClass.isAssignableFrom(rawClass)) {
                Map<TypeVariable<?>, Type> typeArguments = TypeUtils.determineTypeArguments(rawClass,
                        (ParameterizedType) b);
                ownerTypeVariableMap.putAll(typeArguments);
            } else {
                // TODO why is this else case not handled???
                throw new IllegalStateException("I feel like this else-case should be handled");
            }
        });
        ownerTypeVariableMap.putAll(typeMap);
        return ownerTypeVariableMap;
    }

    /**
     * Checks if this generic class satisfies the upper boundaries of a wildcard type.
     *
     * The owner's variable map may be altered to fix type variables for this function call.
     *
     * @param wildcardType the wildcard type, this class should be checked against.
     * @param ownerVariableMap the variable map of the owner.
     * @return whether the upper boundaries are satisfied.
     */
    boolean satisfiesUpperWildcardBoundaries(WildcardType wildcardType, Map<TypeVariable<?>, Type> ownerVariableMap) {
        for (Type upperBound : wildcardType.getUpperBounds()) {
            if (GenericTypeReflector.erase(upperBound).equals(Enum.class)) {
                if (isEnum()) continue;
                else return false;
            }
            // TODO: check if GenericUtils.replaceTypeVariables can really be replaced by TypeUtils.unrollVariables.
            Type type = TypeUtils.unrollVariables(ownerVariableMap, upperBound);
            if (!isAssignableTo(type)) {
                if (GenericTypeReflector.erase(type).isAssignableFrom(getRawClass())) {
                    Type instanceType = GenericTypeReflector.getExactSuperType(type, getRawClass());
                    if (instanceType == null) return false;
                    if (GenericClassUtils.isAssignable(type, instanceType)) continue;
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if this generic class satisfies the lower boundaries of a wildcard type.
     *
     * The owner's variable map may be altered to fix type variables for this function call.
     *
     * @param wildcardType the wildcard type, this class should be checked against.
     * @param ownerVariableMap the variable map of the owner.
     * @return whether the upper boundaries are satisfied.
     */
    boolean satisfiesLowerWildcardBoundary(WildcardType wildcardType, Map<TypeVariable<?>, Type> ownerVariableMap){
        Type[] lowerBounds = wildcardType.getLowerBounds();
        if(lowerBounds == null) return true;
        for(Type lowerBound : lowerBounds){
            Type type = TypeUtils.unrollVariables(ownerVariableMap, lowerBound);
            if(!isAssignableFrom(type)){
                if (type instanceof WildcardType)
                    continue;
                if (GenericTypeReflector.erase(type).isAssignableFrom(getRawClass())){
                    Type instanceType = GenericTypeReflector.getExactSuperType(type, getRawClass());
                    if(instanceType == null) return false;
                    if(GenericClassUtils.isAssignable(type,instanceType)) continue;
                }
                return false;
            }
        }
        return true;
    }

    @Override
    public GenericClass<?> setType(Type type) {
        if(type instanceof ParameterizedType) return new ParameterizedGenericClass((ParameterizedType) type, rawClass);
        if(type instanceof TypeVariable) return new TypeVariableGenericClass((TypeVariable<?>) type, rawClass);
        if(type instanceof GenericArrayType) return new GenericArrayGenericClass((GenericArrayType) type, rawClass);
        if(type instanceof WildcardType) return new WildcardGenericClass((WildcardType) type, rawClass);
        if(type instanceof Class) return new RawClassGenericClass((Class<?>) type);
        throw new IllegalArgumentException("Unsupported generic type: " + type.toString());
    }


    static ParameterizedType parameterizeWithOwner(final Type owner, final Class<?> rawClass,
          final Type... typeArguments){
        return new ParameterizedTypeImpl(rawClass, typeArguments, owner);
        // TODO why does Type Utils not work?
//        return TypeUtils.parameterizeWithOwner(owner, rawClass, typeArguments);
    }
}
