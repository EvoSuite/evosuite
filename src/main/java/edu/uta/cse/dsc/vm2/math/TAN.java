package edu.uta.cse.dsc.vm2.math;

import java.util.Stack;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.RealUnaryExpression;

public class TAN extends MathFunction {

	public TAN() {
		super("tan", MathFunctionCallVM.D2D_DESCRIPTOR);
	}

	public RealExpression execute(Stack<Expression<?>> params, double res) {
		RealExpression realExpression = (RealExpression) params.pop();
		if (realExpression.containsSymbolicVariable()) {
			Operator op = Operator.TAN;
			return new RealUnaryExpression(realExpression, op, res);
		} else
			return null;

	}

}
