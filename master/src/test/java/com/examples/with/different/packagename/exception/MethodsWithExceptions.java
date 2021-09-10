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
package com.examples.with.different.packagename.exception;

import java.io.FileNotFoundException;
import java.sql.SQLException;

/**
 * Created by gordon on 17/03/2016.
 */
public class MethodsWithExceptions {

    public static boolean oneException(int x) throws FileNotFoundException {
        if (x == 5) {
            throw new FileNotFoundException();
        }
        if (x == 6) {
            throw new NullPointerException("somefile");
        }

        return true;
    }

    public static boolean twoExceptions(int x) throws IllegalArgumentException, SQLException {
        if (x == 10)
            throw new IllegalArgumentException();
        else if (x == 42)
            throw new SQLException();
        else
            return true;
    }

    public boolean nonStaticTwoExceptions(int x) throws IllegalArgumentException, SQLException {
        if (x == 10)
            throw new IllegalArgumentException();
        else if (x == 42)
            throw new SQLException();
        else
            return true;
    }
}
