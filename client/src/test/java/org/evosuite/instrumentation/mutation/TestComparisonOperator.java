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
/**
 * 
 */
package org.evosuite.instrumentation.mutation;

import static org.junit.Assert.assertEquals;

import org.evosuite.instrumentation.mutation.ReplaceComparisonOperator;
import org.junit.Test;
import org.objectweb.asm.Opcodes;

/**
 * @author fraser
 * 
 */
public class TestComparisonOperator {

	@Test
	public void testComparisonZeroEQ() {
		// x == 0 vs x != 0		
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(0, Opcodes.IFEQ,
		                                                                 Opcodes.IFNE),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(1, Opcodes.IFEQ,
		                                                                 Opcodes.IFNE),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(-1,
		                                                                 Opcodes.IFEQ,
		                                                                 Opcodes.IFNE),
		             0.0);

		// x == 0 vs x < 0
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(0, Opcodes.IFEQ,
		                                                                 Opcodes.IFLT),
		             0.0);
		assertEquals(1.0, ReplaceComparisonOperator.getInfectionDistance(1, Opcodes.IFEQ,
		                                                                 Opcodes.IFLT),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(-1,
		                                                                 Opcodes.IFEQ,
		                                                                 Opcodes.IFLT),
		             0.0);

		// x == 0 vs x <= 0
		assertEquals(1.0, ReplaceComparisonOperator.getInfectionDistance(0, Opcodes.IFEQ,
		                                                                 Opcodes.IFLE),
		             0.0);
		assertEquals(2.0, ReplaceComparisonOperator.getInfectionDistance(1, Opcodes.IFEQ,
		                                                                 Opcodes.IFLE),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(-1,
		                                                                 Opcodes.IFEQ,
		                                                                 Opcodes.IFLE),
		             0.0);

		// x == 0 vs x > 0
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(0, Opcodes.IFEQ,
		                                                                 Opcodes.IFGT),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(1, Opcodes.IFEQ,
		                                                                 Opcodes.IFGT),
		             0.0);
		assertEquals(1.0, ReplaceComparisonOperator.getInfectionDistance(-1,
		                                                                 Opcodes.IFEQ,
		                                                                 Opcodes.IFGT),
		             0.0);

		// x == 0 vs x >= 0
		assertEquals(1.0, ReplaceComparisonOperator.getInfectionDistance(0, Opcodes.IFEQ,
		                                                                 Opcodes.IFGE),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(1, Opcodes.IFEQ,
		                                                                 Opcodes.IFGE),
		             0.0);
		assertEquals(2.0, ReplaceComparisonOperator.getInfectionDistance(-1,
		                                                                 Opcodes.IFEQ,
		                                                                 Opcodes.IFGE),
		             0.0);
	}

	@Test
	public void testComparisonZeroNE() {
		// x != 0 vs x == 0		
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(0, Opcodes.IFNE,
		                                                                 Opcodes.IFEQ),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(1, Opcodes.IFNE,
		                                                                 Opcodes.IFEQ),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(-1,
		                                                                 Opcodes.IFNE,
		                                                                 Opcodes.IFEQ),
		             0.0);

		// x != 0 vs x < 0
		assertEquals(1.0, ReplaceComparisonOperator.getInfectionDistance(0, Opcodes.IFNE,
		                                                                 Opcodes.IFLT),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(1, Opcodes.IFNE,
		                                                                 Opcodes.IFLT),
		             0.0);
		assertEquals(2.0, ReplaceComparisonOperator.getInfectionDistance(-1,
		                                                                 Opcodes.IFNE,
		                                                                 Opcodes.IFLT),
		             0.0);

		// x != 0 vs x <= 0
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(0, Opcodes.IFNE,
		                                                                 Opcodes.IFLE),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(1, Opcodes.IFNE,
		                                                                 Opcodes.IFLE),
		             0.0);
		assertEquals(1.0, ReplaceComparisonOperator.getInfectionDistance(-1,
		                                                                 Opcodes.IFNE,
		                                                                 Opcodes.IFLE),
		             0.0);

		// x != 0 vs x > 0
		assertEquals(1.0, ReplaceComparisonOperator.getInfectionDistance(0, Opcodes.IFNE,
		                                                                 Opcodes.IFGT),
		             0.0);
		assertEquals(2.0, ReplaceComparisonOperator.getInfectionDistance(1, Opcodes.IFNE,
		                                                                 Opcodes.IFGT),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(-1,
		                                                                 Opcodes.IFNE,
		                                                                 Opcodes.IFGT),
		             0.0);

		// x != 0 vs x >= 0
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(0, Opcodes.IFNE,
		                                                                 Opcodes.IFGE),
		             0.0);
		assertEquals(1.0, ReplaceComparisonOperator.getInfectionDistance(1, Opcodes.IFNE,
		                                                                 Opcodes.IFGE),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(-1,
		                                                                 Opcodes.IFNE,
		                                                                 Opcodes.IFGE),
		             0.0);
	}

	@Test
	public void testComparisonZeroLT() {
		// x < 0 vs x == 0		
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(0, Opcodes.IFLT,
		                                                                 Opcodes.IFEQ),
		             0.0);
		assertEquals(1.0, ReplaceComparisonOperator.getInfectionDistance(1, Opcodes.IFLT,
		                                                                 Opcodes.IFEQ),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(-1,
		                                                                 Opcodes.IFLT,
		                                                                 Opcodes.IFEQ),
		             0.0);

		// x < 0 vs x != 0
		assertEquals(1.0, ReplaceComparisonOperator.getInfectionDistance(0, Opcodes.IFLT,
		                                                                 Opcodes.IFNE),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(1, Opcodes.IFLT,
		                                                                 Opcodes.IFNE),
		             0.0);
		assertEquals(2.0, ReplaceComparisonOperator.getInfectionDistance(-1,
		                                                                 Opcodes.IFLT,
		                                                                 Opcodes.IFNE),
		             0.0);

		// x < 0 vs x <= 0
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(0, Opcodes.IFLT,
		                                                                 Opcodes.IFLE),
		             0.0);
		assertEquals(1.0, ReplaceComparisonOperator.getInfectionDistance(1, Opcodes.IFLT,
		                                                                 Opcodes.IFLE),
		             0.0);
		assertEquals(1.0, ReplaceComparisonOperator.getInfectionDistance(-1,
		                                                                 Opcodes.IFLT,
		                                                                 Opcodes.IFLE),
		             0.0);

		// x < 0 vs x > 0
		assertEquals(1.0, ReplaceComparisonOperator.getInfectionDistance(0, Opcodes.IFLT,
		                                                                 Opcodes.IFGT),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(1, Opcodes.IFLT,
		                                                                 Opcodes.IFGT),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(-1,
		                                                                 Opcodes.IFLT,
		                                                                 Opcodes.IFGT),
		             0.0);

		// x < 0 vs x >= 0
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(0, Opcodes.IFLT,
		                                                                 Opcodes.IFGE),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(1, Opcodes.IFLT,
		                                                                 Opcodes.IFGE),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(-1,
		                                                                 Opcodes.IFLT,
		                                                                 Opcodes.IFGE),
		             0.0);
	}

	@Test
	public void testComparisonZeroLE() {
		// x <= 0 vs x == 0		
		assertEquals(1.0, ReplaceComparisonOperator.getInfectionDistance(0, Opcodes.IFLE,
		                                                                 Opcodes.IFEQ),
		             0.0);
		assertEquals(2.0, ReplaceComparisonOperator.getInfectionDistance(1, Opcodes.IFLE,
		                                                                 Opcodes.IFEQ),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(-1,
		                                                                 Opcodes.IFLE,
		                                                                 Opcodes.IFEQ),
		             0.0);

		// x <= 0 vs x != 0
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(0, Opcodes.IFLE,
		                                                                 Opcodes.IFNE),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(1, Opcodes.IFLE,
		                                                                 Opcodes.IFNE),
		             0.0);
		assertEquals(1.0, ReplaceComparisonOperator.getInfectionDistance(-1,
		                                                                 Opcodes.IFLE,
		                                                                 Opcodes.IFNE),
		             0.0);

		// x <= 0 vs x < 0
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(0, Opcodes.IFLE,
		                                                                 Opcodes.IFLT),
		             0.0);
		assertEquals(1.0, ReplaceComparisonOperator.getInfectionDistance(1, Opcodes.IFLE,
		                                                                 Opcodes.IFLT),
		             0.0);
		assertEquals(1.0, ReplaceComparisonOperator.getInfectionDistance(-1,
		                                                                 Opcodes.IFLE,
		                                                                 Opcodes.IFLT),
		             0.0);

		// x <= 0 vs x > 0
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(0, Opcodes.IFLE,
		                                                                 Opcodes.IFGT),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(1, Opcodes.IFLE,
		                                                                 Opcodes.IFGT),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(-1,
		                                                                 Opcodes.IFLE,
		                                                                 Opcodes.IFGT),
		             0.0);

		// x <= 0 vs x >= 0
		assertEquals(1.0, ReplaceComparisonOperator.getInfectionDistance(0, Opcodes.IFLE,
		                                                                 Opcodes.IFGE),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(1, Opcodes.IFLE,
		                                                                 Opcodes.IFGE),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(-1,
		                                                                 Opcodes.IFLE,
		                                                                 Opcodes.IFGE),
		             0.0);
	}

	@Test
	public void testComparisonZeroGT() {
		// x > 0 vs x == 0		
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(0, Opcodes.IFGT,
		                                                                 Opcodes.IFEQ),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(1, Opcodes.IFGT,
		                                                                 Opcodes.IFEQ),
		             0.0);
		assertEquals(1.0, ReplaceComparisonOperator.getInfectionDistance(-1,
		                                                                 Opcodes.IFGT,
		                                                                 Opcodes.IFEQ),
		             0.0);

		// x > 0 vs x != 0
		assertEquals(1.0, ReplaceComparisonOperator.getInfectionDistance(0, Opcodes.IFGT,
		                                                                 Opcodes.IFNE),
		             0.0);
		assertEquals(2.0, ReplaceComparisonOperator.getInfectionDistance(1, Opcodes.IFGT,
		                                                                 Opcodes.IFNE),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(-1,
		                                                                 Opcodes.IFGT,
		                                                                 Opcodes.IFNE),
		             0.0);

		// x > 0 vs x <= 0
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(0, Opcodes.IFGT,
		                                                                 Opcodes.IFLE),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(1, Opcodes.IFGT,
		                                                                 Opcodes.IFLE),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(-1,
		                                                                 Opcodes.IFGT,
		                                                                 Opcodes.IFLE),
		             0.0);

		// x > 0 vs x < 0
		assertEquals(1.0, ReplaceComparisonOperator.getInfectionDistance(0, Opcodes.IFGT,
		                                                                 Opcodes.IFLT),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(1, Opcodes.IFGT,
		                                                                 Opcodes.IFLT),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(-1,
		                                                                 Opcodes.IFGT,
		                                                                 Opcodes.IFLT),
		             0.0);

		// x > 0 vs x >= 0
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(0, Opcodes.IFGT,
		                                                                 Opcodes.IFGE),
		             0.0);
		assertEquals(1.0, ReplaceComparisonOperator.getInfectionDistance(1, Opcodes.IFGT,
		                                                                 Opcodes.IFGE),
		             0.0);
		assertEquals(1.0, ReplaceComparisonOperator.getInfectionDistance(-1,
		                                                                 Opcodes.IFGT,
		                                                                 Opcodes.IFGE),
		             0.0);
	}

	@Test
	public void testComparisonZeroGE() {
		// x >= 0 vs x == 0		
		assertEquals(1.0, ReplaceComparisonOperator.getInfectionDistance(0, Opcodes.IFGE,
		                                                                 Opcodes.IFEQ),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(1, Opcodes.IFGE,
		                                                                 Opcodes.IFEQ),
		             0.0);
		assertEquals(2.0, ReplaceComparisonOperator.getInfectionDistance(-1,
		                                                                 Opcodes.IFGE,
		                                                                 Opcodes.IFEQ),
		             0.0);

		// x >= 0 vs x != 0
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(0, Opcodes.IFGE,
		                                                                 Opcodes.IFNE),
		             0.0);
		assertEquals(1.0, ReplaceComparisonOperator.getInfectionDistance(1, Opcodes.IFGE,
		                                                                 Opcodes.IFNE),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(-1,
		                                                                 Opcodes.IFGE,
		                                                                 Opcodes.IFNE),
		             0.0);

		// x >= 0 vs x <= 0
		assertEquals(1.0, ReplaceComparisonOperator.getInfectionDistance(0, Opcodes.IFGE,
		                                                                 Opcodes.IFLE),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(1, Opcodes.IFGE,
		                                                                 Opcodes.IFLE),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(-1,
		                                                                 Opcodes.IFGE,
		                                                                 Opcodes.IFLE),
		             0.0);

		// x >= 0 vs x < 0
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(0, Opcodes.IFGE,
		                                                                 Opcodes.IFLT),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(1, Opcodes.IFGE,
		                                                                 Opcodes.IFLT),
		             0.0);
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(-1,
		                                                                 Opcodes.IFGE,
		                                                                 Opcodes.IFLT),
		             0.0);

		// x >= 0 vs x > 0
		assertEquals(0.0, ReplaceComparisonOperator.getInfectionDistance(0, Opcodes.IFGE,
		                                                                 Opcodes.IFGT),
		             0.0);
		assertEquals(1.0, ReplaceComparisonOperator.getInfectionDistance(1, Opcodes.IFGE,
		                                                                 Opcodes.IFGT),
		             0.0);
		assertEquals(1.0, ReplaceComparisonOperator.getInfectionDistance(-1,
		                                                                 Opcodes.IFGE,
		                                                                 Opcodes.IFGT),
		             0.0);
	}

	@Test
	public void testComparisonTwoEQ() {
		// x == 0 vs x != 0		
		assertEquals(0.0,
		             ReplaceComparisonOperator.getInfectionDistance(0, 0,
		                                                            Opcodes.IF_ICMPEQ,
		                                                            Opcodes.IF_ICMPNE),
		             0.0);
		assertEquals(0.0,
		             ReplaceComparisonOperator.getInfectionDistance(1, 0,
		                                                            Opcodes.IF_ICMPEQ,
		                                                            Opcodes.IF_ICMPNE),
		             0.0);
		assertEquals(0.0,
		             ReplaceComparisonOperator.getInfectionDistance(-1, 0,
		                                                            Opcodes.IF_ICMPEQ,
		                                                            Opcodes.IF_ICMPNE),
		             0.0);

		// x == 0 vs x < 0
		assertEquals(0.0,
		             ReplaceComparisonOperator.getInfectionDistance(0, 0,
		                                                            Opcodes.IF_ICMPEQ,
		                                                            Opcodes.IF_ICMPLT),
		             0.0);
		assertEquals(1.0,
		             ReplaceComparisonOperator.getInfectionDistance(1, 0,
		                                                            Opcodes.IF_ICMPEQ,
		                                                            Opcodes.IF_ICMPLT),
		             0.0);
		assertEquals(0.0,
		             ReplaceComparisonOperator.getInfectionDistance(-1, 0,
		                                                            Opcodes.IF_ICMPEQ,
		                                                            Opcodes.IF_ICMPLT),
		             0.0);

		// x == 0 vs x <= 0
		assertEquals(1.0,
		             ReplaceComparisonOperator.getInfectionDistance(0, 0,
		                                                            Opcodes.IF_ICMPEQ,
		                                                            Opcodes.IF_ICMPLE),
		             0.0);
		assertEquals(2.0,
		             ReplaceComparisonOperator.getInfectionDistance(1, 0,
		                                                            Opcodes.IF_ICMPEQ,
		                                                            Opcodes.IF_ICMPLE),
		             0.0);
		assertEquals(0.0,
		             ReplaceComparisonOperator.getInfectionDistance(-1, 0,
		                                                            Opcodes.IF_ICMPEQ,
		                                                            Opcodes.IF_ICMPLE),
		             0.0);

		// x == 0 vs x > 0
		assertEquals(0.0,
		             ReplaceComparisonOperator.getInfectionDistance(0, 0,
		                                                            Opcodes.IF_ICMPEQ,
		                                                            Opcodes.IF_ICMPGT),
		             0.0);
		assertEquals(0.0,
		             ReplaceComparisonOperator.getInfectionDistance(1, 0,
		                                                            Opcodes.IF_ICMPEQ,
		                                                            Opcodes.IF_ICMPGT),
		             0.0);
		assertEquals(1.0,
		             ReplaceComparisonOperator.getInfectionDistance(-1, 0,
		                                                            Opcodes.IF_ICMPEQ,
		                                                            Opcodes.IF_ICMPGT),
		             0.0);

		// x == 0 vs x >= 0
		assertEquals(1.0,
		             ReplaceComparisonOperator.getInfectionDistance(0, 0,
		                                                            Opcodes.IF_ICMPEQ,
		                                                            Opcodes.IF_ICMPGE),
		             0.0);
		assertEquals(0.0,
		             ReplaceComparisonOperator.getInfectionDistance(1, 0,
		                                                            Opcodes.IF_ICMPEQ,
		                                                            Opcodes.IF_ICMPGE),
		             0.0);
		assertEquals(2.0,
		             ReplaceComparisonOperator.getInfectionDistance(-1, 0,
		                                                            Opcodes.IF_ICMPEQ,
		                                                            Opcodes.IF_ICMPGE),
		             0.0);
	}

	@Test
	public void testComparisonTwoEQ2() {
		// x == 0 vs x != 0		
		assertEquals(0.0,
		             ReplaceComparisonOperator.getInfectionDistance(10, 10,
		                                                            Opcodes.IF_ICMPEQ,
		                                                            Opcodes.IF_ICMPNE),
		             0.0);
		assertEquals(0.0,
		             ReplaceComparisonOperator.getInfectionDistance(11, 10,
		                                                            Opcodes.IF_ICMPEQ,
		                                                            Opcodes.IF_ICMPNE),
		             0.0);
		assertEquals(0.0,
		             ReplaceComparisonOperator.getInfectionDistance(-11, 10,
		                                                            Opcodes.IF_ICMPEQ,
		                                                            Opcodes.IF_ICMPNE),
		             0.0);

		// x == 0 vs x < 0
		assertEquals(0.0,
		             ReplaceComparisonOperator.getInfectionDistance(10, 10,
		                                                            Opcodes.IF_ICMPEQ,
		                                                            Opcodes.IF_ICMPLT),
		             0.0);
		assertEquals(1.0,
		             ReplaceComparisonOperator.getInfectionDistance(11, 10,
		                                                            Opcodes.IF_ICMPEQ,
		                                                            Opcodes.IF_ICMPLT),
		             0.0);
		assertEquals(0.0,
		             ReplaceComparisonOperator.getInfectionDistance(-11, 10,
		                                                            Opcodes.IF_ICMPEQ,
		                                                            Opcodes.IF_ICMPLT),
		             0.0);

		// x == 0 vs x <= 0
		assertEquals(1.0,
		             ReplaceComparisonOperator.getInfectionDistance(10, 10,
		                                                            Opcodes.IF_ICMPEQ,
		                                                            Opcodes.IF_ICMPLE),
		             0.0);
		assertEquals(2.0,
		             ReplaceComparisonOperator.getInfectionDistance(11, 10,
		                                                            Opcodes.IF_ICMPEQ,
		                                                            Opcodes.IF_ICMPLE),
		             0.0);
		assertEquals(0.0,
		             ReplaceComparisonOperator.getInfectionDistance(-11, 10,
		                                                            Opcodes.IF_ICMPEQ,
		                                                            Opcodes.IF_ICMPLE),
		             0.0);

		// x == 0 vs x > 0
		assertEquals(0.0,
		             ReplaceComparisonOperator.getInfectionDistance(10, 10,
		                                                            Opcodes.IF_ICMPEQ,
		                                                            Opcodes.IF_ICMPGT),
		             0.0);
		assertEquals(0.0,
		             ReplaceComparisonOperator.getInfectionDistance(11, 10,
		                                                            Opcodes.IF_ICMPEQ,
		                                                            Opcodes.IF_ICMPGT),
		             0.0);
		assertEquals(21.0,
		             ReplaceComparisonOperator.getInfectionDistance(-11, 10,
		                                                            Opcodes.IF_ICMPEQ,
		                                                            Opcodes.IF_ICMPGT),
		             0.0);

		// x == 0 vs x >= 0
		assertEquals(1.0,
		             ReplaceComparisonOperator.getInfectionDistance(10, 10,
		                                                            Opcodes.IF_ICMPEQ,
		                                                            Opcodes.IF_ICMPGE),
		             0.0);
		assertEquals(0.0,
		             ReplaceComparisonOperator.getInfectionDistance(11, 10,
		                                                            Opcodes.IF_ICMPEQ,
		                                                            Opcodes.IF_ICMPGE),
		             0.0);
		assertEquals(22.0,
		             ReplaceComparisonOperator.getInfectionDistance(-11, 10,
		                                                            Opcodes.IF_ICMPEQ,
		                                                            Opcodes.IF_ICMPGE),
		             0.0);
	}

}
