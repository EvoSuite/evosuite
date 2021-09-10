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

public class FloatPrimitiveArrayDefault {

    private final float[] floatArray;

    public FloatPrimitiveArrayDefault(float[] myFloatArray) {
        this.floatArray = myFloatArray;
    }

    public boolean isEmpty() {
        return this.floatArray.length == 0;
    }

    public boolean isZero() {
        for (int i = 0; i < floatArray.length; i++) {
            float f = floatArray[i];
            if (f != 0)
                return false;
        }
        return true;
    }

    public String printArray() {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < floatArray.length; i++) {
            float f = floatArray[i];
            String f_str = String.valueOf(f);
            b.append(f_str);
        }
        return b.toString();
    }

    public boolean moreThanTwoAndZero() {
        if (moreThanTwo() && isZero())
            return true;
        else
            return false;
    }

    /*

        public boolean moreThanTwoAndNull() {
            if (moreThanTwo() &&  !isNonNull())
                return true;
            else
                return false;
        }

     */
    public boolean moreThanTwo() {
        if (this.floatArray.length > 2)
            return true;
        else
            return false;
    }
}
