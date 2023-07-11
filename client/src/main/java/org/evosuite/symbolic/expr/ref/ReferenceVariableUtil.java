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
package org.evosuite.symbolic.expr.ref;

/**
 * Utils related to reference variables
 *
 * TODO: Currently there's a similar idea in {@link org.evosuite.testcase.variable.ArraySymbolicLengthName},
 *       it would be a good idea to unify them.
 *
 * @author Ignacio Lebrero
 */
public abstract class ReferenceVariableUtil {

    public static final String SEPARATOR = "_";
    public static final String SEPARATOR_REGEX = "\\_";
    public static final String REFERENCE_VARIABLE_RESERVED_PREFIX_NAME = "reference";

    /**
     * Builds the variable name that refers to a reference variable.
     * It should be of the form "reference_<Variable_name>".
     *
     * @param name
     * @return
     */
    public static String getReferenceVariableName(String name) {
        StringBuilder builder = new StringBuilder();

        builder.append(REFERENCE_VARIABLE_RESERVED_PREFIX_NAME)
               .append(SEPARATOR)
               .append(name);

        return builder.toString();
    }

    /**
     * Checks whether the variable name refers to a reference variable.
     *
     * This should:
     *     - Not be null.
     *     - Be separated by an underscore.
     *     - Have at least two elements (variable names could potentially have more underscores.
     *     - the name should be preceded by the reference variable prefix.
     *
     * @param variableName
     * @return
     */
    public static boolean isReferenceVariableName(String variableName) {
        if (variableName == null) return false;

        String[] splitName = variableName.split(SEPARATOR_REGEX);

        // Valid names contains at least the prefix + some variable name
        if (splitName.length > 1) {
            return splitName[0].equals(REFERENCE_VARIABLE_RESERVED_PREFIX_NAME);
        }

        return false;
    }
}