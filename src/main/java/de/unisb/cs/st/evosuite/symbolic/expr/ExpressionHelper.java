package de.unisb.cs.st.evosuite.symbolic.expr;

import gov.nasa.jpf.JPF;

import java.util.logging.Logger;

public abstract class ExpressionHelper {
	
	static Logger log = JPF.getLogger("de.unisb.cs.st.evosuite.symbolic.expr.ExpressionHelper");
	
	public static long getLongResult(Expression<?> expr) {
		if (expr instanceof IntegerExpression) {
			return (Long)((IntegerExpression)expr).execute();
		}
		
		/* if we have string expression
		 * we must be in some string method that returns int
		 * 
		 * The possibilities are
		 * 	StringUnaryExpression
		 * 		Length
		 *	StringBinatyExpression
		 *		INDEXOFC
		 *		INDEXOFS
		 *	StringMultipleExpression
		 *		INDEXOFCI
		 *		INDEXOFSI
		 */
		

		
		if (expr instanceof StringUnaryExpression) {
			if (((StringUnaryExpression)expr).getOperator() == Operator.LENGTH){
				return Long.parseLong(((StringUnaryExpression)expr).execute());
			}
		} else if (expr instanceof StringBinaryExpression) {
			if (((StringBinaryExpression)expr).getOperator() == Operator.INDEXOFC
					|| ((StringBinaryExpression)expr).getOperator() == Operator.INDEXOFS
					|| ((StringBinaryExpression)expr).getOperator() == Operator.CHARAT ) {
				return Long.parseLong(((StringBinaryExpression)expr).execute());
			}
		} else if (expr instanceof StringMultipleExpression) {
			if (((StringMultipleExpression)expr).getOperator() == Operator.INDEXOFCI
					|| ((StringMultipleExpression)expr).getOperator() == Operator.INDEXOFSI) {
				return Long.parseLong(((StringMultipleExpression)expr).execute());
			}
		}

		//We shouldn't get here
		log.warning("Changer.getIntResult: got something weird!?!");
		return 0;
	}
	
	
}
