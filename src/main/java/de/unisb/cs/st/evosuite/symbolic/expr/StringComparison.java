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
package de.unisb.cs.st.evosuite.symbolic.expr;

import gov.nasa.jpf.JPF;

import java.util.logging.Logger;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.symbolic.ConstraintTooLongException;
import de.unisb.cs.st.evosuite.symbolic.search.DistanceEstimator;

/**
 * @author krusev
 *
 */
public class StringComparison extends StringExpression {

	private static final long serialVersionUID = -2959676064390810341L;

	static Logger log = JPF.getLogger("de.unisb.cs.st.evosuite.symbolic.expr.StringComparison");
	
	public StringComparison(Expression<String> left, Operator op, Expression<?> right2, Long con) {
		super();
		this.left = left;
		this.op = op;
		this.right = right2;
		this.conVal = con;
		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH)
			throw new ConstraintTooLongException();
	}

	protected final Long conVal;
	protected final Operator op;
	protected final Expression<String> left;
	protected final Expression<?> right;

	@Override
	public Long getConcreteValue() {
		return conVal;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof StringComparison) {
			StringComparison other = (StringComparison) obj;
			return this.op.equals(other.op) && this.conVal.equals(other.conVal) 
					&& this.getSize() == other.getSize() 
					&& this.left.equals(other.left) 
					&& this.right.equals(other.right);
		}

		return false;
	}

	public Expression<?> getRightOperand() {
		return right;
	}

	public Expression<String> getLeftOperand() {
		return left;
	}

	@Override
	public String toString() {
		return "(" + left + op.toString() + right + ")";
	}

	protected int size = 0;

	@Override
	public int getSize() {
		if (size == 0) {
			size = 1 + left.getSize() + right.getSize();
		}
		return size;
	}

	public Operator getOperator() {
		return op;
	}

	@Override
	public Long execute() {
		try {
			String first = (String)left.execute();
			String second = (String)right.execute();
			
			switch (op) {
			case EQUALSIGNORECASE:
				return (long)DistanceEstimator.StrEqualsIgnoreCase(first, second);
			case EQUALS:
				return (long)DistanceEstimator.StrEquals(first, second);
			case ENDSWITH:
				return (long)DistanceEstimator.StrEndsWith(first, second);
			case CONTAINS:
				return (long)DistanceEstimator.StrContains(first, second);
			default:
				log.warning("StringComparison: unimplemented operator!" + op);
				return null;
			}
		} catch (Exception e) {
			return Long.MAX_VALUE;
		}		
	}

}