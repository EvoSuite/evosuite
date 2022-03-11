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
package org.evosuite.testcase.statements.environment;

import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.statements.PrimitiveStatement;

import java.lang.reflect.Type;

/**
 * Created by arcuri on 12/12/14.
 */
public abstract class EnvironmentDataStatement<T> extends PrimitiveStatement<T> {

    private static final long serialVersionUID = -348689954506405873L;

    protected EnvironmentDataStatement(TestCase tc, Type clazz, T value) {
        super(tc, clazz, value);
    }

    public abstract String getTestCode(String varName);
}
