package org.evosuite.feature.converters;

import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * This class is used by XStream, so that it can override its default behaviour
 * of not serializing a static field.
 *
 */
public class StaticFieldConverter extends PureJavaReflectionProvider implements Serializable {

    @Override
    protected boolean fieldModifiersSupported(final Field field) {
        final int modifiers = field.getModifiers();
        return !(Modifier.isTransient(modifiers));
    }
}
