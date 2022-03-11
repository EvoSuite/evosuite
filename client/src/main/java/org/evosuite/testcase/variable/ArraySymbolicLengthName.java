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
package org.evosuite.testcase.variable;

/**
 * Represents the symbolic name of the symbolic value of an array length.
 *
 * @author Ignacio Lebrero
 */
public class ArraySymbolicLengthName {

    public static final String ARRAY_LENGTH_NAME_SEPARATOR = "_";
    public static final String ARRAY_LENGTH_NAME_SEPARATOR_REGEX = "\\_";
    public static final String ARRAY_LENGTH_SYMBOLIC_NAME_SUFFIX = "length";
    public static final String ARRAY_LENGTH_SYMBOLIC_NAME_INVALID_FOR_NAME_EXCEPTION = "Array length symbolic name invalid for name: ";

    public static final int ARRAY_LENGTH_SYMBOLIC_NAME_SECTIONS_AMOUNT = 3;
    public static final int ARRAY_LENGTH_SYMBOLIC_NAME_DIMENSION_POSITION = 2;
    public static final int ARRAY_LENGTH_SYMBOLIC_NAME_ARRAY_NAME_POSITION = 0;
    public static final int ARRAY_LENGTH_SYMBOLIC_NAME_DIMENSION_TAG_POSITION = 1;

    private final int dimension;

    private final String arrayReferenceName;
    private final String symbolicName;

    public ArraySymbolicLengthName(String symbolicName) {
        if (!isArraySymbolicLengthVariableName(symbolicName)) {
            throw new IllegalArgumentException(ARRAY_LENGTH_SYMBOLIC_NAME_INVALID_FOR_NAME_EXCEPTION + symbolicName);
        }

        String[] symbolicNameSections = symbolicName.split(ARRAY_LENGTH_NAME_SEPARATOR_REGEX);
        this.arrayReferenceName = symbolicNameSections[ARRAY_LENGTH_SYMBOLIC_NAME_ARRAY_NAME_POSITION];
        this.dimension = Integer.parseInt(
                symbolicNameSections[ARRAY_LENGTH_SYMBOLIC_NAME_DIMENSION_POSITION]
        );
        this.symbolicName = symbolicName;
    }

    public ArraySymbolicLengthName(String arrayReferenceName, int dimension) {
        this.arrayReferenceName = arrayReferenceName;
        this.dimension = dimension;
        this.symbolicName = buildSymbolicLengthDimensionName(arrayReferenceName, dimension);
    }

    public int getDimension() {
        return dimension;
    }

    public String getSymbolicName() {
        return symbolicName;
    }

    public String getArrayReferenceName() {
        return arrayReferenceName;
    }

    /**
     * Builds the name of an array length symbolic variable.
     *
     * @param arrayReferenceName
     * @param dimension
     * @return
     */
    public static String buildSymbolicLengthDimensionName(String arrayReferenceName, int dimension) {
        return new StringBuilder()
                .append(arrayReferenceName)
                .append(ARRAY_LENGTH_NAME_SEPARATOR)
                .append(ARRAY_LENGTH_SYMBOLIC_NAME_SUFFIX)
                .append(ARRAY_LENGTH_NAME_SEPARATOR)
                .append(dimension)
                .toString();
    }

    /**
     * Checks whether a symbolic variable name corresponds to an array's length.
     *
     * @param symbolicVariableName
     * @return
     */
    public static boolean isArraySymbolicLengthVariableName(String symbolicVariableName) {
        String[] nameSections = symbolicVariableName.split(ArraySymbolicLengthName.ARRAY_LENGTH_NAME_SEPARATOR_REGEX);

        return nameSections.length == ARRAY_LENGTH_SYMBOLIC_NAME_SECTIONS_AMOUNT
                && nameSections[ARRAY_LENGTH_SYMBOLIC_NAME_DIMENSION_TAG_POSITION].equals(ARRAY_LENGTH_SYMBOLIC_NAME_SUFFIX);
    }
}
