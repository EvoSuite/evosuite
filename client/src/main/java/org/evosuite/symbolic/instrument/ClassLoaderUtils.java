package org.evosuite.symbolic.instrument;

import org.evosuite.TestGenerationContext;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericExecutable;
import org.evosuite.utils.generic.GenericMethod;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static org.evosuite.dse.util.Assertions.check;
import static org.evosuite.dse.util.Assertions.notNull;

public class ClassLoaderUtils {

    private static final Logger logger = LoggerFactory.getLogger(ClassLoaderUtils.class);

    // TODO: should/must they be synchronized?
    private static final Map<String, Class<?>> classCache = new HashMap<>();
    private static final Map<Class<?>, Map<String, GenericExecutable<?, ?>>> executableCache =
            new HashMap<>();
    private static final Map<Class<?>, Map<String, Field>> fieldCache = new HashMap<>();

    /**
     * Asm method descriptor --> Method parameters as Java Reflection classes.
     *
     * Does not include the receiver for
     */
    // Copied from CallVM
    public static Class<?>[] getArgumentClasses(ClassLoader classLoader, String methDesc) {
        Class<?>[] classes;

        Type[] asmTypes = Type.getArgumentTypes(methDesc);
        classes = new Class<?>[asmTypes.length];
        for (int i = 0; i < classes.length; i++)
            classes[i] = getClassForType(classLoader, asmTypes[i]);

        return classes;
    }

    /**
     * Loads class whose type is aType, without initializing it.
     *
     * @param aType
     */
    // Copied from ConcolicInstrumentingClassLoader
    public static Class<?> getClassForType(ClassLoader classLoader, Type aType) {
        switch (aType.getSort()) {
            case Type.BOOLEAN:
                return Boolean.TYPE;
            case Type.BYTE:
                return Byte.TYPE;
            case Type.CHAR:
                return Character.TYPE;
            case Type.DOUBLE:
                return Double.TYPE;
            case Type.FLOAT:
                return Float.TYPE;
            case Type.INT:
                return Integer.TYPE;
            case Type.LONG:
                return Long.TYPE;
            case Type.SHORT:
                return Short.TYPE;
            case Type.VOID:
                return Void.TYPE;
            case Type.ARRAY: {
                Class<?> elementClass = getClassForType(classLoader, aType.getElementType());
                int dimensions = aType.getDimensions();
                int[] lenghts = new int[dimensions];
                Class<?> array_class = Array.newInstance(elementClass, lenghts)
                        .getClass();
                return array_class;

            }
            default:
                return getClassForName(classLoader, aType.getInternalName());

        }
    }

    /**
     * Loads class named className, without initializing it.
     *
     * @param className
     *            either as p/q/MyClass or as p.q.MyClass
     */
    // Copied from ConcolicInstrumentingClassLoader
    public static Class<?> getClassForName(ClassLoader classLoader, String className) {
        notNull(className);

        Class<?> res = null;
        String classNameDot = className.replace('/', '.');
        try {
            res = classLoader.loadClass(classNameDot);
        } catch (ClassNotFoundException cnfe) {
            check(false, cnfe);
        }
        return notNull(res);
    }

    /**
     * Returns the {@code Class} instance for the class with the specified fully qualified name.
     * If no matching {@code Class} definition can be found for the given name {@code null} is
     * returned.
     *
     * @param className the name of the class to reflect
     * @return the corresponding {@code Class} instance for the given name or {@code null} if no
     * definition is found
     */
    public static Class<?> getClazz(final String className) {
        if (classCache.containsKey(className)) {
            return classCache.get(className);
        } else {
            final ClassLoader classLoader =
                    TestGenerationContext.getInstance().getClassLoaderForSUT();
            final Class<?> clazz;
            try {
                clazz = Class.forName(className, false, classLoader);
            } catch (ClassNotFoundException e) {
                logger.error("Unable to reflect unknown class {}", className);
                return null;
            }
            classCache.put(className, clazz);
            return clazz;
        }
    }

    /**
     * Tries to reflect the method or constructor specified by the given owner class and method name
     * + descriptor, and creates a corresponding {@code GenericMethod} or {@code GenericConstructor}
     * object as appropriate. Callers may safely downcast the returned {@code
     * GenericExecutableMember} to a {@code GenericMethod} or {@code GenericConstructor} by checking
     * the concrete subtype via the methods {@link GenericExecutable#isMethod() isMethod()} and
     * {@link GenericExecutable#isConstructor() isConstructor()}. The method returns {@code null} if
     * no matching executable could be found. Throws an {@code IllegalArgumentException} if the
     * method name + descriptor is malformed.
     *
     * @param methodNameDesc method name and descriptor of the executable to reflect. Must not be
     *                       {@code null}
     * @param clazz          the {@code Class} instance representing the owner class of the
     *                       executable. Must not be {@code null}.
     * @return the {@code GenericExecutableMember} object that represents the reflected method or
     * constructor, or {@code null} if no such method or constructor can be found
     */
    public static GenericExecutable<?, ?> getExecutable(final String methodNameDesc,
                                                        final Class<?> clazz) {
        Objects.requireNonNull(methodNameDesc, "method name + descriptor must not be null");
        Objects.requireNonNull(clazz, "class must not be null");

        if (!executableCache.containsKey(clazz)) {
            executableCache.put(clazz, new LinkedHashMap<>());
        }

        if (executableCache.get(clazz).containsKey(methodNameDesc)) {
            return executableCache.get(clazz).get(methodNameDesc);
        } else {
            // methodNameDesc = name + descriptor
            // We have to split it into two parts to work with it. The opening parenthesis
            // indicates the start of the method descriptor. Every legal method name in
            // Java must be at least one character long. Every legal descriptor starts
            // with the opening parenthesis.
            final int descriptorStartIndex = methodNameDesc.indexOf('(');
            if (descriptorStartIndex < 1) {
                throw new IllegalArgumentException("malformed method name or descriptor: " + methodNameDesc);
            }

            final String name = methodNameDesc.substring(0, descriptorStartIndex);
            final String descriptor = methodNameDesc.substring(descriptorStartIndex);

            final ClassLoader classLoader =
                    TestGenerationContext.getInstance().getClassLoaderForSUT();

            // Tries to reflect the argument types.
            final Class<?>[] argumentTypes;
            try {
                argumentTypes = getArgumentClasses(classLoader, descriptor);
            } catch (Throwable t) {
                logger.error("Unable to reflect argument types of method {}", methodNameDesc);
                logger.error("\tCause: {}", t.getMessage());
                return null;
            }

            // Tries to reflect the executable (must be a method or constructor).
            final boolean isConstructor = name.equals("<init>");
            final GenericExecutable<?, ?> executable;
            try {
                executable = isConstructor
                        ? new GenericConstructor(clazz.getDeclaredConstructor(argumentTypes), clazz)
                        : new GenericMethod(clazz.getDeclaredMethod(name, argumentTypes), clazz);
            } catch (NoSuchMethodException e) {
                logger.error("No executable with name {} and arguments {} in {}", name,
                        argumentTypes, clazz);
                return null;
            }
            executableCache.get(clazz).put(methodNameDesc, executable);
            return executable;
        }
    }

    public static Field getField(final String fieldName, final Class<?> clazz) {
        Objects.requireNonNull(fieldName);
        Objects.requireNonNull(clazz);

        if (!fieldCache.containsKey(clazz)) {
            fieldCache.put(clazz, new LinkedHashMap<>());
        }

        if (fieldCache.get(clazz).containsKey(fieldName)) {
            return fieldCache.get(clazz).get(fieldName);
        } else {
            final Field field;
            try {
                field = clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                return null;
            }
            fieldCache.get(clazz).put(fieldName, field);
            return field;
        }
    }
}
