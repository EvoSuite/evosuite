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
package com.examples.with.different.packagename.contracts;

public class EqualsHashCode {

    private int x = 0;

    public void setX(int x) {
        this.x = x;
    }

    @Override
    public int hashCode() {
        return x;
    }

    public boolean equals(Object other) {
        if (other == null)
            return false;

        if (other == this)
            return true;

        if (x == 42)
            return true;

        if (other instanceof EqualsHashCode) {
            return ((EqualsHashCode) other).x == x;
        } else {
            return other.equals(this);
        }
    }
}
