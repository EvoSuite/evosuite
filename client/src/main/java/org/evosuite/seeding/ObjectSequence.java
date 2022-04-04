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
package org.evosuite.seeding;

import org.evosuite.testcase.TestCase;
import org.evosuite.utils.generic.GenericClass;

import java.io.Serializable;

class ObjectSequence implements Serializable {

    private static final long serialVersionUID = 346185306757522598L;

    private final GenericClass<?> generatedType;

    private final TestCase test;

    public ObjectSequence(GenericClass<?> generatedType, TestCase test) {
        this.generatedType = generatedType;
        this.test = test;
    }

    public GenericClass<?> getGeneratedClass() {
        return generatedType;
    }

    public TestCase getSequence() {
        return test;
    }

}
