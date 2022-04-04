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
package org.evosuite.symbolic;

public class ConstraintTypeCounter {

    private final int[] countersByType = new int[8];

    private static final int INTEGER_CONSTRAINT_KEY = 0b001;
    private static final int REAL_CONSTRAINT_KEY = 0b010;
    private static final int STRING_CONSTRAINT_KEY = 0b100;

    public void addNewConstraint(boolean isInteger, boolean isReal,
                                 boolean isString) {
        int key = getKey(isInteger, isReal, isString);
        countersByType[key]++;
    }

    private static int getKey(boolean isIntegerConstraint,
                              boolean isRealConstraint, boolean isStringConstraint) {
        int key = 0;
        if (isIntegerConstraint) {
            key = key | INTEGER_CONSTRAINT_KEY;
        }
        if (isRealConstraint) {
            key = key | REAL_CONSTRAINT_KEY;
        }
        if (isStringConstraint) {
            key = key | STRING_CONSTRAINT_KEY;
        }
        return key;
    }

    public void clear() {
        for (int i = 0; i < countersByType.length; i++) {
            countersByType[i] = 0;
        }
    }

    public int getTotalNumberOfConstraints() {
        int count = 0;
        for (int k : countersByType) {
            count += k;
        }
        return count;
    }

    public int getIntegerOnlyConstraints() {
        int key = getKey(true, false, false);
        return countersByType[key];
    }

    public int getRealOnlyConstraints() {
        int key = getKey(false, true, false);
        return countersByType[key];
    }

    public int getStringOnlyConstraints() {
        int key = getKey(false, false, true);
        return countersByType[key];
    }

    public int getIntegerAndRealConstraints() {
        int key = getKey(true, true, false);
        return countersByType[key];
    }

    public int getIntegerAndStringConstraints() {
        int key = getKey(true, false, true);
        return countersByType[key];
    }

    public int getRealAndStringConstraints() {
        int key = getKey(false, true, true);
        return countersByType[key];
    }

    public int getIntegerRealAndStringConstraints() {
        int key = getKey(true, true, true);
        return countersByType[key];
    }

}
