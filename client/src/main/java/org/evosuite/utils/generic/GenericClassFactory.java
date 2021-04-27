package org.evosuite.utils.generic;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.evosuite.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.Objects;

/**
 * Interfaces to construct a {@code GenericClass}
 */
public class GenericClassFactory {

    private final static IGenericClassFactory<?> factory;
    private final static Logger logger = LoggerFactory.getLogger(GenericClassFactory.class);

    static {
        factory = new OldGenericClassFactory();
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
         * Deprecated, because it is not checked if this actually makes sense.
         * Probably its better to just use one of the 2 parameters.
         *
         * @param type  The actual generic type
         * @param clazz The raw class of the generic class
         * @return the constructed generic class
         */
        @Deprecated
        T get(Type type, Class<?> clazz);
    }

    static class OldGenericClassFactory implements IGenericClassFactory<GenericClassImpl> {
        @Override
        public GenericClassImpl get(GenericClass<?> copy) {
            return new GenericClassImpl((GenericClassImpl) copy);
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
}
