package edu.uta.cse.dsc.vm2;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.StringExpression;

public final class OperandStack {

	private final Deque<Operand> stack = new LinkedList<Operand>();

	public OperandStack() {
	}

	public void pushRef(Object o) {
		if (o instanceof String)
			throw new IllegalArgumentException(
					"String references must be pushed using methdo pushStringRef()");

		stack.push(new ReferenceOperand(o));
	}

	public void pushBv32(IntegerExpression e) {
		stack.push(new Bv32Operand(e));
	}

	public void pushBv64(IntegerExpression e) {
		stack.push(new Bv64Operand(e));
	}

	public void pushFp32(RealExpression e) {
		stack.push(new Fp32Operand(e));
	}

	public void pushFp64(RealExpression e) {
		stack.push(new Fp64Operand(e));
	}

	public void pushStringRef(StringExpression e) {
		stack.push(new StringReferenceOperand(e));
	}

	public Object popRef() {
		Operand ret_val = stack.pop();
		ReferenceOperand ref = (ReferenceOperand) ret_val;
		return ref.getReference();
	}

	public IntegerExpression popBv32() {
		Operand x = stack.pop();
		Bv32Operand e = (Bv32Operand) x;
		return e.getIntegerExpression();
	}

	public IntegerExpression popBv64() {
		Operand x = stack.pop();
		Bv64Operand e = (Bv64Operand) x;
		return e.getIntegerExpression();
	}

	public RealExpression popFp32() {
		Operand x = stack.pop();
		Fp32Operand e = (Fp32Operand) x;
		return e.getRealExpression();
	}

	public RealExpression popFp64() {
		Operand x = stack.pop();
		Fp64Operand e = (Fp64Operand) x;
		return e.getRealExpression();
	}

	public Operand popOperand() {
		Operand ret_val = stack.pop();
		return ret_val;
	}

	public void dup() {
		Operand x = this.stack.peek();
		this.stack.push(x);
	}

	/**
	 * duplicate top stack word and insert beneath second word
	 */
	public void dup_x1() {
		Operand a = this.stack.pop();
		Operand b = this.stack.pop();

		stack.push(a);
		stack.push(b);
		stack.push(a);
	}

	/**
	 * duplicate top stack word and insert beneath third word
	 */
	public void dup_x2() {
		Operand a = stack.pop();
		Operand b = stack.pop();

		if (!isCategory2(b)) {
			Operand c = stack.pop();
			stack.push(a);
			stack.push(c);
			stack.push(b);
			stack.push(a);
		} else {
			stack.push(a);
			stack.push(b);
			stack.push(a);
		}
	}

	private boolean isCategory2(Object b) {
		return b instanceof DoubleWordOperand;
	}

	public void dup2() {
		Operand a = stack.pop();

		if (!isCategory2(a)) {
			/* Form 1 */
			Operand b = stack.pop();
			stack.push(b);
			stack.push(a);
			stack.push(b);
			stack.push(a);
		} else {
			/* Form 2 */
			stack.push(a);
			stack.push(a);
		}
	}

	public void dup2_x1() {
		Operand expression = this.stack.pop();

		if (!isCategory2(expression)) {
			/* Form 1 */
			Operand a = expression;
			Operand b = this.stack.pop();
			Operand c = this.stack.pop();
			stack.push(b);
			stack.push(a);
			stack.push(c);
			stack.push(b);
			stack.push(a);
		} else {
			/* Form 2 */
			Operand a = expression;
			Operand b = this.stack.pop();
			stack.push(a);
			stack.push(b);
			stack.push(a);
		}

	}

	public void dup2_x2() {
		Operand first = stack.pop();
		Operand second = stack.pop();

		if (isCategory2(first)) {
			Operand a = first;

			if (isCategory2(second)) {
				/* Form 4 */
				Operand b = second;
				stack.push(a);
				stack.push(b);
				stack.push(a);
			} else {
				/* Form 2 */
				Operand b = second;
				Operand c = stack.pop();
				stack.push(a);
				stack.push(c);
				stack.push(b);
				stack.push(a);
			}
		} else {
			Operand a = first;
			Operand b = second;
			Operand third = this.stack.pop();

			if (isCategory2(third)) {
				/* Form 3 */
				Operand c = third;
				stack.push(b);
				stack.push(a);
				stack.push(c);
				stack.push(b);
				stack.push(a);
			} else {
				/* Form 1 */
				Operand c = third;
				Operand d = this.stack.pop();
				stack.push(b);
				stack.push(a);
				stack.push(d);
				stack.push(c);
				stack.push(b);
				stack.push(a);
			}
		}
	}

	public void swap() {
		Operand a = stack.pop();
		Operand b = stack.pop();
		stack.push(a);
		stack.push(b);
	}

	public void pop() {
		Operand a = stack.pop();
		assert a instanceof SingleWordOperand;
	}

	public void pop2() {
		Operand top = stack.pop();

		if (top instanceof DoubleWordOperand)
			/* Form 2 */
			return;

		/* Form 1 */
		Operand b = stack.pop();
		assert b instanceof SingleWordOperand;

	}

	public void clearOperands() {
		stack.clear();
	}

	public void pushOperand(Operand operand) {
		stack.push(operand);
	}

	public Object peekRef() {
		Operand operand = stack.peek();
		ReferenceOperand refOp = (ReferenceOperand) operand;
		return refOp.getReference();
	}

	public RealExpression peekFp64() {
		Operand operand = stack.peek();
		Fp64Operand fp64 = (Fp64Operand) operand;
		return fp64.getRealExpression();
	}

	public RealExpression peekFp32() {
		Operand operand = stack.peek();
		Fp32Operand fp32 = (Fp32Operand) operand;
		return fp32.getRealExpression();
	}

	public IntegerExpression peekBv64() {
		Operand operand = stack.peek();
		Bv64Operand bv64 = (Bv64Operand) operand;
		return bv64.getIntegerExpression();
	}

	public IntegerExpression peekBv32() {
		Operand operand = stack.peek();
		Bv32Operand bv32 = (Bv32Operand) operand;
		return bv32.getIntegerExpression();
	}

	public Iterator<Operand> iterator() {
		return stack.iterator();
	}

	public StringExpression peekStringRef() {
		Operand operand = stack.peek();
		StringReferenceOperand strRef = (StringReferenceOperand) operand;
		return strRef.getStringExpression();
	}

	public StringExpression popStringRef() {
		Operand operand = stack.pop();
		StringReferenceOperand strRef = (StringReferenceOperand) operand;
		return strRef.getStringExpression();
	}

}
