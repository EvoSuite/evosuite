/**
 * Copyright (C) 2012 Gordon Fraser, Andrea Arcuri
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.symbolic.expr;

import gov.nasa.jpf.JPF;

import java.util.logging.Logger;

public abstract class ExpressionHelper {
	
	static Logger log = JPF.getLogger("de.unisb.cs.st.evosuite.symbolic.expr.ExpressionHelper");
	
	public static long getLongResult(Expression<?> expr) {
		if (expr instanceof IntegerExpression) {
			return (Long)((IntegerExpression)expr).execute();
		}

		// charAt returns String but can be in an int constraint so handle:		
		if (expr instanceof StringBinaryExpression) {
			if (((StringBinaryExpression)expr).getOperator() == Operator.CHARAT ) {
				return (((StringBinaryExpression)expr).execute()).charAt(0);
			}
		}

		log.warning("Changer.getIntResult: got something weird!?!" + expr);
		return 0;
	}	
}
