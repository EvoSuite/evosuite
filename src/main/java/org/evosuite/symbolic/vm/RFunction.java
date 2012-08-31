package org.evosuite.symbolic.vm;

import mockit.external.asm4.Type;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.fp.RealValue;

public abstract class RFunction {

	public RFunction(SymbolicEnvironment env, String owner, String name,
			String desc) {
		super();
		this.env = env;
		this.owner = owner;
		this.name = name;
		this.desc = desc;
		this.symb_args = new Object[Type.getArgumentTypes(desc).length];
		this.conc_args = new Object[Type.getArgumentTypes(desc).length];
	}

	/* non-assignable references */
	protected final SymbolicEnvironment env;
	private final String owner;
	private final String name;
	private final String desc;
	private final Object[] symb_args;
	private final Object[] conc_args;

	/* assignable references */
	private Object conc_receiver;
	private Reference symb_receiver;

	private Object conc_ret_val;
	private Object symb_ret_val;

	public String getOwner() {
		return owner;
	}

	public String getName() {
		return name;
	}

	void setReceiver(Object conc_receiver, Reference symb_receiver) {
		this.conc_receiver = conc_receiver;
		this.symb_receiver = symb_receiver;
	}

	// IntegerExpression parameters
	void setParam(int i, int conc_arg, IntegerValue symb_arg) {
		this.conc_args[i] = conc_arg;
		this.symb_args[i] = symb_arg;
	}

	void setParam(int i, char conc_arg, IntegerValue symb_arg) {
		this.conc_args[i] = conc_arg;
		this.symb_args[i] = symb_arg;
	}

	void setParam(int i, byte conc_arg, IntegerValue symb_arg) {
		this.conc_args[i] = conc_arg;
		this.symb_args[i] = symb_arg;
	}

	void setParam(int i, short conc_arg, IntegerValue symb_arg) {
		this.conc_args[i] = conc_arg;
		this.symb_args[i] = symb_arg;
	}

	void setParam(int i, boolean conc_arg, IntegerValue symb_arg) {
		this.conc_args[i] = conc_arg;
		this.symb_args[i] = symb_arg;
	}

	void setParam(int i, long conc_arg, IntegerValue symb_arg) {
		this.conc_args[i] = conc_arg;
		this.symb_args[i] = symb_arg;
	}

	// RealExpression params

	void setParam(int i, float conc_arg, RealValue symb_arg) {
		this.conc_args[i] = conc_arg;
		this.symb_args[i] = symb_arg;
	}

	void setParam(int i, double conc_arg, RealValue symb_arg) {
		this.conc_args[i] = conc_arg;
		this.symb_args[i] = symb_arg;
	}

	// Reference params

	void setParam(int i, Object conc_arg, Reference symb_arg) {
		this.conc_args[i] = conc_arg;
		this.symb_args[i] = symb_arg;
	}

	void setReturnValue(int conc_ret_val, IntegerValue symb_ret_val) {
		this.conc_ret_val = conc_ret_val;
		this.symb_ret_val = symb_ret_val;
	}

	void setReturnValue(boolean conc_ret_val, IntegerValue symb_ret_val) {
		this.conc_ret_val = conc_ret_val;
		this.symb_ret_val = symb_ret_val;
	}

	void setReturnValue(long conc_ret_val, IntegerValue symb_ret_val) {
		this.conc_ret_val = conc_ret_val;
		this.symb_ret_val = symb_ret_val;
	}

	void setReturnValue(float conc_ret_val, RealValue symb_ret_val) {
		this.conc_ret_val = conc_ret_val;
		this.symb_ret_val = symb_ret_val;
	}

	void setReturnValue(double conc_ret_val, RealValue symb_ret_val) {
		this.conc_ret_val = conc_ret_val;
		this.symb_ret_val = symb_ret_val;
	}

	void setReturnValue(Object conc_ret_val, Reference symb_ret_val) {
		this.conc_ret_val = conc_ret_val;
		this.symb_ret_val = symb_ret_val;
	}

	/**
	 * Helper methos
	 */
	protected NonNullReference getSymbReceiver() {
		return (NonNullReference) symb_receiver;
	}

	protected Object getConcReceiver() {
		return this.conc_receiver;
	}

	protected int getConcIntArgument(int i) {
		Integer int0 = (Integer) this.conc_args[i];
		return int0.intValue();
	}

	protected short getConcShortArgument(int i) {
		Short short0 = (Short) this.conc_args[i];
		return short0.shortValue();
	}

	protected char getConcCharArgument(int i) {
		Character char0 = (Character) this.conc_args[i];
		return char0.charValue();
	}

	protected double getConcDoubleArgument(int i) {
		Double double0 = (Double) this.conc_args[i];
		return double0.doubleValue();
	}

	protected float getConcFloatArgument(int i) {
		Float float0 = (Float) this.conc_args[i];
		return float0.floatValue();
	}

	protected boolean getConcBooleanArgument(int i) {
		Boolean boolean0 = (Boolean) this.conc_args[i];
		return boolean0.booleanValue();
	}

	protected byte getConcByteArgument(int i) {
		Byte byte0 = (Byte) this.conc_args[i];
		return byte0.byteValue();
	}

	protected long getConcLongArgument(int i) {
		Long long0 = (Long) this.conc_args[i];
		return long0.longValue();
	}

	protected Object getConcArgument(int i) {
		Object arg = this.conc_args[i];
		return arg;
	}

	protected IntegerValue getSymbIntegerArgument(int i) {
		IntegerValue intExpr = (IntegerValue) this.symb_args[i];
		return intExpr;
	}

	protected RealValue getSymbRealArgument(int i) {
		RealValue realExpr = (RealValue) this.symb_args[i];
		return realExpr;
	}

	protected Reference getSymbArgument(int i) {
		Reference ref = (Reference) this.symb_args[i];
		return ref;
	}

	protected Reference getSymbRetVal() {
		return (Reference) this.symb_ret_val;
	}

	protected IntegerValue getSymbIntegerRetVal() {
		IntegerValue intExpr = (IntegerValue) this.symb_ret_val;
		return intExpr;
	}

	protected RealValue getSymbRealRetVal() {
		RealValue realExpr = (RealValue) this.symb_ret_val;
		return realExpr;
	}

	/**
	 * Returns new symbolic return value
	 * 
	 * @return object!=null && object instanceof Reference or object instanceof
	 *         IntegerExpression or object instanceof RealExpression
	 */
	public abstract Object executeFunction();

	protected int getConcIntRetVal() {
		Integer int0 = (Integer) this.conc_ret_val;
		return int0.intValue();
	}

	protected short getConcShortRetVal() {
		Integer integer0 = (Integer) this.conc_ret_val;
		return integer0.shortValue();
	}

	protected char getConcCharRetVal() {
		Integer char0 = (Integer) this.conc_ret_val;
		return (char) char0.intValue();
	}

	protected double getConcDoubleRetVal() {
		Double double0 = (Double) this.conc_ret_val;
		return double0.doubleValue();
	}

	protected float getConcFloatRetVal() {
		Float float0 = (Float) this.conc_ret_val;
		return float0.floatValue();
	}

	protected boolean getConcBooleanRetVal() {
		Boolean boolean0 = (Boolean) this.conc_ret_val;
		return boolean0.booleanValue();
	}

	protected byte getConcByteRetVal() {
		Integer integer0 = (Integer) this.conc_ret_val;
		return integer0.byteValue();
	}

	protected long getConcLongRetVal() {
		Long long0 = (Long) this.conc_ret_val;
		return long0.longValue();
	}

	protected Object getConcRetVal() {
		Object arg = this.conc_ret_val;
		return arg;
	}

	public String getDesc() {
		return desc;
	}

}
