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
package org.evosuite.testcase.variable;

import org.evosuite.testcase.TestCase;

import java.lang.reflect.Type;

/**
 * Special case of VariableInstance pointing to null
 *
 * @author Gordon Fraser
 */
public class NullReference extends VariableReferenceImpl {

    private static final long serialVersionUID = -6172885297590386463L;

    /**
     * <p>Constructor for NullReference.</p>
     *
     * @param type     a {@link java.lang.reflect.Type} object.
     * @param testCase a {@link org.evosuite.testcase.TestCase} object.
     */
    public NullReference(TestCase testCase, Type type) {
        super(testCase, type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VariableReference clone() {
        throw new UnsupportedOperationException();
    }
}
