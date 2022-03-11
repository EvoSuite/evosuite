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
package org.evosuite.runtime;

/**
 * This exception is thrown by the EvoSuite framework when a test case uses too many resources.
 * These resources are for example number of threads and number of iterations in loops.
 * This is done to avoid very expensive test cases, although technically it does not represent
 * a bug in the class under test.
 * <p>
 * Created by Andrea Arcuri on 29/03/15.
 */
public class TooManyResourcesException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TooManyResourcesException() {
        super();
    }

    public TooManyResourcesException(String msg) {
        super(msg);
    }
}
