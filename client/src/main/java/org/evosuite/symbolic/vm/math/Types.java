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
package org.evosuite.symbolic.vm.math;

import org.objectweb.asm.Type;

public interface Types {

	// homogeneuos unary descriptors
    String I2I_DESCRIPTOR = Type.getMethodDescriptor(
			Type.INT_TYPE, Type.INT_TYPE); // "(I)I";
	String L2L_DESCRIPTOR = Type.getMethodDescriptor(
	Type.LONG_TYPE, Type.LONG_TYPE);// "(J)J";
	String F2F_DESCRIPTOR = Type.getMethodDescriptor(
	Type.FLOAT_TYPE, Type.FLOAT_TYPE);// "(F)F";
	// heterogeneous unary descriptors
    String F2I_DESCRIPTOR = Type.getMethodDescriptor(
			Type.INT_TYPE, Type.FLOAT_TYPE);// "(F)I";
	// homogeneuos binary descriptors
    String II2I_DESCRIPTOR = Type.getMethodDescriptor(
			Type.INT_TYPE, Type.INT_TYPE, Type.INT_TYPE);// "(II)I";
	String LL2L_DESCRIPTOR = Type.getMethodDescriptor(
	Type.LONG_TYPE, Type.LONG_TYPE, Type.LONG_TYPE);// "(JJ)J";
	String FF2F_DESCRIPTOR = Type.getMethodDescriptor(
	Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE);// "(FF)F";
	// heterogeneous binary descriptors
    String FI2F_DESCRIPTOR = Type.getMethodDescriptor(
			Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.INT_TYPE);// "(FI)F";
	String FD2F_DESCRIPTOR = Type.getMethodDescriptor(
	Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.DOUBLE_TYPE);// "(FD)F";
	String DI2D_DESCRIPTOR = Type.getMethodDescriptor(
	Type.DOUBLE_TYPE, Type.DOUBLE_TYPE, Type.INT_TYPE);// "(DI)D";
	String D2I_DESCRIPTOR = Type.getMethodDescriptor(
	Type.INT_TYPE, Type.DOUBLE_TYPE);// "(D)I";
	String D2L_DESCRIPTOR = Type.getMethodDescriptor(
	Type.LONG_TYPE, Type.DOUBLE_TYPE);// "(D)J";
	String DD2D_DESCRIPTOR = Type.getMethodDescriptor(
	Type.DOUBLE_TYPE, Type.DOUBLE_TYPE, Type.DOUBLE_TYPE);// "(DD)D";
	String D2D_DESCRIPTOR = Type.getMethodDescriptor(
	Type.DOUBLE_TYPE, Type.DOUBLE_TYPE);// "(D)D";
	String JAVA_LANG_MATH = Math.class.getName().replace(
	".", "/");

}
