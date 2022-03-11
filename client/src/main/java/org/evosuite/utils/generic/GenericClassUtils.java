/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.utils.generic;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.List;

/**
 * Utility class for {@code GenericClassImpl}.
 */
public class GenericClassUtils {

    private final static Logger logger = LoggerFactory.getLogger(GenericClassUtils.class);

    private GenericClassUtils() {
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
        if (rhsType == null || lhsType == null) return false;

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

}
