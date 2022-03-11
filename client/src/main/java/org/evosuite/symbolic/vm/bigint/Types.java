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
package org.evosuite.symbolic.vm.bigint;

import org.objectweb.asm.Type;

import java.math.BigInteger;

public interface Types {

    String JAVA_MATH_BIG_INTEGER = BigInteger.class
            .getName().replace('.', '/');

    String INIT = "<init>";

    String JAVA_LANG_STRING = String.class.getName()
            .replace('.', '/');

    Type BIG_INTEGER = Type.getType(BigInteger.class);

    Type BIG_INTEGER_ARRAY = Type
            .getType(BigInteger[].class);

    String BIG_INTEGER_TO_BIG_INTEGER_ARRAY = Type
            .getMethodDescriptor(BIG_INTEGER_ARRAY, BIG_INTEGER);

    String TO_INT = Type.getMethodDescriptor(Type.INT_TYPE);

    String STRING_TO_VOID = Type.getMethodDescriptor(Type.VOID_TYPE,
            Type.getType(String.class));

    String BIG_INTEGER_TO_BIG_INTEGER = Type.getMethodDescriptor(
            BIG_INTEGER, BIG_INTEGER);

}
