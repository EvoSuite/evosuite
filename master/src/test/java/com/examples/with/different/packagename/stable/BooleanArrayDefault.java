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
package com.examples.with.different.packagename.stable;

public class BooleanArrayDefault {

    private final Boolean[] booleanArray;

    public BooleanArrayDefault(Boolean[] myBooleanArray) {
        this.booleanArray = myBooleanArray;
    }

    public boolean isEmpty() {
        return this.booleanArray.length == 0;
    }

    public boolean isNull() {
        for (int i = 0; i < booleanArray.length; i++) {
            Boolean f = booleanArray[i];
            if (f != null)
                return false;
        }
        return true;
    }

    public String printArray() {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < booleanArray.length; i++) {
            Boolean f = booleanArray[i];
            String f_str = f.toString();
            b.append(f_str);
        }
        return b.toString();
    }
}
