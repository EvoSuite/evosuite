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
package org.evosuite.symbolic.expr.token;

import java.util.HashSet;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.symbolic.ConstraintTooLongException;
import org.evosuite.symbolic.DSEStats;
import org.evosuite.symbolic.expr.AbstractExpression;
import org.evosuite.symbolic.expr.ExpressionVisitor;
import org.evosuite.symbolic.expr.Variable;
import org.evosuite.symbolic.expr.bv.StringComparison;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * StringComparison class.
 * </p>
 * 
 * @author krusev
 */
public final class HasMoreTokensExpr extends AbstractExpression<Long> implements
        StringComparison {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2154812241163764621L;
	protected static Logger log = LoggerFactory.getLogger(HasMoreTokensExpr.class);

	public HasMoreTokensExpr(TokenizerExpr tokenizerExpr, Long con) {
		super(con, 1 + tokenizerExpr.getSize(), tokenizerExpr.containsSymbolicVariable());
		this.tokenizerExpr = tokenizerExpr;

		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH) {
			DSEStats.getInstance().reportConstraintTooLong(getSize());
			throw new ConstraintTooLongException(getSize());
		}
	}

	private final TokenizerExpr tokenizerExpr;

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof HasMoreTokensExpr) {
			HasMoreTokensExpr other = (HasMoreTokensExpr) obj;
			return this.tokenizerExpr.equals(other.tokenizerExpr);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return this.tokenizerExpr.hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "hasMoreTokens(" + tokenizerExpr.toString() + ")";
	}

	@Override
	public Set<Variable<?>> getVariables() {
		Set<Variable<?>> variables = new HashSet<Variable<?>>();
		variables.addAll(this.tokenizerExpr.getVariables());
		return variables;
	}

	@Override
	public Set<Object> getConstants() {
		return this.tokenizerExpr.getConstants();
	}

	public TokenizerExpr getTokenizerExpr() {
		return tokenizerExpr;
	}

	@Override
	public <K, V> K accept(ExpressionVisitor<K, V> v, V arg) {
		return v.visit(this, arg);
	}
}
