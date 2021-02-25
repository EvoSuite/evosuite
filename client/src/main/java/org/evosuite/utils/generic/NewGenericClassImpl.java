package org.evosuite.utils.generic;

import com.googlecode.gentyref.GenericTypeReflector;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.instrumentation.ReturnValueAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NewGenericClassImpl implements GenericClass<NewGenericClassImpl> {

    private static final Logger logger = LoggerFactory.getLogger(NewGenericClassImpl.class);

    // raw class of the generic class, e.g. the class without the generic information.
    private final Class<?> rawClass;
    // type represented by this generic class.
    private final Type type;

    /**
     * Copy Constructor for any {@code GenericClass} implementation.
     *
     * @param copy the object to be copied.
     */
    NewGenericClassImpl(GenericClass<?> copy) {
        Objects.requireNonNull(copy);
        this.rawClass = copy.getRawClass();
        this.type = copy.getType();
    }

    /**
     * Construct a generic class from a {@code Class} object.
     *
     * @param clazz the class that should be represented by this instance.
     */
    NewGenericClassImpl(Class<?> clazz) {
        Objects.requireNonNull(clazz);
        this.rawClass = clazz;
        type = genericTypeOf(clazz);
    }

    NewGenericClassImpl(Type type) {
        Objects.requireNonNull(type);
        if (type instanceof Class<?>) {
            Class<?> clazz = (Class<?>) type;
            this.rawClass = clazz;
            this.type = genericTypeOf(clazz);
        }
        // TODO what to do if the class is actually a type.
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#<init>");
    }

    /**
     * @param type
     * @param clazz
     */
    @Deprecated
    NewGenericClassImpl(Type type, Class<?> clazz) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(clazz);
        this.type = type;
        this.rawClass = clazz;
    }

    /**
     * Converts a {@code Class} object to a {@code Type} object.
     * <p>
     * If {@param clazz} does not contain generics, it will be returned.
     * Otherwise, a Type object is returned, that contains the generic information
     *
     * @param clazz
     * @return
     */
    static Type genericTypeOf(Class<?> clazz) {
        if (clazz.isArray()) {
            // TODO don't know if this actually works.
            Type arrayComponentType = TypeUtils.getArrayComponentType(clazz);
            return TypeUtils.genericArrayType(arrayComponentType);
        } else if (clazz.getTypeParameters().length > 0) {
            return TypeUtils.parameterize(clazz, clazz.getTypeParameters());
        } else {
            return clazz;
        }
    }

    @Override
    public NewGenericClassImpl self() {
        return this;
    }

    @Override
    public boolean canBeInstantiatedTo(GenericClass<?> otherType) {
        /* I think that this function checks if we can instantiate this type with other type.
         *
         * (Let:
         *      interface A<T extends B> defined the method foo(A<T>)
         *      class C extend A<B>
         * E.g.
         * A<? extends B> first = new C(); // canBeInstantiatedTo checks if this is valid;
         *
         * IsAssignable just checks if two different types can be assigned to each other?
         *
         * E.g.
         * A<? extends B> first = getFirst();
         * C second = new C();
         * first = first.foo(second); // isAssignable checks if this is valid
         *
         * To demonstrate the difference:
         * The first shall be valid, because the wildcard of the first type is allowed to be B.
         * The second shall be invalid, because we don't know the actual type of first and the wildcards may not match.
         */

        // TODO: Understand this. Maybe we can't instantiate a wrapper type to a primitive?
        if(isPrimitive() && otherType.isWrapperType())
            return false;

        // If we can assign this type to the other type we can also instantiate it as such type.
        if(isAssignableTo(otherType))
            return true;

        // If we can not assign it, we still maybe can instantiate it to the type.
        if(!isTypeVariable() && !otherType.isTypeVariable()){
            // None of the types are variables.
            if(otherType.isGenericSuperTypeOf(this))
                /* If the other type is a generic super type of this, we can surely instantiate this type to other type
                 *
                 * E.g: The type C can surely be assigned to A<? extends B>
                 */
                return true;
        }

        // TODO: What happens if just one is a TypeVariable?
        //       Can this actually happen?

        if (otherType.getRawClass().isAssignableFrom(rawClass)){
            // TODO check if raw classes actually match
            throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#canBeInstantiatedTo");
        }
        return false;
    }

    @Override
    public void changeClassLoader(ClassLoader loader) {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#changeClassLoader");
    }

    @Override
    public Class<?> getBoxedType() {
        return GenericClassUtils.getBoxedType(rawClass);
    }

    @Override
    public String getClassName() {
        return rawClass.getName();
    }

    @Override
    public NewGenericClassImpl getComponentClass() {
        // TODO not 100% sure if this actually preserves all functionality.
        return new NewGenericClassImpl(TypeUtils.getArrayComponentType(this.type));
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
    public Collection<NewGenericClassImpl> getGenericBounds() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#getGenericBounds");
    }

    @Override
    public GenericClass<?> getGenericInstantiation() throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#getGenericInstantiation");
    }

    @Override
    public GenericClass<?> getGenericInstantiation(Map<TypeVariable<?>, Type> typeMap) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#getGenericInstantiation");
    }

    @Override
    public GenericClass<?> getGenericInstantiation(Map<TypeVariable<?>, Type> typeMap, int recursionLevel) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#getGenericInstantiation");
    }

    @Override
    public List<NewGenericClassImpl> getInterfaces() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#getInterfaces");
    }

    @Override
    public int getNumParameters() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#getNumParameters");
    }

    @Override
    public NewGenericClassImpl getOwnerType() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#getOwnerType");
    }

    @Override
    public List<Type> getParameterTypes() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#getParameterTypes");
    }

    @Override
    public List<GenericClass<?>> getParameterClasses() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#getParameterClasses");
    }

    @Override
    public Class<?> getRawClass() {
        return rawClass;
    }

    @Override
    public Type getRawComponentClass() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#getRawComponentClass");
    }

    @Override
    public String getSimpleName() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#getSimpleName");
    }

    @Override
    public NewGenericClassImpl getSuperClass() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#getSuperClass");
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String getTypeName() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#getTypeName");
    }

    @Override
    public Map<TypeVariable<?>, Type> getTypeVariableMap() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#getTypeVariableMap");
    }

    @Override
    public List<TypeVariable<?>> getTypeVariables() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#getTypeVariables");
    }

    @Override
    public Class<?> getUnboxedType() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#getUnboxedType");
    }

    @Override
    public GenericClass<?> getWithComponentClass(GenericClass<?> componentClass) {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#getWithComponentClass");
    }

    @Override
    public GenericClass<?> getWithGenericParameterTypes(List<NewGenericClassImpl> parameters) {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#getWithGenericParameterTypes");
    }

    @Override
    public GenericClass<?> getWithParametersFromSuperclass(GenericClass<?> superClass) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#getWithParametersFromSuperclass");
    }

    @Override
    public GenericClass<?> getWithParameterTypes(List<Type> parameters) {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#getWithParameterTypes");
    }

    @Override
    public GenericClass<?> getWithParameterTypes(Type[] parameters) {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#getWithParameterTypes");
    }

    @Override
    public GenericClass<?> getWithWildcardTypes() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#getWithWildcardTypes");
    }

    @Override
    public boolean hasGenericSuperType(GenericClass<?> superType) {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#hasGenericSuperType");
    }

    @Override
    public boolean hasGenericSuperType(Type superType) {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#hasGenericSuperType");
    }

    @Override
    public boolean hasOwnerType() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#hasOwnerType");
    }

    @Override
    public boolean hasTypeVariables() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#hasTypeVariables");
    }

    @Override
    public boolean hasWildcardOrTypeVariables() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#hasWildcardOrTypeVariables");
    }

    @Override
    public boolean hasWildcardTypes() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#hasWildcardTypes");
    }

    @Override
    public boolean isAbstract() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#isAbstract");
    }

    @Override
    public boolean isAnonymous() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#isAnonymous");
    }

    @Override
    public boolean isArray() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#isArray");
    }

    @Override
    public boolean isAssignableFrom(GenericClass<?> rhsType) {
        // TODO when is this function actually called with null?
        //      looks like that should not happen? -> throw Exception and fix it at call side?
        if (rhsType == null)
            // throw new IllegalArgumentException();
            return false;
        return TypeUtils.isAssignable(rhsType.getType(), this.type);
    }

    @Override
    public boolean isAssignableFrom(Type rhsType) {
        // TODO when is this function actually called with null?
        //      looks like that should not happen? -> throw Exception and fix it at call side?
        if(rhsType == null)
            // throw new IllegalArgumentException();
            return false;
        return TypeUtils.isAssignable(rhsType, this.type);
    }

    @Override
    public boolean isAssignableTo(GenericClass<?> lhsType) {
        // TODO when is this function actually called with null?
        //      looks like that should not happen? -> throw Exception and fix it at call side?
        if(lhsType == null)
            // throw new IllegalArgumentException();
            return false;
        return TypeUtils.isAssignable(this.type, lhsType.getType());
    }

    @Override
    public boolean isAssignableTo(Type lhsType) {
        // TODO when is this function actually called with null?
        //      looks like that should not happen? -> throw Exception and fix it at call side?
        if (lhsType == null)
            // throw new IllegalArgumentException();
            return false;
        return TypeUtils.isAssignable(this.type, lhsType);
    }

    @Override
    public boolean isClass() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#isClass");
    }

    @Override
    public boolean isEnum() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#isEnum");
    }

    @Override
    public boolean isGenericArray() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#isGenericArray");
    }

    @Override
    public boolean isGenericSuperTypeOf(GenericClass<?> subType) {
        // TODO search for replacement for GenericTypeReflector#isSuperType.
        return GenericTypeReflector.isSuperType(type, subType.getType());
    }

    @Override
    public boolean isGenericSuperTypeOf(Type subType) {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#isGenericSuperTypeOf");
    }

    @Override
    public boolean isObject() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#isObject");
    }

    @Override
    public boolean isParameterizedType() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#isParameterizedType");
    }

    @Override
    public boolean isPrimitive() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#isPrimitive");
    }

    @Override
    public boolean isRawClass() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#isRawClass");
    }

    @Override
    public boolean isTypeVariable() {
        return type instanceof TypeVariable;
    }

    @Override
    public boolean isWildcardType() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#isWildcardType");
    }

    @Override
    public boolean isString() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#isString");
    }

    @Override
    public boolean isVoid() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#isVoid");
    }

    @Override
    public boolean isWrapperType() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#isWrapperType");
    }

    @Override
    public boolean satisfiesBoundaries(TypeVariable<?> typeVariable) {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#satisfiesBoundaries");
    }

    @Override
    public boolean satisfiesBoundaries(TypeVariable<?> typeVariable, Map<TypeVariable<?>, Type> typeMap) {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#satisfiesBoundaries");
    }

    @Override
    public boolean satisfiesBoundaries(WildcardType wildcardType) {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#satisfiesBoundaries");
    }

    @Override
    public boolean satisfiesBoundaries(WildcardType wildcardType, Map<TypeVariable<?>, Type> typeMap) {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#satisfiesBoundaries");
    }

    @Override
    public NewGenericClassImpl getRawGenericClass() {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#getRawGenericClass");
    }

    @Override
    public GenericClass<?> getGenericWildcardInstantiation(Map<TypeVariable<?>, Type> typeMap, int recursionLevel) throws ConstructionFailedException {
        throw new UnsupportedOperationException("Not Implemented: NewGenericClassImpl#getGenericWildcardInstantiation");
    }
}
