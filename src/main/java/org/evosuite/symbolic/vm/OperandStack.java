package org.evosuite.symbolic.vm;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.StringExpression;

/**
 * 
 * @author galeotti
 * 
 */
public final class OperandStack implements Iterable<Operand> {

	private final Deque<Operand> stack = new LinkedList<Operand>();

	public OperandStack() {
	}

	public void pushRef(Reference r) {
		stack.push(new ReferenceOperand(r));
	 int i=0;
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
		stack.push(new ReferenceOperand(new StringReference(e)));
	}

	public Reference popRef() {
		Operand ret_val = this.popOperand();
		ReferenceOperand ref = (ReferenceOperand) ret_val;
		return ref.getReference();
	}

	public IntegerExpression popBv32() {
		Operand x = this.popOperand();
		Bv32Operand e = (Bv32Operand) x;
		return e.getIntegerExpression();
	}

	public IntegerExpression popBv64() {
		Operand x = this.popOperand();
		Bv64Operand e = (Bv64Operand) x;
		return e.getIntegerExpression();
	}

	public RealExpression popFp32() {
		Operand x = this.popOperand();
		Fp32Operand e = (Fp32Operand) x;
		return e.getRealExpression();
	}

	public RealExpression popFp64() {
		Operand x = this.popOperand();
		Fp64Operand e = (Fp64Operand) x;
		return e.getRealExpression();
	}

	public Operand popOperand() {
		Operand ret_val = this.stack.pop();
		return ret_val;
	}

	public void clearOperands() {
		stack.clear();
	}

	public void pushOperand(Operand operand) {
		stack.push(operand);
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

	public Operand peekOperand() {
		return stack.peek();
	}

	public Iterator<Operand> iterator() {
		return stack.iterator();
	}

	public StringExpression peekStringRef() {
		Operand operand = stack.peek();
		ReferenceOperand refOp = (ReferenceOperand) operand;
		Reference ref = refOp.getReference();
		StringReference strRef = (StringReference) ref;
		return strRef.getStringExpression();
	}

	public StringExpression popStringRef() {
		Operand operand = this.popOperand();
		ReferenceOperand refOp = (ReferenceOperand) operand;
		Reference ref = refOp.getReference();
		StringReference strRef = (StringReference) ref;
		return strRef.getStringExpression();
	}

	public Reference peekRef() {
		Operand operand = this.peekOperand();
		ReferenceOperand refOp = (ReferenceOperand) operand;
		Reference ref = refOp.getReference();
		return ref;
	}

	@Override
	public String toString() {
		if (this.stack.isEmpty()) {
			return "<<EMPTY_OPERAND_STACK>>";
		}

		StringBuffer buff = new StringBuffer();
		for (Operand operand : this) {
			buff.append(operand.toString() + "\n");
		}
		return buff.toString();
	}

	public int size() {
		return stack.size();
	}
	
	public boolean isEmpty() {
		return stack.isEmpty();
	}
}
