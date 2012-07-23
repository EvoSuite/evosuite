/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.javaagent;

import org.objectweb.asm.Opcodes;

/**
 * <p>ErrorConditionChecker class.</p>
 *
 * @author fraser
 */
public class ErrorConditionChecker {

	/**
	 * <p>scale</p>
	 *
	 * @param value a float.
	 * @return a int.
	 */
	public static int scale(float value) {
		return (Integer.MAX_VALUE - 2) * (int) Math.ceil((value / (value + 1.0F)));
	}

	/**
	 * <p>scale</p>
	 *
	 * @param value a double.
	 * @return a int.
	 */
	public static int scale(double value) {
		return (Integer.MAX_VALUE - 2) * (int) Math.ceil((value / (value + 1.0)));
	}

	/**
	 * <p>scale</p>
	 *
	 * @param value a long.
	 * @return a int.
	 */
	public static int scale(long value) {
		return (Integer.MAX_VALUE - 2) * (int) Math.ceil((value / (value + 1L)));
	}

	/**
	 * <p>overflowDistance</p>
	 *
	 * @param op1 a int.
	 * @param op2 a int.
	 * @param opcode a int.
	 * @return a int.
	 */
	public static int overflowDistance(int op1, int op2, int opcode) {
		int result = 0;

		// result = overflowDistance(opcode, op1, op2);
		// if(result <= 0) 
		//   throw OverflowException()

		switch (opcode) {
		case Opcodes.IADD:
			result = op1 + op2;
			if (op1 > 0 && op2 > 0) {
				// result has to be < 0 for overflow
				return result < 0 ? -1 : (Integer.MAX_VALUE - result);
			} else if (op1 < 0 && op2 < 0)
				// result has to be > 0 for overflow
				return result > 0 ? -1 : (Integer.MAX_VALUE - Math.abs(result));
			break;
		case Opcodes.ISUB:
			result = op1 - op2;
			if (op1 > 0 && op2 < 0)
				// result has to be < 0 for overflow
				return result < 0 ? -1 : (Integer.MAX_VALUE - result);
			else if (op1 < 0 && op2 > 0)
				// result has to be > 0 for overflow
				return result > 0 ? -1 : (Integer.MAX_VALUE - Math.abs(result));
			break;
		case Opcodes.IMUL:
			result = op1 * op2;
			if (op1 > 0 && op2 > 0)
				// result has to be < 0
				return result < 0 ? -1 : (Integer.MAX_VALUE - result);
			else if (op1 < 0 && op2 < 0)
				// result has to be < 0
				return result < 0 ? -1 : (Integer.MAX_VALUE - result);
			else if (op1 < 0 && op2 > 0)
				// result has to be > 0
				return result > 0 ? -1 : (Integer.MAX_VALUE - Math.abs(result));
			else if (op1 > 0 && op2 < 0)
				// result has to be > 0
				return result > 0 ? -1 : (Integer.MAX_VALUE - Math.abs(result));
			break;
		}
		return 1;
	}

	/**
	 * <p>overflowDistance</p>
	 *
	 * @param op1 a float.
	 * @param op2 a float.
	 * @param opcode a int.
	 * @return a int.
	 */
	public static int overflowDistance(float op1, float op2, int opcode) {
		float result = 0.0F;
		switch (opcode) {
		case Opcodes.FADD:
			result = op1 + op2;
			if (op1 > 0 && op2 > 0)
				// result has to be < 0 for overflow
				return result < 0 ? -1 : scale(Float.MAX_VALUE - result);
			else if (op1 < 0 && op2 < 0)
				// result has to be > 0 for overflow
				return result > 0 ? -1 : scale(Float.MAX_VALUE - Math.abs(result));
		case Opcodes.FSUB:
			result = op1 - op2;
			if (op1 > 0 && op2 < 0)
				// result has to be < 0 for overflow
				return result < 0 ? -1 : scale(Float.MAX_VALUE - result);
			else if (op1 < 0 && op2 > 0)
				// result has to be > 0 for overflow
				return result > 0 ? -1 : scale(Float.MAX_VALUE - Math.abs(result));
		case Opcodes.FMUL:
			result = op1 * op2;
			if (op1 > 0 && op2 > 0)
				// result has to be < 0
				return result < 0 ? -1 : scale(Float.MAX_VALUE - result);
			else if (op1 < 0 && op2 < 0)
				// result has to be < 0
				return result < 0 ? -1 : scale(Float.MAX_VALUE - result);
			else if (op1 < 0 && op2 > 0)
				// result has to be > 0
				return result > 0 ? -1 : scale(Float.MAX_VALUE - Math.abs(result));
			else if (op1 > 0 && op2 < 0)
				// result has to be > 0
				return result > 0 ? -1 : scale(Float.MAX_VALUE - Math.abs(result));
		}
		return 1;
	}

	/**
	 * <p>overflowDistance</p>
	 *
	 * @param op1 a double.
	 * @param op2 a double.
	 * @param opcode a int.
	 * @return a int.
	 */
	public static int overflowDistance(double op1, double op2, int opcode) {
		double result = 0.0;
		switch (opcode) {
		case Opcodes.DADD:
			result = op1 + op2;
			if (op1 > 0 && op2 > 0)
				// result has to be < 0 for overflow
				return result < 0 ? -1 : scale(Double.MAX_VALUE - result);
			else if (op1 < 0 && op2 < 0)
				// result has to be > 0 for overflow
				return result > 0 ? -1 : scale(Double.MAX_VALUE - Math.abs(result));
		case Opcodes.DSUB:
			result = op1 - op2;
			if (op1 > 0 && op2 < 0)
				// result has to be < 0 for overflow
				return result < 0 ? -1 : scale(Double.MAX_VALUE - result);
			else if (op1 < 0 && op2 > 0)
				// result has to be > 0 for overflow
				return result > 0 ? -1 : scale(Double.MAX_VALUE - Math.abs(result));
		case Opcodes.DMUL:
			result = op1 * op2;
			if (op1 > 0 && op2 > 0)
				// result has to be < 0
				return result < 0 ? -1 : scale(Double.MAX_VALUE - result);
			else if (op1 < 0 && op2 < 0)
				// result has to be < 0
				return result < 0 ? -1 : scale(Double.MAX_VALUE - result);
			else if (op1 < 0 && op2 > 0)
				// result has to be > 0
				return result > 0 ? -1 : scale(Double.MAX_VALUE - Math.abs(result));
			else if (op1 > 0 && op2 < 0)
				// result has to be > 0
				return result > 0 ? -1 : scale(Double.MAX_VALUE - Math.abs(result));
		}
		return 1;
	}

	/**
	 * <p>overflowDistance</p>
	 *
	 * @param op1 a long.
	 * @param op2 a long.
	 * @param opcode a int.
	 * @return a int.
	 */
	public static int overflowDistance(long op1, long op2, int opcode) {
		long result = 0L;

		switch (opcode) {
		case Opcodes.LADD:
			result = op1 + op2;
			if (op1 > 0 && op2 > 0)
				// result has to be < 0 for overflow
				return result < 0 ? -1 : scale(Long.MAX_VALUE - result);
			else if (op1 < 0 && op2 < 0)
				// result has to be > 0 for overflow
				return result > 0 ? -1 : scale(Long.MAX_VALUE - Math.abs(result));
		case Opcodes.LSUB:
			result = op1 - op2;
			if (op1 > 0 && op2 < 0)
				// result has to be < 0 for overflow
				return result < 0 ? -1 : scale(Long.MAX_VALUE - result);
			else if (op1 < 0 && op2 > 0)
				// result has to be > 0 for overflow
				return result > 0 ? -1 : scale(Long.MAX_VALUE - Math.abs(result));
		case Opcodes.LMUL:
			result = op1 * op2;
			if (op1 > 0 && op2 > 0)
				// result has to be < 0
				return result < 0 ? -1 : scale(Long.MAX_VALUE - result);
			else if (op1 < 0 && op2 < 0)
				// result has to be < 0
				return result < 0 ? -1 : scale(Long.MAX_VALUE - result);
			else if (op1 < 0 && op2 > 0)
				// result has to be > 0
				return result > 0 ? -1 : scale(Long.MAX_VALUE - Math.abs(result));
			else if (op1 > 0 && op2 < 0)
				// result has to be > 0
				return result > 0 ? -1 : scale(Long.MAX_VALUE - Math.abs(result));
		}
		return 1;
	}
}
