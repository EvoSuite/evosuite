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

import java.util.ArrayList;
import java.util.List;

/**
 * Holds input parameter tuples for a code snippet.
 */
public final class SnippetInputContainer {
    /** The number of required parameters for the code snippet. */
    private final int parameterCount;
    /** The list of inputs for the code snippet. */
    private final List<SnippetInput> inputs = new ArrayList<>();

    /**
     * Creates an instance with the specified parameter count. Throws an
     * {@link IllegalArgumentException} if the parameter count is negative.
     *
     * @param pParameterCount
     *            The number of required parameters for the code snippet.
     */
    public SnippetInputContainer(final int pParameterCount) {
        if (pParameterCount < 0) {
            throw new IllegalArgumentException(
                    "The parameter count must not be a negative number");
        }
        parameterCount = pParameterCount;
    }

    /**
     * Returns the number of parameters.
     *
     * @return The number of parameters.
     */
    public int getParameterCount() {
        return parameterCount;
    }

    /**
     * Returns the number of input tuples.
     *
     * @return The number of input tuples.
     */
    public int size() {
        return inputs.size();
    }

    /**
     * Returns the input tuple at the specified position in the underlying list.
     * Throws an {@link IndexOutOfBoundsException} if the index is out of range.
     *
     * @param index
     *            Index of the input tuple to return.
     * @return The input tuple at the specified position.
     *
     */
    public SnippetInput get(final int index) {
        if (0 <= index && index < size()) {
            return inputs.get(index);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Returns an array of input tuples.
     *
     * @return An array of input tuples.
     */
    public SnippetInput[] toArray() {
        return inputs.toArray(new SnippetInput[size()]);
    }

    /**
     * Adds the specified input tuple to the container. Throws an
     * {@link IllegalArgumentException} if the input is null or the parameter
     * count does not match.
     *
     * @param input
     *            The input tuple.
     * @return This object.
     */
    public SnippetInputContainer add(final SnippetInput input) {
        if (input == null) {
            throw new IllegalArgumentException(
                    "The input must not be null");
        } else if (input.getParameterCount() != parameterCount) {
            throw new IllegalArgumentException(
                    "Parameter count of the input must match "
                            + "the parameter count of the container");
        }

        inputs.add(input);
        return this;
    }

    /**
     * Adds the specified input tuple to the container. Throws an
     * {@link IllegalArgumentException} if the the parameter count does not
     * match.
     *
     * @param parameters
     *            The parameters of the input.
     * @return This object.
     *
     */
    public SnippetInputContainer addByParameters(
            final Object... parameters) {
        return addByParametersAndExpected(null, parameters);
    }

    /**
     * Adds the specified input tuple to the container. Throws an
     * {@link IllegalArgumentException} if the the parameter count does not
     * match.
     *
     * @param expected
     *            Excepted exception which is thrown when executing the snippet
     *            with the specified parameters. It should be null when no
     *            exception is thrown.
     * @param parameters
     *            The parameters of the input.
     * @return This object.
     *
     */
    public SnippetInputContainer addByParametersAndExpected(
            final Class<? extends Throwable> expected,
            final Object... parameters) {
        return add(new SnippetInput(expected, parameters));
    }
}