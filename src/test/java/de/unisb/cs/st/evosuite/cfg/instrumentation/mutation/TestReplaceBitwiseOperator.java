/**
 * 
 */
package de.unisb.cs.st.evosuite.cfg.instrumentation.mutation;

import static org.junit.Assert.assertEquals;

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
