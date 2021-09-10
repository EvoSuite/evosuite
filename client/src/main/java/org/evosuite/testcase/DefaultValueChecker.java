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
package org.evosuite.testcase;

import java.util.function.Function;

public enum DefaultValueChecker {
    INTEGER((value) -> value.equals(0)),
    BYTE((value) -> value.equals(0)),
    SHORT((value) -> value.equals(0)),
    LONG((value) -> value.equals(0L)),
    CHAR((value) -> value.equals('\u0000')),
    BOOLEAN((value) -> ((Boolean) value)),
    STRING((value) -> !(value == null)),
    FLOAT((value) -> value.equals(0.0f)),
    DOUBLE((value) -> value.equals(0.0d));

    public static final String UNEXPECTED_VALUE = "Unexpected value: ";

    private final Function<Object, Boolean> checker;

    DefaultValueChecker(final Function<Object, Boolean> statementCheck) {
        this.checker = statementCheck;
    }

    public static boolean isDefaultValue(Object value) {
        if (Integer.class.equals(value.getClass())) {
            return INTEGER.checker.apply(value);
        } else if (Byte.class.equals(value.getClass())) {
            return BYTE.checker.apply(value);
        } else if (Short.class.equals(value.getClass())) {
            return SHORT.checker.apply(value);
        } else if (Long.class.equals(value.getClass())) {
            return LONG.checker.apply(value);
        } else if (Boolean.class.equals(value.getClass())) {
            return BOOLEAN.checker.apply(value);
        } else if (Character.class.equals(value.getClass())) {
            return CHAR.checker.apply(value);
        } else if (String.class.equals(value.getClass())) {
            return STRING.checker.apply(value);
        } else if (Float.class.equals(value.getClass())) {
            return FLOAT.checker.apply(value);
        } else if (Double.class.equals(value.getClass())) {
            return DOUBLE.checker.apply(value);
        }
        throw new IllegalStateException(UNEXPECTED_VALUE + value.getClass());
    }
}
