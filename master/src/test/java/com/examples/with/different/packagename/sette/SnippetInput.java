/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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

/*
 * SETTE - Symbolic Execution based Test Tool Evaluator
 *
 * SETTE is a tool to help the evaluation and comparison of symbolic execution
 * based test input generator tools.
 *
 * Budapest University of Technology and Economics (BME)
 *
 * Authors: Lajos Cseppentő <lajos.cseppento@inf.mit.bme.hu>, Zoltán Micskei
 * <micskeiz@mit.bme.hu>
 *
 * Copyright 2014
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/**
 * Holds input parameters and the type of the produced exception for a code
 * snippet. This class is immutable, thus the array of parameters and the type
 * of the exception cannot be modified. However, this class only returns a
 * shallow copy of the array, so the attributes of the parameters and the
 * exception can be modified, but it should be avoided.
 */
public final class SnippetInput {
    /** Array of input parameters. */
    private final Object[] params;
    /**
     * Excepted exception which is thrown when executing the snippet with the
     * specified parameters. It should be null when no exception is thrown.
     */
    private final Class<? extends Throwable> expected;

    /**
     * Creates an instance with the specified excepted exception and parameters.
     *
     * @param pExpected
     *            Excepted exception which is thrown when executing the snippet
     *            with the specified parameters. It should be null when no
     *            exception is thrown.
     * @param parameters
     *            The parameters of the input.
     */
    public SnippetInput(final Class<? extends Throwable> pExpected,
            final Object... parameters) {
        if (parameters == null) {
            params = new Object[0];
        } else {
            params = new Object[parameters.length];
            System.arraycopy(parameters, 0, params, 0,
                    parameters.length);
        }

        expected = pExpected;
    }

    /**
     * Returns the number of input parameters.
     *
     * @return The number of input parameters.
     */
    public int getParameterCount() {
        return params.length;
    }

    /**
     * Returns the input parameter at the specified position in the underlying
     * array. Throws an {@link IndexOutOfBoundsException} if the index is out of
     * range.
     *
     * @param index
     *            Index of the input parameter to return.
     * @return The input parameter at the specified position.
     *
     */
    public Object getParameter(final int index) {
        if (0 <= index && index < params.length) {
            return params[index];
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Returns a shallow copy of the array containing the input parameters.
     *
     * @return An array containing the input parameters.
     */
    public Object[] getParameters() {
        Object[] copy = new Object[params.length];
        System.arraycopy(params, 0, copy, 0, params.length);
        return copy;
    }

    /**
     * Returns the type of the expected exception.
     *
     * @return The type of the expected exception.
     */
    public Class<? extends Throwable> getExpected() {
        return expected;
    }
}
