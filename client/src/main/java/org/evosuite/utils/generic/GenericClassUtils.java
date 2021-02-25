package org.evosuite.utils.generic;

import com.googlecode.gentyref.CaptureType;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.evosuite.TestGenerationContext;
import org.evosuite.utils.ParameterizedTypeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for {@code GenericClassImpl}.
 */
public class GenericClassUtils {

    private final static Logger logger = LoggerFactory.getLogger(GenericClassUtils.class);

    /**
     * Set of wrapper classes
     */
    static final Set<Class<?>> WRAPPER_TYPES = new LinkedHashSet<>(Arrays.asList(Boolean.class,
            Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class,
            Void.class));

    static final List<String> primitiveClasses = Arrays.asList("char", "int", "short", "long", "boolean",
            "float", "double", "byte");

    private GenericClassUtils(){}

    // TODO: Unsere ParameterizedTypeImpl könnte durch die Implementierung von TypeUtils ersetzt
    //  werden
    protected static Type addTypeParameters(Class<?> clazz) {
        if (clazz.isArray()) {
            return GenericArrayTypeImpl.createArrayType(addTypeParameters(clazz.getComponentType()));
        } else if (isMissingTypeParameters(clazz)) {
            final TypeVariable<?>[] vars = clazz.getTypeParameters();

            // Handle nested classes: if clazz is an inner class, then recursively add type
            // parameters to its outer class. This is necessary because an inner class might refer
            // to the type parameters declared by an outer class.
            final boolean isInnerClass = clazz.getDeclaringClass() != null;
            final Type owner = isInnerClass ? addTypeParameters(clazz.getDeclaringClass()) : null;

            return new ParameterizedTypeImpl(clazz, vars, owner);
        } else {
            return clazz;
        }
    }

    /**
     * Computes the raw type (without any generic information) of the given type {@param type}
     *
     * @param type
     * @return
     */
    static Class<?> erase(Type type) { // TODO: TypeUtils könnte Ersatz haben (getRawType)
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        } else if (type instanceof TypeVariable) {
            TypeVariable<?> tv = (TypeVariable<?>) type;
            if (tv.getBounds().length == 0) {
                return Object.class;
            } else {
                // TODO What if more than one bound, ignore all other?
                //  at least search for a non-interface bound?
                return erase(tv.getBounds()[0]); // TODO: Bei mehreren bounds: was soll es sonst
                //  sein?
            }
        } else if (type instanceof GenericArrayType) {
            GenericArrayType aType = (GenericArrayType) type;
            return GenericArrayTypeImpl.createArrayType(erase(aType.getGenericComponentType()));
        } else if (type instanceof CaptureType) {
            CaptureType captureType = (CaptureType) type;
            if (captureType.getUpperBounds().length == 0) {
                return Object.class;
            } else {
                // TODO What if more than one bound, ignore all other?
                return erase(captureType.getUpperBounds()[0]);
            }
        } else {
            throw new RuntimeException("not supported: " + type.getClass());
        }
    }

    /**
     * Either use getClassByDescriptor or getClassByFullyQualifiedName
     */
    @Deprecated
    static Class<?> getClass(String name) throws ClassNotFoundException {
        return getClass(name, TestGenerationContext.getInstance().getClassLoaderForSUT());
    }

    /**
     * Get a class with the class loader for the SUT.
     * <p>
     * Delegates to {@see getClassByDescriptor(String, ClassLoader)}
     *
     * @param descriptor The descriptor of the class.
     * @return Class object of the described type.
     * @throws ClassNotFoundException if the loader can't load the class
     */
    static Class<?> getClassByDescriptor(String descriptor) throws ClassNotFoundException {
        return getClassByDescriptor(descriptor, TestGenerationContext.getInstance().getClassLoaderForSUT());
    }

    /**
     * Loads a class specified by {@param name}
     * <p>
     * Delegates to {@see getClassByFullyQualifiedName(String, ClassLoader)}
     *
     * @param name The fully-qualified name of the class to be loaded.
     * @return Class object of the described type.
     * @throws ClassNotFoundException if the loader can't load the class
     */
    static Class<?> getClassByFullyQualifiedName(String name) throws ClassNotFoundException {
        return getClassByFullyQualifiedName(name, TestGenerationContext.getInstance().getClassLoaderForSUT());
    }

    /**
     * Loads a class described by {@param descriptor}.
     * <p>
     * If the descriptor notates a primitive type, the primitive type is returned.
     *
     * @param descriptor The descriptor of the class.
     * @param loader     The class loader that should load non-primitve types. (Ignored for primitive types)
     * @return Class object of the described type.
     * @throws ClassNotFoundException if loader can't load the class
     */
    static Class<?> getClassByDescriptor(String descriptor, ClassLoader loader) throws ClassNotFoundException {
        if ("V".equals(descriptor)) {
            return void.class;
        } else if ("I".equals(descriptor)) {
            return int.class;
        } else if ("S".equals(descriptor)) {
            return short.class;
        } else if ("J".equals(descriptor)) {
            return long.class;
        } else if ("F".equals(descriptor)) {
            return float.class;
        } else if ("D".equals(descriptor)) {
            return double.class;
        } else if ("Z".equals(descriptor)) {
            return boolean.class;
        } else if ("B".equals(descriptor)) {
            return byte.class;
        } else if (descriptor.startsWith("[")) {
            Class<?> componentType = getClass(descriptor.substring(1), loader);
            Object array = Array.newInstance(componentType, 0);
            return array.getClass();
        } else if ("C".equals(descriptor)) {
            return char.class;
        } else if (descriptor.startsWith("L") && descriptor.endsWith(";")) {
            return loader.loadClass(descriptor.substring(1, descriptor.length() - 1));
        } else {
            throw new IllegalArgumentException(descriptor + " is not a valid java descriptor");
        }
    }

    /**
     * Loads a class specified by {@param name}
     * <p>
     * If name notates a primitive type, the primitive type is returned.
     *
     * @param name   The fully-qualified name of the class to be loaded.
     * @param loader The class loader that should load the class. (Ignored for primitive types)
     * @return Class object of the described type.
     * @throws ClassNotFoundException if the loader can't load the class
     */
    static Class<?> getClassByFullyQualifiedName(String name, ClassLoader loader) throws ClassNotFoundException {
        if (name.equals("void")) return void.class;
        else if (name.equals("int")) return int.class;
        else if (name.equals("short")) return short.class;
        else if (name.equals("long")) return long.class;
        else if (name.equals("float")) return float.class;
        else if (name.equals("double")) return double.class;
        else if (name.equals("boolean")) return boolean.class;
        else if (name.equals("byte")) return byte.class;
        else if (name.equals("char")) return char.class;
        else if (name.startsWith("[")) {
            Class<?> componentType = getClassByFullyQualifiedName(name.substring(1), loader);
            Object array = Array.newInstance(componentType, 0);
            return array.getClass();
        } else if (name.endsWith(".class")) {
            return loader.loadClass(name.substring(0, ".class".length()));
        } else {
            return loader.loadClass(name);
        }
    }

    /**
     * Use either descriptor or fully qualified name to load the class
     */
    @Deprecated
    static Class<?> getClass(String name, ClassLoader loader) throws ClassNotFoundException {
        // TODO we should really use either descriptor, or fully qualified name.
        if (name.equals("void")) {
            return void.class;
        } else if (name.equals("int") || name.equals("I")) {
            return int.class;
        } else if (name.equals("short") || name.equals("S")) {
            return short.class;
        } else if (name.equals("long") || name.equals("J")) {
            return long.class;
        } else if (name.equals("float") || name.equals("F")) {
            return float.class;
        } else if (name.equals("double") || name.equals("D")) {
            return double.class;
        } else if (name.equals("boolean") || name.equals("Z")) {
            return boolean.class;
        } else if (name.equals("byte") || name.equals("B")) {
            return byte.class;
        } else if (name.equals("char") || name.equals("C")) {
            return char.class;
        } else if (name.startsWith("[")) {
            Class<?> componentType = getClass(name.substring(1), loader);
            Object array = Array.newInstance(componentType, 0);
            return array.getClass();
        } else if (name.startsWith("L") && name.endsWith(";")) {
            // FIXME if a class with a name that is also a descriptor of a native type (e.g. "I") outside any package,
            //  this code will break?
            return getClass(name.substring(1, name.length() - 1), loader);
        } else if (name.endsWith(";")) {
            return getClass(name.substring(0, name.length() - 1), loader);
        } else if (name.endsWith(".class")) {
            return getClass(name.substring(0, name.length() - ".class".length()), loader);
        } else {
            return loader.loadClass(name);
        }
    }


    /**
     * Tells whether the type {@code rhsType} (on the right-hand side of an assignment) can be
     * assigned to the type {@code lhsType} (on the left-hand side of an assignment).
     *
     * @param lhsType the type on the left-hand side (target type)
     * @param rhsType the type on the right-hand side (subject type to be assigned to target type) a
     *                {@link java.lang.reflect.Type} object.
     * @return {@code true} if {@code rhsType} is assignable to {@code lhsType}
     */
    public static boolean isAssignable(Type lhsType, Type rhsType) {
        if (rhsType == null || lhsType == null) {
            return false;
        }

        try {
            return TypeUtils.isAssignable(rhsType, lhsType);
        } catch (Throwable e) {
            logger.debug("Found unassignable type: " + e);
            return false;
        }
    }


    /**
     * Checks if {@code type} is a instanceof {@code java.lang.Class}. If so, this method checks if type or an
     * enclosing class has a type parameter.
     * <p>
     * If type is not an instance of java.lang.Class, it is assumed that no type parameter is missing.
     *
     * @param type The type which should be checked.
     * @return Whether at least one missing type parameter was found.
     */
    public static boolean isMissingTypeParameters(Type type) {
        if (type instanceof Class) {
            // Handle nested classes: check if any of the enclosing classes declares a type
            // parameter.
            for (Class<?> clazz = (Class<?>) type; clazz != null; clazz = clazz.getEnclosingClass()) {
                if (clazz.getTypeParameters().length != 0) {
                    return true;
                }
            }

            return false;
        }

        if (type instanceof ParameterizedType || type instanceof GenericArrayType || type instanceof TypeVariable || type instanceof WildcardType) { // TODO what about CaptureType?
            return false;
        }

        // Should not happen unless we have a custom implementation of the Type interface.
        throw new AssertionError("Unexpected type " + type.getClass());
    }

    /**
     * Tells whether {@code subclass} extends or implements the given {@code superclass}.
     *
     * @param superclass the superclass
     * @param subclass   the subclass
     * @return {@code true} if {@code subclass} is a subclass of {@code superclass}
     */
    public static boolean isSubclass(Type superclass, Type subclass) {
        List<Class<?>> superclasses = ClassUtils.getAllSuperclasses((Class<?>) subclass);
        List<Class<?>> interfaces = ClassUtils.getAllInterfaces((Class<?>) subclass);
        return superclasses.contains(superclass) || interfaces.contains(superclass);
    }

    static boolean hasTypeVariables(ParameterizedType parameterType) {
        for (Type t : parameterType.getActualTypeArguments()) {
            if (t instanceof TypeVariable) {
                return true;
            } else if (t instanceof ParameterizedType) {
                if (hasTypeVariables((ParameterizedType) t)) {
                    return true;
                }
            }
        }

        return false;
    }

    static Class<?> getBoxedType(Class<?> rawClass) {
        if (rawClass.isPrimitive()) {
            if (rawClass.equals(int.class)) {
                return Integer.class;
            } else if (rawClass.equals(byte.class)) {
                return Byte.class;
            } else if (rawClass.equals(short.class)) {
                return Short.class;
            } else if (rawClass.equals(long.class)) {
                return Long.class;
            } else if (rawClass.equals(float.class)) {
                return Float.class;
            } else if (rawClass.equals(double.class)) {
                return Double.class;
            } else if (rawClass.equals(char.class)) {
                return Character.class;
            } else if (rawClass.equals(boolean.class)) {
                return Boolean.class;
            } else if (rawClass.equals(void.class)) {
                return Void.class;
            } else {
                throw new RuntimeException("Unknown unboxed type: " + rawClass);
            }
        }
        return rawClass;
    }

}
