package org.evosuite.utils.generic;

import com.googlecode.gentyref.GenericTypeReflector;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.evosuite.ga.ConstructionFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.evosuite.utils.generic.GenericClassUtils.WRAPPER_TYPES;
import static org.evosuite.utils.generic.GenericClassUtils.primitiveClasses;

public abstract class AbstractGenericClass<T extends Type> implements GenericClass<AbstractGenericClass<T>> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractGenericClass.class);
    protected T type;
    protected Class<?> rawClass;

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
        if (otherType.isTypeVariable()) return canBeInstantiatedTo((TypeVariableGenericClass) otherType);
        else if (otherType.isWildcardType()) return canBeInstantiatedTo((WildcardGenericClass) otherType);
        else if (otherType.isGenericArray()) return canBeInstantiatedTo((ArrayGenericClass) otherType);
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
    public List<GenericClass<?>> getParameterClasses() {
        return getParameterTypes().stream().map(GenericClassFactory::get).collect(Collectors.toList());
    }

    @Override
    public Class<?> getRawClass() {
        return rawClass;
    }

    @Override
    public T getType() {
        return type;
    }

    @Override
    public GenericClass<?> getWithParametersFromSuperclass(GenericClass<?> superClass) throws ConstructionFailedException {
        if (superClass.isTypeVariable()) return getWithParametersFromSuperclass((TypeVariableGenericClass) superClass);
        else if (superClass.isWildcardType()) return getWithParametersFromSuperclass((WildcardGenericClass) superClass);
        else if (superClass.isGenericArray()) return getWithParametersFromSuperclass((ArrayGenericClass) superClass);
        else if (superClass.isRawClass()) return getWithParametersFromSuperclass((RawClassGenericClass) superClass);
        else if (superClass.isParameterizedType())
            return getWithParametersFromSuperclass((ParameterizedGenericClass) superClass);
        else throw new IllegalArgumentException(superClass.getClass().getSimpleName() + " is not supported");
    }

    @Override
    public GenericClass<?> getWithParameterTypes(List<Type> parameters) {
        Type[] typeArray = new Type[parameters.size()];
        return GenericClassFactory.get(TypeUtils.parameterize(rawClass, parameters.toArray(typeArray)));
    }

    @Override
    public GenericClass<?> getWithParameterTypes(Type[] parameters) {
        return GenericClassFactory.get(TypeUtils.parameterize(rawClass, parameters));
    }

    @Override
    public GenericClass<?> getWithWildcardTypes() {
        return GenericClassFactory.get(GenericTypeReflector.addWildcardParameters(rawClass));
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
    public GenericClass<?> getGenericInstantiation() throws ConstructionFailedException {
        return getGenericInstantiation(new HashMap<>());
    }

    @Override
    public GenericClass<?> getGenericInstantiation(Map<TypeVariable<?>, Type> typeMap) throws ConstructionFailedException {
        return getGenericInstantiation(typeMap, 0);
    }

    @Override
    public boolean hasGenericSuperType(GenericClass<?> superType) {
        return hasGenericSuperType(superType.getType());
    }

    @Override
    public boolean hasGenericSuperType(Type superType) {
        return GenericTypeReflector.isSuperType(superType,type);
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
    public boolean isWrapperType() {
        return WRAPPER_TYPES.contains(rawClass);
    }

    @Override
    public boolean satisfiesBoundaries(TypeVariable<?> typeVariable) {
        return satisfiesBoundaries(typeVariable, getTypeVariableMap());
    }

    @Override
    public boolean satisfiesBoundaries(WildcardType wildcardType) {
        return satisfiesBoundaries(wildcardType, getTypeVariableMap());
    }

    @Override
    public GenericClass<?> getRawGenericClass() {
        return new RawClassGenericClass(rawClass);
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
    abstract boolean canBeInstantiatedTo(ArrayGenericClass otherType);

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
     * Fill the parameters of the super class to this generic class:
     * <p>
     * e.g:
     * if
     * this == LinkedList<?> and {@param otherType} == List<Integer>
     * <p>
     * then this function returns:
     * LinkedList<Integer>
     *
     * @param otherType the super class.
     * @return a generic class with the filled in Parameters.
     */
    abstract GenericClass<?> getWithParametersFromSuperclass(TypeVariableGenericClass otherType) throws ConstructionFailedException;

    /**
     * Fill the parameters of the super class to this generic class:
     * <p>
     * e.g:
     * if
     * this == LinkedList<?> and {@param otherType} == List<Integer>
     * <p>
     * then this function returns:
     * LinkedList<Integer>
     *
     * @param otherType the super class.
     * @return a generic class with the filled in Parameters.
     */
    abstract GenericClass<?> getWithParametersFromSuperclass(WildcardGenericClass otherType) throws ConstructionFailedException;

    /**
     * Fill the parameters of the super class to this generic class:
     * <p>
     * e.g:
     * if
     * this == LinkedList<?> and {@param otherType} == List<Integer>
     * <p>
     * then this function returns:
     * LinkedList<Integer>
     *
     * @param otherType the super class.
     * @return a generic class with the filled in Parameters.
     */
    abstract GenericClass<?> getWithParametersFromSuperclass(ArrayGenericClass otherType) throws ConstructionFailedException;

    /**
     * Fill the parameters of the super class to this generic class:
     * <p>
     * e.g:
     * if
     * this == LinkedList<?> and {@param otherType} == List<Integer>
     * <p>
     * then this function returns:
     * LinkedList<Integer>
     *
     * @param otherType the super class.
     * @return a generic class with the filled in Parameters.
     */
    abstract GenericClass<?> getWithParametersFromSuperclass(RawClassGenericClass otherType) throws ConstructionFailedException;

    /**
     * Fill the parameters of the super class to this generic class:
     * <p>
     * e.g:
     * if
     * this == LinkedList<?> and {@param otherType} == List<Integer>
     * <p>
     * then this function returns:
     * LinkedList<Integer>
     *
     * @param otherType the super class.
     * @return a generic class with the filled in Parameters.
     */
    abstract GenericClass<?> getWithParametersFromSuperclass(ParameterizedGenericClass otherType) throws ConstructionFailedException;
}
