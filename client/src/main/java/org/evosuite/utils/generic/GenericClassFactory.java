package org.evosuite.utils.generic;

import java.lang.reflect.Type;

/**
 *
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

    private interface IGenericClassFactory<T extends GenericClass<T>> {
        T get(GenericClass<?> copy);

        T get(Class<?> clazz);

        T get(Type type);

        /**
         * Sets the raw class and the type of a generic class directly.
         *
         * Deprecated, because it is not checked if this makes actually makes sense.
         *             Probably its better to just use one of the 2 parameters.
         *
         * @param type The actual generic type
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

    private static class NewGenericClassFactory implements IGenericClassFactory<NewGenericClassImpl> {

        @Override
        public NewGenericClassImpl get(GenericClass<?> copy) {
            return new NewGenericClassImpl(copy);
        }

        @Override
        public NewGenericClassImpl get(Class<?> clazz) {
            return new NewGenericClassImpl(clazz);
        }

        @Override
        public NewGenericClassImpl get(Type type) {
            return new NewGenericClassImpl(type);
        }

        @Override
        public NewGenericClassImpl get(Type type, Class<?> clazz) {
            return new NewGenericClassImpl(type, clazz);
        }
    }
}
