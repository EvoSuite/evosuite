package org.evosuite.symbolic.vm;

import org.evosuite.symbolic.expr.IntegerBinaryExpression;
import org.evosuite.symbolic.expr.IntegerComparison;
import org.evosuite.symbolic.expr.IntegerConstant;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.IntegerToRealCast;
import org.evosuite.symbolic.expr.IntegerUnaryExpression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealBinaryExpression;
import org.evosuite.symbolic.expr.RealComparison;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.RealToIntegerCast;
import org.evosuite.symbolic.expr.RealUnaryExpression;

import edu.uta.cse.dsc.AbstractVM;

/**
 * ByteCode instructions that pop operands off the stack, perform some
 * computation, and optionally push the result back onto the stack. - No heap
 * access - No local variable access - No branching
 * 
 * @author csallner@uta.edu (Christoph Csallner)
 */
public final class ArithmeticVM extends AbstractVM {

	private final SymbolicEnvironment env;

	private final PathConstraint pathConstraint;

	public ArithmeticVM(SymbolicEnvironment env, PathConstraint pathConstraint) {
		this.env = env;
		this.pathConstraint = pathConstraint;
	}

	private boolean zeroViolation(IntegerExpression value, long valueConcrete) {
		IntegerConstant zero = ExpressionFactory.ICONST_0;
		IntegerConstraint zeroCheck;
		if (valueConcrete == 0)
			zeroCheck = ConstraintFactory.eq(value, zero);
		else
			zeroCheck = ConstraintFactory.neq(value, zero);

		if (zeroCheck.getLeftOperand().containsSymbolicVariable()
				|| zeroCheck.getRightOperand().containsSymbolicVariable())
			pathConstraint.pushSupportingConstraint(zeroCheck);

		if (valueConcrete == 0) {
			// JVM will throw an exception
			return true;
		}

		return false;
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc11.html#pop
	 */
	@Override
	public void POP() {
		OperandStack stack = env.topFrame().operandStack;
		Operand a = stack.popOperand();
		if (!(a instanceof SingleWordOperand)) {
			throw new IllegalStateException(
					"pop should be applied iif top is SingleWordOperand");
		}
	}

	/**
	 * One of the following two:
	 * 
	 * Pop two category-1 operands from the stack. Pop single category-2 operand
	 * from the stack.
	 * 
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc11.html#pop2
	 */
	@Override
	public void POP2() {
		OperandStack stack = env.topFrame().operandStack;
		Operand top = stack.popOperand();

		if (top instanceof DoubleWordOperand)
			/* Form 2 */
			return;

		/* Form 1 */
		stack.popOperand();
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc3.html#dup
	 */
	@Override
	public void DUP() {
		Operand x = env.topFrame().operandStack.peekOperand();
		env.topFrame().operandStack.pushOperand(x);
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc3.html#dup_x1
	 */
	@Override
	public void DUP_X1() {
		OperandStack stack = env.topFrame().operandStack;

		Operand a = stack.popOperand();
		Operand b = stack.popOperand();

		stack.pushOperand(a);
		stack.pushOperand(b);
		stack.pushOperand(a);
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc3.html#dup_x2
	 */
	@Override
	public void DUP_X2() {
		OperandStack stack = env.topFrame().operandStack;

		Operand a = stack.popOperand();
		Operand b = stack.popOperand();

		if (b instanceof SingleWordOperand) {
			Operand c = stack.popOperand();
			stack.pushOperand(a);
			stack.pushOperand(c);
			stack.pushOperand(b);
			stack.pushOperand(a);
		} else {
			stack.pushOperand(a);
			stack.pushOperand(b);
			stack.pushOperand(a);
		}
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc3.html#dup2
	 */
	@Override
	public void DUP2() {
		OperandStack stack = env.topFrame().operandStack;
		Operand a = stack.popOperand();

		if (a instanceof SingleWordOperand) {
			/* Form 1 */
			Operand b = stack.popOperand();
			stack.pushOperand(b);
			stack.pushOperand(a);
			stack.pushOperand(b);
			stack.pushOperand(a);
		} else {
			/* Form 2 */
			stack.pushOperand(a);
			stack.pushOperand(a);
		}

	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc3.html#dup2_x1
	 */
	@Override
	public void DUP2_X1() {
		OperandStack stack = env.topFrame().operandStack;

		Operand expression = stack.popOperand();

		if (expression instanceof SingleWordOperand) {
			/* Form 1 */
			Operand a = expression;
			Operand b = stack.popOperand();
			Operand c = stack.popOperand();
			stack.pushOperand(b);
			stack.pushOperand(a);
			stack.pushOperand(c);
			stack.pushOperand(b);
			stack.pushOperand(a);
		} else {
			/* Form 2 */
			Operand a = expression;
			Operand b = stack.popOperand();
			stack.pushOperand(a);
			stack.pushOperand(b);
			stack.pushOperand(a);
		}

	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc3.html#dup2_x2
	 */
	@Override
	public void DUP2_X2() {
		OperandStack stack = env.topFrame().operandStack;

		Operand first = stack.popOperand();
		Operand second = stack.popOperand();

		if (first instanceof DoubleWordOperand) {
			Operand a = first;

			if (second instanceof DoubleWordOperand) {
				/* Form 4 */
				Operand b = second;
				stack.pushOperand(a);
				stack.pushOperand(b);
				stack.pushOperand(a);
			} else {
				/* Form 2 */
				Operand b = second;
				Operand c = stack.popOperand();
				stack.pushOperand(a);
				stack.pushOperand(c);
				stack.pushOperand(b);
				stack.pushOperand(a);
			}
		} else {
			Operand a = first;
			Operand b = second;
			Operand third = stack.popOperand();

			if (third instanceof DoubleWordOperand) {
				/* Form 3 */
				Operand c = third;
				stack.pushOperand(b);
				stack.pushOperand(a);
				stack.pushOperand(c);
				stack.pushOperand(b);
				stack.pushOperand(a);
			} else {
				/* Form 1 */
				Operand c = third;
				Operand d = stack.popOperand();
				stack.pushOperand(b);
				stack.pushOperand(a);
				stack.pushOperand(d);
				stack.pushOperand(c);
				stack.pushOperand(b);
				stack.pushOperand(a);
			}
		}

	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc13.html#swap
	 */
	@Override
	public void SWAP() {
		OperandStack stack = env.topFrame().operandStack;
		Operand a = stack.popOperand();
		Operand b = stack.popOperand();
		stack.pushOperand(a);
		stack.pushOperand(b);
	}

	/**
	 * @see http 
	 *      ://java.sun.com/docs/books/jvms/second_edition/html/Instructions2
	 *      .doc6.html#iadd
	 */
	@Override
	public void IADD() {
		IntegerExpression right = env.topFrame().operandStack.popBv32();
		IntegerExpression left = env.topFrame().operandStack.popBv32();

		int left_concrete_value = ((Long) left.getConcreteValue()).intValue();
		int right_concrete_value = ((Long) right.getConcreteValue()).intValue();

		if (!left.containsSymbolicVariable()) {
			left = ExpressionFactory
					.buildNewIntegerConstant(left_concrete_value);
		}
		if (!right.containsSymbolicVariable()) {
			right = ExpressionFactory
					.buildNewIntegerConstant(right_concrete_value);
		}

		int con = left_concrete_value + right_concrete_value;

		IntegerExpression intExpr = new IntegerBinaryExpression(left,
				Operator.PLUS, right, (long) con);

		env.topFrame().operandStack.pushBv32(intExpr);

	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc8.html#ladd
	 */
	@Override
	public void LADD() {
		IntegerExpression right = env.topFrame().operandStack.popBv64();
		IntegerExpression left = env.topFrame().operandStack.popBv64();

		long left_concrete_value = ((Long) left.getConcreteValue()).longValue();
		long right_concrete_value = ((Long) right.getConcreteValue())
				.longValue();

		if (!left.containsSymbolicVariable()) {
			left = ExpressionFactory
					.buildNewIntegerConstant(left_concrete_value);
		}
		if (!right.containsSymbolicVariable()) {
			right = ExpressionFactory
					.buildNewIntegerConstant(right_concrete_value);
		}

		long con = left_concrete_value + right_concrete_value;

		IntegerExpression intExpr = new IntegerBinaryExpression(left,
				Operator.PLUS, right, (long) con);

		env.topFrame().operandStack.pushBv64(intExpr);
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc4.html#fadd
	 */
	@Override
	public void FADD() {
		RealExpression right = env.topFrame().operandStack.popFp32();
		RealExpression left = env.topFrame().operandStack.popFp32();

		float left_concrete_value = ((Double) left.getConcreteValue())
				.floatValue();
		float right_concrete_value = ((Double) right.getConcreteValue())
				.floatValue();

		if (!left.containsSymbolicVariable()) {
			left = ExpressionFactory.buildNewRealConstant(left_concrete_value);
		}
		if (!right.containsSymbolicVariable()) {
			right = ExpressionFactory
					.buildNewRealConstant(right_concrete_value);
		}

		float con = left_concrete_value + right_concrete_value;

		RealExpression realExpr = new RealBinaryExpression(left, Operator.PLUS,
				right, (double) con);

		env.topFrame().operandStack.pushFp32(realExpr);

	}

	@Override
	public void DADD() {
		RealExpression right = env.topFrame().operandStack.popFp64();
		RealExpression left = env.topFrame().operandStack.popFp64();

		double left_concrete_value = ((Double) left.getConcreteValue())
				.doubleValue();
		double right_concrete_value = ((Double) right.getConcreteValue())
				.doubleValue();

		if (!left.containsSymbolicVariable()) {
			left = ExpressionFactory.buildNewRealConstant(left_concrete_value);
		}
		if (!right.containsSymbolicVariable()) {
			right = ExpressionFactory
					.buildNewRealConstant(right_concrete_value);
		}

		double con = left_concrete_value + right_concrete_value;

		RealExpression realExpr = new RealBinaryExpression(left, Operator.PLUS,
				right, con);

		env.topFrame().operandStack.pushFp64(realExpr);
	}

	/**
	 * @see http 
	 *      ://java.sun.com/docs/books/jvms/second_edition/html/Instructions2
	 *      .doc6.html#isub
	 */
	@Override
	public void ISUB() {
		IntegerExpression right = env.topFrame().operandStack.popBv32();
		IntegerExpression left = env.topFrame().operandStack.popBv32();

		int left_concrete_value = ((Long) left.getConcreteValue()).intValue();
		int right_concrete_value = ((Long) right.getConcreteValue()).intValue();

		if (!left.containsSymbolicVariable()) {
			left = ExpressionFactory
					.buildNewIntegerConstant(left_concrete_value);
		}
		if (!right.containsSymbolicVariable()) {
			right = ExpressionFactory
					.buildNewIntegerConstant(right_concrete_value);
		}

		int con = left_concrete_value - right_concrete_value;

		IntegerExpression intExpr = new IntegerBinaryExpression(left,
				Operator.MINUS, right, (long) con);

		env.topFrame().operandStack.pushBv32(intExpr);

	}

	/**
	 * @see http 
	 *      ://java.sun.com/docs/books/jvms/second_edition/html/Instructions2
	 *      .doc8.html#lsub
	 */
	@Override
	public void LSUB() {
		IntegerExpression right = env.topFrame().operandStack.popBv64();
		IntegerExpression left = env.topFrame().operandStack.popBv64();

		long left_concrete_value = ((Long) left.getConcreteValue()).longValue();
		long right_concrete_value = ((Long) right.getConcreteValue())
				.longValue();

		if (!left.containsSymbolicVariable()) {
			left = ExpressionFactory
					.buildNewIntegerConstant(left_concrete_value);
		}
		if (!right.containsSymbolicVariable()) {
			right = ExpressionFactory
					.buildNewIntegerConstant(right_concrete_value);
		}

		long con = left_concrete_value - right_concrete_value;

		IntegerExpression intExpr = new IntegerBinaryExpression(left,
				Operator.MINUS, right, (long) con);

		env.topFrame().operandStack.pushBv64(intExpr);
	}

	/**
	 *  
	 */
	@Override
	public void FSUB() {
		RealExpression right = env.topFrame().operandStack.popFp32();
		RealExpression left = env.topFrame().operandStack.popFp32();

		float left_concrete_value = ((Double) left.getConcreteValue())
				.floatValue();
		float right_concrete_value = ((Double) right.getConcreteValue())
				.floatValue();

		if (!left.containsSymbolicVariable()) {
			left = ExpressionFactory.buildNewRealConstant(left_concrete_value);
		}
		if (!right.containsSymbolicVariable()) {
			right = ExpressionFactory
					.buildNewRealConstant(right_concrete_value);
		}

		float con = left_concrete_value - right_concrete_value;

		RealExpression realExpr = new RealBinaryExpression(left,
				Operator.MINUS, right, (double) con);

		env.topFrame().operandStack.pushFp32(realExpr);
	}

	@Override
	public void DSUB() {
		RealExpression right = env.topFrame().operandStack.popFp64();
		RealExpression left = env.topFrame().operandStack.popFp64();

		double left_concrete_value = ((Double) left.getConcreteValue())
				.doubleValue();
		double right_concrete_value = ((Double) right.getConcreteValue())
				.doubleValue();

		if (!left.containsSymbolicVariable()) {
			left = ExpressionFactory.buildNewRealConstant(left_concrete_value);
		}
		if (!right.containsSymbolicVariable()) {
			right = ExpressionFactory
					.buildNewRealConstant(right_concrete_value);
		}

		double con = left_concrete_value - right_concrete_value;

		RealExpression realExpr = new RealBinaryExpression(left,
				Operator.MINUS, right, con);

		env.topFrame().operandStack.pushFp64(realExpr);
	}

	/**
	 * @see http 
	 *      ://java.sun.com/docs/books/jvms/second_edition/html/Instructions2
	 *      .doc6.html#imul
	 */
	@Override
	public void IMUL() {
		IntegerExpression right = env.topFrame().operandStack.popBv32();
		IntegerExpression left = env.topFrame().operandStack.popBv32();

		int left_concrete_value = ((Long) left.getConcreteValue()).intValue();
		int right_concrete_value = ((Long) right.getConcreteValue()).intValue();

		if (!left.containsSymbolicVariable()) {
			left = ExpressionFactory
					.buildNewIntegerConstant(left_concrete_value);
		}
		if (!right.containsSymbolicVariable()) {
			right = ExpressionFactory
					.buildNewIntegerConstant(right_concrete_value);
		}

		int con = left_concrete_value * right_concrete_value;

		IntegerExpression intExpr = new IntegerBinaryExpression(left,
				Operator.MUL, right, (long) con);

		env.topFrame().operandStack.pushBv32(intExpr);

	}

	/**
	 * @see http 
	 *      ://java.sun.com/docs/books/jvms/second_edition/html/Instructions2
	 *      .doc8.html#lmul
	 */
	@Override
	public void LMUL() {
		IntegerExpression right = env.topFrame().operandStack.popBv64();
		IntegerExpression left = env.topFrame().operandStack.popBv64();

		long left_concrete_value = ((Long) left.getConcreteValue()).longValue();
		long right_concrete_value = ((Long) right.getConcreteValue())
				.longValue();

		if (!left.containsSymbolicVariable()) {
			left = ExpressionFactory
					.buildNewIntegerConstant(left_concrete_value);
		}
		if (!right.containsSymbolicVariable()) {
			right = ExpressionFactory
					.buildNewIntegerConstant(right_concrete_value);
		}

		long con = left_concrete_value * right_concrete_value;

		IntegerExpression intExpr = new IntegerBinaryExpression(left,
				Operator.MUL, right, (long) con);

		env.topFrame().operandStack.pushBv64(intExpr);
	}

	@Override
	public void FMUL() {
		RealExpression right = env.topFrame().operandStack.popFp32();
		RealExpression left = env.topFrame().operandStack.popFp32();

		float left_concrete_value = ((Double) left.getConcreteValue())
				.floatValue();
		float right_concrete_value = ((Double) right.getConcreteValue())
				.floatValue();

		if (!left.containsSymbolicVariable()) {
			left = ExpressionFactory.buildNewRealConstant(left_concrete_value);
		}
		if (!right.containsSymbolicVariable()) {
			right = ExpressionFactory
					.buildNewRealConstant(right_concrete_value);
		}

		float con = left_concrete_value * right_concrete_value;

		RealExpression realExpr = new RealBinaryExpression(left, Operator.MUL,
				right, (double) con);

		env.topFrame().operandStack.pushFp32(realExpr);
	}

	@Override
	public void DMUL() {
		RealExpression right = env.topFrame().operandStack.popFp64();
		RealExpression left = env.topFrame().operandStack.popFp64();

		double left_concrete_value = ((Double) left.getConcreteValue())
				.doubleValue();
		double right_concrete_value = ((Double) right.getConcreteValue())
				.doubleValue();

		if (!left.containsSymbolicVariable()) {
			left = ExpressionFactory.buildNewRealConstant(left_concrete_value);
		}
		if (!right.containsSymbolicVariable()) {
			right = ExpressionFactory
					.buildNewRealConstant(right_concrete_value);
		}

		double con = left_concrete_value * right_concrete_value;

		RealExpression realExpr = new RealBinaryExpression(left, Operator.MUL,
				right, con);

		env.topFrame().operandStack.pushFp64(realExpr);
	}

	/**
	 * a/b
	 * 
	 * if (b==0) throw exception; // clear stack, push exception else actual
	 * division // compute, push result
	 * 
	 * @see http 
	 *      ://java.sun.com/docs/books/jvms/second_edition/html/Instructions2
	 *      .doc6.html#idiv
	 */
	@Override
	public void IDIV(int rhsValue) {
		// consume all operands in stack
		IntegerExpression right = env.topFrame().operandStack.popBv32();
		IntegerExpression left = env.topFrame().operandStack.popBv32();

		if (zeroViolation(right, rhsValue))
			return;

		int left_concrete_value = ((Long) left.getConcreteValue()).intValue();
		int right_concrete_value = ((Long) right.getConcreteValue()).intValue();

		if (!left.containsSymbolicVariable()) {
			left = ExpressionFactory
					.buildNewIntegerConstant(left_concrete_value);
		}
		if (!right.containsSymbolicVariable()) {
			right = ExpressionFactory
					.buildNewIntegerConstant(right_concrete_value);
		}

		int con = left_concrete_value / right_concrete_value;

		IntegerExpression intExpr = new IntegerBinaryExpression(left,
				Operator.DIV, right, (long) con);

		env.topFrame().operandStack.pushBv32(intExpr);

	}

	/**
	 * @see http 
	 *      ://java.sun.com/docs/books/jvms/second_edition/html/Instructions2
	 *      .doc8.html#ldiv
	 */
	@Override
	public void LDIV(long rhsValue) {
		IntegerExpression right = env.topFrame().operandStack.popBv64();
		IntegerExpression left = env.topFrame().operandStack.popBv64();

		if (zeroViolation(right, rhsValue))
			return;

		long left_concrete_value = ((Long) left.getConcreteValue()).longValue();
		long right_concrete_value = ((Long) right.getConcreteValue())
				.longValue();

		if (!left.containsSymbolicVariable()) {
			left = ExpressionFactory
					.buildNewIntegerConstant(left_concrete_value);
		}
		if (!right.containsSymbolicVariable()) {
			right = ExpressionFactory
					.buildNewIntegerConstant(right_concrete_value);
		}

		long con = left_concrete_value / right_concrete_value;

		IntegerExpression intExpr = new IntegerBinaryExpression(left,
				Operator.DIV, right, (long) con);

		env.topFrame().operandStack.pushBv64(intExpr);
	}

	@Override
	public void FDIV(float rhsValue) {
		RealExpression right = env.topFrame().operandStack.popFp32();
		RealExpression left = env.topFrame().operandStack.popFp32();

		float left_concrete_value = ((Double) left.getConcreteValue())
				.floatValue();
		float right_concrete_value = ((Double) right.getConcreteValue())
				.floatValue();

		if (!left.containsSymbolicVariable()) {
			left = ExpressionFactory.buildNewRealConstant(left_concrete_value);
		}
		if (!right.containsSymbolicVariable()) {
			right = ExpressionFactory
					.buildNewRealConstant(right_concrete_value);
		}

		float con = left_concrete_value / right_concrete_value;

		RealExpression realExpr = new RealBinaryExpression(left, Operator.DIV,
				right, (double) con);

		env.topFrame().operandStack.pushFp32(realExpr);
	}

	@Override
	public void DDIV(double rhsValue) {
		RealExpression right = env.topFrame().operandStack.popFp64();
		RealExpression left = env.topFrame().operandStack.popFp64();

		double left_concrete_value = ((Double) left.getConcreteValue())
				.doubleValue();
		double right_concrete_value = ((Double) right.getConcreteValue())
				.doubleValue();

		if (!left.containsSymbolicVariable()) {
			left = ExpressionFactory.buildNewRealConstant(left_concrete_value);
		}
		if (!right.containsSymbolicVariable()) {
			right = ExpressionFactory
					.buildNewRealConstant(right_concrete_value);
		}

		double con = left_concrete_value / right_concrete_value;

		RealExpression realExpr = new RealBinaryExpression(left, Operator.DIV,
				right, con);

		env.topFrame().operandStack.pushFp64(realExpr);
	}

	/**
	 * Modulo -- Remainder -- %
	 * 
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc6.html#irem
	 */
	@Override
	public void IREM(int rhsValue) {
		IntegerExpression right = env.topFrame().operandStack.popBv32();
		IntegerExpression left = env.topFrame().operandStack.popBv32();

		if (zeroViolation(right, rhsValue))
			return;

		int left_concrete_value = ((Long) left.getConcreteValue()).intValue();
		int right_concrete_value = ((Long) right.getConcreteValue()).intValue();

		if (!left.containsSymbolicVariable()) {
			left = ExpressionFactory
					.buildNewIntegerConstant(left_concrete_value);
		}
		if (!right.containsSymbolicVariable()) {
			right = ExpressionFactory
					.buildNewIntegerConstant(right_concrete_value);
		}

		int con = left_concrete_value % right_concrete_value;

		IntegerExpression intExpr = new IntegerBinaryExpression(left,
				Operator.REM, right, (long) con);

		env.topFrame().operandStack.pushBv32(intExpr);

	}

	@Override
	public void FREM(float rhs) {
		RealExpression right = env.topFrame().operandStack.popFp32();
		RealExpression left = env.topFrame().operandStack.popFp32();

		float left_concrete_value = ((Double) left.getConcreteValue())
				.floatValue();
		float right_concrete_value = ((Double) right.getConcreteValue())
				.floatValue();

		if (!left.containsSymbolicVariable()) {
			left = ExpressionFactory.buildNewRealConstant(left_concrete_value);
		}
		if (!right.containsSymbolicVariable()) {
			right = ExpressionFactory
					.buildNewRealConstant(right_concrete_value);
		}

		float con = left_concrete_value % right_concrete_value;

		RealExpression realExpr = new RealBinaryExpression(left, Operator.REM,
				right, (double) con);

		env.topFrame().operandStack.pushFp32(realExpr);
	}

	@Override
	public void DREM(double rhs) {
		RealExpression right = env.topFrame().operandStack.popFp64();
		RealExpression left = env.topFrame().operandStack.popFp64();

		double left_concrete_value = ((Double) left.getConcreteValue())
				.doubleValue();
		double right_concrete_value = ((Double) right.getConcreteValue())
				.doubleValue();

		if (!left.containsSymbolicVariable()) {
			left = ExpressionFactory.buildNewRealConstant(left_concrete_value);
		}
		if (!right.containsSymbolicVariable()) {
			right = ExpressionFactory
					.buildNewRealConstant(right_concrete_value);
		}

		double con = left_concrete_value % right_concrete_value;

		RealExpression realExpr = new RealBinaryExpression(left, Operator.REM,
				right, con);

		env.topFrame().operandStack.pushFp64(realExpr);
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc6.html#ineg
	 */
	@Override
	public void INEG() {
		IntegerExpression param = env.topFrame().operandStack.popBv32();

		int param_concrete_value = ((Long) param.getConcreteValue()).intValue();

		if (!param.containsSymbolicVariable()) {
			param = ExpressionFactory
					.buildNewIntegerConstant(param_concrete_value);
		}

		int con = -param_concrete_value;

		IntegerExpression intExpr = new IntegerUnaryExpression(param,
				Operator.NEG, (long) con);

		env.topFrame().operandStack.pushBv32(intExpr);

	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc8.html#lneg
	 */
	@Override
	public void LNEG() {
		IntegerExpression param = env.topFrame().operandStack.popBv64();

		long param_concrete_value = ((Long) param.getConcreteValue())
				.longValue();

		if (!param.containsSymbolicVariable()) {
			param = ExpressionFactory
					.buildNewIntegerConstant(param_concrete_value);
		}

		long con = -param_concrete_value;

		IntegerExpression intExpr = new IntegerUnaryExpression(param,
				Operator.NEG, (long) con);

		env.topFrame().operandStack.pushBv64(intExpr);
	}

	@Override
	public void FNEG() {
		RealExpression param = env.topFrame().operandStack.popFp32();

		float param_concrete_value = ((Double) param.getConcreteValue())
				.floatValue();

		if (!param.containsSymbolicVariable()) {
			param = ExpressionFactory
					.buildNewRealConstant(param_concrete_value);
		}
		float con = -param_concrete_value;

		RealExpression realExpr = new RealUnaryExpression(param, Operator.NEG,
				(double) con);

		env.topFrame().operandStack.pushFp32(realExpr);
	}

	@Override
	public void DNEG() {
		RealExpression param = env.topFrame().operandStack.popFp64();

		double param_concrete_value = ((Double) param.getConcreteValue())
				.doubleValue();

		if (!param.containsSymbolicVariable()) {
			param = ExpressionFactory
					.buildNewRealConstant(param_concrete_value);
		}
		double con = -param_concrete_value;

		RealExpression realExpr = new RealUnaryExpression(param, Operator.NEG,
				con);

		env.topFrame().operandStack.pushFp64(realExpr);
	}

	/**
	 * Stack=value1(int)|value2(int)
	 * 
	 * Pops two ints off the stack. Shifts value2 left by the amount indicated
	 * in the five low bits of value1. The int result is then pushed back onto
	 * the stack.
	 */
	@Override
	public void ISHL() {
		IntegerExpression right_expr = (IntegerExpression) env.topFrame().operandStack
				.popBv32();
		IntegerExpression left_expr = (IntegerExpression) env.topFrame().operandStack
				.popBv32();

		int left_concrete_value = ((Long) left_expr.getConcreteValue())
				.intValue();
		int right_concrete_value = ((Long) right_expr.getConcreteValue())
				.intValue();

		int concrete_value = left_concrete_value << (right_concrete_value & 0x001F);

		IntegerBinaryExpression intExpr = new IntegerBinaryExpression(
				left_expr, Operator.SHL, right_expr, (long) concrete_value);

		env.topFrame().operandStack.pushBv32(intExpr);
	}

	/**
	 * Stack=value1(int)|value2(int)
	 * 
	 * Pops two ints off the stack. Shifts value2 left by the amount indicated
	 * in the five low bits of value1. The int result is then pushed back onto
	 * the stack.
	 */
	@Override
	public void ISHR() {
		IntegerExpression right_expr = (IntegerExpression) env.topFrame().operandStack
				.popBv32();
		IntegerExpression left_expr = (IntegerExpression) env.topFrame().operandStack
				.popBv32();

		int left_concrete_value = ((Long) left_expr.getConcreteValue())
				.intValue();
		int right_concrete_value = ((Long) right_expr.getConcreteValue())
				.intValue();

		int concrete_value = left_concrete_value >> (right_concrete_value & 0x001F);

		IntegerBinaryExpression intExpr = new IntegerBinaryExpression(
				left_expr, Operator.SHR, right_expr, (long) concrete_value);

		env.topFrame().operandStack.pushBv32(intExpr);
	}

	/**
	 * Stack=value1(int)|value2(int)
	 * 
	 * Pops two ints off the operand stack. Shifts value1 right by the amount
	 * indicated in the five low bits of value2. The int result is then pushed
	 * back onto the stack. value1 is shifted logically (ignoring the sign
	 * extension - useful for unsigned values).
	 */
	@Override
	public void IUSHR() {
		IntegerExpression right_expr = (IntegerExpression) env.topFrame().operandStack
				.popBv32();
		IntegerExpression left_expr = (IntegerExpression) env.topFrame().operandStack
				.popBv32();

		int left_concrete_value = ((Long) left_expr.getConcreteValue())
				.intValue();
		int right_concrete_value = ((Long) right_expr.getConcreteValue())
				.intValue();

		int concrete_value = left_concrete_value >>> (right_concrete_value & 0x001F);

		IntegerBinaryExpression intExpr = new IntegerBinaryExpression(
				left_expr, Operator.USHR, right_expr, (long) concrete_value);

		env.topFrame().operandStack.pushBv32(intExpr);
	}

	/**
	 * Stack=value1(int)|value2(long)
	 * 
	 * Pops an integer and a long integer and from the stack. Shifts value2 (the
	 * long integer) right by the amount indicated in the low six bits of value1
	 * (an int). The long integer result is then pushed back onto the stack. The
	 * value is shifted logically (ignoring the sign extension - useful for
	 * unsigned values).
	 */
	@Override
	public void LUSHR() {
		IntegerExpression right_expr = (IntegerExpression) env.topFrame().operandStack
				.popBv32();
		IntegerExpression left_expr = (IntegerExpression) env.topFrame().operandStack
				.popBv64();

		long left_concrete_value = ((Long) left_expr.getConcreteValue())
				.longValue();
		int right_concrete_value = ((Long) right_expr.getConcreteValue())
				.intValue();

		long concrete_value = left_concrete_value >>> (right_concrete_value & 0x001F);

		IntegerBinaryExpression intExpr = new IntegerBinaryExpression(
				left_expr, Operator.USHR, right_expr, (long) concrete_value);

		env.topFrame().operandStack.pushBv64(intExpr);
	}

	/**
	 * Stack=value1(int)|value2(long)
	 * 
	 * Pops an int and a long integer from the stack. Shifts value2 (the long
	 * integer) right by the amount indicated in the low six bits of value1 (an
	 * int). The long integer result is then pushed back onto the stack. The
	 * value is shifted arithmetically (preserving the sign extension).
	 */
	@Override
	public void LSHR() {
		IntegerExpression right_expr = (IntegerExpression) env.topFrame().operandStack
				.popBv32();
		IntegerExpression left_expr = (IntegerExpression) env.topFrame().operandStack
				.popBv64();

		long left_concrete_value = ((Long) left_expr.getConcreteValue())
				.longValue();
		int right_concrete_value = ((Long) right_expr.getConcreteValue())
				.intValue();

		long concrete_value = left_concrete_value >> (right_concrete_value & 0x001F);

		IntegerBinaryExpression intExpr = new IntegerBinaryExpression(
				left_expr, Operator.SHL, right_expr, (long) concrete_value);

		env.topFrame().operandStack.pushBv64(intExpr);
	}

	/**
	 * Stack=value1(int)|value2(long)
	 * 
	 * Pops a long integer and an int from the stack. Shifts value2 (the long
	 * integer) left by the amount indicated in the low six bits of value1 (an
	 * int). The long integer result is then pushed back onto the stack.
	 */
	@Override
	public void LSHL() {
		IntegerExpression right_expr = (IntegerExpression) env.topFrame().operandStack
				.popBv32();
		IntegerExpression left_expr = (IntegerExpression) env.topFrame().operandStack
				.popBv64();

		long left_concrete_value = ((Long) left_expr.getConcreteValue())
				.longValue();
		int right_concrete_value = ((Long) right_expr.getConcreteValue())
				.intValue();

		long concrete_value = left_concrete_value << (right_concrete_value & 0x001F);

		IntegerBinaryExpression intExpr = new IntegerBinaryExpression(
				left_expr, Operator.SHL, right_expr, (long) concrete_value);

		env.topFrame().operandStack.pushBv64(intExpr);
	}

	/**
	 * bitwise AND
	 * 
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc6.html#iand
	 */
	@Override
	public void IAND() {
		IntegerExpression right = env.topFrame().operandStack.popBv32();
		IntegerExpression left = env.topFrame().operandStack.popBv32();

		int left_concrete_value = ((Long) left.getConcreteValue()).intValue();
		int right_concrete_value = ((Long) right.getConcreteValue()).intValue();

		if (!left.containsSymbolicVariable()) {
			left = ExpressionFactory
					.buildNewIntegerConstant(left_concrete_value);
		}
		if (!right.containsSymbolicVariable()) {
			right = ExpressionFactory
					.buildNewIntegerConstant(right_concrete_value);
		}

		int con = left_concrete_value & right_concrete_value;

		IntegerExpression intExpr = new IntegerBinaryExpression(left,
				Operator.IAND, right, (long) con);

		env.topFrame().operandStack.pushBv32(intExpr);

	}

	/**
	 * bitwise OR
	 * 
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc6.html#ior
	 */
	@Override
	public void IOR() {
		IntegerExpression right = env.topFrame().operandStack.popBv32();
		IntegerExpression left = env.topFrame().operandStack.popBv32();

		int left_concrete_value = ((Long) left.getConcreteValue()).intValue();
		int right_concrete_value = ((Long) right.getConcreteValue()).intValue();

		if (!left.containsSymbolicVariable()) {
			left = ExpressionFactory
					.buildNewIntegerConstant(left_concrete_value);
		}
		if (!right.containsSymbolicVariable()) {
			right = ExpressionFactory
					.buildNewIntegerConstant(right_concrete_value);
		}

		int con = left_concrete_value | right_concrete_value;

		IntegerExpression intExpr = new IntegerBinaryExpression(left,
				Operator.IOR, right, (long) con);

		env.topFrame().operandStack.pushBv32(intExpr);

	}

	/**
	 * bitwise XOR
	 * 
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc6.html#ixor
	 */
	@Override
	public void IXOR() {
		IntegerExpression right = env.topFrame().operandStack.popBv32();
		IntegerExpression left = env.topFrame().operandStack.popBv32();

		int left_concrete_value = ((Long) left.getConcreteValue()).intValue();
		int right_concrete_value = ((Long) right.getConcreteValue()).intValue();

		if (!left.containsSymbolicVariable()) {
			left = ExpressionFactory
					.buildNewIntegerConstant(left_concrete_value);
		}
		if (!right.containsSymbolicVariable()) {
			right = ExpressionFactory
					.buildNewIntegerConstant(right_concrete_value);
		}

		int con = left_concrete_value ^ right_concrete_value;

		IntegerExpression intExpr = new IntegerBinaryExpression(left,
				Operator.IXOR, right, (long) con);

		env.topFrame().operandStack.pushBv32(intExpr);

	}

	/**
	 * @see http 
	 *      ://java.sun.com/docs/books/jvms/second_edition/html/Instructions2
	 *      .doc8.html#land
	 */
	@Override
	public void LAND() {
		IntegerExpression right = env.topFrame().operandStack.popBv64();
		IntegerExpression left = env.topFrame().operandStack.popBv64();

		long left_concrete_value = ((Long) left.getConcreteValue()).longValue();
		long right_concrete_value = ((Long) right.getConcreteValue())
				.longValue();

		if (!left.containsSymbolicVariable()) {
			left = ExpressionFactory
					.buildNewIntegerConstant(left_concrete_value);
		}
		if (!right.containsSymbolicVariable()) {
			right = ExpressionFactory
					.buildNewIntegerConstant(right_concrete_value);
		}

		long con = left_concrete_value & right_concrete_value;

		IntegerExpression intExpr = new IntegerBinaryExpression(left,
				Operator.IAND, right, (long) con);

		env.topFrame().operandStack.pushBv64(intExpr);
	}

	/**
	 * @see http 
	 *      ://java.sun.com/docs/books/jvms/second_edition/html/Instructions2
	 *      .doc8.html#lor
	 */
	@Override
	public void LOR() {
		IntegerExpression right = env.topFrame().operandStack.popBv64();
		IntegerExpression left = env.topFrame().operandStack.popBv64();

		long left_concrete_value = ((Long) left.getConcreteValue()).longValue();
		long right_concrete_value = ((Long) right.getConcreteValue())
				.longValue();

		if (!left.containsSymbolicVariable()) {
			left = ExpressionFactory
					.buildNewIntegerConstant(left_concrete_value);
		}
		if (!right.containsSymbolicVariable()) {
			right = ExpressionFactory
					.buildNewIntegerConstant(right_concrete_value);
		}

		long con = left_concrete_value | right_concrete_value;

		IntegerExpression intExpr = new IntegerBinaryExpression(left,
				Operator.IOR, right, (long) con);

		env.topFrame().operandStack.pushBv64(intExpr);
	}

	/**
	 * @see http 
	 *      ://java.sun.com/docs/books/jvms/second_edition/html/Instructions2
	 *      .doc8.html#lxor
	 */
	@Override
	public void LXOR() {
		IntegerExpression right = env.topFrame().operandStack.popBv64();
		IntegerExpression left = env.topFrame().operandStack.popBv64();

		long left_concrete_value = ((Long) left.getConcreteValue()).longValue();
		long right_concrete_value = ((Long) right.getConcreteValue())
				.longValue();

		if (!left.containsSymbolicVariable()) {
			left = ExpressionFactory
					.buildNewIntegerConstant(left_concrete_value);
		}
		if (!right.containsSymbolicVariable()) {
			right = ExpressionFactory
					.buildNewIntegerConstant(right_concrete_value);
		}

		long con = left_concrete_value ^ right_concrete_value;

		IntegerExpression intExpr = new IntegerBinaryExpression(left,
				Operator.IXOR, right, (long) con);

		env.topFrame().operandStack.pushBv64(intExpr);
	}

	/**
	 * Increment i-th local (int) variable by constant (int) value
	 * 
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc6.html#iinc
	 */
	@Override
	public void IINC(int i, int value) {
		IntegerConstant right = ExpressionFactory
				.buildNewIntegerConstant(value);
		IntegerExpression left = env.topFrame().localsTable.getBv32Local(i);

		int left_concrete_value = ((Long) left.getConcreteValue()).intValue();
		int right_concrete_value = value;

		if (!left.containsSymbolicVariable()) {
			left = ExpressionFactory
					.buildNewIntegerConstant(left_concrete_value);
		}

		int con = left_concrete_value + right_concrete_value;

		IntegerExpression intExpr = new IntegerBinaryExpression(left,
				Operator.PLUS, right, (long) con);

		env.topFrame().localsTable.setBv32Local(i, intExpr);
	}

	/**
	 * <pre>
	 * (a > b)  ==>  1
	 * (a == b) ==>  0
	 * (a < b)  ==> -1
	 * </pre>
	 * 
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc8.html#lcmp
	 */
	@Override
	public void LCMP() {
		IntegerExpression right = env.topFrame().operandStack.popBv64();
		IntegerExpression left = env.topFrame().operandStack.popBv64();

		long left_concrete_value = (Long) left.getConcreteValue();
		long right_concrete_value = (Long) right.getConcreteValue();

		if (!left.containsSymbolicVariable()) {
			left = ExpressionFactory
					.buildNewIntegerConstant(left_concrete_value);
		}
		if (!right.containsSymbolicVariable()) {
			right = ExpressionFactory
					.buildNewIntegerConstant(right_concrete_value);
		}

		int concrete_value = 0;
		if (left_concrete_value == right_concrete_value) {
			concrete_value = 0;
		} else if (left_concrete_value > right_concrete_value) {
			concrete_value = 1;
		} else {
			assert left_concrete_value < right_concrete_value;
			concrete_value = -1;
		}

		IntegerComparison intComp = new IntegerComparison(left, right,
				(long) concrete_value);

		env.topFrame().operandStack.pushBv32(intComp);
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc4.html#fcmpop
	 */
	@Override
	public void FCMPL() {
		RealExpression right = env.topFrame().operandStack.popFp32();
		RealExpression left = env.topFrame().operandStack.popFp32();

		float left_concrete_value = ((Double) left.getConcreteValue())
				.floatValue();
		float right_concrete_value = ((Double) right.getConcreteValue())
				.floatValue();

		if (!left.containsSymbolicVariable()) {
			left = ExpressionFactory.buildNewRealConstant(left_concrete_value);
		}
		if (!right.containsSymbolicVariable()) {
			right = ExpressionFactory
					.buildNewRealConstant(right_concrete_value);
		}

		int concrete_value;
		if (new Double(left_concrete_value).isNaN()
				|| new Double(right_concrete_value).isNaN()) {
			concrete_value = 1;
		} else if (left_concrete_value == right_concrete_value) {
			concrete_value = 0;
		} else if (left_concrete_value > right_concrete_value) {
			concrete_value = 1;
		} else {
			assert left_concrete_value < right_concrete_value;
			concrete_value = -1;
		}

		RealComparison ret = new RealComparison(left, right,
				(long) concrete_value);

		env.topFrame().operandStack.pushBv32(ret);
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc4.html#fcmpop
	 */
	@Override
	public void FCMPG() {
		FCMPL(); // TODO: NaN treatment differs
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc3.html#dcmpop
	 */
	@Override
	public void DCMPL() {
		RealExpression right = env.topFrame().operandStack.popFp64();
		RealExpression left = env.topFrame().operandStack.popFp64();

		double left_concrete_value = (Double) left.getConcreteValue();
		double right_concrete_value = (Double) right.getConcreteValue();

		if (!left.containsSymbolicVariable()) {
			left = ExpressionFactory.buildNewRealConstant(left_concrete_value);
		}
		if (!right.containsSymbolicVariable()) {
			right = ExpressionFactory
					.buildNewRealConstant(right_concrete_value);
		}

		int concrete_value;
		if (new Double(left_concrete_value).isNaN()
				|| new Double(right_concrete_value).isNaN()) {
			concrete_value = 1;
		} else if (left_concrete_value == right_concrete_value) {
			concrete_value = 0;
		} else if (left_concrete_value > right_concrete_value) {
			concrete_value = 1;
		} else {
			assert left_concrete_value < right_concrete_value;
			concrete_value = -1;
		}

		RealComparison ret = new RealComparison(left, right,
				(long) concrete_value);

		env.topFrame().operandStack.pushBv32(ret);
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc3.html#dcmpop
	 */
	@Override
	public void DCMPG() {
		DCMPL(); // FIXME: NaN treatment differs
	}

	/**
	 * int --> long
	 * 
	 * This conversion is exact = preserve all information.
	 * 
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc6.html#i2l
	 */
	@Override
	public void I2L() {
		IntegerExpression intExpr = env.topFrame().operandStack.popBv32();
		env.topFrame().operandStack.pushBv64(intExpr);
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc6.html#i2f
	 */
	@Override
	public void I2F() {
		IntegerExpression integerExpr = env.topFrame().operandStack.popBv32();
		int integerValue = ((Long) integerExpr.getConcreteValue()).intValue();
		RealExpression realExpr;
		float concreteValue = (float) integerValue;
		if (!integerExpr.containsSymbolicVariable()) {
			realExpr = ExpressionFactory.buildNewRealConstant(concreteValue);
		} else {
			realExpr = new IntegerToRealCast(integerExpr,
					(double) concreteValue);
		}
		env.topFrame().operandStack.pushFp32(realExpr);
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc6.html#i2d
	 */
	@Override
	public void I2D() {
		IntegerExpression integerExpr = env.topFrame().operandStack.popBv32();
		int integerValue = ((Long) integerExpr.getConcreteValue()).intValue();
		RealExpression realExpr;
		double concreteValue = (double) integerValue;
		if (!integerExpr.containsSymbolicVariable()) {
			realExpr = ExpressionFactory.buildNewRealConstant(concreteValue);
		} else {
			realExpr = new IntegerToRealCast(integerExpr,
					(double) concreteValue);
		}
		env.topFrame().operandStack.pushFp64(realExpr);
	}

	@Override
	public void L2I() {
		IntegerExpression integerExpr = env.topFrame().operandStack.popBv64();
		env.topFrame().operandStack.pushBv32(integerExpr);
	}

	@Override
	public void L2F() {
		IntegerExpression integerExpr = env.topFrame().operandStack.popBv64();
		long longValue = ((Long) integerExpr.getConcreteValue()).longValue();
		RealExpression realExpr;
		float concreteValue = (float) longValue;
		if (!integerExpr.containsSymbolicVariable()) {
			realExpr = ExpressionFactory.buildNewRealConstant(concreteValue);
		} else {
			realExpr = new IntegerToRealCast(integerExpr,
					(double) concreteValue);
		}
		env.topFrame().operandStack.pushFp32(realExpr);
	}

	@Override
	public void L2D() {
		IntegerExpression integerExpr = env.topFrame().operandStack.popBv64();
		long longValue = ((Long) integerExpr.getConcreteValue()).longValue();
		RealExpression realExpr;
		double concreteValue = (double) longValue;
		if (!integerExpr.containsSymbolicVariable()) {
			realExpr = ExpressionFactory.buildNewRealConstant(concreteValue);
		} else {
			realExpr = new IntegerToRealCast(integerExpr,
					(double) concreteValue);
		}
		env.topFrame().operandStack.pushFp64(realExpr);
	}

	@Override
	public void F2I() {
		RealExpression realExpr = env.topFrame().operandStack.popFp32();
		float doubleValue = ((Double) realExpr.getConcreteValue()).floatValue();
		IntegerExpression intExpr;
		int concreteValue = (int) doubleValue;
		if (!realExpr.containsSymbolicVariable()) {
			intExpr = ExpressionFactory.buildNewIntegerConstant(concreteValue);
		} else {
			intExpr = new RealToIntegerCast(realExpr, (long) concreteValue);
		}
		env.topFrame().operandStack.pushBv32(intExpr);
	}

	@Override
	public void F2L() {
		RealExpression realExpr = env.topFrame().operandStack.popFp32();
		float floatValue = ((Double) realExpr.getConcreteValue()).floatValue();
		IntegerExpression intExpr;
		long concreteValue = (long) floatValue;
		if (!realExpr.containsSymbolicVariable()) {
			intExpr = ExpressionFactory.buildNewIntegerConstant(concreteValue);
		} else {
			intExpr = new RealToIntegerCast(realExpr, concreteValue);
		}
		env.topFrame().operandStack.pushBv64(intExpr);
	}

	@Override
	public void F2D() {
		RealExpression e = env.topFrame().operandStack.popFp32();
		env.topFrame().operandStack.pushFp64(e);
	}

	@Override
	public void D2I() {
		RealExpression realExpr = env.topFrame().operandStack.popFp64();
		double doubleValue = ((Double) realExpr.getConcreteValue())
				.doubleValue();
		IntegerExpression intExpr;
		int concreteValue = (int) doubleValue;
		if (!realExpr.containsSymbolicVariable()) {
			intExpr = ExpressionFactory.buildNewIntegerConstant(concreteValue);
		} else {
			intExpr = new RealToIntegerCast(realExpr, (long) concreteValue);
		}
		env.topFrame().operandStack.pushBv32(intExpr);
	}

	@Override
	public void D2L() {
		RealExpression realExpr = env.topFrame().operandStack.popFp64();
		double doubleValue = ((Double) realExpr.getConcreteValue())
				.doubleValue();
		IntegerExpression intExpr;
		long concreteValue = (long) doubleValue;
		if (!realExpr.containsSymbolicVariable()) {
			intExpr = ExpressionFactory.buildNewIntegerConstant(concreteValue);
		} else {
			intExpr = new RealToIntegerCast(realExpr, concreteValue);
		}
		env.topFrame().operandStack.pushBv64(intExpr);
	}

	@Override
	public void D2F() {
		RealExpression e = env.topFrame().operandStack.popFp64();
		env.topFrame().operandStack.pushFp32(e);
	}

	@Override
	public void I2B() {
		return; /* ignore I2B */
	}

	@Override
	public void I2C() {
		return; /* ignore I2C */
	}

	@Override
	public void I2S() {
		return; /* ignore I2C */
	}

	@Override
	public void LREM(long rhs) {
		IntegerExpression right = env.topFrame().operandStack.popBv64();
		IntegerExpression left = env.topFrame().operandStack.popBv64();

		if (zeroViolation(right, rhs))
			return;

		long left_concrete_value = ((Long) left.getConcreteValue()).longValue();
		long right_concrete_value = ((Long) right.getConcreteValue())
				.longValue();

		if (!left.containsSymbolicVariable()) {
			left = ExpressionFactory
					.buildNewIntegerConstant(left_concrete_value);
		}
		if (!right.containsSymbolicVariable()) {
			right = ExpressionFactory
					.buildNewIntegerConstant(right_concrete_value);
		}

		long con = left_concrete_value % right_concrete_value;

		IntegerExpression intExpr = new IntegerBinaryExpression(left,
				Operator.REM, right, (long) con);

		env.topFrame().operandStack.pushBv64(intExpr);
	}
}
