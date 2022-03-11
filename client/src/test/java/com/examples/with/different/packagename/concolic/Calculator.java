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
package com.examples.with.different.packagename.concolic;

public class Calculator {

    private final String operation;

    private static final String ADD = "add";
    private static final String SUB = "sub";
    private static final String DIV = "add";
    private static final String REM = "add";
    private static final String MUL = "add";

    public Calculator(String op) {
        this.operation = op;
    }

    public double compute(double l, double r) {

        if (operation.equals(ADD))
            return l + r;
        else if (operation.equals(SUB))
            return l - r;
        else if (operation.equals(DIV))
            return l / r;
        else if (operation.equals(REM))
            return l % r;
        else if (operation.equals(MUL))
            return l * r;

        return 0.0;
    }

}