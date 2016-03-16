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

import org.evosuite.instrumentation.mutation.ReplaceBitwiseOperator;
import org.junit.Test;
import org.objectweb.asm.Opcodes;

/**
 * @author fraser
 * 
 */
public class TestReplaceBitwiseOperator {

	@Test
	public void testGetInfectionDistanceInt() {
		assertEquals(ReplaceBitwiseOperator.getInfectionDistanceInt(1, 0, Opcodes.IAND,
		                                                            Opcodes.IOR), 0.0,
		             0.0);
		assertEquals(ReplaceBitwiseOperator.getInfectionDistanceInt(0, 0, Opcodes.IAND,
		                                                            Opcodes.IOR), 1.0,
		             0.0);
		assertEquals(ReplaceBitwiseOperator.getInfectionDistanceInt(1, 1, Opcodes.IAND,
		                                                            Opcodes.IOR), 1.0,
		             0.0);
		assertEquals(ReplaceBitwiseOperator.getInfectionDistanceInt(1, 2, Opcodes.IAND,
		                                                            Opcodes.IOR), 0.0,
		             0.0);
	}

	@Test
	public void testGetInfectionDistanceIntShift() {
		assertEquals(ReplaceBitwiseOperator.getInfectionDistanceInt(1, 0, Opcodes.ISHL,
		                                                            Opcodes.ISHR), 1.0,
		             0.0);
		assertEquals(ReplaceBitwiseOperator.getInfectionDistanceInt(1, 1, Opcodes.ISHL,
		                                                            Opcodes.ISHR), 0.0,
		             0.0);
		assertEquals(ReplaceBitwiseOperator.getInfectionDistanceInt(1, 0, Opcodes.ISHL,
		                                                            Opcodes.IUSHR), 1.0,
		             0.0);
		assertEquals(ReplaceBitwiseOperator.getInfectionDistanceInt(1, 1, Opcodes.ISHL,
		                                                            Opcodes.IUSHR), 0.0,
		             0.0);
		assertEquals(ReplaceBitwiseOperator.getInfectionDistanceInt(1, 0, Opcodes.ISHR,
		                                                            Opcodes.IUSHR), 1.0,
		             0.0);
		assertEquals(2.0, ReplaceBitwiseOperator.getInfectionDistanceInt(1, 1,
		                                                                 Opcodes.ISHR,
		                                                                 Opcodes.IUSHR),
		             0.0);
		assertEquals(ReplaceBitwiseOperator.getInfectionDistanceInt(-1, 1, Opcodes.ISHR,
		                                                            Opcodes.IUSHR), 0.0,
		             0.0);
		assertEquals(2.0, ReplaceBitwiseOperator.getInfectionDistanceInt(1, -1,
		                                                                 Opcodes.ISHR,
		                                                                 Opcodes.IUSHR),
		             0.0);
		assertEquals(ReplaceBitwiseOperator.getInfectionDistanceInt(-1, -1, Opcodes.ISHR,
		                                                            Opcodes.IUSHR), 0.0,
		             0.0);
		assertEquals(0.0, ReplaceBitwiseOperator.getInfectionDistanceInt(-1449, -1,
		                                                                 Opcodes.ISHR,
		                                                                 Opcodes.IUSHR),
		             0.0);
	}

}
