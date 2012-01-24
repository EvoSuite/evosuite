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
