package org.evosuite.symbolic.vm;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.wrappers.Types;

/**
 * 
 * @author galeotti
 * 
 */
public abstract class Function {

	protected static RealValue fp32(Operand op) {
		return ((Fp32Operand) op).getRealExpression();
	}

	protected static IntegerValue bv32(Operand op) {
		return ((Bv32Operand) op).getIntegerExpression();
	}

	protected static IntegerValue bv64(Operand op) {
		return ((Bv64Operand) op).getIntegerExpression();
	}

	public Function(SymbolicEnvironment env, String owner, String name,
			String desc) {
		super();
		this.env = env;
		this.owner = owner;
		this.name = name;
		this.desc = desc;
	}

	protected final SymbolicEnvironment env;

	private final String owner;
	private final String name;
	private final String desc;

	public void INVOKESTATIC() {
		/* STUB */
	}

	public void CALL_RESULT(int res) {
		/* STUB */
	}

	public void CALL_RESULT(Object res) {
		/* STUB */
	}

	public void CALL_RESULT(double res) {
		/* STUB */
	}

	public void CALL_RESULT(float res) {
		/* STUB */
	}

	public void CALL_RESULT(long res) {
		/* STUB */
	}

	public void CALL_RESULT(boolean res) {
		/* STUB */
	}

	public String getOwner() {
		return owner;
	}

	public String getName() {
		return name;
	}

	public String getDesc() {
		return desc;
	}

	public void INVOKEVIRTUAL(Object receiver) {
		/* STUB */
	}

	public void CALL_RESULT() {
		/* STUB */
	}

	protected void replaceTopBv32(IntegerValue expr) {
		env.topFrame().operandStack.popBv32();
		env.topFrame().operandStack.pushBv32(expr);
	}

	protected void replaceTopFp64(RealValue expr) {
		env.topFrame().operandStack.popFp64();
		env.topFrame().operandStack.pushFp64(expr);
	}

	protected void replaceTopFp32(RealValue expr) {
		this.env.topFrame().operandStack.popFp32();
		this.env.topFrame().operandStack.pushFp32(expr);
	}

	protected void replaceTopBv64(IntegerValue expr) {
		env.topFrame().operandStack.popBv64();
		env.topFrame().operandStack.pushBv64(expr);
	}

	protected static RealValue fp64(Operand op) {
		return ((Fp64Operand) op).getRealExpression();
	}

	protected static Reference ref(Operand operand) {
		ReferenceOperand ref = (ReferenceOperand) operand;
		return ref.getReference();
	}

	protected static boolean isNullRef(Reference ref) {
		return ref instanceof NullReference;
	}

	protected StringValue getStringExpression(Operand operand) {
		ReferenceOperand refOp = (ReferenceOperand) operand;
		Reference ref = (Reference) refOp.getReference();
		if (ref instanceof NullReference) {
			return null;
		} else {
			NonNullReference nonNullRef = (NonNullReference) ref;
			if (nonNullRef.isString()) {
				String conc_string = (String) nonNullRef
						.getWeakConcreteObject();
				StringValue strExpr = env.heap.getField(
						Types.JAVA_LANG_STRING, SymbolicHeap.$STRING_VALUE,
						conc_string, nonNullRef, conc_string);
				return strExpr;
			} else {
				return null;
			}
		}
	}

	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, int value) { /* stub */
	}

	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, boolean value) { /* stub */
	}

	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, short value) { /* stub */
	}

	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, byte value) { /* stub */
	}

	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, char value) { /* stub */
	}

	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, long value) { /* stub */
	}

	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, float value) { /* stub */
	}

	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, double value) { /* stub */
	}

	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, Object value) { /* stub */
	}

	public void INVOKESPECIAL() {
		/* STUB */
	}

	public void INVOKESPECIAL(Object receiver) {
		/* STUB */
	}

	public void INVOKEINTERFACE(Object receiver) {
		/* STUB */
	}
}
