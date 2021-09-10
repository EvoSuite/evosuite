/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.utils.generic;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

public class GenericArrayTypeImpl implements GenericArrayType {

    private final Type componentType;

    public static Class<?> createArrayType(Class<?> componentType) {
        // there's no (clean) other way to create a array class, than creating an instance of it
        return Array.newInstance(componentType, 0).getClass();
    }

    public static Type createArrayType(Type componentType) {
        if (componentType instanceof Class) {
            return createArrayType((Class<?>) componentType);
        } else {
            return new GenericArrayTypeImpl(componentType);
        }
    }

    private GenericArrayTypeImpl(Type componentType) {
        super();
        this.componentType = componentType;
    }

    public Type getGenericComponentType() {
        return componentType;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GenericArrayType))
            return false;
        return componentType.equals(((GenericArrayType) obj).getGenericComponentType());
    }

    @Override
    public int hashCode() {
        return componentType.hashCode() * 7;
    }

    @Override
    public String toString() {
        return componentType + "[]";
    }
}
