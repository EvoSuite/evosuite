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
package org.evosuite.symbolic.expr.ref.array;

/**
 * Utils for arrays symbolic variables naming.
 *
 * @author Ignacio Lebrero
 */
public class SymbolicArrayUtil {

    public static final String ARRAY_NAME_CANNOT_BE_NULL = "Array name cannot be null.";
    public static final String ARRAY_NAME_CANNOT_BE_EMPTY = "Array name cannot be empty.";
    public static final String ARRAY_INDEX_CANNOT_BE_LOWER_THAN_0 = "Array index cannot be lower than 0.";
    public static final String ARRAY_VARIABLE_NAME_CONTENT_LITERAL = "content";
    public static final String ARRAY_CONTENT_VARIABLE_NAME_SEPARATOR = "_";
    public static final String ARRAY_CONTENT_VARIABLE_NAME_SEPARATOR_REGEX = "\\_";

    public static String buildArrayContentVariableName(String arrayVariableName, int index) {
        if (arrayVariableName == null) throw new IllegalArgumentException(ARRAY_NAME_CANNOT_BE_NULL);
        if (arrayVariableName.length() == 0) throw new IllegalArgumentException(ARRAY_NAME_CANNOT_BE_EMPTY);
        if (index < 0) throw new IllegalArgumentException(ARRAY_INDEX_CANNOT_BE_LOWER_THAN_0);

        return new StringBuilder()
                .append(arrayVariableName)
                .append(ARRAY_CONTENT_VARIABLE_NAME_SEPARATOR)
                .append(ARRAY_VARIABLE_NAME_CONTENT_LITERAL)
                .append(ARRAY_CONTENT_VARIABLE_NAME_SEPARATOR)
                .append(index)
                .toString();
    }

    public static boolean isArrayContentVariableName(String name) {
        if (name == null) throw new IllegalArgumentException(ARRAY_NAME_CANNOT_BE_NULL);

        String[] nameElements = name.split(ARRAY_CONTENT_VARIABLE_NAME_SEPARATOR_REGEX);

        return nameElements.length == 3
                && nameElements[0].length() > 0
                && ARRAY_VARIABLE_NAME_CONTENT_LITERAL.equals(nameElements[1]);
    }

}
