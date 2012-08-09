package edu.uta.cse.dsc.vm2.math;

import java.util.Stack;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealBinaryExpression;
import org.evosuite.symbolic.expr.RealExpression;

public class POW extends MathFunction {

	public POW() {
		super("pow", MathFunctionCallVM.DD2D_DESCRIPTOR);
	}

	public RealExpression execute(Stack<Expression<?>> params, double res) {
		RealExpression right = (RealExpression) params.pop();
		RealExpression left = (RealExpression) params.pop();
		if (left.containsSymbolicVariable() || right.containsSymbolicVariable()) {
			Operator op = Operator.POW;
			return new RealBinaryExpression(left, op, right, res);
		} else {
			return null;
		}

	}

}
