/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.symbolic.vm;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.constraint.IntegerConstraint;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.ref.ReferenceExpression;
import org.objectweb.asm.Type;

/**
 * This class represents the execution of a concrete method (Math.abs(), new
 * Integer(), etc.) at the symbolic level.
 *
 * @author galeotti
 */
public abstract class SymbolicFunction {

    public SymbolicFunction(SymbolicEnvironment env, String owner,
                            String name, String desc) {
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
    private ReferenceExpression symb_receiver;

    private Object conc_ret_val;
    private Object symb_ret_val;

    final public String getOwner() {
        return owner;
    }

    final public String getName() {
        return name;
    }

    void setReceiver(Object conc_receiver, ReferenceExpression symb_receiver) {
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

    void setParam(int i, Object conc_arg, ReferenceExpression symb_arg) {
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

    void setReturnValue(Object conc_ret_val, ReferenceExpression symb_ret_val) {
        this.conc_ret_val = conc_ret_val;
        this.symb_ret_val = symb_ret_val;
    }

    /**
     * Helper methos
     */

    /**
     * For non-static method invocations (not constructors) returns the symbolic
     * receiver.
     *
     * @return a NonNullReference with the symbolic object receiver.
     */
    final protected ReferenceConstant getSymbReceiver() {
        return (ReferenceConstant) symb_receiver;
    }

    /**
     * For non-static method invocations (neither constructors) returns the
     * concrete method receiver.
     *
     * @return a Object reference (non-null) with the concrete method receiver.
     */
    final protected Object getConcReceiver() {
        return this.conc_receiver;
    }

    /**
     * Returns the i-th concrete parameter. The parameter must be an instance of
     * Object.
     *
     * @param i the parameter index.
     * @return a concrete Object reference to the concrete parameter.
     */
    final protected int getConcIntArgument(int i) {
        Integer int0 = (Integer) this.conc_args[i];
        return int0;
    }

    /**
     * Returns the i-th concrete parameter. The parameter must be a short value.
     *
     * @param i the parameter index.
     * @return a concrete short value.
     */
    final protected short getConcShortArgument(int i) {
        Short short0 = (Short) this.conc_args[i];
        return short0;
    }

    /**
     * Returns the i-th concrete parameter. The parameter must be a char value.
     *
     * @param i the parameter index.
     * @return a concrete char value.
     */
    final protected char getConcCharArgument(int i) {
        Character char0 = (Character) this.conc_args[i];
        return char0;
    }

    /**
     * Returns the i-th concrete parameter. The parameter must be a double
     * value.
     *
     * @param i the parameter index.
     * @return a concrete double value.
     */
    final protected double getConcDoubleArgument(int i) {
        Double double0 = (Double) this.conc_args[i];
        return double0;
    }

    /**
     * Returns the i-th concrete parameter. The parameter must be a float value.
     *
     * @param i the parameter index.
     * @return a concrete float value.
     */
    final protected float getConcFloatArgument(int i) {
        Float float0 = (Float) this.conc_args[i];
        return float0;
    }

    /**
     * Returns the i-th concrete parameter. The parameter must be a boolean
     * value.
     *
     * @param i the parameter index.
     * @return a concrete boolean value.
     */
    final protected boolean getConcBooleanArgument(int i) {
        Boolean boolean0 = (Boolean) this.conc_args[i];
        return boolean0;
    }

    /**
     * Returns the i-th concrete parameter. The parameter must be a byte value.
     *
     * @param i the parameter index.
     * @return a concrete byte value.
     */
    final protected byte getConcByteArgument(int i) {
        Byte byte0 = (Byte) this.conc_args[i];
        return byte0;
    }

    /**
     * Returns the i-th concrete parameter. The parameter must be a long value.
     *
     * @param i the parameter index.
     * @return a concrete long value.
     */
    final protected long getConcLongArgument(int i) {
        Long long0 = (Long) this.conc_args[i];
        return long0;
    }

    /**
     * Returns the i-th concrete parameter. The parameter must be an Object
     * reference.
     *
     * @param i the parameter index.
     * @return a concrete object reference.
     */
    final protected Object getConcArgument(int i) {
        Object arg = this.conc_args[i];
        return arg;
    }

    /**
     * Returns the i-th symbolic parameter. The parameter must be a symbolic
     * integer value.
     *
     * @param i the parameter index.
     * @return a symbolic integer value.
     */
    final protected IntegerValue getSymbIntegerArgument(int i) {
        IntegerValue intExpr = (IntegerValue) this.symb_args[i];
        return intExpr;
    }

    /**
     * Returns the i-th symbolic parameter. The parameter must be a symbolic
     * real value.
     *
     * @param i the parameter index.
     * @return a symbolic real value.
     */
    final protected RealValue getSymbRealArgument(int i) {
        RealValue realExpr = (RealValue) this.symb_args[i];
        return realExpr;
    }

    /**
     * Returns the i-th symbolic parameter. The parameter must be a symbolic
     * reference.
     *
     * @param i the parameter index.
     * @return a symbolic reference.
     */
    final protected ReferenceExpression getSymbArgument(int i) {
        ReferenceExpression ref = (ReferenceExpression) this.symb_args[i];
        return ref;
    }

    /**
     * Returns the symbolic return value. The return value must be a symbolic
     * reference.
     *
     * @return a symbolic reference of the return value.
     */
    final protected ReferenceExpression getSymbRetVal() {
        return (ReferenceExpression) this.symb_ret_val;
    }

    /**
     * Returns the symbolic return value. The return value must be a symbolic
     * integer value.
     *
     * @return a symbolic integer value of the return value.
     */
    final protected IntegerValue getSymbIntegerRetVal() {
        IntegerValue intExpr = (IntegerValue) this.symb_ret_val;
        return intExpr;
    }

    /**
     * Returns the symbolic return value. The return value must be a symbolic
     * real value.
     *
     * @return a symbolic real return value.
     */
    final protected RealValue getSymbRealRetVal() {
        RealValue realExpr = (RealValue) this.symb_ret_val;
        return realExpr;
    }

    /**
     * Returns new symbolic return value. All symbolic method invocations with
     * return values (except constructor calls or void calls) should return a
     * symbolic value. The old symbolic value can be obtained by using the
     * <code>getSymbRetVal</code>, <code>getSymbRealRetVal</code> and
     * <code>getSymbIntegerRetVal</code> methods.
     *
     * @return object!=null && object instanceof Reference or object instanceof
     * IntegerExpression or object instanceof RealExpression
     */
    public abstract Object executeFunction();

    /**
     * Returns the concrete return value of the concrete method execution. The
     * concrete return value should be an integer value
     *
     * @return an integer value with the concrete method execution.
     */
    final protected int getConcIntRetVal() {
        Integer int0 = (Integer) this.conc_ret_val;
        return int0;
    }

    /**
     * Returns the concrete return value of the concrete method execution. The
     * concrete return value should be a short value
     *
     * @return a short value with the concrete method execution.
     */
    final protected short getConcShortRetVal() {
        Integer integer0 = (Integer) this.conc_ret_val;
        return integer0.shortValue();
    }

    /**
     * Returns the concrete return value of the concrete method execution. The
     * concrete return value should be a char value
     *
     * @return a char value with the concrete method execution.
     */
    final protected char getConcCharRetVal() {
        Integer char0 = (Integer) this.conc_ret_val;
        return (char) char0.intValue();
    }

    /**
     * Returns the concrete return value of the concrete method execution. The
     * concrete return value should be a double value
     *
     * @return a double value with the concrete method execution.
     */
    final protected double getConcDoubleRetVal() {
        Double double0 = (Double) this.conc_ret_val;
        return double0;
    }

    /**
     * Returns the concrete return value of the concrete method execution. The
     * concrete return value should be a float value
     *
     * @return a float value with the concrete method execution.
     */
    final protected float getConcFloatRetVal() {
        Float float0 = (Float) this.conc_ret_val;
        return float0;
    }

    /**
     * Returns the concrete return value of the concrete method execution. The
     * concrete return value should be a boolean value
     *
     * @return a boolean value with the concrete method execution.
     */
    final protected boolean getConcBooleanRetVal() {
        Boolean boolean0 = (Boolean) this.conc_ret_val;
        return boolean0;
    }

    /**
     * Returns the concrete return value of the concrete method execution. The
     * concrete return value should be a byte value
     *
     * @return a byte value with the concrete method execution.
     */
    final protected byte getConcByteRetVal() {
        Integer integer0 = (Integer) this.conc_ret_val;
        return integer0.byteValue();
    }

    /**
     * Returns the concrete return value of the concrete method execution. The
     * concrete return value should be a long value
     *
     * @return a long value with the concrete method execution.
     */
    final protected long getConcLongRetVal() {
        Long long0 = (Long) this.conc_ret_val;
        return long0;
    }

    /**
     * Returns the concrete return value of the concrete method execution. The
     * concrete return value should be an Object reference
     *
     * @return an Object reference with the concrete method execution.
     */
    final protected Object getConcRetVal() {
        Object arg = this.conc_ret_val;
        return arg;
    }

    final public String getDesc() {
        return desc;
    }

    /**
     * This callback-method is invoked by the VM before the actual execution of
     * the method.
     * <p>
     * This is the very last chance of saving concrete values before the
     * execution of the concrete method.
     * <p>
     * This could return an IntegerConstraint (such as String.isInteger)
     */
    public IntegerConstraint beforeExecuteFunction() {
        return null;
    }

}
