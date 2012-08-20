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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ExpressionHelper {

	protected static Logger log = LoggerFactory.getLogger(ExpressionHelper.class);

	/**
	 * <p>
	 * getLongResult
	 * </p>
	 * 
	 * @param expr
	 *            a {@link org.evosuite.symbolic.expr.Expression} object.
	 * @return a long.
	 */
	public static long getLongResult(Expression<?> expr) {
		if (expr instanceof IntegerExpression) {
			return (Long) ((IntegerExpression) expr).execute();
		}

		// charAt returns String but can be in an int constraint so handle:		
		if (expr instanceof StringBinaryExpression) {
			if (((StringBinaryExpression) expr).getOperator() == Operator.CHARAT) {
				return (((StringBinaryExpression) expr).execute()).charAt(0);
			}
		}

		log.warn("Changer.getIntResult: got something weird!?!" + expr);
		return 0;
	}
}
