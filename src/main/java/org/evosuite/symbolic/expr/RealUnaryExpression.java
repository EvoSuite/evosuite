
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
 *
 * @author Gordon Fraser
 */
package org.evosuite.symbolic.expr;

import gov.nasa.jpf.JPF;

import java.util.logging.Logger;

import org.evosuite.Properties;
import org.evosuite.symbolic.ConstraintTooLongException;
public class RealUnaryExpression extends RealExpression implements
		UnaryExpression<Double> {

	private static final long serialVersionUID = 9086637495150131445L;

	static Logger log = JPF.getLogger("org.evosuite.symbolic.expr.RealUnaryExpression");
	
	protected Double concretValue;
	
	protected Operator op;
	
	protected Expression<Double> expr;

	/**
	 * <p>Constructor for RealUnaryExpression.</p>
	 *
	 * @param e a {@link org.evosuite.symbolic.expr.Expression} object.
	 * @param op2 a {@link org.evosuite.symbolic.expr.Operator} object.
	 * @param con a {@link java.lang.Double} object.
	 */
	public RealUnaryExpression(Expression<Double> e, Operator op2, Double con) {
		this.expr=e;
		this.op=op2;
		this.concretValue=con;
		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH)
			throw new ConstraintTooLongException();
	}

	/** {@inheritDoc} */
	@Override
	public Double getConcreteValue() {
		return concretValue;
	}

	/** {@inheritDoc} */
	@Override
	public Expression<Double> getOperand() {
		return expr;
	}

	/** {@inheritDoc} */
	@Override
	public Operator getOperator() {
		return op;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return op.toString()+"("+expr+")";
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof RealUnaryExpression)
		{
			RealUnaryExpression v=(RealUnaryExpression) obj;
			return this.op.equals(v.op) 
						&& this.getSize()==v.getSize() 
						&& this.expr.equals(v.expr);
		}
		return false;
	}
	
	protected int size=0;
	/** {@inheritDoc} */
	@Override
	public int getSize() {
		if(size == 0)
		{
			size=1 + expr.getSize();
		}
		return size;
	}

	/** {@inheritDoc} */
	@Override
	public Double execute() {
		double leftVal = (Double)expr.execute();
		
		switch (op) {

		case ABS:
			return Math.abs(leftVal);
		case ACOS:
			return Math.acos(leftVal);
		case ASIN:
			return Math.asin(leftVal);
		case ATAN:
			return Math.atan(leftVal);
		case CBRT:
			return Math.cbrt(leftVal);
		case CEIL:
			return Math.ceil(leftVal);
		case COS:
			return Math.cos(leftVal);
		case COSH:
			return Math.cosh(leftVal);
		case EXP:
			return Math.exp(leftVal);
		case EXPM1:
			return Math.expm1(leftVal);
		case FLOOR:
			return Math.floor(leftVal);
		case GETEXPONENT:
			return (double)Math.getExponent(leftVal);
		case LOG:
			return Math.log(leftVal);
		case LOG10:
			return Math.log10(leftVal);
		case LOG1P:
			return Math.log1p(leftVal);
		case NEG:
			return -leftVal;
		case NEXTUP:
			return Math.nextUp(leftVal);
		case RINT:
			return Math.rint(leftVal);
		case SIGNUM:
			return Math.signum(leftVal);
		case SIN:
			return Math.sin(leftVal);
		case SINH:
			return Math.sinh(leftVal);
		case SQRT:
			return Math.sqrt(leftVal);
		case TAN:
			return Math.tan(leftVal);
		case TANH:
			return Math.tanh(leftVal);
		case TODEGREES:
			return Math.toDegrees(leftVal);
		case TORADIANS:
			return Math.toRadians(leftVal);
		case ULP:
			return Math.ulp(leftVal);
					
		default:
			log.warning("IntegerUnaryExpression: unimplemented operator!");
			return null;
		}
	}

}
