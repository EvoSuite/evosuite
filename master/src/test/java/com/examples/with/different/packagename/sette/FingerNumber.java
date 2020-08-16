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
package com.examples.with.different.packagename.sette;


/**
 * Simple class implementing the Number interface. This class can only represent between 1 to 10.
 */
public final class FingerNumber extends Number {
    private static final long serialVersionUID = 4280286901518300224L;
    private final int value;

    public FingerNumber(int v) {
        if (0 > v || v > 10) {
            throw new IllegalArgumentException();
        }
        value = v;
    }

    public FingerNumber add(FingerNumber o) {
        int r = value + o.value;
        if (r > 10) {
            throw new RuntimeException("Out of range");
        }
        return new FingerNumber(r);
    }

    @Override
    public int intValue() {
        return value;
    }

    @Override
    public long longValue() {
        return value;
    }

    @Override
    public float floatValue() {
        return value;
    }

    @Override
    public double doubleValue() {
        return value;
    }
}
