package org.evosuite.symbolic.instrument;

import org.objectweb.asm.Type;

import java.lang.reflect.Array;

import static org.evosuite.dse.util.Assertions.check;
import static org.evosuite.dse.util.Assertions.notNull;

public class ClassLoaderUtils {

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
}
