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
package com.examples.with.different.packagename.purity;

public class SpecialInspector extends AbstractInspector {

    private int value;

    public SpecialInspector(int myValue) {
        this.value = myValue;
    }

    public boolean greaterThanZero() {
        return getValue() > 0;
    }

    private int getValue() {
        return value;
    }

    @Override
    public boolean notPureGreaterThanZero() {
        return notPureGetValue() > 0;
    }

    private int notPureGetValue() {
        value++;
        return value;
    }

    public boolean notPureCreationOfObject() {
        SpecialInspector other = new SpecialInspector(this.value);
        return other.greaterThanZero();
    }

    public boolean pureCreationOfObject() {
        EmptyBox box = new EmptyBox();
        return this.getValue() > 0;
    }

    public boolean superPureCall() {
        return super.negateValue(this.value) > 0;
    }

    public boolean superNotPureCall() {
        return super.impureNegateValue(this.value) > 0;
    }
}
