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
package org.evosuite.instrumentation.error;

import java.math.BigDecimal;

import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * ErrorConditionChecker class.
 * </p>
 * 
 * @author fraser
 */
public class ErrorConditionChecker {

	private static final Logger logger = LoggerFactory.getLogger(ErrorConditionChecker.class);
	
	/**
	 * <p>
	 * scale
	 * </p>
	 * 
	 * @param value
	 *            a float.
	 * @return a int.
	 */
	public static int scale(float value) {
		return (Integer.MAX_VALUE - 2) * (int) Math.ceil((value / (value + 1.0F)));
	}

	/**
	 * <p>
	 * scale
	 * </p>
	 * 
	 * @param value
	 *            a double.
	 * @return a int.
	 */
	public static int scale(double value) {
		return (Integer.MAX_VALUE - 2) * (int) Math.ceil((value / (value + 1.0)));
	}

	/**
	 * <p>
	 * scale
	 * </p>
	 * 
	 * @param value
	 *            a long.
	 * @return a int.
	 */
	public static int scale(long value) {
		return (Integer.MAX_VALUE - 2) * (int) Math.ceil((value / (value + 1.0)));
	}

	public static int scaleTo(double value, int max) {
		return (int) (Math.ceil(max * (1.0 * value / (value + 1.0))));
	}

	/**
	 * <p>
	 * overflowDistance
	 * </p>
	 * 
	 * @param op1
	 *            a int.
	 * @param op2
	 *            a int.
	 * @param opcode
	 *            a int.
	 * @return a int.
	 */
	public static int overflowDistance(int op1, int op2, int opcode) {
		switch (opcode) {

		case Opcodes.IADD:
			int result = overflowDistanceAdd(op1, op2);
			logger.debug("O: "+op1+" + "+op2+" = "+(op1+op2)+" -> "+result);
			return result;

		case Opcodes.ISUB:
			return overflowDistanceSub(op1, op2);

		case Opcodes.IMUL:
			return overflowDistanceMul(op1, op2);

		case Opcodes.IDIV:
			return overflowDistanceDiv(op1, op2);
		}
		return Integer.MAX_VALUE;
	}

	protected final static int HALFWAY = Integer.MAX_VALUE / 2;

	protected static int overflowDistanceAdd(int op1, int op2) {
		int result = op1 + op2;
		if (op1 > 0 && op2 > 0) {
			// result has to be < 0 for overflow
			if(result < 0)
				return result;
			else {
				int retVal = HALFWAY - scaleTo(result, HALFWAY);
				if(retVal != 0)
					return retVal;
				else 
					return 1;
			}
		} else if (op1 < 0 && op2 < 0) {
			// if both are negative then both need to be increased
			return HALFWAY
			        + scaleTo(Math.abs((long) op1) + Math.abs((long) op2), HALFWAY);
		} else if (op1 >= 0 && op2 < 0) {
			// If only one is negative, then optimize that to be positive
			return HALFWAY + scaleTo(Math.abs(op2), HALFWAY);
		} else if (op1 < 0 && op2 >= 0) {
			// If only one is negative, then optimize that to be positive
			return HALFWAY + scaleTo(Math.abs(op1), HALFWAY);
		} else {
			// At least one of them is zero, and the sum is larger or equals than 0
			return 1 + HALFWAY - scaleTo(result, HALFWAY);
		}
	}

	protected static int underflowDistanceAdd(int op1, int op2) {
		int result = op1 + op2;
		if (op1 <= 0 && op2 <= 0) {
			if(op1 == Integer.MIN_VALUE && op2 == Integer.MIN_VALUE) {
				return Integer.MIN_VALUE;
			} else {
				// result has to be < 0 for overflow
				return result > 0 ? -result : HALFWAY
						- scaleTo(Math.abs((long) result), HALFWAY) + 1;
			}
		} else if (op1 > 0 && op2 > 0) {
			// if both are positive then both need to be decreased
			return HALFWAY
			        + scaleTo(Math.abs((long) op1) + Math.abs((long) op2), HALFWAY);
		} else if (op1 >= 0 && op2 < 0) {
			return HALFWAY + scaleTo(Math.abs(op1), HALFWAY);
		} else if (op1 < 0 && op2 >= 0) {
			return HALFWAY + scaleTo(Math.abs(op2), HALFWAY);
		} else {
			// Unreachable
			return Integer.MAX_VALUE;
		}
	}

	protected static int overflowDistanceSub(int op1, int op2) {
		int result = op1 - op2;
		if (op1 >= 0 && op2 <= 0) {
			// result has to be < 0 for overflow
			return result < 0 ? result : HALFWAY + 1 - scaleTo(result, HALFWAY);
		} else if (op1 < 0 && op2 > 0) {
			// if both are negative then an overflow will be difficult
			return HALFWAY
			        + scaleTo(Math.abs((long) op1) + Math.abs((long) op2), HALFWAY);
		} else if (op1 >= 0 && op2 > 0) {
			// In this case we can't have an overflow yet
			return HALFWAY + scaleTo(op2, HALFWAY);
		} else if (op1 < 0 && op2 <= 0) {
			return HALFWAY + scaleTo(Math.abs((long) op1), HALFWAY);
		} else {
			// At least one of them is zero, and the sum is larger or equals than 0
			return 1 + HALFWAY - scaleTo(result, HALFWAY);
		}
	}

	protected static int underflowDistanceSub(int op1, int op2) {
		int result = op1 - op2;
		if (op1 <= 0 && op2 >= 0) {
			return result > 0 ? -result : HALFWAY + 1 - scaleTo(result, HALFWAY);
		} else if (op1 > 0 && op2 < 0) {
			return HALFWAY
			        + scaleTo(Math.abs((long) op1) + Math.abs((long) op2), HALFWAY);
		} else if (op1 >= 0 && op2 > 0) {
			// In this case we can't have an overflow yet
			return HALFWAY + scaleTo(op1, HALFWAY);
		} else if (op1 < 0 && op2 <= 0) {
			return HALFWAY + scaleTo(Math.abs((long) op2), HALFWAY);
		} else {
			// Not sure if this can be reached
			return 1 + HALFWAY - scaleTo(result, HALFWAY);
		}
	}

	protected static int overflowDistanceMul(int op1, int op2) {
		int result = op1 * op2;
		if (op1 > 0 && op2 > 0) {
			// result has to be < 0 for overflow
			// the result can be so large that it overflows so much to be positive again
			// so we need to use longs to check this
			long longResult = (long)op1 * (long)(op2);
			if(longResult > Integer.MAX_VALUE) {
				if(result <= 0)
					return result;
				else
					return Integer.MIN_VALUE;
			}
			else {
				int retval = HALFWAY - scaleTo(result, HALFWAY);
				if(retval > 0)
					return retval;
				else
					return 1;
			}
			//System.out.println(op1+" * "+op2 +" -> "+result +" -> "+(HALFWAY - scaleTo(result, HALFWAY)));
			//return result <= 0 ? result : HALFWAY - scaleTo(result, HALFWAY);
		} else if (op1 < 0 && op2 < 0) {
			return result <= 0 ? result : HALFWAY - scaleTo(result, HALFWAY);
		} else if (op1 > 0 && op2 < 0) {
			// In this case we can't have an overflow yet
			return HALFWAY + scaleTo(Math.abs(op2), HALFWAY);
		} else if (op1 < 0 && op2 > 0) {
			return HALFWAY + scaleTo(Math.abs(op1), HALFWAY);
		} else {
			// One of them is zero
			return HALFWAY;
		}
	}

	protected static int underflowDistanceMul(int op1, int op2) {
		int result = op1 * op2;
		if (op1 > 0 && op2 < 0) {
			return result >= 0 ? -result : HALFWAY - scaleTo(result, HALFWAY);
		} else if (op1 < 0 && op2 > 0) {
			return result >= 0 ? -result : HALFWAY - scaleTo(result, HALFWAY);
		} else if (op1 > 0 && op2 > 0) {
			return HALFWAY + scaleTo(Math.min(op1, op2), HALFWAY);
		} else if (op1 < 0 && op2 < 0) {
			return HALFWAY + scaleTo(Math.abs(Math.max(op1, op2)), HALFWAY);
		} else {
			// One of them is zero
			return HALFWAY;
		}
	}

	protected static int overflowDistanceDiv(int op1, int op2) {
		if (op1 == Integer.MIN_VALUE && op2 == -1)
			return -1;
		else {
			// If op2 is MAX_VALUE then -1 -op2 will give us an overflow
			if(op2 == Integer.MAX_VALUE)
				return Integer.MAX_VALUE;
			
			// TODO There may be an overflow here
			return scaleTo(Math.abs(Integer.MIN_VALUE - op1), HALFWAY)
			        + scaleTo(Math.abs(-1 - op2), HALFWAY);
		}
	}

	public static int underflowDistance(int op1, int op2, int opcode) {
		switch (opcode) {

		case Opcodes.IADD:
			int result = underflowDistanceAdd(op1, op2);
			logger.debug("U: "+op1+" + "+op2+" = "+(op1+op2)+" -> "+result);
			return result;

		case Opcodes.ISUB:
			return underflowDistanceSub(op1, op2);

		case Opcodes.IMUL:
			return underflowDistanceMul(op1, op2);

		}
		return Integer.MAX_VALUE;
	}

	public static int overflowDistance(float op1, float op2, int opcode) {
		switch (opcode) {

		case Opcodes.FADD:
			return overflowDistanceAdd(op1, op2);

		case Opcodes.FSUB:
			return overflowDistanceSub(op1, op2);

		case Opcodes.FMUL:
			return overflowDistanceMul(op1, op2);

		case Opcodes.FDIV:
			return overflowDistanceDiv(op1, op2);
		}
		return Integer.MAX_VALUE;
	}

	public static int underflowDistance(float op1, float op2, int opcode) {
		switch (opcode) {

		case Opcodes.FADD:
			return underflowDistanceAdd(op1, op2);

		case Opcodes.FSUB:
			return underflowDistanceSub(op1, op2);

		case Opcodes.FMUL:
			return underflowDistanceMul(op1, op2);

		}
		return Integer.MAX_VALUE;
	}

	protected static int overflowDistanceAdd(float op1, float op2) {
		float result = op1 + op2;
		if (op1 > 0 && op2 > 0) {
			// result has to be < 0 for overflow
			return result == Float.POSITIVE_INFINITY ? -1 : HALFWAY
			        - scaleTo(result, HALFWAY) + 1;

		} else if (op1 < 0 && op2 < 0) {
			// if both are negative then both need to be increased
			return result == Float.NEGATIVE_INFINITY ? Integer.MAX_VALUE : HALFWAY
			        + scaleTo((double) op1 + (double) op2, HALFWAY);
		} else if (op1 >= 0 && op2 < 0) {
			// If only one is negative, then optimize that to be positive
			return HALFWAY + scaleTo(Math.abs(op2), HALFWAY);
		} else if (op1 < 0 && op2 >= 0) {
			// If only one is negative, then optimize that to be positive
			return HALFWAY + scaleTo(Math.abs(op1), HALFWAY);
		} else {
			// At least one of them is zero, and the sum is larger or equals than 0
			return 1 + HALFWAY - scaleTo(result, HALFWAY);
		}
	}

	protected static int underflowDistanceAdd(float op1, float op2) {
		float result = op1 + op2;
		if (op1 <= 0 && op2 <= 0) {
			// result has to be < 0 for overflow
			return result == Float.NEGATIVE_INFINITY ? -1 : HALFWAY
			        - scaleTo(Math.abs((double) result), HALFWAY) + 1;
		} else if (op1 > 0 && op2 > 0) {
			// if both are positive then both need to be decreased
			return result == Float.POSITIVE_INFINITY ? Integer.MAX_VALUE : HALFWAY
			        + scaleTo(Math.abs((double) op1) + Math.abs((double) op2), HALFWAY);
		} else if (op1 >= 0 && op2 < 0) {
			return HALFWAY + scaleTo(op1, HALFWAY);
		} else if (op1 < 0 && op2 >= 0) {
			return HALFWAY + scaleTo(op2, HALFWAY);
		} else {
			// Unreachable
			return Integer.MAX_VALUE;
		}
	}

	protected static int overflowDistanceSub(float op1, float op2) {
		float result = op1 - op2;
		if (op1 >= 0 && op2 <= 0) {
			// result has to be < 0 for overflow
			return result == Float.POSITIVE_INFINITY ? -1 : HALFWAY + 1
			        - scaleTo(result, HALFWAY);
		} else if (op1 < 0 && op2 > 0) {
			// if both are negative then an overflow will be difficult
			return result == Float.NEGATIVE_INFINITY ? Integer.MAX_VALUE : HALFWAY
			        + scaleTo(Math.abs((double) op1) + Math.abs((double) op2), HALFWAY);
		} else if (op1 >= 0 && op2 > 0) {
			// In this case we can't have an overflow yet
			return HALFWAY + scaleTo(op2, HALFWAY);
		} else if (op1 < 0 && op2 <= 0) {
			return HALFWAY + scaleTo(Math.abs((double) op1), HALFWAY);
		} else {
			// At least one of them is zero, and the sum is larger or equals than 0
			return 1 + HALFWAY - scaleTo(result, HALFWAY);
		}
	}

	protected static int underflowDistanceSub(float op1, float op2) {
		float result = op1 - op2;
		if (op1 <= 0 && op2 >= 0) {
			return result == Float.NEGATIVE_INFINITY ? -1 : HALFWAY + 1
			        - scaleTo(result, HALFWAY);
		} else if (op1 > 0 && op2 < 0) {
			return result == Float.POSITIVE_INFINITY ? Integer.MAX_VALUE : HALFWAY
			        + scaleTo(Math.abs((double) op1) + Math.abs((double) op2), HALFWAY);
		} else if (op1 >= 0 && op2 > 0) {
			// In this case we can't have an overflow yet
			return HALFWAY + scaleTo(Math.abs((double) op1), HALFWAY);
		} else if (op1 < 0 && op2 <= 0) {
			return HALFWAY + scaleTo(Math.abs((double) op2), HALFWAY);
		} else {
			// Not sure if this can be reached
			return 1 + HALFWAY - scaleTo(result, HALFWAY);
		}
	}

	protected static int overflowDistanceMul(float op1, float op2) {
		float result = op1 * op2;
		if (op1 > 0 && op2 > 0) {
			// result has to be < 0 for overflow
			return result == Float.POSITIVE_INFINITY ? -1 : HALFWAY
			        - scaleTo(result, HALFWAY);
		} else if (op1 < 0 && op2 < 0) {
			return result == Float.POSITIVE_INFINITY ? -1 : HALFWAY
			        - scaleTo(result, HALFWAY) + 1;
		} else if (op1 > 0 && op2 < 0) {
			// In this case we can't have an overflow yet
			return result == Float.NEGATIVE_INFINITY ? Integer.MAX_VALUE : HALFWAY
			        + scaleTo(Math.abs(op2), HALFWAY);
		} else if (op1 < 0 && op2 > 0) {
			return result == Float.NEGATIVE_INFINITY ? Integer.MAX_VALUE : HALFWAY
			        + scaleTo(Math.abs(op1), HALFWAY);
		} else {
			// One of them is zero
			return HALFWAY;
		}
	}

	protected static int underflowDistanceMul(float op1, float op2) {
		float result = op1 * op2;
		if (op1 > 0 && op2 < 0) {
			return result == Float.NEGATIVE_INFINITY ? -1 : HALFWAY
			        - scaleTo(result, HALFWAY);
		} else if (op1 < 0 && op2 > 0) {
			return result == Float.NEGATIVE_INFINITY ? -1 : HALFWAY
			        - scaleTo(result, HALFWAY);
		} else if (op1 > 0 && op2 > 0) {
			return result == Float.POSITIVE_INFINITY ? Integer.MAX_VALUE : HALFWAY
			        + scaleTo(Math.min(op1, op2), HALFWAY);
		} else if (op1 < 0 && op2 < 0) {
			return result == Float.POSITIVE_INFINITY ? Integer.MAX_VALUE : HALFWAY
			        + scaleTo(Math.abs(Math.max(op1, op2)), HALFWAY);
		} else {
			// One of them is zero
			return HALFWAY;
		}
	}

	protected static int overflowDistanceDiv(float op1, float op2) {
		if (op1 == -Float.MAX_VALUE && op2 == -1.0)
			return -1;
		else
			// TODO There may be an overflow here
			return scaleTo(Math.abs(-Float.MAX_VALUE - op1), HALFWAY)
			        + scaleTo(Math.abs(-1.0 - op2), HALFWAY);
	}

	public static int overflowDistance(double op1, double op2, int opcode) {
		switch (opcode) {

		case Opcodes.DADD:
			return overflowDistanceAdd(op1, op2);

		case Opcodes.DSUB:
			return overflowDistanceSub(op1, op2);

		case Opcodes.DMUL:
			return overflowDistanceMul(op1, op2);

		case Opcodes.DDIV:
			return overflowDistanceDiv(op1, op2);
		}
		return Integer.MAX_VALUE;
	}

	public static int underflowDistance(double op1, double op2, int opcode) {
		switch (opcode) {

		case Opcodes.DADD:
			return underflowDistanceAdd(op1, op2);

		case Opcodes.DSUB:
			return underflowDistanceSub(op1, op2);

		case Opcodes.DMUL:
			return underflowDistanceMul(op1, op2);

		}
		return Integer.MAX_VALUE;
	}

	protected static int overflowDistanceAdd(double op1, double op2) {
		double result = op1 + op2;
		if (op1 > 0 && op2 > 0) {
			// result has to be < 0 for overflow
			return result == Double.POSITIVE_INFINITY ? -1 : HALFWAY
			        - scaleTo(result, HALFWAY) + 1;

		} else if (op1 < 0 && op2 < 0) {
			return result == Double.NEGATIVE_INFINITY ? Integer.MAX_VALUE : HALFWAY
			        - scaleTo(result, HALFWAY) + 1;
		} else if (op1 >= 0 && op2 < 0) {
			// If only one is negative, then optimize that to be positive
			return HALFWAY + scaleTo(Math.abs(op2), HALFWAY);
		} else if (op1 < 0 && op2 >= 0) {
			// If only one is negative, then optimize that to be positive
			return HALFWAY + scaleTo(Math.abs(op1), HALFWAY);
		} else {
			// At least one of them is zero, and the sum is larger or equals than 0
			return 1 + HALFWAY - scaleTo(result, HALFWAY);
		}
	}

	protected static int underflowDistanceAdd(double op1, double op2) {
		double result = op1 + op2;
		if (op1 <= 0 && op2 <= 0) {
			// result has to be < 0 for overflow
			return result == Double.NEGATIVE_INFINITY ? -1 : HALFWAY
			        - scaleTo(Math.abs(result), HALFWAY) + 1;
		} else if (op1 > 0 && op2 > 0) {
			// if both are positive then both need to be decreased
			return result == Double.POSITIVE_INFINITY ? Integer.MAX_VALUE : HALFWAY
			        + scaleTo(Math.abs(op1) + Math.abs(op2), HALFWAY);
		} else if (op1 >= 0 && op2 < 0) {
			return HALFWAY + scaleTo(op1, HALFWAY);
		} else if (op1 < 0 && op2 >= 0) {
			return HALFWAY + scaleTo(op2, HALFWAY);
		} else {
			// Unreachable
			return Integer.MAX_VALUE;
		}
	}

	protected static int overflowDistanceSub(double op1, double op2) {
		double result = op1 - op2;
		if (op1 >= 0 && op2 <= 0) {
			// result has to be < 0 for overflow
			return result == Double.POSITIVE_INFINITY ? -1 : HALFWAY + 1
			        - scaleTo(result, HALFWAY);
		} else if (op1 < 0 && op2 > 0) {
			// if both are negative then an overflow will be difficult
			return result == Double.NEGATIVE_INFINITY ? Integer.MAX_VALUE : HALFWAY
			        + scaleTo(Math.abs(op1) + Math.abs(op2), HALFWAY);
		} else if (op1 >= 0 && op2 > 0) {
			// In this case we can't have an overflow yet
			return HALFWAY + scaleTo(op2, HALFWAY);
		} else if (op1 < 0 && op2 <= 0) {
			return HALFWAY + scaleTo(Math.abs(op1), HALFWAY);
		} else {
			// At least one of them is zero, and the sum is larger or equals than 0
			return 1 + HALFWAY - scaleTo(result, HALFWAY);
		}
	}

	protected static int underflowDistanceSub(double op1, double op2) {
		double result = op1 - op2;
		if (op1 <= 0 && op2 >= 0) {
			return result == Double.NEGATIVE_INFINITY ? -1 : HALFWAY + 1
			        - scaleTo(result, HALFWAY);
		} else if (op1 > 0 && op2 < 0) {
			return result == Double.POSITIVE_INFINITY ? Integer.MAX_VALUE : HALFWAY
			        + scaleTo(Math.abs(op1) + Math.abs(op2), HALFWAY);
		} else if (op1 >= 0 && op2 > 0) {
			// In this case we can't have an overflow yet
			return HALFWAY + scaleTo(Math.abs(op1), HALFWAY);
		} else if (op1 < 0 && op2 <= 0) {
			return HALFWAY + scaleTo(Math.abs(op2), HALFWAY);
		} else {
			// Not sure if this can be reached
			return 1 + HALFWAY - scaleTo(result, HALFWAY);
		}
	}

	protected static int overflowDistanceMul(double op1, double op2) {
		double result = op1 * op2;
		if (op1 > 0 && op2 > 0) {
			// result has to be < 0 for overflow
			return result == Double.POSITIVE_INFINITY ? -1 : HALFWAY
			        - scaleTo(result, HALFWAY);
		} else if (op1 < 0 && op2 < 0) {
			return result == Double.POSITIVE_INFINITY ? -1 : HALFWAY
			        - scaleTo(result, HALFWAY) + 1;
		} else if (op1 > 0 && op2 < 0) {
			// In this case we can't have an overflow yet
			return result == Double.NEGATIVE_INFINITY ? Integer.MAX_VALUE : HALFWAY
			        + scaleTo(Math.abs(op2), HALFWAY);
		} else if (op1 < 0 && op2 > 0) {
			return result == Double.NEGATIVE_INFINITY ? Integer.MAX_VALUE : HALFWAY
			        + scaleTo(Math.abs(op1), HALFWAY);
		} else {
			// One of them is zero
			return HALFWAY;
		}
	}

	protected static int underflowDistanceMul(double op1, double op2) {
		double result = op1 * op2;
		if (op1 > 0 && op2 < 0) {
			return result == Double.NEGATIVE_INFINITY ? -1 : HALFWAY
			        - scaleTo(result, HALFWAY);
		} else if (op1 < 0 && op2 > 0) {
			return result == Double.NEGATIVE_INFINITY ? -1 : HALFWAY
			        - scaleTo(result, HALFWAY);
		} else if (op1 > 0 && op2 > 0) {
			return result == Double.POSITIVE_INFINITY ? Integer.MAX_VALUE : HALFWAY
			        + scaleTo(Math.min(op1, op2), HALFWAY);
		} else if (op1 < 0 && op2 < 0) {
			return result == Double.POSITIVE_INFINITY ? Integer.MAX_VALUE : HALFWAY
			        + scaleTo(Math.abs(Math.max(op1, op2)), HALFWAY);
		} else {
			// One of them is zero
			return HALFWAY;
		}
	}

	protected static int overflowDistanceDiv(double op1, double op2) {
		if (op1 == -Double.MAX_VALUE && op2 == -1.0)
			return -1;
		else
			// TODO There may be an overflow here
			return scaleTo(Math.abs(-Double.MAX_VALUE - op1), HALFWAY)
			        + scaleTo(Math.abs(-1.0 - op2), HALFWAY);
	}

	public static int overflowDistance(long op1, long op2, int opcode) {
		switch (opcode) {

		case Opcodes.LADD:
			return overflowDistanceAdd(op1, op2);

		case Opcodes.LSUB:
			return overflowDistanceSub(op1, op2);

		case Opcodes.LMUL:
			return overflowDistanceMul(op1, op2);

		case Opcodes.LDIV:
			return overflowDistanceDiv(op1, op2);
		}
		return Integer.MAX_VALUE;
	}

	public static int underflowDistance(long op1, long op2, int opcode) {
		switch (opcode) {

		case Opcodes.LADD:
			return underflowDistanceAdd(op1, op2);

		case Opcodes.LSUB:
			return underflowDistanceSub(op1, op2);

		case Opcodes.LMUL:
			return underflowDistanceMul(op1, op2);

		}
		return Integer.MAX_VALUE;
	}

	protected static int overflowDistanceAdd(long op1, long op2) {
		long result = op1 + op2;
		if (op1 > 0 && op2 > 0) {
			// result has to be < 0 for overflow
			return result < 0 ? -scaleTo(Math.abs(result), HALFWAY) : HALFWAY
			        - scaleTo(result, HALFWAY) + 1;

		} else if (op1 < 0 && op2 < 0) {
			return result > 0 ? Integer.MAX_VALUE : HALFWAY - scaleTo(result, HALFWAY)
			        + 1;
		} else if (op1 >= 0 && op2 < 0) {
			// If only one is negative, then optimize that to be positive
			return HALFWAY + scaleTo(Math.abs(op2), HALFWAY);
		} else if (op1 < 0 && op2 >= 0) {
			// If only one is negative, then optimize that to be positive
			return HALFWAY + scaleTo(Math.abs(op1), HALFWAY);
		} else {
			// At least one of them is zero, and the sum is larger or equals than 0
			return 1 + HALFWAY - scaleTo(result, HALFWAY);
		}
	}

	protected static int underflowDistanceAdd(long op1, long op2) {
		long result = op1 + op2;
		if (op1 <= 0L && op2 <= 0L) {
			// result has to be < 0 for underflow
			if(result > 0) {
				int retval = -scaleTo(result, HALFWAY);
				if(retval < 0)
					return retval;
				else
					return -1;
			} else if(result == 0L) {
				if(op1 != 0 && op2 != 0) {
					return -1;					
				} else {
					return HALFWAY
					        - scaleTo(Math.abs(result), HALFWAY);
				}
			} else {
				int intResult = HALFWAY
				        - scaleTo(Math.abs(result), HALFWAY);
				if(intResult == 0)
					return 1;
				else
					return intResult;
			}
		} else if (op1 > 0 && op2 > 0) {
			// if both are positive then both need to be decreased
			return result < 0 ? Integer.MAX_VALUE : HALFWAY
			        + scaleTo(Math.abs(op1) + Math.abs(op2), HALFWAY);
		} else if (op1 >= 0 && op2 < 0) {
			return HALFWAY + scaleTo(op1, HALFWAY);
		} else if (op1 < 0 && op2 >= 0) {
			return HALFWAY + scaleTo(op2, HALFWAY);
		} else {
			// Unreachable
			return Integer.MAX_VALUE;
		}
	}

	protected static int overflowDistanceSub(long op1, long op2) {
		long result = op1 - op2;
		if (op1 >= 0 && op2 <= 0) {
			// result has to be < 0 for overflow
			return result < 0 ? -scaleTo(Math.abs(result), HALFWAY) : HALFWAY + 1
			        - scaleTo(result, HALFWAY);
		} else if (op1 < 0 && op2 > 0) {
			// if both are negative then an overflow will be difficult
			return result > 0 ? Integer.MAX_VALUE : HALFWAY
			        + scaleTo(Math.abs(op1) + Math.abs(op2), HALFWAY);
		} else if (op1 >= 0 && op2 > 0) {
			// In this case we can't have an overflow yet
			return HALFWAY + scaleTo(op2, HALFWAY);
		} else if (op1 < 0 && op2 <= 0) {
			return HALFWAY + scaleTo(Math.abs(op1), HALFWAY);
		} else {
			// At least one of them is zero, and the sum is larger or equals than 0
			return 1 + HALFWAY - scaleTo(result, HALFWAY);
		}
	}

	protected static int underflowDistanceSub(long op1, long op2) {
		long result = op1 - op2;
		if (op1 <= 0 && op2 >= 0) {
			return result > 0 ? -scaleTo(result, HALFWAY) : HALFWAY + 1
			        - scaleTo(result, HALFWAY);
		} else if (op1 > 0 && op2 < 0) {
			return result < 0 ? Integer.MAX_VALUE : HALFWAY
			        + scaleTo(Math.abs(op1) + Math.abs(op2), HALFWAY);
		} else if (op1 >= 0 && op2 > 0) {
			// In this case we can't have an overflow yet
			return HALFWAY + scaleTo(Math.abs(op1), HALFWAY);
		} else if (op1 < 0 && op2 <= 0) {
			return 1 + HALFWAY + scaleTo(Math.abs(op2), HALFWAY);
		} else {
			// Not sure if this can be reached
			return 1 + HALFWAY - scaleTo(result, HALFWAY);
		}
	}

	protected static int overflowDistanceMul(long op1, long op2) {
		long result = op1 * op2;
		if ((op1 > 0 && op2 > 0) || (op1 < 0 && op2 < 0)) {
			BigDecimal bigDecimal = new BigDecimal(op1).multiply(new BigDecimal(op2));
			BigDecimal maxResult = new BigDecimal(Long.MAX_VALUE);
			
			if(bigDecimal.compareTo(maxResult) > 0) {
				int intResult = -scaleTo(Math.abs(result), HALFWAY); 
				if(result <= 0)
					return intResult;
				else
					return Integer.MIN_VALUE;
			}
			else {
				int retval = HALFWAY - scaleTo(result, HALFWAY);
				if(retval > 0)
					return retval;
				else
					return 1;
			}
			// result has to be < 0 for overflow
		} else if (op1 > 0 && op2 < 0) {
			// In this case we can't have an overflow yet
			return result > 0 ? Integer.MAX_VALUE : HALFWAY
			        + scaleTo(Math.abs(op2), HALFWAY);
		} else if (op1 < 0 && op2 > 0) {
			return result > 0 ? Integer.MAX_VALUE : HALFWAY
			        + scaleTo(Math.abs(op1), HALFWAY);
		} else {
			// One of them is zero
			return HALFWAY;
		}
	}

	protected static int underflowDistanceMul(long op1, long op2) {
		long result = op1 * op2;
		BigDecimal bigDecimal = new BigDecimal(op1).multiply(new BigDecimal(op2));
		BigDecimal minResult = new BigDecimal(Long.MIN_VALUE);

		if(bigDecimal.compareTo(minResult) < 0) {
			int intResult = -scaleTo(Math.abs(result), HALFWAY); 
			if(result <= 0)
				return intResult;
			else
				return Integer.MIN_VALUE;
		}
		else {
			int retval = HALFWAY - scaleTo(result, HALFWAY);
			if(retval > 0)
				return retval;
			else
				return 1;
		}		
	}

	protected static int overflowDistanceDiv(long op1, long op2) {
		if (op1 == Long.MIN_VALUE && op2 == -1L)
			return -1;
		else
			// TODO There may be an overflow here
			return scaleTo(Math.abs(Long.MIN_VALUE - op1), HALFWAY)
			        + scaleTo(Math.abs(-1L - op2), HALFWAY);
	}

}
