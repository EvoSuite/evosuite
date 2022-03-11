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

public class TypeErasure<E> {

    private final E value;
    private final Object[] arrayOfE;

    public TypeErasure(E value) throws IllegalArgumentException {
        if (value == null)
            throw new IllegalArgumentException();

        if (value instanceof Object[])
            throw new IllegalArgumentException();

        this.value = value;
        this.arrayOfE = new Object[1];
    }


    public E executionCausesClassCastException() {
        return (E) arrayOfE;
    }

    public E executionIsFine() {
        return value;
    }

}
