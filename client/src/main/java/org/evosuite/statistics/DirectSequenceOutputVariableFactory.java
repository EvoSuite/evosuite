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
package org.evosuite.statistics;

import org.evosuite.testsuite.TestSuiteChromosome;

/**
 * Sequence output variable whose value can be set directly,
 * instead of retrieved from individual.
 *
 * @author Jose Miguel Rojas
 */
public class DirectSequenceOutputVariableFactory<T extends Number> extends SequenceOutputVariableFactory<T> {

    protected T value;
    private final Class<T> type;

    public DirectSequenceOutputVariableFactory(RuntimeVariable variable, Class<T> type, T startValue) {
        super(variable);
        this.type = type;
        this.value = startValue;
    }

    @Override
    public T getValue(TestSuiteChromosome individual) {
        return this.value;
    }

    /**
     * Sets value directly
     *
     * @param value
     */
    public void setValue(T value) {
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    public void setValue(Object value) {
        if (this.type.isInstance(value)) {
            this.setValue((T) value);
        } else {
            throw new IllegalArgumentException("value of type " + value.getClass().getName()
                    + " is incompatible with expected type " + this.type.getName());
        }
    }

    public static DirectSequenceOutputVariableFactory<Double> getDouble(RuntimeVariable variable) {
        return new DirectSequenceOutputVariableFactory<>(variable, Double.class, 0.0);
    }

    public static DirectSequenceOutputVariableFactory<Integer> getInteger(RuntimeVariable variable) {
        return new DirectSequenceOutputVariableFactory<>(variable, Integer.class, 0);
    }
}
