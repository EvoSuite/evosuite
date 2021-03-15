package org.evosuite.utils.generic;

import org.apache.commons.lang3.reflect.TypeUtils;

import java.lang.reflect.*;
import java.util.Objects;

/**
 * Interfaces to construct a {@code GenericClass}
 */
public class GenericClassFactory {

    private final static boolean USE_NEW_GENERIC_CLASS_IMPLEMENTATION = true;
    private final static IGenericClassFactory<?> factory;

    static {
        factory = USE_NEW_GENERIC_CLASS_IMPLEMENTATION ? new NewGenericClassFactory() : new OldGenericClassFactory();
    }

    public static GenericClass<?> get(GenericClass<?> copy) {
        return factory.get(copy);
    }

    public static GenericClass<?> get(Class<?> clazz) {
        return factory.get(clazz);
    }

    public static GenericClass<?> get(Type type) {
        return factory.get(type);
    }

    public static GenericClass<?> get(Type type, Class<?> clazz) {
        return factory.get(type, clazz);
    }

    private interface IGenericClassFactory<T extends GenericClass<?>> {
        T get(GenericClass<?> copy);

        T get(Class<?> clazz);

        T get(Type type);

        /**
         * Sets the raw class and the type of a generic class directly.
         * <p>
         * Deprecated, because it is not checked if this makes actually makes sense.
         * Probably its better to just use one of the 2 parameters.
         *
         * @param type  The actual generic type
         * @param clazz The raw class of the generic class
         * @return the constructed generic class
         */
        @Deprecated
        T get(Type type, Class<?> clazz);
    }

    private static class OldGenericClassFactory implements IGenericClassFactory<GenericClassImpl> {
        @Override
        public GenericClassImpl get(GenericClass<?> copy) {
            return new GenericClassImpl(copy);
        }

        @Override
        public GenericClassImpl get(Class<?> clazz) {
            return new GenericClassImpl(clazz);
        }

        @Override
        public GenericClassImpl get(Type type) {
            return new GenericClassImpl(type);
        }

        @Override
        public GenericClassImpl get(Type type, Class<?> clazz) {
            return new GenericClassImpl(type, clazz);
        }
    }

    private static class NewGenericClassFactory implements IGenericClassFactory<AbstractGenericClass<?>> {

        @Override
        public AbstractGenericClass<?> get(GenericClass<?> copy) {
            Objects.requireNonNull(copy);
            return setDirectly(copy.getType(), copy.getRawClass());
        }

        @Override
        public AbstractGenericClass<?> get(Class<?> clazz) {
            Objects.requireNonNull(clazz);
            return setDirectly(GenericClassUtils.addTypeParameters(clazz), clazz);
        }

        @Override
        public AbstractGenericClass<?> get(Type type) {
            Objects.requireNonNull(type);
            Type _type;
            Class<?> rawClass;
            if (type instanceof Class<?>) {
                rawClass = (Class<?>) type;
                _type = genericTypeOf(rawClass);
            } else {
                _type = type;
                rawClass = GenericClassUtils.getRawClass(type);
            }
            return setDirectly(_type, rawClass);
        }

        /**
         * Converts a {@code Class} object to a {@code Type} object.
         * <p>
         * If {@param clazz} does not contain generics, it will be returned.
         * Otherwise, a Type object is returned, that contains the generic information.
         *
         * @param clazz the raw class object.
         * @return the type containing generic information if present.
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
        public AbstractGenericClass<?> get(Type type, Class<?> clazz) {
            Objects.requireNonNull(type);
            Objects.requireNonNull(clazz);
            return setDirectly(type, clazz);
        }

        private AbstractGenericClass<?> setDirectly(Type type, Class<?> rawClass) {
            if (type instanceof ParameterizedType)
                return new ParameterizedGenericClass((ParameterizedType) type, rawClass);
            else if (type instanceof WildcardType) return new WildcardGenericClass((WildcardType) type, rawClass);
            else if (type instanceof TypeVariable)
                return new TypeVariableGenericClass((TypeVariable<?>) type, rawClass);
            else if (type instanceof Class) return new RawClassGenericClass((Class<?>) type);
            else if (type instanceof GenericArrayType)
                return new GenericArrayGenericClass((GenericArrayType) type, rawClass);
            else throw new IllegalArgumentException("Can't create Generic Class for type " + type);
        }
    }
}
